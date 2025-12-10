package com.inc.sh.controller;

import com.inc.sh.dto.warehouse.reqDto.WarehouseSearchDto;
import com.inc.sh.dto.warehouse.reqDto.WarehouseDeleteReqDto;
import com.inc.sh.dto.warehouse.reqDto.WarehouseReqDto;
import com.inc.sh.dto.warehouse.reqDto.WarehouseSaveReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.warehouse.respDto.WarehouseListRespDto;
import com.inc.sh.dto.warehouse.respDto.WarehouseSaveRespDto;
import com.inc.sh.dto.warehouse.respDto.WarehouseBatchResult;
import com.inc.sh.dto.warehouse.respDto.WarehouseDeleteRespDto;
import com.inc.sh.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/erp/warehouse")
@RequiredArgsConstructor
@Slf4j
public class WarehouseController {

    private final WarehouseService warehouseService;

    /**
     * 창고 목록 조회
     * GET /api/v1/erp/warehouse/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<WarehouseListRespDto>>> getWarehouseList(
            @RequestParam(value = "warehouseCode", required = false) Integer warehouseCode,
            @RequestParam(value = "distCenterCode", required = false) Integer distCenterCode,
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("창고 목록 조회 요청 - warehouseCode: {}, distCenterCode: {}, hqCode: {}", warehouseCode, distCenterCode, hqCode);
        
        WarehouseSearchDto searchDto = WarehouseSearchDto.builder()
                .warehouseCode(warehouseCode)
                .distCenterCode(distCenterCode)
                .hqCode(hqCode)
                .build();
        
        RespDto<List<WarehouseListRespDto>> response = warehouseService.getWarehouseList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 창고 다중 저장 (신규/수정) - 유효성 검증 추가
     * POST /api/v1/erp/warehouse/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<WarehouseBatchResult>> saveWarehouses(@RequestBody WarehouseSaveReqDto request) {
        
        log.info("창고 다중 저장 요청 - 총 {}건", 
                request.getWarehouses() != null ? request.getWarehouses().size() : 0);
        
        // 요청 데이터 검증
        if (request.getWarehouses() == null || request.getWarehouses().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("저장할 창고 데이터가 없습니다."));
        }
        
        // 개별 항목 필수 필드 검증
        for (WarehouseSaveReqDto.WarehouseItemDto warehouse : request.getWarehouses()) {
            if (warehouse.getHqCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("본사코드는 필수입니다."));
            }
            if (warehouse.getDistCenterCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("물류센터코드는 필수입니다."));
            }
            if (warehouse.getWarehouseName() == null || warehouse.getWarehouseName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("창고명은 필수입니다."));
            }
            // useYn 기본값 설정 (null인 경우)
            if (warehouse.getUseYn() == null) {
                warehouse.setUseYn(1);  // 기본값: 사용
            }
        }
        
        RespDto<WarehouseBatchResult> response = warehouseService.saveWarehouses(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 창고 다중 삭제 (재고 확인 포함)
     * DELETE /api/v1/erp/warehouse/delete
     */
    @DeleteMapping("/delete")
    public ResponseEntity<RespDto<WarehouseBatchResult>> deleteWarehouses(@RequestBody WarehouseDeleteReqDto request) {
        
        log.info("창고 다중 삭제 요청 - 총 {}건", 
                request.getWarehouseCodes() != null ? request.getWarehouseCodes().size() : 0);
        
        // 요청 데이터 검증
        if (request.getWarehouseCodes() == null || request.getWarehouseCodes().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("삭제할 창고 코드가 없습니다."));
        }
        
        // 중복 제거
        List<Integer> uniqueCodes = request.getWarehouseCodes().stream()
                .distinct()
                .collect(Collectors.toList());
        
        if (uniqueCodes.size() != request.getWarehouseCodes().size()) {
            log.info("중복된 창고 코드 제거됨 - 원본: {}건, 제거 후: {}건", 
                    request.getWarehouseCodes().size(), uniqueCodes.size());
            request.setWarehouseCodes(uniqueCodes);
        }
        
        RespDto<WarehouseBatchResult> response = warehouseService.deleteWarehouses(request);
        return ResponseEntity.ok(response);
    }
}