package com.inc.sh.service;

import com.inc.sh.dto.returnManagement.reqDto.ReturnApprovalDto;
import com.inc.sh.dto.returnManagement.reqDto.ReturnDeleteReqDto;
import com.inc.sh.dto.returnManagement.reqDto.ReturnSaveReqDto;
import com.inc.sh.dto.returnManagement.reqDto.ReturnSearchDto;
import com.inc.sh.dto.returnManagement.reqDto.ReturnUpdateDto;
import com.inc.sh.dto.returnManagement.respDto.ReturnBatchResult;
import com.inc.sh.dto.returnManagement.respDto.ReturnRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.entity.Customer;
import com.inc.sh.entity.CustomerAccountTransactions;
import com.inc.sh.entity.InventoryTransactions;
import com.inc.sh.entity.Item;
import com.inc.sh.entity.MonthlyInventoryClosing;
import com.inc.sh.entity.Order;
import com.inc.sh.entity.OrderItem;
import com.inc.sh.entity.Return;
import com.inc.sh.entity.Warehouse;
import com.inc.sh.entity.WarehouseItems;
import com.inc.sh.repository.CustomerAccountTransactionsRepository;
import com.inc.sh.repository.CustomerRepository;
import com.inc.sh.repository.InventoryTransactionsRepository;
import com.inc.sh.repository.ItemRepository;
import com.inc.sh.repository.MonthlyInventoryClosingRepository;
import com.inc.sh.repository.OrderItemRepository;
import com.inc.sh.repository.OrderRepository;
import com.inc.sh.repository.ReturnRepository;
import com.inc.sh.repository.WarehouseItemsRepository;
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
import java.util.Optional;
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
    private final WarehouseItemsRepository warehouseItemsRepository;
    private final MonthlyInventoryClosingRepository monthlyInventoryClosingRepository;
    private final InventoryTransactionsRepository inventoryTransactionsRepository;
    
    // 금융처리를 위한 의존성 추가
    private final CustomerAccountTransactionsRepository customerAccountTransactionsRepository;
    
    /**
     * 반품 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<ReturnRespDto>> getReturnList(ReturnSearchDto searchDto) {
        try {
            log.info("반품 조회 시작 - 조건: startDate: {}, endDate: {}, customerCode: {}, status: {}, hqCode: {}", 
                    searchDto.getStartDate(), searchDto.getEndDate(), searchDto.getCustomerCode(), 
                    searchDto.getStatus(), searchDto.getHqCode());
            
            List<Object[]> results = returnRepository.findReturnsWithJoinByConditionsAndHqCode(
                    searchDto.getStartDate(),
                    searchDto.getEndDate(),
                    searchDto.getCustomerCode(),
                    searchDto.getStatus(),
                    searchDto.getHqCode()
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
                                .orderNo((String) result[24])
                                .unitPrice((Integer) result[25])
                                .build();
                        return dto;
                    })
                    .collect(Collectors.toList());
            
            log.info("반품 조회 완료 - hqCode: {}, 조회 건수: {}", searchDto.getHqCode(), returnList.size());
            return RespDto.success("반품 조회 성공", returnList);
            
        } catch (Exception e) {
            log.error("반품 조회 중 오류 발생 - hqCode: {}", searchDto.getHqCode(), e);
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
                    .orderNo(saveDto.getOrderNo())
                    .orderItemCode(saveDto.getOrderItemCode())
                    .itemCode(saveDto.getItemCode())
                    .receiveWarehouseCode(saveDto.getReceiveWarehouseCode())
                    .warehouseName(saveDto.getWarehouseName())
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
                    .replyMessage(saveDto.getReplyMessage())
                    .note(saveDto.getNote())
                    .progressStatus(saveDto.getProgressStatus() != null ? saveDto.getProgressStatus() : "미승인")
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
            returnEntity.setReplyMessage(saveDto.getReplyMessage());
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
                .replyMessage(returnEntity.getReplyMessage())
                .message(returnEntity.getReplyMessage())
                .note(returnEntity.getNote())
                .orderNo(returnEntity.getOrderNo())
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
    
    /**
     * 반품 승인/미승인 처리 (✅ 재고처리 포함)
     */
    @Transactional
    public RespDto<ReturnBatchResult> processReturnApproval(ReturnApprovalDto approvalDto) {
        try {
            log.info("반품 승인/미승인 처리 시작 - 액션: {}, 대상: {}건", 
                    approvalDto.getApprovalAction(), approvalDto.getReturnNos().size());
            
            List<ReturnRespDto> successList = new ArrayList<>();
            List<ReturnBatchResult.ReturnErrorDto> failureList = new ArrayList<>();
            
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String currentYm = currentDate.substring(0, 6); // YYYYMM
            
            for (String returnNo : approvalDto.getReturnNos()) {
                try {
                    Return returnEntity = returnRepository.findByReturnNo(returnNo);
                    if (returnEntity == null) {
                        failureList.add(ReturnBatchResult.ReturnErrorDto.builder()
                                .returnNo(returnNo)
                                .customerName("알 수 없음")
                                .itemName("알 수 없음")
                                .errorMessage("존재하지 않는 반품입니다")
                                .build());
                        continue;
                    }
                    
                    // 현재 상태와 요청 액션이 같으면 스킵
                    if (approvalDto.getApprovalAction().equals(returnEntity.getProgressStatus())) {
                        failureList.add(ReturnBatchResult.ReturnErrorDto.builder()
                                .returnNo(returnNo)
                                .customerName(returnEntity.getReturnCustomerName())
                                .itemName(returnEntity.getItemName())
                                .errorMessage("이미 " + approvalDto.getApprovalAction() + " 상태입니다")
                                .build());
                        continue;
                    }
                    
                    // ✅ 승인처리시 재고 업데이트 및 금융처리
                    if ("승인".equals(approvalDto.getApprovalAction())) {
                        processReturnApprovalInventory(returnEntity, currentDate, currentYm);
                        processReturnApprovalPayment(returnEntity, currentDate); // 금융처리 추가
                    }
                    
                    // ✅ 미승인으로 되돌릴 때 재고 롤백 및 금융 롤백
                    if ("미승인".equals(approvalDto.getApprovalAction()) && "승인".equals(returnEntity.getProgressStatus())) {
                        rollbackReturnApprovalInventory(returnEntity, currentDate, currentYm);
                        rollbackReturnApprovalPayment(returnEntity, currentDate); // 금융롤백 추가
                    }
                    
                    // 반품 상태 업데이트
                    returnEntity.setProgressStatus(approvalDto.getApprovalAction());
                    if ("승인".equals(approvalDto.getApprovalAction())) {
                        returnEntity.setReturnApproveDt(currentDate);
                    } else {
                        returnEntity.setReturnApproveDt(null); // 미승인시 승인일자 초기화
                    }
                    
                    // 승인사유 업데이트 (선택사항)
                    if (approvalDto.getApprovalNote() != null) {
                        returnEntity.setReplyMessage(approvalDto.getApprovalNote());
                    }
                    
                    returnEntity = returnRepository.save(returnEntity);
                    
                    // 성공 데이터 생성
                    ReturnRespDto successData = ReturnRespDto.builder()
                            .returnNo(returnEntity.getReturnNo())
                            .orderNo(returnEntity.getOrderNo())
                            .customerCode(returnEntity.getReturnCustomerCode())
                            .customerName(returnEntity.getReturnCustomerName())
                            .itemCode(returnEntity.getItemCode())
                            .itemName(returnEntity.getItemName())
                            .quantity(returnEntity.getQty())
                            .status(returnEntity.getProgressStatus())
                            .returnApprovalDate(returnEntity.getReturnApproveDt())
                            .build();
                    
                    successList.add(successData);
                    
                    log.info("반품 {}처리 완료 - returnNo: {}, customerName: {}", 
                            approvalDto.getApprovalAction(), returnNo, returnEntity.getReturnCustomerName());
                    
                } catch (Exception e) {
                    log.error("반품 {}처리 중 오류 발생 - returnNo: {}", approvalDto.getApprovalAction(), returnNo, e);
                    
                    // 에러 정보 안전 조회
                    ReturnBatchResult.ReturnErrorDto errorInfo = getReturnErrorInfoSafely(returnNo, e.getMessage());
                    failureList.add(errorInfo);
                }
            }
            
            // 배치 결과 생성
            ReturnBatchResult batchResult = ReturnBatchResult.builder()
                    .totalCount(approvalDto.getReturnNos().size())
                    .successCount(successList.size())
                    .failCount(failureList.size())
                    .successData(successList)
                    .failData(failureList)
                    .build();
            
            String message = String.format("반품 %s처리 완료 - 성공: %d건, 실패: %d건", 
                    approvalDto.getApprovalAction(), batchResult.getSuccessCount(), batchResult.getFailCount());
            
            log.info("반품 승인/미승인 배치 처리 완료 - 총:{}건, 성공:{}건, 실패:{}건", 
                    batchResult.getTotalCount(), batchResult.getSuccessCount(), batchResult.getFailCount());
            
            return RespDto.success(message, batchResult);
            
        } catch (Exception e) {
            log.error("반품 승인/미승인 처리 중 오류 발생", e);
            return RespDto.fail("반품 승인/미승인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 반품승인시 재고 처리 (창고재고 증가 + 월별재고마감 입고량 증가)
     */
    private void processReturnApprovalInventory(Return returnEntity, String currentDate, String currentYm) {
        
        // 1. 창고재고 증가
        Optional<WarehouseItems> warehouseItemOpt = warehouseItemsRepository
                .findByWarehouseCodeAndItemCode(returnEntity.getReceiveWarehouseCode(), returnEntity.getItemCode());
        
        if (warehouseItemOpt.isPresent()) {
            WarehouseItems warehouseItem = warehouseItemOpt.get();
            
            // 창고재고 증가 (반품입고)
            warehouseItem.setCurrentQuantity(warehouseItem.getCurrentQuantity() + returnEntity.getQty());
            warehouseItemsRepository.save(warehouseItem);
            
            // 2. 재고수불부 기록 추가
            createReturnInventoryTransaction(warehouseItem, returnEntity, currentDate, "반품입고");
            
            // 3. 월별재고마감 업데이트 (입고량 증가)
            updateMonthlyInventoryForReturn(returnEntity.getReceiveWarehouseCode(), 
                    returnEntity.getItemCode(), returnEntity.getQty(), 
                    returnEntity.getQty() * returnEntity.getUnitPrice(), currentYm);
            
            log.info("반품승인 재고처리 완료 - 품목코드: {}, 입고수량: {}, 창고코드: {}", 
                    returnEntity.getItemCode(), returnEntity.getQty(), returnEntity.getReceiveWarehouseCode());
            
        } else {
            throw new RuntimeException(String.format(
                "창고품목을 찾을 수 없습니다. 창고코드: %d, 품목코드: %d", 
                returnEntity.getReceiveWarehouseCode(), returnEntity.getItemCode()));
        }
    }
    
    /**
     * 반품승인 롤백시 재고 처리 (창고재고 감소 + 월별재고마감 입고량 감소)
     */
    private void rollbackReturnApprovalInventory(Return returnEntity, String currentDate, String currentYm) {
        
        // 1. 창고재고 감소
        Optional<WarehouseItems> warehouseItemOpt = warehouseItemsRepository
                .findByWarehouseCodeAndItemCode(returnEntity.getReceiveWarehouseCode(), returnEntity.getItemCode());
        
        if (warehouseItemOpt.isPresent()) {
            WarehouseItems warehouseItem = warehouseItemOpt.get();
            
            // 재고 부족 체크
            if (warehouseItem.getCurrentQuantity() < returnEntity.getQty()) {
                throw new RuntimeException(String.format(
                    "재고 부족으로 반품승인을 취소할 수 없습니다. 현재고: %d, 반품수량: %d", 
                    warehouseItem.getCurrentQuantity(), returnEntity.getQty()));
            }
            
            // 창고재고 감소
            warehouseItem.setCurrentQuantity(warehouseItem.getCurrentQuantity() - returnEntity.getQty());
            warehouseItemsRepository.save(warehouseItem);
            
            // 2. 재고수불부 기록 추가
            createReturnInventoryTransaction(warehouseItem, returnEntity, currentDate, "반품승인취소");
            
            // 3. 월별재고마감 업데이트 (입고량 감소)
            updateMonthlyInventoryForReturn(returnEntity.getReceiveWarehouseCode(), 
                    returnEntity.getItemCode(), -returnEntity.getQty(), 
                    -(returnEntity.getQty() * returnEntity.getUnitPrice()), currentYm);
            
            log.info("반품승인 롤백 재고처리 완료 - 품목코드: {}, 감소수량: {}, 창고코드: {}", 
                    returnEntity.getItemCode(), returnEntity.getQty(), returnEntity.getReceiveWarehouseCode());
            
        } else {
            throw new RuntimeException(String.format(
                "창고품목을 찾을 수 없습니다. 창고코드: %d, 품목코드: %d", 
                returnEntity.getReceiveWarehouseCode(), returnEntity.getItemCode()));
        }
    }
    
    /**
     * 반품용 재고수불부 기록 생성
     */
    private void createReturnInventoryTransaction(WarehouseItems warehouseItem, Return returnEntity, 
                                                String currentDate, String transactionType) {
        InventoryTransactions transaction = InventoryTransactions.builder()
                .warehouseCode(warehouseItem.getWarehouseCode())
                .warehouseItemCode(warehouseItem.getWarehouseItemCode())
                .itemCode(warehouseItem.getItemCode())
                .transactionDate(currentDate)
                .transactionType(transactionType)
                .quantity(returnEntity.getQty())
                .unitPrice(returnEntity.getUnitPrice())
                .amount(returnEntity.getQty() * returnEntity.getUnitPrice())
                .description("반품번호: " + returnEntity.getReturnNo())
                .build();
        
        inventoryTransactionsRepository.save(transaction);
    }
    
    /**
     * 반품용 월별재고마감 업데이트
     */
    private void updateMonthlyInventoryForReturn(Integer warehouseCode, Integer itemCode, 
                                               Integer inQuantityChange, Integer inAmountChange, String closingYm) {
        
        Optional<MonthlyInventoryClosing> closingOpt = monthlyInventoryClosingRepository
                .findByWarehouseCodeAndItemCodeAndClosingYm(warehouseCode, itemCode, closingYm);
        
        if (closingOpt.isPresent()) {
            MonthlyInventoryClosing closing = closingOpt.get();
            
            // ✅ 입고량/입고금액 업데이트 (반품은 입고로 처리)
            closing.setInQuantity(closing.getInQuantity() + inQuantityChange);
            closing.setInAmount(closing.getInAmount() + inAmountChange);
            
            // 계산수량/계산금액 재계산 (이월+입고-출고)
            Integer calQuantity = closing.getOpeningQuantity() + closing.getInQuantity() - closing.getOutQuantity();
            Integer calAmount = closing.getOpeningAmount() + closing.getInAmount() - closing.getOutAmount();
            
            closing.setCalQuantity(calQuantity);
            closing.setCalAmount(calAmount);
            
            monthlyInventoryClosingRepository.save(closing);
            
            log.info("반품 월별재고마감 업데이트 완료 - 창고코드: {}, 품목코드: {}, 입고량 변화: {}", 
                    warehouseCode, itemCode, inQuantityChange);
            
        } else {
            // 월별재고마감 데이터가 없으면 에러
            throw new RuntimeException(String.format(
                "반품처리를 위한 월별재고마감 데이터가 없습니다. 창고코드: %d, 품목코드: %d, 마감년월: %s (재고등록이 필요합니다)", 
                warehouseCode, itemCode, closingYm));
        }
    }
    
    /**
     * 에러 정보 안전 조회
     */
    private ReturnBatchResult.ReturnErrorDto getReturnErrorInfoSafely(String returnNo, String errorMessage) {
        try {
            Return returnEntity = returnRepository.findByReturnNo(returnNo);
            if (returnEntity != null) {
                return ReturnBatchResult.ReturnErrorDto.builder()
                        .returnNo(returnNo)
                        .customerName(returnEntity.getReturnCustomerName())
                        .itemName(returnEntity.getItemName())
                        .errorMessage(errorMessage)
                        .build();
            }
        } catch (Exception e) {
            log.warn("에러 정보 조회 실패 - returnNo: {}", returnNo, e);
        }
        
        return ReturnBatchResult.ReturnErrorDto.builder()
                .returnNo(returnNo)
                .customerName("알 수 없음")
                .itemName("알 수 없음")
                .errorMessage(errorMessage)
                .build();
    }
    
    /**
     * ✅ 반품승인시 금융처리 (모든 거래처 처리)
     */
    private void processReturnApprovalPayment(Return returnEntity, String currentDate) {
        try {
            // 1. 거래처 조회
            Customer customer = customerRepository.findByCustomerCode(returnEntity.getReturnCustomerCode());
            if (customer == null) {
                log.warn("거래처를 찾을 수 없어 금융처리를 스킵합니다 - customerCode: {}", returnEntity.getReturnCustomerCode());
                return;
            }
            
            // 2. 거래처 잔액 증가 (반품금액만큼 환불) - 충전형/후입금 구분없이 처리
            Integer refundAmount = returnEntity.getTotalAmt() != null ? returnEntity.getTotalAmt() : 0;
            customer.setBalanceAmt(customer.getBalanceAmt() + refundAmount);
            customerRepository.save(customer);
            
            // 3. 거래처계좌내역 생성
            CustomerAccountTransactions transaction = CustomerAccountTransactions.builder()
                    .customerCode(returnEntity.getReturnCustomerCode())
                    .virtualAccountCode(customer.getVirtualAccountCode())
                    .transactionDate(currentDate)
                    .transactionType("입금") // 반품승인 = 고객에게 환불 = 입금
                    .amount(refundAmount)
                    .balanceAfter(customer.getBalanceAmt())
                    .referenceType("반품")
                    .referenceId(returnEntity.getReturnNo())
                    .note("반품승인 환불 - " + returnEntity.getItemName())
                    .description("반품승인처리")
                    .build();
            
            customerAccountTransactionsRepository.save(transaction);
            
            log.info("반품승인 금융처리 완료 - customerCode: {}, 환불금액: {}, 잔액: {}", 
                    returnEntity.getReturnCustomerCode(), refundAmount, customer.getBalanceAmt());
            
        } catch (Exception e) {
            log.error("반품승인 금융처리 중 오류 발생 - customerCode: {}", returnEntity.getReturnCustomerCode(), e);
            throw new RuntimeException("반품승인 금융처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * ✅ 반품승인 롤백시 금융처리 (모든 거래처 처리)
     */
    private void rollbackReturnApprovalPayment(Return returnEntity, String currentDate) {
        try {
            // 1. 거래처 조회
            Customer customer = customerRepository.findByCustomerCode(returnEntity.getReturnCustomerCode());
            if (customer == null) {
                log.warn("거래처를 찾을 수 없어 금융롤백을 스킵합니다 - customerCode: {}", returnEntity.getReturnCustomerCode());
                return;
            }
            
            Integer rollbackAmount = returnEntity.getTotalAmt() != null ? returnEntity.getTotalAmt() : 0;
            
            // 2. 잔액 부족 체크
            if (customer.getBalanceAmt() < rollbackAmount) {
                throw new RuntimeException(String.format(
                    "잔액 부족으로 반품승인을 취소할 수 없습니다. 현재잔액: %d, 필요금액: %d", 
                    customer.getBalanceAmt(), rollbackAmount));
            }
            
            // 3. 거래처 잔액 감소 (환불했던 금액 회수)
            customer.setBalanceAmt(customer.getBalanceAmt() - rollbackAmount);
            customerRepository.save(customer);
            
            // 4. 거래처계좌내역 생성
            CustomerAccountTransactions transaction = CustomerAccountTransactions.builder()
                    .customerCode(returnEntity.getReturnCustomerCode())
                    .virtualAccountCode(customer.getVirtualAccountCode())
                    .transactionDate(currentDate)
                    .transactionType("출금") // 승인취소 = 환불금액 회수 = 출금
                    .amount(rollbackAmount)
                    .balanceAfter(customer.getBalanceAmt())
                    .referenceType("반품취소")
                    .referenceId(returnEntity.getReturnNo())
                    .note("반품승인취소 - " + returnEntity.getItemName())
                    .description("반품승인취소처리")
                    .build();
            
            customerAccountTransactionsRepository.save(transaction);
            
            log.info("반품승인 금융롤백 완료 - customerCode: {}, 회수금액: {}, 잔액: {}", 
                    returnEntity.getReturnCustomerCode(), rollbackAmount, customer.getBalanceAmt());
            
        } catch (Exception e) {
            log.error("반품승인 금융롤백 중 오류 발생 - customerCode: {}", returnEntity.getReturnCustomerCode(), e);
            throw new RuntimeException("반품승인 금융롤백 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}