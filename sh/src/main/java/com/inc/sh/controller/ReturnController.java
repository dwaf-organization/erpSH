package com.inc.sh.controller;

import com.inc.sh.dto.returnManagement.reqDto.ReturnDeleteReqDto;
import com.inc.sh.dto.returnManagement.reqDto.ReturnSaveReqDto;
import com.inc.sh.dto.returnManagement.reqDto.ReturnSearchDto;
import com.inc.sh.dto.returnManagement.reqDto.ReturnUpdateDto;
import com.inc.sh.dto.returnManagement.respDto.ReturnBatchResult;
import com.inc.sh.dto.returnManagement.respDto.ReturnRespDto;
import com.inc.sh.dto.returnRegistration.reqDto.ReturnOrderSearchDto;
import com.inc.sh.dto.returnRegistration.reqDto.ReturnRegistrationSaveDto;
import com.inc.sh.dto.returnRegistration.respDto.ReturnOrderItemRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.ReturnManagementService;
import com.inc.sh.service.ReturnRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/erp/return")
@RequiredArgsConstructor
@Slf4j
public class ReturnController {

    private final ReturnManagementService returnManagementService;
    private final ReturnRegistrationService returnRegistrationService;

    // ==================== 반품관리 (조회/수정/삭제) ====================

    /**
     * 반품 조회
     * GET /api/v1/erp/return/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<ReturnRespDto>>> getReturnList(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "customerCode", required = false) Integer customerCode,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("반품 조회 요청 - 기간: {} ~ {}, 거래처코드: {}, 진행상태: {}, hqCode: {}", 
                startDate, endDate, customerCode, status, hqCode);
        
        ReturnSearchDto searchDto = ReturnSearchDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .customerCode(customerCode)
                .status(status)
                .hqCode(hqCode)
                .build();
        
        RespDto<List<ReturnRespDto>> response = returnManagementService.getReturnList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 반품등록용 주문품목 조회
     * GET /api/v1/erp/return/order-items
     */
    @GetMapping("/order-items")
    public ResponseEntity<RespDto<List<ReturnOrderItemRespDto>>> getOrderItemsForReturn(
            @RequestParam(value = "customerCode", required = false) Integer customerCode,
            @RequestParam(value = "orderNo", required = false) String orderNo) {
        
        log.info("반품등록용 주문품목 조회 요청 - 거래처코드: {}, 주문번호: {}", customerCode, orderNo);
        
        // 검색 조건 DTO 생성
        ReturnOrderSearchDto searchDto = ReturnOrderSearchDto.builder()
                .customerCode(customerCode)
                .orderNo(orderNo)
                .build();
        
        // Service 호출
        RespDto<List<ReturnOrderItemRespDto>> response = returnRegistrationService.getOrderItemsForReturn(searchDto);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 반품 다중 저장 (신규/수정) - 모든 검증 포함
     * PUT /api/v1/erp/return/save
     */
    @PutMapping("/save")
    public ResponseEntity<RespDto<ReturnBatchResult>> saveReturns(@RequestBody ReturnSaveReqDto request) {
        
        log.info("반품 다중 저장 요청 - 총 {}건", 
                request.getReturns() != null ? request.getReturns().size() : 0);
        
        // 요청 데이터 검증
        if (request.getReturns() == null || request.getReturns().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("저장할 반품 데이터가 없습니다."));
        }
        
        // 개별 항목 필수 필드 검증
        for (ReturnSaveReqDto.ReturnSaveItemDto returnItem : request.getReturns()) {
            if (returnItem.getReturnCustomerCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("거래처코드는 필수입니다."));
            }
            if (returnItem.getItemCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("품목코드는 필수입니다."));
            }
            if (returnItem.getReturnRequestDt() == null || returnItem.getReturnRequestDt().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("반품요청일자는 필수입니다."));
            }
            if (returnItem.getOrderItemCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("주문품목코드는 필수입니다."));
            }
            if (returnItem.getOrderNo() == null || returnItem.getOrderNo().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("주문번호는 필수입니다."));
            }
            if (returnItem.getQty() == null || returnItem.getQty() <= 0) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("반품수량은 1 이상이어야 합니다."));
            }
            
            // 기본값 설정
            if (returnItem.getProgressStatus() == null || returnItem.getProgressStatus().trim().isEmpty()) {
                returnItem.setProgressStatus("미승인");
            }
        }
        
        RespDto<ReturnBatchResult> response = returnManagementService.saveReturns(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 반품 다중 삭제 (미승인 상태만 삭제 가능)
     * DELETE /api/v1/erp/return/delete
     */
    @DeleteMapping("/delete")
    public ResponseEntity<RespDto<ReturnBatchResult>> deleteReturns(@RequestBody ReturnDeleteReqDto request) {
        
        log.info("반품 다중 삭제 요청 - 총 {}건", 
                request.getReturnNos() != null ? request.getReturnNos().size() : 0);
        
        // 요청 데이터 검증
        if (request.getReturnNos() == null || request.getReturnNos().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("삭제할 반품번호가 없습니다."));
        }
        
        // 반품번호 형식 검증 및 중복 제거
        List<String> validReturnNos = request.getReturnNos().stream()
                .filter(returnNo -> returnNo != null && !returnNo.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
        
        if (validReturnNos.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("유효한 반품번호가 없습니다."));
        }
        
        if (validReturnNos.size() != request.getReturnNos().size()) {
            log.info("중복/빈값 반품번호 제거됨 - 원본: {}건, 제거 후: {}건", 
                    request.getReturnNos().size(), validReturnNos.size());
            request.setReturnNos(validReturnNos);
        }
        
        RespDto<ReturnBatchResult> response = returnManagementService.deleteReturns(request);
        return ResponseEntity.ok(response);
    }
}