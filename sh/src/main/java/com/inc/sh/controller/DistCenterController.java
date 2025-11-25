package com.inc.sh.controller;

import com.inc.sh.dto.distCenter.reqDto.DistCenterSearchDto;
import com.inc.sh.dto.distCenter.reqDto.DistCenterReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.distCenter.respDto.DistCenterRespDto;
import com.inc.sh.dto.distCenter.respDto.DistCenterSaveRespDto;
import com.inc.sh.dto.distCenter.respDto.DistCenterDeleteRespDto;
import com.inc.sh.service.DistCenterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/dist-center")
@RequiredArgsConstructor
@Slf4j
public class DistCenterController {

    private final DistCenterService distCenterService;

    /**
     * 물류센터 목록 조회
     * GET /api/v1/erp/dist-center/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<DistCenterRespDto>>> getDistCenterList(
            @RequestParam(value = "distCenterCode", required = false) Integer distCenterCode,
            @RequestParam(value = "useYn", required = false) Integer useYn) {
        
        log.info("물류센터 목록 조회 요청 - distCenterCode: {}, useYn: {}", distCenterCode, useYn);
        
        DistCenterSearchDto searchDto = DistCenterSearchDto.builder()
                .distCenterCode(distCenterCode)
                .useYn(useYn)
                .build();
        
        RespDto<List<DistCenterRespDto>> response = distCenterService.getDistCenterList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 물류센터 상세 조회
     * GET /api/v1/erp/dist-center/detail/{distCenterCode}
     */
    @GetMapping("/detail/{distCenterCode}")
    public ResponseEntity<RespDto<DistCenterRespDto>> getDistCenter(
            @PathVariable("distCenterCode") Integer distCenterCode) {
        
        log.info("물류센터 상세 조회 요청 - distCenterCode: {}", distCenterCode);
        
        RespDto<DistCenterRespDto> response = distCenterService.getDistCenter(distCenterCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 물류센터 저장 (신규/수정)
     * POST /api/v1/erp/dist-center/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<DistCenterSaveRespDto>> saveDistCenter(
            @Valid @RequestBody DistCenterReqDto request) {
        
        if (request.getDistCenterCode() == null) {
            log.info("물류센터 신규 등록 요청 - distCenterName: {}", request.getDistCenterName());
        } else {
            log.info("물류센터 수정 요청 - distCenterCode: {}, distCenterName: {}", 
                    request.getDistCenterCode(), request.getDistCenterName());
        }
        
        RespDto<DistCenterSaveRespDto> response = distCenterService.saveDistCenter(request);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 물류센터 삭제 (하드 삭제)
     * DELETE /api/v1/erp/dist-center/{distCenterCode}
     */
    @DeleteMapping("/{distCenterCode}")
    public ResponseEntity<RespDto<DistCenterDeleteRespDto>> deleteDistCenter(
            @PathVariable("distCenterCode") Integer distCenterCode) {
        
        log.info("물류센터 삭제 요청 - distCenterCode: {}", distCenterCode);
        
        RespDto<DistCenterDeleteRespDto> response = distCenterService.deleteDistCenter(distCenterCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}