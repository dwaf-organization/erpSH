package com.inc.sh.controller;

import com.inc.sh.dto.distCenter.reqDto.DistCenterSearchDto;
import com.inc.sh.dto.distCenter.reqDto.DistCenterDeleteReqDto;
import com.inc.sh.dto.distCenter.reqDto.DistCenterReqDto;
import com.inc.sh.dto.distCenter.reqDto.DistCenterSaveReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.distCenter.respDto.DistCenterRespDto;
import com.inc.sh.dto.distCenter.respDto.DistCenterSaveRespDto;
import com.inc.sh.dto.distCenter.respDto.DistCenterBatchResult;
import com.inc.sh.dto.distCenter.respDto.DistCenterDeleteRespDto;
import com.inc.sh.service.DistCenterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
            @RequestParam(value = "useYn", required = false) Integer useYn,
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("물류센터 목록 조회 요청 - distCenterCode: {}, useYn: {}, hqCode: {}", distCenterCode, useYn, hqCode);
        
        DistCenterSearchDto searchDto = DistCenterSearchDto.builder()
                .distCenterCode(distCenterCode)
                .useYn(useYn)
                .hqCode(hqCode)
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
     * 물류센터 다중 저장 (신규/수정) - 본사코드 검증 포함
     * POST /api/v1/erp/dist-center/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<DistCenterBatchResult>> saveDistCenters(@RequestBody DistCenterSaveReqDto request) {
        
        log.info("물류센터 다중 저장 요청 - 총 {}건", 
                request.getDistCenters() != null ? request.getDistCenters().size() : 0);
        
        // 요청 데이터 검증
        if (request.getDistCenters() == null || request.getDistCenters().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("저장할 물류센터 데이터가 없습니다."));
        }
        
        // 개별 항목 필수 필드 검증
        for (DistCenterSaveReqDto.DistCenterItemDto distCenter : request.getDistCenters()) {
            if (distCenter.getHqCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("본사코드는 필수입니다."));
            }
            if (distCenter.getDistCenterName() == null || distCenter.getDistCenterName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("물류센터명은 필수입니다."));
            }
            // useYn 기본값 설정 (null인 경우)
            if (distCenter.getUseYn() == null) {
                distCenter.setUseYn(1);  // 기본값: 사용
            }
        }
        
        RespDto<DistCenterBatchResult> response = distCenterService.saveDistCenters(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 물류센터 다중 삭제 (창고 연결 확인 포함)
     * DELETE /api/v1/erp/dist-center/delete
     */
    @DeleteMapping("/delete")
    public ResponseEntity<RespDto<DistCenterBatchResult>> deleteDistCenters(@RequestBody DistCenterDeleteReqDto request) {
        
        log.info("물류센터 다중 삭제 요청 - 총 {}건", 
                request.getDistCenterCodes() != null ? request.getDistCenterCodes().size() : 0);
        
        // 요청 데이터 검증
        if (request.getDistCenterCodes() == null || request.getDistCenterCodes().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("삭제할 물류센터 코드가 없습니다."));
        }
        
        // 중복 제거
        List<Integer> uniqueCodes = request.getDistCenterCodes().stream()
                .distinct()
                .collect(Collectors.toList());
        
        if (uniqueCodes.size() != request.getDistCenterCodes().size()) {
            log.info("중복된 물류센터 코드 제거됨 - 원본: {}건, 제거 후: {}건", 
                    request.getDistCenterCodes().size(), uniqueCodes.size());
            request.setDistCenterCodes(uniqueCodes);
        }
        
        RespDto<DistCenterBatchResult> response = distCenterService.deleteDistCenters(request);
        return ResponseEntity.ok(response);
    }
}