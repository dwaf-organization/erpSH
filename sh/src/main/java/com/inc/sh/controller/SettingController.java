package com.inc.sh.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.headquarter.reqDto.HeadquarterReqDto;
import com.inc.sh.dto.headquarter.reqDto.OrderConfigUpdateReqDto;
import com.inc.sh.dto.headquarter.respDto.HeadquarterRespDto;
import com.inc.sh.dto.headquarter.respDto.OrderConfigRespDto;
import com.inc.sh.dto.orderLimitSet.reqDto.OrderLimitSaveReqDto;
import com.inc.sh.dto.orderLimitSet.respDto.OrderLimitRespDto;
import com.inc.sh.service.HeadquarterService;
import com.inc.sh.service.SettingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/erp/setting")
@RequiredArgsConstructor
public class SettingController {

	private final SettingService settingService;
	
    /**
     * 주문관리 조회
     * GET /api/v1/erp/setting/order-config
     */
	@GetMapping("/order-config")
	public ResponseEntity<RespDto<OrderConfigRespDto>> getOrderConfig(
    		@RequestParam("hq_code") Integer hqCode) {
        
        RespDto<OrderConfigRespDto> response = settingService.getOrderConfigByHqCode(hqCode);
        return ResponseEntity.ok(response);
    }
	
	/**
     * 주문관리 설정 업데이트
     * POST /api/v1/erp/setting/order-config/update
     */
    @PostMapping("/order-config/update")
    public ResponseEntity<RespDto<OrderConfigRespDto>> updateOrderConfig(
            @RequestBody OrderConfigUpdateReqDto reqDto) {
        
        RespDto<OrderConfigRespDto> updatedDto = settingService.updateOrderConfig(reqDto);
        return ResponseEntity.ok(updatedDto);
    }
    
    /**
     * 주문 제한 설정 목록 조회
     * GET /api/v1/erp/setting/order-limit/list
     */
    @GetMapping("/order-limit/list")
    public ResponseEntity<RespDto<List<OrderLimitRespDto>>> getOrderLimitList(
    		@RequestParam("brand_code") Integer brandCode) {
        
        RespDto<List<OrderLimitRespDto>> response = settingService.getOrderLimitListByBrandCode(brandCode);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 주문 제한 설정 등록 및 수정 (다중 저장)
     * POST /api/v1/erp/setting/order-limit/save
     */
    @PostMapping("/order-limit/save")
    public ResponseEntity<RespDto<List<OrderLimitRespDto>>> saveOrderLimit(
            @RequestBody OrderLimitSaveReqDto reqDto) {
        
        // 다중 저장 서비스 호출
        RespDto<List<OrderLimitRespDto>> response = settingService.saveOrUpdateOrderLimitMultiple(reqDto);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 주문 제한 설정 삭제
     * DELETE /api/v1/erp/setting/order-limit/{limit_code}
     */
    @DeleteMapping("/order-limit/{limit_code}")
    public ResponseEntity<RespDto<Void>> deleteOrderLimit(
            @PathVariable("limit_code") Integer limitCode) {
        
        RespDto<Void> response = settingService.deleteOrderLimit(limitCode);
        return ResponseEntity.ok(response);
    }
}