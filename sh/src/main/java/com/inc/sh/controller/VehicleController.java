package com.inc.sh.controller;

import com.inc.sh.dto.vehicle.reqDto.VehicleSearchDto;
import com.inc.sh.dto.vehicle.reqDto.VehicleReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.vehicle.respDto.VehicleRespDto;
import com.inc.sh.dto.vehicle.respDto.VehicleSaveRespDto;
import com.inc.sh.dto.vehicle.respDto.VehicleDeleteRespDto;
import com.inc.sh.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @RequestParam(value = "category", required = false) String category) {
        
        log.info("차량 목록 조회 요청 - vehicleCode: {}, category: {}", vehicleCode, category);
        
        VehicleSearchDto searchDto = VehicleSearchDto.builder()
                .vehicleCode(vehicleCode)
                .category(category)
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
     * 차량 저장 (신규/수정)
     * POST /api/v1/erp/vehicle/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<VehicleSaveRespDto>> saveVehicle(
            @Valid @RequestBody VehicleReqDto request) {
        
        if (request.getVehicleCode() == null) {
            log.info("차량 신규 등록 요청 - vehicleName: {}, category: {}", 
                    request.getVehicleName(), request.getCategory());
        } else {
            log.info("차량 수정 요청 - vehicleCode: {}, vehicleName: {}", 
                    request.getVehicleCode(), request.getVehicleName());
        }
        
        RespDto<VehicleSaveRespDto> response = vehicleService.saveVehicle(request);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 차량 삭제 (하드 삭제)
     * DELETE /api/v1/erp/vehicle/{vehicleCode}
     */
    @DeleteMapping("/{vehicleCode}")
    public ResponseEntity<RespDto<VehicleDeleteRespDto>> deleteVehicle(
            @PathVariable("vehicleCode") Integer vehicleCode) {
        
        log.info("차량 삭제 요청 - vehicleCode: {}", vehicleCode);
        
        RespDto<VehicleDeleteRespDto> response = vehicleService.deleteVehicle(vehicleCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}