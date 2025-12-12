package com.inc.sh.controller.admin;

import com.inc.sh.dto.headquarter.respDto.AdminHeadquarterListRespDto;
import com.inc.sh.dto.headquarter.respDto.HeadquarterRespDto;
import com.inc.sh.dto.headquarter.reqDto.AdminHeadquarterReqDto;
import com.inc.sh.dto.headquarter.reqDto.HeadquarterReqDto;
import com.inc.sh.dto.headquarter.respDto.AdminHeadquarterDetailRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.admin.AdminHeadquarterService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/headquarters")
@RequiredArgsConstructor
@Slf4j
public class AdminHeadquarterController {

    private final AdminHeadquarterService adminHeadquarterService;
    /**
     * 관리자 본사 목록 조회 (전체)
     * GET /api/v1/admin/headquarters/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<AdminHeadquarterListRespDto>>> getHeadquarterList() {
        
        log.info("관리자 본사 목록 조회 요청");
        
        RespDto<List<AdminHeadquarterListRespDto>> response = adminHeadquarterService.getHeadquarterList();
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 관리자 본사 상세 조회 (개별)
     * GET /api/v1/admin/headquarters/one?hqCode={hqCode}
     */
    @GetMapping("/one")
    public ResponseEntity<RespDto<AdminHeadquarterDetailRespDto>> getHeadquarterDetail(
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("관리자 본사 상세 조회 요청 - hqCode: {}", hqCode);
        
        RespDto<AdminHeadquarterDetailRespDto> response = adminHeadquarterService.getHeadquarterDetail(hqCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 관리자 본사 저장 (생성/수정 통합)
     * POST /api/v1/admin/headquarters/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<HeadquarterRespDto>> saveHeadquarterAdmin(
            @Valid @RequestBody AdminHeadquarterReqDto request) {
        
        String action = request.getHqCode() == null ? "생성" : "수정";
        log.info("관리자 본사 {} 요청 - hqCode: {}, companyName: {}", action, request.getHqCode(), request.getCompanyName());
        
        RespDto<HeadquarterRespDto> response = adminHeadquarterService.saveHeadquarterAdmin(request);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 관리자 본사 삭제
     * DELETE /api/v1/admin/headquarters/delete?hqCode={hqCode}
     */
    @DeleteMapping("/delete")
    public ResponseEntity<RespDto<String>> deleteHeadquarter(
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("관리자 본사 삭제 요청 - hqCode: {}", hqCode);
        
        RespDto<String> response = adminHeadquarterService.deleteHeadquarter(hqCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}