package com.inc.sh.controller;

import com.inc.sh.dto.returnManagement.reqDto.ReturnSearchDto;
import com.inc.sh.dto.returnManagement.reqDto.ReturnUpdateDto;
import com.inc.sh.dto.returnManagement.respDto.ReturnRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.ReturnManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/return-management")
@RequiredArgsConstructor
@Slf4j
public class ReturnManagementController {

    private final ReturnManagementService returnManagementService;

    /**
     * 반품 조회
     * GET /api/v1/erp/return-management/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<ReturnRespDto>>> getReturnList(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "customerCode", required = false) Integer customerCode,
            @RequestParam(value = "status", required = false) String status) {
        
        log.info("반품 조회 요청 - 기간: {} ~ {}, 거래처코드: {}, 진행상태: {}", 
                startDate, endDate, customerCode, status);
        
        // YYYYMMDD 형식으로 받음 (예: 20241117)
        
        ReturnSearchDto searchDto = ReturnSearchDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .customerCode(customerCode)
                .status(status)
                .build();
        
        RespDto<List<ReturnRespDto>> response = returnManagementService.getReturnList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 반품 수정
     * PUT /api/v1/erp/return-management/update
     */
    @PutMapping("/update")
    public ResponseEntity<RespDto<String>> updateReturn(@RequestBody ReturnUpdateDto updateDto) {
        
        log.info("반품 수정 요청 - 반품코드: {}", updateDto.getReturnCode());
        
        RespDto<String> response = returnManagementService.updateReturn(updateDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 반품 삭제 (미승인 상태만 삭제 가능)
     * DELETE /api/v1/erp/return-management/{returnNo}
     */
    @DeleteMapping("/{returnNo}")
    public ResponseEntity<RespDto<String>> deleteReturn(@PathVariable String returnNo) {
        
        log.info("반품 삭제 요청 - 반품번호: {}", returnNo);
        
        RespDto<String> response = returnManagementService.deleteReturn(returnNo);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}