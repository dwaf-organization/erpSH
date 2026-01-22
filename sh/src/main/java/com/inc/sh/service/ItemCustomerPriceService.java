package com.inc.sh.service;

import com.inc.sh.dto.itemCustomerPrice.reqDto.ItemCustomerPriceSearchDto;
import com.inc.sh.dto.itemCustomerPrice.reqDto.ItemCustomerPriceUpdateReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.itemCustomerPrice.respDto.ItemForCustomerPriceRespDto;
import com.inc.sh.dto.itemCustomerPrice.respDto.CustomerPriceRespDto;
import com.inc.sh.dto.itemCustomerPrice.respDto.CustomerWithBrandDto;
import com.inc.sh.dto.itemCustomerPrice.respDto.ItemCustomerPriceUpdateRespDto;
import com.inc.sh.entity.Item;
import com.inc.sh.entity.Customer;
import com.inc.sh.entity.ItemCustomerPrice;
import com.inc.sh.repository.ItemRepository;
import com.inc.sh.repository.CustomerRepository;
import com.inc.sh.repository.ItemCustomerPriceRepository;
import com.inc.sh.repository.OrderLimitCustomerRepository;
import com.inc.sh.util.ItemPriceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ItemCustomerPriceService {
    
    private final ItemRepository itemRepository;
    private final CustomerRepository customerRepository;
    private final ItemCustomerPriceRepository itemCustomerPriceRepository;
    private final OrderLimitCustomerRepository orderLimitCustomerRepository;
    
    /**
     * 거래처별 단가관리용 품목 목록 조회
     * @param searchDto 검색 조건
     * @return 품목 목록
     */
    @Transactional(readOnly = true)
    public RespDto<List<ItemForCustomerPriceRespDto>> getItemListForCustomerPrice(ItemCustomerPriceSearchDto searchDto) {
        try {
            log.info("거래처별 단가관리 품목 조회 시작 - categoryCode: {}, itemName: {}, hqCode: {}", 
                    searchDto.getCategoryCode(), searchDto.getItemName(), searchDto.getHqCode());
            
            List<Item> items = itemRepository.findForItemCustomerPriceManagementWithHqCode(
                    searchDto.getCategoryCode(),
                    searchDto.getItemName(),
                    searchDto.getHqCode()
            );
            
            List<ItemForCustomerPriceRespDto> responseList = items.stream()
                    .map(ItemForCustomerPriceRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("거래처별 단가관리 품목 조회 완료 - hqCode: {}, 조회 건수: {}", searchDto.getHqCode(), responseList.size());
            return RespDto.success("품목 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("거래처별 단가관리 품목 조회 중 오류 발생 - hqCode: {}", searchDto.getHqCode(), e);
            return RespDto.fail("품목 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 품목별 거래처 단가 조회
     * @param itemCode 품목코드
     * @param hqCode 본사코드
     * @return 거래처별 단가 정보
     */
    @Transactional(readOnly = true)
    public RespDto<List<CustomerPriceRespDto>> getCustomerPricesByItem(Integer itemCode, Integer hqCode) {
        try {
            log.info("품목별 거래처 단가 조회 시작 - itemCode: {}, hqCode: {}", itemCode, hqCode);
            
            // 품목 정보 조회
            Item item = itemRepository.findByItemCode(itemCode);
            if (item == null) {
                log.warn("품목을 찾을 수 없습니다 - itemCode: {}", itemCode);
                return RespDto.fail("품목을 찾을 수 없습니다.");
            }
            
            // 주문 가능한 거래처 조회 (브랜드명 포함, 본사별)
            List<CustomerWithBrandDto> orderableCustomers = getOrderableCustomersWithBrandByHqCode(item, hqCode);
            
            // 품목별 거래처 단가 조회
            List<ItemCustomerPrice> customerPrices = itemCustomerPriceRepository.findByItemCode(itemCode);
            Map<Integer, Integer> customerPriceMap = customerPrices.stream()
                    .collect(Collectors.toMap(
                            ItemCustomerPrice::getCustomerCode,
                            ItemCustomerPrice::getCustomerSupplyPrice
                    ));
            
            // 응답 데이터 생성
            List<CustomerPriceRespDto> responseList = new ArrayList<>();
            for (CustomerWithBrandDto customer : orderableCustomers) {
                Integer customerPrice = customerPriceMap.get(customer.getCustomerCode());
                CustomerPriceRespDto dto = CustomerPriceRespDto.builder()
                        .customerCode(customer.getCustomerCode())
                        .brandName(customer.getBrandName())
                        .customerName(customer.getCustomerName())
                        .basePrice(item.getBasePrice())
                        .customerPrice(customerPrice != null ? customerPrice : 0)
                        .build();
                responseList.add(dto);
            }
            
            log.info("품목별 거래처 단가 조회 완료 - itemCode: {}, hqCode: {}, 거래처 수: {}", itemCode, hqCode, responseList.size());
            return RespDto.success("거래처 단가 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("품목별 거래처 단가 조회 중 오류 발생 - itemCode: {}, hqCode: {}", itemCode, hqCode, e);
            return RespDto.fail("거래처 단가 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 거래처별 단가 일괄 수정
     * @param request 단가 수정 요청
     * @return 처리 결과
     */
    public RespDto<ItemCustomerPriceUpdateRespDto> updateCustomerPrices(ItemCustomerPriceUpdateReqDto request) {
        try {
            log.info("거래처별 단가 일괄 수정 시작 - itemCode: {}, 거래처 수: {}", 
                    request.getItemCode(), request.getCustomerPrices().size());
            
            // 품목 존재 확인
            Item item = itemRepository.findByItemCode(request.getItemCode());
            if (item == null) {
                log.warn("품목을 찾을 수 없습니다 - itemCode: {}", request.getItemCode());
                return RespDto.fail("품목을 찾을 수 없습니다.");
            }
            
            List<ItemCustomerPriceUpdateRespDto.ProcessResult> processResults = new ArrayList<>();
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            for (ItemCustomerPriceUpdateReqDto.CustomerPriceDto customerPriceDto : request.getCustomerPrices()) {
                ItemCustomerPriceUpdateRespDto.ProcessResult result = processCustomerPrice(
                        request.getItemCode(), 
                        customerPriceDto, 
                        currentDate,
                        item
                );
                processResults.add(result);
            }
            
            // 응답 생성
            ItemCustomerPriceUpdateRespDto responseDto = ItemCustomerPriceUpdateRespDto.builder()
                    .itemCode(request.getItemCode())
                    .processedCount(processResults.size())
                    .processResults(processResults)
                    .build();
            
            log.info("거래처별 단가 일괄 수정 완료 - itemCode: {}, 처리 건수: {}", 
                    request.getItemCode(), processResults.size());
            
            return RespDto.success("거래처별 단가가 성공적으로 수정되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("거래처별 단가 일괄 수정 중 오류 발생 - itemCode: {}", request.getItemCode(), e);
            return RespDto.fail("거래처별 단가 수정 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 개별 거래처 단가 처리
     */
    private ItemCustomerPriceUpdateRespDto.ProcessResult processCustomerPrice(
            Integer itemCode, 
            ItemCustomerPriceUpdateReqDto.CustomerPriceDto customerPriceDto, 
            String currentDate,
            Item item) {
        
        Optional<ItemCustomerPrice> existingOpt = itemCustomerPriceRepository
                .findByItemCodeAndCustomerCode(itemCode, customerPriceDto.getCustomerCode());
        
        if (customerPriceDto.getCustomerPrice() == 0) {
            // 단가가 0인 경우 → 삭제
            if (existingOpt.isPresent()) {
                itemCustomerPriceRepository.deleteByItemCodeAndCustomerCode(itemCode, customerPriceDto.getCustomerCode());
                log.info("거래처 단가 삭제 - itemCode: {}, customerCode: {}", itemCode, customerPriceDto.getCustomerCode());
                
                return ItemCustomerPriceUpdateRespDto.ProcessResult.builder()
                        .customerCode(customerPriceDto.getCustomerCode())
                        .action("DELETED")
                        .customerPrice(0)
                        .message("거래처 단가가 삭제되었습니다.")
                        .build();
            } else {
                // 원래 데이터가 없으므로 변경사항 없음
                return ItemCustomerPriceUpdateRespDto.ProcessResult.builder()
                        .customerCode(customerPriceDto.getCustomerCode())
                        .action("NO_CHANGE")
                        .customerPrice(0)
                        .message("변경사항이 없습니다.")
                        .build();
            }
        } else {
            // 단가가 0이 아닌 경우 → 생성 또는 수정
            if (existingOpt.isPresent()) {
                // 수정
                ItemCustomerPrice existing = existingOpt.get();
                existing.setCustomerSupplyPrice(customerPriceDto.getCustomerPrice());
                if (customerPriceDto.getStartDt() != null) {
                    existing.setStartDt(customerPriceDto.getStartDt());
                }
                if (customerPriceDto.getDescription() != null) {
                    existing.setDescription(customerPriceDto.getDescription());
                }
                
                // 금액 계산 및 설정
                calculateAndSetCustomerPrices(existing, item);
                
                itemCustomerPriceRepository.save(existing);
                log.info("거래처 단가 수정 - itemCode: {}, customerCode: {}, price: {}", 
                        itemCode, customerPriceDto.getCustomerCode(), customerPriceDto.getCustomerPrice());
                
                return ItemCustomerPriceUpdateRespDto.ProcessResult.builder()
                        .customerCode(customerPriceDto.getCustomerCode())
                        .action("UPDATED")
                        .customerPrice(customerPriceDto.getCustomerPrice())
                        .message("거래처 단가가 수정되었습니다.")
                        .build();
            } else {
                // 생성
                ItemCustomerPrice newPrice = ItemCustomerPrice.builder()
                        .itemCode(itemCode)
                        .customerCode(customerPriceDto.getCustomerCode())
                        .customerSupplyPrice(customerPriceDto.getCustomerPrice())
                        .startDt(customerPriceDto.getStartDt() != null ? customerPriceDto.getStartDt() : currentDate)
                        .description(customerPriceDto.getDescription())
                        .build();
                
                // 금액 계산 및 설정
                calculateAndSetCustomerPrices(newPrice, item);
                
                itemCustomerPriceRepository.save(newPrice);
                log.info("거래처 단가 생성 - itemCode: {}, customerCode: {}, price: {}", 
                        itemCode, customerPriceDto.getCustomerCode(), customerPriceDto.getCustomerPrice());
                
                return ItemCustomerPriceUpdateRespDto.ProcessResult.builder()
                        .customerCode(customerPriceDto.getCustomerCode())
                        .action("CREATED")
                        .customerPrice(customerPriceDto.getCustomerPrice())
                        .message("거래처 단가가 생성되었습니다.")
                        .build();
            }
        }
    }
    
    /**
     * 주문 가능한 거래처 조회 (브랜드명 포함, 본사별)
     */
    private List<CustomerWithBrandDto> getOrderableCustomersWithBrandByHqCode(Item item, Integer hqCode) {
        if (item.getOrderAvailableYn() == 0) {
            // 전체불가 → 빈 목록 반환
            return Collections.emptyList();
        } else if (item.getOrderAvailableYn() == 1) {
            // 전체가능 → 모든 거래처 (본사별)
            List<Object[]> results = customerRepository.findAllActiveCustomersWithBrandByHqCode(hqCode);
            return results.stream()
                    .map(CustomerWithBrandDto::of)
                    .collect(Collectors.toList());
        } else if (item.getOrderAvailableYn() == 2) {
            // 선택불가 → 제한된 거래처 제외 (본사별)
            List<Integer> limitedCustomerCodes = orderLimitCustomerRepository.findCustomerCodesByItemCode(item.getItemCode());
            if (limitedCustomerCodes.isEmpty()) {
                limitedCustomerCodes = List.of(-1); // 빈 IN 절 방지
            }
            List<Object[]> results = customerRepository.findOrderableCustomersWithBrandExcludingByHqCode(limitedCustomerCodes, hqCode);
            return results.stream()
                    .map(CustomerWithBrandDto::of)
                    .collect(Collectors.toList());
        } else {
            // 기본값: 모든 거래처 (본사별)
            List<Object[]> results = customerRepository.findAllActiveCustomersWithBrandByHqCode(hqCode);
            return results.stream()
                    .map(CustomerWithBrandDto::of)
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * 주문 가능한 거래처 조회 (브랜드명 포함)
     */
    private List<CustomerWithBrandDto> getOrderableCustomersWithBrand(Item item) {
        if (item.getOrderAvailableYn() == 0) {
            // 전체불가 → 빈 목록 반환
            return Collections.emptyList();
        } else if (item.getOrderAvailableYn() == 1) {
            // 전체가능 → 모든 거래처
            List<Object[]> results = customerRepository.findAllActiveCustomersWithBrand();
            return results.stream()
                    .map(CustomerWithBrandDto::of)
                    .collect(Collectors.toList());
        } else if (item.getOrderAvailableYn() == 2) {
            // 선택불가 → 제한된 거래처 제외
            List<Integer> limitedCustomerCodes = orderLimitCustomerRepository.findCustomerCodesByItemCode(item.getItemCode());
            if (limitedCustomerCodes.isEmpty()) {
                limitedCustomerCodes = List.of(-1); // 빈 IN 절 방지
            }
            List<Object[]> results = customerRepository.findOrderableCustomersWithBrandExcluding(limitedCustomerCodes);
            return results.stream()
                    .map(CustomerWithBrandDto::of)
                    .collect(Collectors.toList());
        } else {
            // 기본값: 모든 거래처
            List<Object[]> results = customerRepository.findAllActiveCustomersWithBrand();
            return results.stream()
                    .map(CustomerWithBrandDto::of)
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * 주문 가능한 거래처 조회
     */
    private List<Customer> getOrderableCustomers(Item item) {
        if (item.getOrderAvailableYn() == 0) {
            // 전체불가 → 빈 목록 반환
            return Collections.emptyList();
        } else if (item.getOrderAvailableYn() == 1) {
            // 전체가능 → 모든 거래처
            return customerRepository.findAllActiveCustomers();
        } else if (item.getOrderAvailableYn() == 2) {
            // 선택불가 → 제한된 거래처 제외
            List<Integer> limitedCustomerCodes = orderLimitCustomerRepository.findCustomerCodesByItemCode(item.getItemCode());
            if (limitedCustomerCodes.isEmpty()) {
                limitedCustomerCodes = List.of(-1); // 빈 IN 절 방지
            }
            return customerRepository.findOrderableCustomersForItem(
//                    item.getItemCode(), 
                    item.getOrderAvailableYn(), 
                    limitedCustomerCodes
            );
        } else {
            // 기본값: 모든 거래처
            return customerRepository.findAllActiveCustomers();
        }
    }
    
    /**
     * 거래처별 품목 금액 계산 및 설정
     */
    private void calculateAndSetCustomerPrices(ItemCustomerPrice customerPrice, Item item) {
        int[] calculatedPrices = ItemPriceCalculator.calculateItemPrices(
                customerPrice.getCustomerSupplyPrice(), 
                item.getVatType(), 
                item.getVatDetail()
        );
        
        customerPrice.setSupplyPrice(calculatedPrices[0]);       // 공급가액
        customerPrice.setTaxAmount(calculatedPrices[1]);         // 부가세액  
        customerPrice.setTaxableAmount(calculatedPrices[2]);     // 과세액
        customerPrice.setDutyFreeAmount(calculatedPrices[3]);    // 면세액
        customerPrice.setTotalAmount(calculatedPrices[4]);       // 총액
        
        log.debug("거래처별 품목 금액 계산 완료 - itemCode: {}, customerCode: {}, customerPrice: {}, supplyPrice: {}, totalAmount: {}", 
                customerPrice.getItemCode(), customerPrice.getCustomerCode(), 
                customerPrice.getCustomerSupplyPrice(), customerPrice.getSupplyPrice(), customerPrice.getTotalAmount());
    }
}