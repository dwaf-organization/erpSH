package com.inc.sh.controller;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.virtualAccount.reqDto.VirtualAccountReqDto;
import com.inc.sh.dto.virtualAccount.reqDto.VirtualAccountSearchDto;
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
     * 가상계좌 목록 조회
     * GET /api/v1/erp/virtual-account/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<VirtualAccountRespDto>>> getVirtualAccountList(
            @RequestParam(value = "linkedCustomerCode", required = false) Integer linkedCustomerCode,
            @RequestParam(value = "virtualAccountStatus", required = false) String virtualAccountStatus,
            @RequestParam(value = "closeDtYn", required = false) String closeDtYn) {
        
        log.info("가상계좌 목록 조회 요청 - linkedCustomerCode: {}, virtualAccountStatus: {}, closeDtYn: {}", 
                linkedCustomerCode, virtualAccountStatus, closeDtYn);
        
        VirtualAccountSearchDto searchDto = VirtualAccountSearchDto.builder()
                .linkedCustomerCode(linkedCustomerCode)
                .virtualAccountStatus(virtualAccountStatus)
                .closeDtYn(closeDtYn)
                .build();
        
        RespDto<List<VirtualAccountRespDto>> response = virtualAccountService.getVirtualAccountList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
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
     * 가상계좌 저장 (신규/수정)
     * POST /api/v1/erp/virtual-account/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<VirtualAccountSaveRespDto>> saveVirtualAccount(
            @Valid @RequestBody VirtualAccountReqDto request) {
        
        if (request.getVirtualAccountCode() == null) {
            log.info("가상계좌 신규 등록 요청 - virtualAccountNum: {}, bankName: {}, linkedCustomerCode: {}", 
                    request.getVirtualAccountNum(), request.getBankName(), request.getLinkedCustomerCode());
        } else {
            log.info("가상계좌 수정 요청 - virtualAccountCode: {}, virtualAccountNum: {}, linkedCustomerCode: {}", 
                    request.getVirtualAccountCode(), request.getVirtualAccountNum(), request.getLinkedCustomerCode());
        }
        
        RespDto<VirtualAccountSaveRespDto> response = virtualAccountService.saveVirtualAccount(request);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
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