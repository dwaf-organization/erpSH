package com.inc.sh.controller;

import com.inc.sh.dto.vehicle.reqDto.VehicleSearchDto;
import com.inc.sh.dto.vehicle.reqDto.VehicleDeleteReqDto;
import com.inc.sh.dto.vehicle.reqDto.VehicleReqDto;
import com.inc.sh.dto.vehicle.reqDto.VehicleSaveReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.vehicle.respDto.VehicleRespDto;
import com.inc.sh.dto.vehicle.respDto.VehicleSaveRespDto;
import com.inc.sh.dto.vehicle.respDto.VehicleBatchResult;
import com.inc.sh.dto.vehicle.respDto.VehicleDeleteRespDto;
import com.inc.sh.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/erp/vehicle")
@RequiredArgsConstructor
@Slf4j
public class VehicleController {

    private final VehicleService vehicleService;

    /**
     * 차량 목록 조회
     * GET /api/v1/erp/vehicle/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<VehicleRespDto>>> getVehicleList(
            @RequestParam(value = "vehicleCode", required = false) Integer vehicleCode,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("차량 목록 조회 요청 - vehicleCode: {}, category: {}, hqCode: {}", vehicleCode, category, hqCode);
        
        VehicleSearchDto searchDto = VehicleSearchDto.builder()
                .vehicleCode(vehicleCode)
                .category(category)
                .hqCode(hqCode)
                .build();
        
        RespDto<List<VehicleRespDto>> response = vehicleService.getVehicleList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 차량 상세 조회
     * GET /api/v1/erp/vehicle/detail/{vehicleCode}
     */
    @GetMapping("/detail/{vehicleCode}")
    public ResponseEntity<RespDto<VehicleRespDto>> getVehicle(
            @PathVariable("vehicleCode") Integer vehicleCode) {
        
        log.info("차량 상세 조회 요청 - vehicleCode: {}", vehicleCode);
        
        RespDto<VehicleRespDto> response = vehicleService.getVehicle(vehicleCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }


    /**
     * 차량 다중 저장 (신규/수정) - 유효성 검증 추가
     * POST /api/v1/erp/vehicle/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<VehicleBatchResult>> saveVehicles(@RequestBody VehicleSaveReqDto request) {
        
        log.info("차량 다중 저장 요청 - 총 {}건", 
                request.getVehicles() != null ? request.getVehicles().size() : 0);
        
        // 요청 데이터 검증
        if (request.getVehicles() == null || request.getVehicles().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("저장할 차량 데이터가 없습니다."));
        }
        
        // 개별 항목 필수 필드 검증
        for (VehicleSaveReqDto.VehicleItemDto vehicle : request.getVehicles()) {
            if (vehicle.getHqCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("본사코드는 필수입니다."));
            }
            if (vehicle.getVehicleName() == null || vehicle.getVehicleName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("차량명은 필수입니다."));
            }
            if (vehicle.getCategory() == null || vehicle.getCategory().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("구분은 필수입니다."));
            }
        }
        
        RespDto<VehicleBatchResult> response = vehicleService.saveVehicles(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 차량 다중 삭제 (배송중인 주문 확인 포함)
     * DELETE /api/v1/erp/vehicle/delete
     */
    @DeleteMapping("/delete")
    public ResponseEntity<RespDto<VehicleBatchResult>> deleteVehicles(@RequestBody VehicleDeleteReqDto request) {
        
        log.info("차량 다중 삭제 요청 - 총 {}건", 
                request.getVehicleCodes() != null ? request.getVehicleCodes().size() : 0);
        
        // 요청 데이터 검증
        if (request.getVehicleCodes() == null || request.getVehicleCodes().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("삭제할 차량 코드가 없습니다."));
        }
        
        // 중복 제거
        List<Integer> uniqueCodes = request.getVehicleCodes().stream()
                .distinct()
                .collect(Collectors.toList());
        
        if (uniqueCodes.size() != request.getVehicleCodes().size()) {
            log.info("중복된 차량 코드 제거됨 - 원본: {}건, 제거 후: {}건", 
                    request.getVehicleCodes().size(), uniqueCodes.size());
            request.setVehicleCodes(uniqueCodes);
        }
        
        RespDto<VehicleBatchResult> response = vehicleService.deleteVehicles(request);
        return ResponseEntity.ok(response);
    }

}