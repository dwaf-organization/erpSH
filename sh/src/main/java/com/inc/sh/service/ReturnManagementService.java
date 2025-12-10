package com.inc.sh.service;

import com.inc.sh.dto.returnManagement.reqDto.ReturnDeleteReqDto;
import com.inc.sh.dto.returnManagement.reqDto.ReturnSaveReqDto;
import com.inc.sh.dto.returnManagement.reqDto.ReturnSearchDto;
import com.inc.sh.dto.returnManagement.reqDto.ReturnUpdateDto;
import com.inc.sh.dto.returnManagement.respDto.ReturnBatchResult;
import com.inc.sh.dto.returnManagement.respDto.ReturnRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.entity.Customer;
import com.inc.sh.entity.Item;
import com.inc.sh.entity.Order;
import com.inc.sh.entity.OrderItem;
import com.inc.sh.entity.Return;
import com.inc.sh.entity.Warehouse;
import com.inc.sh.repository.CustomerRepository;
import com.inc.sh.repository.ItemRepository;
import com.inc.sh.repository.OrderItemRepository;
import com.inc.sh.repository.OrderRepository;
import com.inc.sh.repository.ReturnRepository;
import com.inc.sh.repository.WarehouseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnManagementService {
    
    private final ReturnRepository returnRepository;
    private final CustomerRepository customerRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final WarehouseRepository warehouseRepository;
    
    /**
     * 반품 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<ReturnRespDto>> getReturnList(ReturnSearchDto searchDto) {
        try {
            log.info("반품 조회 시작 - 조건: {}", searchDto);
            
            List<Object[]> results = returnRepository.findReturnsWithJoinByConditions(
                    searchDto.getStartDate(),
                    searchDto.getEndDate(),
                    searchDto.getCustomerCode(),
                    searchDto.getStatus()
            );
            
            List<ReturnRespDto> returnList = results.stream()
                    .map(result -> {
                        ReturnRespDto dto = ReturnRespDto.builder()
                                .returnNo((String) result[0]) // return_no를 returnCode로
                                .customerCode((Integer) result[1])
                                .customerName((String) result[2])
                                .returnRequestDate((String) result[3])
                                .itemCode((Integer) result[4])
                                .itemName((String) result[5])
                                .specification((String) result[6])
                                .unit((String) result[7])
                                .priceType(result[8] != null ? result[8].toString() : null)
                                .quantity((Integer) result[9])
                                .supplyPrice(result[11] != null ? new BigDecimal(result[11].toString()) : BigDecimal.ZERO)
                                .vat(result[12] != null ? new BigDecimal(result[12].toString()) : BigDecimal.ZERO)
                                .totalAmount(result[13] != null ? new BigDecimal(result[13].toString()) : BigDecimal.ZERO)
                                .warehouseCode((Integer) result[14])
                                .warehouseName((String) result[15])
                                .distCenterCode((Integer) result[16])
                                .distCenterName((String) result[17])
                                .status((String) result[18])
                                .returnApprovalDate((String) result[19])
                                .message((String) result[20])
                                .note((String) result[21])
                                .description((String) result[23])
                                .createdAt(result[24] != null ? result[24].toString() : null)
                                .updatedAt(result[25] != null ? result[25].toString() : null)
                                .build();
                        return dto;
                    })
                    .collect(Collectors.toList());
            
            log.info("반품 조회 완료 - 조회 건수: {}", returnList.size());
            return RespDto.success("반품 조회 성공", returnList);
            
        } catch (Exception e) {
            log.error("반품 조회 중 오류 발생", e);
            return RespDto.fail("반품 조회 중 오류가 발생했습니다.");
        }
    }
    /**
     * 반품 다중 저장 (신규/수정) - 모든 기존 검증 포함
     */
    @Transactional
    public RespDto<ReturnBatchResult> saveReturns(ReturnSaveReqDto reqDto) {
        
        log.info("반품 다중 저장 시작 - 총 {}건", reqDto.getReturns().size());
        
        List<ReturnRespDto> successData = new ArrayList<>();
        List<ReturnBatchResult.ReturnErrorDto> failData = new ArrayList<>();
        
        for (ReturnSaveReqDto.ReturnSaveItemDto returnItem : reqDto.getReturns()) {
            try {
                // 개별 반품 저장 처리
                ReturnRespDto savedReturn = saveSingleReturn(returnItem);
                successData.add(savedReturn);
                
                log.info("반품 저장 성공 - returnNo: {}, customerCode: {}", 
                        savedReturn.getReturnNo(), savedReturn.getCustomerCode());
                
            } catch (Exception e) {
                log.error("반품 저장 실패 - customerCode: {}, 에러: {}", returnItem.getReturnCustomerCode(), e.getMessage());
                
                ReturnBatchResult.ReturnErrorDto errorDto = ReturnBatchResult.ReturnErrorDto.builder()
                        .returnNo(returnItem.getReturnNo())
                        .customerName(returnItem.getReturnCustomerName())
                        .itemName(returnItem.getItemName())
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성
        ReturnBatchResult result = ReturnBatchResult.builder()
                .totalCount(reqDto.getReturns().size())
                .successCount(successData.size())
                .failCount(failData.size())
                .successData(successData)
                .failData(failData)
                .build();
        
        String message = String.format("반품 저장 완료 - 성공: %d건, 실패: %d건", 
                successData.size(), failData.size());
        
        log.info("반품 다중 저장 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getReturns().size(), successData.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 반품 저장 처리 (기존 검증 로직 포함)
     */
    private ReturnRespDto saveSingleReturn(ReturnSaveReqDto.ReturnSaveItemDto saveDto) {
        
        Return returnEntity;
        
        if (saveDto.getReturnNo() == null || saveDto.getReturnNo().trim().isEmpty()) {
            // 신규 반품 생성
            
            // 1. 필수 필드 검증
            if (saveDto.getReturnCustomerCode() == null) {
                throw new RuntimeException("거래처코드는 필수입니다.");
            }
            if (saveDto.getItemCode() == null) {
                throw new RuntimeException("품목코드는 필수입니다.");
            }
            if (saveDto.getReturnRequestDt() == null || saveDto.getReturnRequestDt().trim().isEmpty()) {
                throw new RuntimeException("반품요청일자는 필수입니다.");
            }
            if (saveDto.getOrderItemCode() == null) {
                throw new RuntimeException("주문품목코드는 필수입니다.");
            }
            if (saveDto.getOrderNo() == null || saveDto.getOrderNo().trim().isEmpty()) {
                throw new RuntimeException("주문번호는 필수입니다.");
            }
            if (saveDto.getQty() == null || saveDto.getQty() <= 0) {
                throw new RuntimeException("반품수량은 1 이상이어야 합니다.");
            }

            // 3. 거래처 존재 확인
            Customer customer = customerRepository.findByCustomerCode(saveDto.getReturnCustomerCode());
            if (customer == null) {
                throw new RuntimeException("존재하지 않는 거래처입니다: " + saveDto.getReturnCustomerCode());
            }

            // 4. 품목 존재 확인
            Item item = itemRepository.findByItemCode(saveDto.getItemCode());
            if (item == null) {
                throw new RuntimeException("존재하지 않는 품목입니다: " + saveDto.getItemCode());
            }

            // 5. 주문품목 존재 확인
            OrderItem orderItem = orderItemRepository.findById(saveDto.getOrderItemCode())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 주문품목입니다: " + saveDto.getOrderItemCode()));

            // 6. 주문 존재 확인
            Order order = orderRepository.findByOrderNo(saveDto.getOrderNo());
            if (order == null) {
                throw new RuntimeException("존재하지 않는 주문입니다: " + saveDto.getOrderNo());
            }

            // 7. 주문품목과 주문번호 일치 확인
            if (!orderItem.getOrderNo().equals(saveDto.getOrderNo())) {
                throw new RuntimeException("주문품목과 주문번호가 일치하지 않습니다.");
            }

            // 8. 반품가능수량 확인
            Integer totalReturnedQty = returnRepository.getTotalReturnedQtyByOrderItemCode(saveDto.getOrderItemCode());
            Integer availableReturnQty = orderItem.getOrderQty() - (totalReturnedQty != null ? totalReturnedQty : 0);
            
            if (saveDto.getQty() > availableReturnQty) {
                throw new RuntimeException("반품가능수량을 초과했습니다. 가능수량: " + availableReturnQty + "개, 요청수량: " + saveDto.getQty() + "개");
            }

            // 9. 창고 존재 확인 (창고코드가 있는 경우)
            if (saveDto.getReceiveWarehouseCode() != null) {
                Warehouse warehouse = warehouseRepository.findById(saveDto.getReceiveWarehouseCode())
                        .orElseThrow(() -> new RuntimeException("존재하지 않는 창고입니다: " + saveDto.getReceiveWarehouseCode()));
            }

            // 반품번호 생성
            String returnNo = generateReturnNo();
            
            returnEntity = Return.builder()
                    .returnNo(returnNo)
                    .returnCustomerCode(saveDto.getReturnCustomerCode())
                    .itemCode(saveDto.getItemCode())
                    .receiveWarehouseCode(saveDto.getReceiveWarehouseCode())
                    .returnCustomerName(saveDto.getReturnCustomerName() != null ? saveDto.getReturnCustomerName() : customer.getCustomerName())
                    .returnRequestDt(saveDto.getReturnRequestDt())
                    .itemName(saveDto.getItemName() != null ? saveDto.getItemName() : item.getItemName())
                    .specification(saveDto.getSpecification() != null ? saveDto.getSpecification() : item.getSpecification())
                    .unit(saveDto.getUnit() != null ? saveDto.getUnit() : item.getPurchaseUnit())
                    .qty(saveDto.getQty())
                    .priceType(saveDto.getPriceType() != null ? Integer.valueOf(saveDto.getPriceType()) : item.getPriceType())
                    .unitPrice(saveDto.getUnitPrice() != null ? saveDto.getUnitPrice() : orderItem.getOrderUnitPrice())
                    .supplyPrice(saveDto.getSupplyPrice() != null ? saveDto.getSupplyPrice().intValue() : orderItem.getSupplyAmt())
                    .vatAmt(saveDto.getVat() != null ? saveDto.getVat().intValue() : orderItem.getVatAmt())
                    .totalAmt(saveDto.getTotalAmount() != null ? saveDto.getTotalAmount().intValue() : orderItem.getTotalAmt())
                    .returnMessage(saveDto.getReturnMessage())
                    .note(saveDto.getNote())
                    .progressStatus(saveDto.getProgressStatus() != null ? saveDto.getProgressStatus() : "미승인")
                    .warehouseName(saveDto.getWarehouseName())
                    .orderItemCode(saveDto.getOrderItemCode())
                    .orderNo(saveDto.getOrderNo())
                    .build();
            
            returnEntity = returnRepository.save(returnEntity);
            
            log.info("반품 신규 생성 - returnNo: {}, customerName: {}", returnNo, customer.getCustomerName());
            
        } else {
            // 반품 수정
            returnEntity = returnRepository.findByReturnNo(saveDto.getReturnNo());
            if (returnEntity == null) {
                throw new RuntimeException("존재하지 않는 반품입니다: " + saveDto.getReturnNo());
            }
            
            // 승인된 반품은 수정 불가
            if ("승인".equals(returnEntity.getProgressStatus())) {
                throw new RuntimeException("승인된 반품은 수정할 수 없습니다.");
            }
            
            // 수정 가능한 필드들 업데이트
            returnEntity.setReturnCustomerCode(saveDto.getReturnCustomerCode());
            returnEntity.setItemCode(saveDto.getItemCode());
            returnEntity.setReceiveWarehouseCode(saveDto.getReceiveWarehouseCode());
            returnEntity.setReturnCustomerName(saveDto.getReturnCustomerName());
            returnEntity.setReturnRequestDt(saveDto.getReturnRequestDt());
            returnEntity.setItemName(saveDto.getItemName());
            returnEntity.setSpecification(saveDto.getSpecification());
            returnEntity.setUnit(saveDto.getUnit());
            returnEntity.setQty(saveDto.getQty());
            
            if (saveDto.getPriceType() != null) {
                returnEntity.setPriceType(Integer.valueOf(saveDto.getPriceType()));
            }
            
            if (saveDto.getSupplyPrice() != null) {
                returnEntity.setSupplyPrice(saveDto.getSupplyPrice().intValue());
            }
            if (saveDto.getVat() != null) {
                returnEntity.setVatAmt(saveDto.getVat().intValue());
            }
            if (saveDto.getTotalAmount() != null) {
                returnEntity.setTotalAmt(saveDto.getTotalAmount().intValue());
            }
            
            returnEntity.setReturnMessage(saveDto.getReturnMessage());
            returnEntity.setNote(saveDto.getNote());
            returnEntity.setProgressStatus(saveDto.getProgressStatus());
            returnEntity.setWarehouseName(saveDto.getWarehouseName());
            returnEntity.setDescription("반품수정");
            
            returnEntity = returnRepository.save(returnEntity);
            
            log.info("반품 정보 수정 - returnNo: {}", saveDto.getReturnNo());
        }
        
        return convertToRespDto(returnEntity);
    }

    /**
     * 반품 다중 삭제 (미승인 상태만 삭제 가능)
     */
    @Transactional
    public RespDto<ReturnBatchResult> deleteReturns(ReturnDeleteReqDto reqDto) {
        
        log.info("반품 다중 삭제 시작 - 총 {}건", reqDto.getReturnNos().size());
        
        List<String> successReturnNos = new ArrayList<>();
        List<ReturnBatchResult.ReturnErrorDto> failData = new ArrayList<>();
        
        for (String returnNo : reqDto.getReturnNos()) {
            try {
                // 개별 반품 삭제 처리
                deleteSingleReturn(returnNo);
                successReturnNos.add(returnNo);
                
                log.info("반품 삭제 성공 - returnNo: {}", returnNo);
                
            } catch (Exception e) {
                log.error("반품 삭제 실패 - returnNo: {}, 에러: {}", returnNo, e.getMessage());
                
                // 에러 시 반품 정보 조회 시도
                Return returnEntity = returnRepository.findByReturnNo(returnNo);
                String customerName = returnEntity != null ? returnEntity.getReturnCustomerName() : "알 수 없음";
                String itemName = returnEntity != null ? returnEntity.getItemName() : "알 수 없음";
                
                ReturnBatchResult.ReturnErrorDto errorDto = ReturnBatchResult.ReturnErrorDto.builder()
                        .returnNo(returnNo)
                        .customerName(customerName)
                        .itemName(itemName)
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성 (삭제는 successData 대신 성공 반품번호만)
        ReturnBatchResult result = ReturnBatchResult.builder()
                .totalCount(reqDto.getReturnNos().size())
                .successCount(successReturnNos.size())
                .failCount(failData.size())
                .successData(successReturnNos.stream()
                        .map(returnNo -> ReturnRespDto.builder().returnNo(returnNo).build())  // String 그대로 사용
                        .collect(Collectors.toList()))
                .failData(failData)
                .build();
        
        String message = String.format("반품 삭제 완료 - 성공: %d건, 실패: %d건", 
                successReturnNos.size(), failData.size());
        
        log.info("반품 다중 삭제 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getReturnNos().size(), successReturnNos.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 반품 삭제 처리 (기존 검증 로직 포함)
     */
    private void deleteSingleReturn(String returnNo) {
        
        // 미승인 상태의 반품만 조회
        Return returnToDelete = returnRepository.findByReturnNoAndStatusUnapproved(returnNo);
        if (returnToDelete == null) {
            throw new RuntimeException("해당 반품을 찾을 수 없거나 이미 승인된 반품은 삭제할 수 없습니다: " + returnNo);
        }
        
        // Hard Delete
        returnRepository.delete(returnToDelete);
        
        log.info("반품 삭제 완료 - returnNo: {}, customerName: {}", 
                returnNo, returnToDelete.getReturnCustomerName());
    }
    
    /**
     * Entity to RespDto 변환
     */
    private ReturnRespDto convertToRespDto(Return returnEntity) {
        return ReturnRespDto.builder()
                .returnNo(returnEntity.getReturnNo())  // String 그대로 사용
                .customerCode(returnEntity.getReturnCustomerCode())
                .customerName(returnEntity.getReturnCustomerName())
                .returnRequestDate(returnEntity.getReturnRequestDt())
                .itemCode(returnEntity.getItemCode())
                .itemName(returnEntity.getItemName())
                .specification(returnEntity.getSpecification())
                .unit(returnEntity.getUnit())
                .priceType(returnEntity.getPriceType() != null ? returnEntity.getPriceType().toString() : null)
                .quantity(returnEntity.getQty())
                .supplyPrice(returnEntity.getSupplyPrice() != null ? BigDecimal.valueOf(returnEntity.getSupplyPrice()) : BigDecimal.ZERO)
                .vat(returnEntity.getVatAmt() != null ? BigDecimal.valueOf(returnEntity.getVatAmt()) : BigDecimal.ZERO)
                .totalAmount(returnEntity.getTotalAmt() != null ? BigDecimal.valueOf(returnEntity.getTotalAmt()) : BigDecimal.ZERO)
                .warehouseCode(returnEntity.getReceiveWarehouseCode())
                .warehouseName(returnEntity.getWarehouseName())
                .status(returnEntity.getProgressStatus())
                .returnApprovalDate(returnEntity.getReturnApproveDt())
                .returnMessage(returnEntity.getReturnMessage())
                .message(returnEntity.getReplyMessage())
                .note(returnEntity.getNote())
                .description(returnEntity.getDescription())
                .createdAt(returnEntity.getCreatedAt() != null ? returnEntity.getCreatedAt().toString() : null)
                .updatedAt(returnEntity.getUpdatedAt() != null ? returnEntity.getUpdatedAt().toString() : null)
                .build();
    }
    
    /**
     * 반품번호 생성 (RET + YYYYMMDD + 001 형태)
     */
    private String generateReturnNo() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String datePrefix = "RET" + today;
        String latestReturnNo = returnRepository.findLatestReturnNoByDate(datePrefix);

        int sequence = 1;
        if (latestReturnNo != null && latestReturnNo.length() >= 14) {
            String sequencePart = latestReturnNo.substring(11);
            sequence = Integer.parseInt(sequencePart) + 1;
        }

        return String.format("%s%03d", datePrefix, sequence);
    }
}