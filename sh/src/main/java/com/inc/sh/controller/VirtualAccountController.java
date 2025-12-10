package com.inc.sh.controller;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.virtualAccount.reqDto.VirtualAccountDeleteReqDto;
import com.inc.sh.dto.virtualAccount.reqDto.VirtualAccountReqDto;
import com.inc.sh.dto.virtualAccount.reqDto.VirtualAccountSaveReqDto;
import com.inc.sh.dto.virtualAccount.reqDto.VirtualAccountSearchDto;
import com.inc.sh.dto.virtualAccount.respDto.VirtualAccountBatchResult;
import com.inc.sh.dto.virtualAccount.respDto.VirtualAccountDeleteRespDto;
import com.inc.sh.dto.virtualAccount.respDto.VirtualAccountRespDto;
import com.inc.sh.dto.virtualAccount.respDto.VirtualAccountSaveRespDto;
import com.inc.sh.service.VirtualAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/virtual-account")
@RequiredArgsConstructor
@Slf4j
public class VirtualAccountController {

    private final VirtualAccountService virtualAccountService;

    /**
     * 가상계좌 목록 조회 (수정됨 - hqCode 추가, 거래처명 포함)
     * GET /api/v1/erp/virtual-account/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<VirtualAccountRespDto>>> getVirtualAccountList(
            @RequestParam("hqCode") Integer hqCode,
            @RequestParam(value = "linkedCustomerCode", required = false) Integer linkedCustomerCode,
            @RequestParam(value = "virtualAccountStatus", defaultValue = "전체") String virtualAccountStatus,
            @RequestParam(value = "closeDtYn", defaultValue = "전체") String closeDtYn) {
        
        log.info("가상계좌 목록 조회 요청 - hqCode: {}, linkedCustomerCode: {}, virtualAccountStatus: {}, closeDtYn: {}", 
                hqCode, linkedCustomerCode, virtualAccountStatus, closeDtYn);
        
        RespDto<List<VirtualAccountRespDto>> response = virtualAccountService.getVirtualAccountList(
                hqCode, linkedCustomerCode, virtualAccountStatus, closeDtYn);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 가상계좌 상세 조회
     * GET /api/v1/erp/virtual-account/detail/{virtualAccountCode}
     */
    @GetMapping("/detail/{virtualAccountCode}")
    public ResponseEntity<RespDto<VirtualAccountRespDto>> getVirtualAccount(
            @PathVariable("virtualAccountCode") Integer virtualAccountCode) {
        
        log.info("가상계좌 상세 조회 요청 - virtualAccountCode: {}", virtualAccountCode);
        
        RespDto<VirtualAccountRespDto> response = virtualAccountService.getVirtualAccount(virtualAccountCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 가상계좌 다중 저장 (신규/수정)
     * POST /api/v1/erp/virtual-account/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<VirtualAccountBatchResult>> saveVirtualAccounts(@RequestBody VirtualAccountSaveReqDto request) {
        
        log.info("가상계좌 다중 저장 요청 - 총 {}건", 
                request.getVirtualAccounts() != null ? request.getVirtualAccounts().size() : 0);
        
        // 요청 데이터 검증
        if (request.getVirtualAccounts() == null || request.getVirtualAccounts().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("저장할 가상계좌 데이터가 없습니다."));
        }
        
        RespDto<VirtualAccountBatchResult> response = virtualAccountService.saveVirtualAccounts(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 가상계좌 다중 삭제 (Hard Delete)
     * DELETE /api/v1/erp/virtual-account/delete
     */
    @DeleteMapping("/delete")
    public ResponseEntity<RespDto<VirtualAccountBatchResult>> deleteVirtualAccounts(@RequestBody VirtualAccountDeleteReqDto request) {
        
        log.info("가상계좌 다중 삭제 요청 - 총 {}건", 
                request.getVirtualAccountCodes() != null ? request.getVirtualAccountCodes().size() : 0);
        
        // 요청 데이터 검증
        if (request.getVirtualAccountCodes() == null || request.getVirtualAccountCodes().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("삭제할 가상계좌 코드가 없습니다."));
        }
        
        RespDto<VirtualAccountBatchResult> response = virtualAccountService.deleteVirtualAccounts(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 가상계좌 삭제 (하드 삭제)
     * DELETE /api/v1/erp/virtual-account/{virtualAccountCode}
     */
    @DeleteMapping("/{virtualAccountCode}")
    public ResponseEntity<RespDto<VirtualAccountDeleteRespDto>> deleteVirtualAccount(
            @PathVariable("virtualAccountCode") Integer virtualAccountCode) {
        
        log.info("가상계좌 삭제 요청 - virtualAccountCode: {}", virtualAccountCode);
        
        RespDto<VirtualAccountDeleteRespDto> response = virtualAccountService.deleteVirtualAccount(virtualAccountCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}