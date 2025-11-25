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
	public ResponseEntity<RespDto<OrderConfigRespDto>> getOrderConfig( // 메서드명 변경 (관례)
    		@RequestParam("hq_code") Integer hqCode) {
        
        // Service에서 이미 RespDto<OrderConfigRespDto> 형태로 변환하여 반환하므로 그대로 사용
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
        
        // 1. 서비스 호출 및 업데이트된 DTO 반환
        RespDto<OrderConfigRespDto> updatedDto = settingService.updateOrderConfig(reqDto);

        // 2. RespDto 포맷에 성공 메시지를 담아 반환
        // HTTP 200 OK와 함께 RespDto.success 응답을 보냅니다.
        return ResponseEntity.ok(updatedDto);
    }
    
    /**
     * 주문 제한 설정 목록 조회
     * GET /api/v1/erp/setting/order-limit/list
     */
    @GetMapping("/order-limit/list")
    public ResponseEntity<RespDto<List<OrderLimitRespDto>>> getOrderLimitList(
    		@RequestParam("brand_code") Integer brandCode) {
        
        // 서비스 호출. 서비스에서 RespDto<List<OrderLimitRespDto>>를 반환합니다.
        RespDto<List<OrderLimitRespDto>> response = settingService.getOrderLimitListByBrandCode(brandCode);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 주문 제한 설정 등록 및 수정
     * POST /api/v1/erp/setting/order-limit/save
     */
    @PostMapping("/order-limit/save")
    public ResponseEntity<RespDto<OrderLimitRespDto>> saveOrderLimit(
            @RequestBody OrderLimitSaveReqDto reqDto) {
        
        // 1. 서비스 호출 (등록 또는 수정)
        OrderLimitRespDto resultDto = settingService.saveOrUpdateOrderLimit(reqDto);

        // 2. RespDto 포맷에 성공 메시지를 담아 반환
        return ResponseEntity.ok(RespDto.success("주문 제한 설정 저장 완료", resultDto));
    }
    
    /**
     * 주문 제한 설정 삭제
     * DELETE /api/v1/erp/setting/order-limit/{limit_code}
     */
    @DeleteMapping("/order-limit/{limit_code}")
    public ResponseEntity<RespDto<Void>> deleteOrderLimit(
            @PathVariable("limit_code") Integer limitCode) {
        
        // 1. 서비스 호출 및 삭제 실행. 서비스에서 이미 성공/실패 RespDto를 처리함.
        RespDto<Void> response = settingService.deleteOrderLimit(limitCode);

        // 2. 서비스에서 반환된 RespDto를 그대로 ResponseEntity에 담아 반환
        return ResponseEntity.ok(response);
    }
}
