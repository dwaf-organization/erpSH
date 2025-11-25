package com.inc.sh.service;

import com.inc.sh.dto.returnRegistration.reqDto.ReturnOrderSearchDto;
import com.inc.sh.dto.returnRegistration.reqDto.ReturnRegistrationSaveDto;
import com.inc.sh.dto.returnRegistration.respDto.ReturnOrderItemRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.entity.Return;
import com.inc.sh.entity.OrderItem;
import com.inc.sh.repository.OrderItemRepository;
import com.inc.sh.repository.ReturnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnRegistrationService {
    
    private final OrderItemRepository orderItemRepository;
    private final ReturnRepository returnRepository;
    
    /**
     * 반품등록용 주문품목 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<ReturnOrderItemRespDto>> getOrderItemsForReturn(ReturnOrderSearchDto searchDto) {
        try {
            log.info("반품등록용 주문품목 조회 시작 - 조건: {}", searchDto);
            
            List<Object[]> results = orderItemRepository.findOrderItemsForReturn(
                    searchDto.getCustomerCode(),
                    searchDto.getOrderNo()
            );
            
            List<ReturnOrderItemRespDto> orderItemList = results.stream()
                    .map(result -> ReturnOrderItemRespDto.builder()
                            .customerCode((Integer) result[0])
                            .customerName((String) result[1])
                            .orderNo((String) result[2])
                            .itemCode((Integer) result[3])
                            .itemName((String) result[4])
                            .specification((String) result[5])
                            .unit((String) result[6])
                            .priceType(result[7] != null ? result[7].toString() : null)
                            .orderPrice((Integer) result[8])
                            .orderQuantity((Integer) result[9])
                            .taxTarget((String) result[10])
                            .warehouseCode((Integer) result[11])
                            .warehouseName((String) result[12])
                            .taxableAmount((Integer) result[13])
                            .taxFreeAmount((Integer) result[14])
                            .supplyAmount((Integer) result[15])
                            .vatAmount((Integer) result[16])
                            .totalAmount((Integer) result[17])
                            .build())
                    .collect(Collectors.toList());
            
            log.info("반품등록용 주문품목 조회 완료 - 조회 건수: {}", orderItemList.size());
            return RespDto.success("주문품목 조회 성공", orderItemList);
            
        } catch (Exception e) {
            log.error("반품등록용 주문품목 조회 중 오류 발생", e);
            return RespDto.fail("주문품목 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 반품 등록 (신규) - 실제 테이블 구조에 맞춰 수정
     */
    @Transactional
    public RespDto<String> saveReturn(ReturnRegistrationSaveDto saveDto) {
        try {
            log.info("반품 등록 시작 - 거래처코드: {}, 품목코드: {}", saveDto.getCustomerCode(), saveDto.getItemCode());
            
            // 1. 주문품목 조회 및 반품 가능 수량 체크 (order_item에 returned_qty 있는 경우)
            OrderItem orderItem = orderItemRepository.findByOrderItemCode(saveDto.getOrderItemCode());
            if (orderItem != null && orderItem.getAvailableReturnQty() != null) {
                if (saveDto.getReturnQuantity() > orderItem.getAvailableReturnQty()) {
                    return RespDto.fail("반품 가능 수량을 초과했습니다. (가능: " + orderItem.getAvailableReturnQty() + "EA)");
                }
            }
            
            // 2. 반품번호 생성 (RET_YYYYMMDD001 형태)
            String returnNo = generateReturnNo();
            
            // 3. 반품 금액 계산 (반품수량 * 주문단가)
            int returnUnitPrice = saveDto.getOrderPrice() != null ? saveDto.getOrderPrice() : 0;
            int returnQuantity = saveDto.getReturnQuantity() != null ? saveDto.getReturnQuantity() : 0;
            int supplyPrice = returnUnitPrice * returnQuantity;
            int vatAmount = (int) (supplyPrice * 0.1); // 부가세 10%
            int totalAmount = supplyPrice + vatAmount;
            
            // 4. Return Entity 생성 - 실제 테이블 구조에 맞춤
            Return newReturn = Return.builder()
                    .returnNo(returnNo)
                    .orderItemCode(saveDto.getOrderItemCode())  // 추가된 컬럼
                    .orderNo(saveDto.getOrderNo())              // 추가된 컬럼  
                    .returnCustomerCode(saveDto.getCustomerCode())
                    .itemCode(saveDto.getItemCode())
                    .receiveWarehouseCode(saveDto.getWarehouseCode())
                    .returnCustomerName(saveDto.getCustomerName())
                    .returnRequestDt(saveDto.getReturnRequestDate())
                    .itemName(saveDto.getItemName())
                    .specification(saveDto.getSpecification())
                    .unit(saveDto.getUnit())
                    .qty(returnQuantity)
                    .priceType(saveDto.getPriceType() != null ? Integer.valueOf(saveDto.getPriceType()) : 1)  // Integer 타입으로 저장
                    .unitPrice(returnUnitPrice)
                    .supplyPrice(supplyPrice)
                    .vatAmt(vatAmount)
                    .totalAmt(totalAmount)
                    .returnMessage(saveDto.getReturnMessage())  // return_message 컬럼
                    .replyMessage(saveDto.getReplyMessage())    // reply_message 컬럼
                    .note(saveDto.getNote())
                    .progressStatus(saveDto.getProgressStatus() != null ? saveDto.getProgressStatus() : "미승인")
                    .warehouseName(saveDto.getWarehouseName())
                    .returnApproveDt(saveDto.getReturnApprovalDate())
                    .description("반품등록")
                    .build();
            
            returnRepository.save(newReturn);
            
            // 5. 주문품목의 returned_qty 업데이트 (order_item에 returned_qty 컬럼이 있는 경우)
            if (orderItem != null) {
                orderItemRepository.updateReturnedQty(saveDto.getOrderItemCode(), saveDto.getReturnQuantity());
            }
            
            log.info("반품 등록 완료 - 반품번호: {}", returnNo);
            return RespDto.success("반품이 등록되었습니다.", returnNo);
            
        } catch (Exception e) {
            log.error("반품 등록 중 오류 발생", e);
            return RespDto.fail("반품 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 반품번호 생성 (RET_YYYYMMDD001 형태)
     */
    private String generateReturnNo() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String datePrefix = "RET_" + today;
        
        // 오늘 날짜의 최신 반품번호 조회
        String latestReturnNo = returnRepository.findLatestReturnNoByDate(datePrefix);
        
        int sequence = 1;
        if (latestReturnNo != null && latestReturnNo.length() >= 12) {
            // "RET_20241117001"에서 "001" 부분 추출
            String sequencePart = latestReturnNo.substring(12); // 마지막 3자리
            sequence = Integer.parseInt(sequencePart) + 1;
        }
        
        return String.format("%s%03d", datePrefix, sequence);
    }
}