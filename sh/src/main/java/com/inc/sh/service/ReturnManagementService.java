package com.inc.sh.service;

import com.inc.sh.dto.returnManagement.reqDto.ReturnSearchDto;
import com.inc.sh.dto.returnManagement.reqDto.ReturnUpdateDto;
import com.inc.sh.dto.returnManagement.respDto.ReturnRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.entity.Return;
import com.inc.sh.repository.ReturnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnManagementService {
    
    private final ReturnRepository returnRepository;
    
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
                                .returnCode(result[0] != null ? Integer.valueOf(result[0].toString()) : null) // return_no를 returnCode로
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
     * 반품 수정 (수정만 가능, 생성은 다른 페이지에서 처리)
     */
    @Transactional
    public RespDto<String> updateReturn(ReturnUpdateDto updateDto) {
        try {
            log.info("반품 수정 시작 - 반품코드: {}", updateDto.getReturnCode());
            
            // 기존 반품 조회
            Return existingReturn = returnRepository.findByReturnNo(updateDto.getReturnCode().toString());
            if (existingReturn == null) {
                return RespDto.fail("해당 반품을 찾을 수 없습니다.");
            }
            
            // 수정 가능한 필드들 업데이트
            existingReturn.setReturnCustomerCode(updateDto.getCustomerCode());
            existingReturn.setItemCode(updateDto.getItemCode());
            existingReturn.setReceiveWarehouseCode(updateDto.getWarehouseCode());
            existingReturn.setReturnCustomerName(updateDto.getCustomerName());
            existingReturn.setReturnRequestDt(updateDto.getReturnRequestDate());
            existingReturn.setItemName(updateDto.getItemName());
            existingReturn.setSpecification(updateDto.getSpecification());
            existingReturn.setUnit(updateDto.getUnit());
            existingReturn.setQty(updateDto.getQuantity());
            
            // priceType은 Integer 타입으로 저장
            existingReturn.setPriceType(updateDto.getPriceType() != null ? Integer.valueOf(updateDto.getPriceType()) : null);
            
            // 금액 필드들 (BigDecimal → Integer 변환)
            if (updateDto.getSupplyPrice() != null) {
                existingReturn.setSupplyPrice(updateDto.getSupplyPrice().intValue());
            }
            if (updateDto.getVat() != null) {
                existingReturn.setVatAmt(updateDto.getVat().intValue());
            }
            if (updateDto.getTotalAmount() != null) {
                existingReturn.setTotalAmt(updateDto.getTotalAmount().intValue());
            }
            
            // return_message와 reply_message 구분하여 처리
            existingReturn.setReturnMessage(updateDto.getReturnMessage()); // 반품사유
            existingReturn.setReplyMessage(updateDto.getReplyMessage());         // 회신메시지
            existingReturn.setNote(updateDto.getNote());
            existingReturn.setProgressStatus(updateDto.getStatus());
            existingReturn.setWarehouseName(updateDto.getWarehouseName());
            existingReturn.setReturnApproveDt(updateDto.getReturnApprovalDate());
            existingReturn.setDescription("반품수정");
            
            returnRepository.save(existingReturn);
            
            log.info("반품 수정 완료 - 반품코드: {}", updateDto.getReturnCode());
            return RespDto.success("반품이 수정되었습니다.", updateDto.getReturnCode().toString());
            
        } catch (Exception e) {
            log.error("반품 수정 중 오류 발생", e);
            return RespDto.fail("반품 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 반품 삭제 (미승인 상태만 삭제 가능)
     */
    @Transactional
    public RespDto<String> deleteReturn(String returnNo) {
        try {
            log.info("반품 삭제 시작 - 반품번호: {}", returnNo);
            
            // 미승인 상태의 반품만 조회
            Return returnToDelete = returnRepository.findByReturnNoAndStatusUnapproved(returnNo);
            if (returnToDelete == null) {
                return RespDto.fail("해당 반품을 찾을 수 없거나 이미 승인된 반품은 삭제할 수 없습니다.");
            }
            
            returnRepository.delete(returnToDelete);
            
            log.info("반품 삭제 완료 - 반품번호: {}", returnNo);
            return RespDto.success("반품이 삭제되었습니다.", "삭제 완료");
            
        } catch (Exception e) {
            log.error("반품 삭제 중 오류 발생", e);
            return RespDto.fail("반품 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}