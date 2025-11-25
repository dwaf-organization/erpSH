package com.inc.sh.service;

import com.inc.sh.dto.orderLimit.reqDto.OrderLimitSearchDto;
import com.inc.sh.dto.orderLimit.reqDto.OrderLimitUpdateReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.orderLimit.respDto.ItemForOrderLimitRespDto;
import com.inc.sh.dto.orderLimit.respDto.CustomerLimitRespDto;
import com.inc.sh.dto.orderLimit.respDto.OrderLimitUpdateRespDto;
import com.inc.sh.entity.Item;
import com.inc.sh.entity.OrderLimitCustomer;
import com.inc.sh.repository.ItemRepository;
import com.inc.sh.repository.CustomerRepository;
import com.inc.sh.repository.OrderLimitCustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderLimitService {
    
    private final ItemRepository itemRepository;
    private final CustomerRepository customerRepository;
    private final OrderLimitCustomerRepository orderLimitCustomerRepository;
    
    /**
     * 주문제한 설정용 품목 목록 조회
     * @param searchDto 검색 조건
     * @return 품목 목록
     */
    @Transactional(readOnly = true)
    public RespDto<List<ItemForOrderLimitRespDto>> getItemListForOrderLimit(OrderLimitSearchDto searchDto) {
        try {
            log.info("주문제한 설정 품목 조회 시작 - categoryCode: {}, itemName: {}", 
                    searchDto.getCategoryCode(), searchDto.getItemName());
            
            List<Item> items = itemRepository.findForOrderLimitManagement(
                    searchDto.getCategoryCode(),
                    searchDto.getItemName()
            );
            
            List<ItemForOrderLimitRespDto> responseList = items.stream()
                    .map(ItemForOrderLimitRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("주문제한 설정 품목 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("품목 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("주문제한 설정 품목 조회 중 오류 발생", e);
            return RespDto.fail("품목 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 품목별 거래처 제한 정보 조회
     * @param itemCode 품목코드
     * @return 거래처별 제한 정보
     */
    @Transactional(readOnly = true)
    public RespDto<List<CustomerLimitRespDto>> getCustomerLimitsByItem(Integer itemCode) {
        try {
            log.info("품목별 거래처 제한 정보 조회 시작 - itemCode: {}", itemCode);
            
            // 품목 존재 확인
            Item item = itemRepository.findByItemCode(itemCode);
            if (item == null) {
                log.warn("품목을 찾을 수 없습니다 - itemCode: {}", itemCode);
                return RespDto.fail("품목을 찾을 수 없습니다.");
            }
            
            // 활성 거래처 조회 (브랜드명 포함)
            List<Object[]> activeCustomers = customerRepository.findActiveCustomersWithBrandForOrderLimit();
            
            // 해당 품목에 대한 제한된 거래처 조회
            List<Integer> limitedCustomerCodes = orderLimitCustomerRepository.findCustomerCodesByItemCode(itemCode);
            Set<Integer> limitedCustomerSet = new HashSet<>(limitedCustomerCodes);
            
            // 응답 데이터 생성
            List<CustomerLimitRespDto> responseList = activeCustomers.stream()
                    .map(customerData -> CustomerLimitRespDto.of(
                            customerData, 
                            limitedCustomerSet.contains((Integer) customerData[0]) // customerCode가 제한목록에 있으면 true
                    ))
                    .collect(Collectors.toList());
            
            log.info("품목별 거래처 제한 정보 조회 완료 - itemCode: {}, 거래처 수: {}, 제한된 거래처 수: {}", 
                    itemCode, responseList.size(), limitedCustomerCodes.size());
            return RespDto.success("거래처 제한 정보 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("품목별 거래처 제한 정보 조회 중 오류 발생 - itemCode: {}", itemCode, e);
            return RespDto.fail("거래처 제한 정보 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 주문제한 일괄 수정
     * @param request 제한 수정 요청
     * @return 처리 결과
     */
    public RespDto<OrderLimitUpdateRespDto> updateOrderLimits(OrderLimitUpdateReqDto request) {
        try {
            log.info("주문제한 일괄 수정 시작 - itemCode: {}, 거래처 수: {}", 
                    request.getItemCode(), request.getCustomerLimits().size());
            
            // 품목 존재 확인
            Item item = itemRepository.findByItemCode(request.getItemCode());
            if (item == null) {
                log.warn("품목을 찾을 수 없습니다 - itemCode: {}", request.getItemCode());
                return RespDto.fail("품목을 찾을 수 없습니다.");
            }
            
            List<OrderLimitUpdateRespDto.ProcessResult> processResults = new ArrayList<>();
            
            // 각 거래처별 제한 설정 처리
            for (OrderLimitUpdateReqDto.CustomerLimitDto customerLimit : request.getCustomerLimits()) {
                OrderLimitUpdateRespDto.ProcessResult result = processCustomerLimit(
                        request.getItemCode(), 
                        customerLimit
                );
                processResults.add(result);
            }
            
            // 품목의 주문가능여부 자동 업데이트
            Integer updatedOrderAvailableYn = updateItemOrderAvailableYn(request.getItemCode());
            
            // 응답 생성
            OrderLimitUpdateRespDto responseDto = OrderLimitUpdateRespDto.builder()
                    .itemCode(request.getItemCode())
                    .updatedOrderAvailableYn(updatedOrderAvailableYn)
                    .processedCount(processResults.size())
                    .processResults(processResults)
                    .build();
            
            log.info("주문제한 일괄 수정 완료 - itemCode: {}, 처리 건수: {}, 주문가능여부: {}", 
                    request.getItemCode(), processResults.size(), updatedOrderAvailableYn);
            
            return RespDto.success("주문제한이 성공적으로 수정되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("주문제한 일괄 수정 중 오류 발생 - itemCode: {}", request.getItemCode(), e);
            return RespDto.fail("주문제한 수정 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 개별 거래처 제한 처리
     */
    private OrderLimitUpdateRespDto.ProcessResult processCustomerLimit(
            Integer itemCode, 
            OrderLimitUpdateReqDto.CustomerLimitDto customerLimit) {
        
        boolean existsInDb = orderLimitCustomerRepository.existsByItemCodeAndCustomerCode(
                itemCode, customerLimit.getCustomerCode());
        
        if (customerLimit.getIsLimited() == 0) {
            // 불가능으로 설정 (0=불가능)
            if (!existsInDb) {
                // DB에 없으면 추가
                OrderLimitCustomer newLimit = OrderLimitCustomer.builder()
                        .itemCode(itemCode)
                        .customerCode(customerLimit.getCustomerCode())
                        .limitReason("관리자 설정")
                        .build();
                
                orderLimitCustomerRepository.save(newLimit);
                log.info("주문제한 추가 - itemCode: {}, customerCode: {}", itemCode, customerLimit.getCustomerCode());
                
                return OrderLimitUpdateRespDto.ProcessResult.builder()
                        .customerCode(customerLimit.getCustomerCode())
                        .action("ADDED")
                        .isLimited(0)
                        .message("주문제한이 추가되었습니다.")
                        .build();
            } else {
                // DB에 이미 있으면 패스
                return OrderLimitUpdateRespDto.ProcessResult.builder()
                        .customerCode(customerLimit.getCustomerCode())
                        .action("NO_CHANGE")
                        .isLimited(0)
                        .message("이미 주문제한 상태입니다.")
                        .build();
            }
        } else {
            // 가능으로 설정 (isLimited = 1)
            if (existsInDb) {
                // DB에 있으면 삭제
                orderLimitCustomerRepository.deleteByItemCodeAndCustomerCode(itemCode, customerLimit.getCustomerCode());
                log.info("주문제한 해제 - itemCode: {}, customerCode: {}", itemCode, customerLimit.getCustomerCode());
                
                return OrderLimitUpdateRespDto.ProcessResult.builder()
                        .customerCode(customerLimit.getCustomerCode())
                        .action("REMOVED")
                        .isLimited(1)
                        .message("주문제한이 해제되었습니다.")
                        .build();
            } else {
                // DB에 없으면 패스
                return OrderLimitUpdateRespDto.ProcessResult.builder()
                        .customerCode(customerLimit.getCustomerCode())
                        .action("NO_CHANGE")
                        .isLimited(1)
                        .message("이미 주문가능 상태입니다.")
                        .build();
            }
        }
    }
    
    /**
     * 품목의 주문가능여부 자동 업데이트
     * @param itemCode 품목코드
     * @return 업데이트된 주문가능여부
     */
    private Integer updateItemOrderAvailableYn(Integer itemCode) {
        // 현재 제한된 거래처 수 조회
        List<Integer> limitedCustomerCodes = orderLimitCustomerRepository.findCustomerCodesByItemCode(itemCode);
        
        // 전체 활성 거래처 수 조회
        List<Object[]> allActiveCustomers = customerRepository.findActiveCustomersWithBrandForOrderLimit();
        int totalActiveCustomerCount = allActiveCustomers.size();
        
        Integer orderAvailableYn;
        
        if (limitedCustomerCodes.isEmpty()) {
            // 제한된 거래처가 없음 → 전체가능
            orderAvailableYn = 1;
        } else if (limitedCustomerCodes.size() >= totalActiveCustomerCount) {
            // 모든 거래처가 제한됨 → 전체불가
            orderAvailableYn = 0;
        } else {
            // 일부 거래처만 제한됨 → 선택불가
            orderAvailableYn = 2;
        }
        
        // 품목 테이블 업데이트
        Item item = itemRepository.findByItemCode(itemCode);
        item.setOrderAvailableYn(orderAvailableYn);
        itemRepository.save(item);
        
        log.info("품목 주문가능여부 업데이트 - itemCode: {}, orderAvailableYn: {}, 제한거래처수: {}/{}", 
                itemCode, orderAvailableYn, limitedCustomerCodes.size(), totalActiveCustomerCount);
        
        return orderAvailableYn;
    }
}