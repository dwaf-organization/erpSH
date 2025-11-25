package com.inc.sh.controller;

import com.inc.sh.dto.deliveryHoliday.reqDto.DeliveryHolidaySearchDto;
import com.inc.sh.dto.deliveryHoliday.reqDto.BasicHolidayReqDto;
import com.inc.sh.dto.deliveryHoliday.reqDto.RegularHolidayReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.deliveryHoliday.respDto.DeliveryHolidayRespDto;
import com.inc.sh.dto.deliveryHoliday.respDto.DeliveryHolidaySaveRespDto;
import com.inc.sh.dto.deliveryHoliday.respDto.DeliveryHolidayDeleteRespDto;
import com.inc.sh.service.DeliveryHolidayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/delivery-holiday")
@RequiredArgsConstructor
@Slf4j
public class DeliveryHolidayController {

    private final DeliveryHolidayService deliveryHolidayService;

    /**
     * 배송휴일 목록 조회
     * GET /api/v1/erp/delivery-holiday/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<DeliveryHolidayRespDto>>> getDeliveryHolidayList(
            @RequestParam(value = "brandCode", required = false) Integer brandCode) {
        
        log.info("배송휴일 목록 조회 요청 - brandCode: {}", brandCode);
        
        DeliveryHolidaySearchDto searchDto = DeliveryHolidaySearchDto.builder()
                .brandCode(brandCode)
                .build();
        
        RespDto<List<DeliveryHolidayRespDto>> response = deliveryHolidayService.getDeliveryHolidayList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 기본휴일 등록
     * POST /api/v1/erp/delivery-holiday/basic
     */
    @PostMapping("/basic")
    public ResponseEntity<RespDto<DeliveryHolidaySaveRespDto>> saveBasicHoliday(
            @Valid @RequestBody BasicHolidayReqDto request) {
        
        log.info("기본휴일 등록 요청 - brandCode: {}, holidayDt: {}, holidayName: {}", 
                request.getBrandCode(), request.getHolidayDt(), request.getHolidayName());
        
        RespDto<DeliveryHolidaySaveRespDto> response = deliveryHolidayService.saveBasicHoliday(request);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 정기휴일 등록
     * POST /api/v1/erp/delivery-holiday/regular
     */
    @PostMapping("/regular")
    public ResponseEntity<RespDto<DeliveryHolidaySaveRespDto>> saveRegularHoliday(
            @Valid @RequestBody RegularHolidayReqDto request) {
        
        log.info("정기휴일 등록 요청 - brandCode: {}, holidayDt: {}, holidayName: {}", 
                request.getBrandCode(), request.getHolidayDt(), request.getHolidayName());
        
        RespDto<DeliveryHolidaySaveRespDto> response = deliveryHolidayService.saveRegularHoliday(request);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 배송휴일 삭제 (하드 삭제)
     * DELETE /api/v1/erp/delivery-holiday/{deliveryHolidayCode}
     */
    @DeleteMapping("/{deliveryHolidayCode}")
    public ResponseEntity<RespDto<DeliveryHolidayDeleteRespDto>> deleteDeliveryHoliday(
            @PathVariable("deliveryHolidayCode") Integer deliveryHolidayCode) {
        
        log.info("배송휴일 삭제 요청 - deliveryHolidayCode: {}", deliveryHolidayCode);
        
        RespDto<DeliveryHolidayDeleteRespDto> response = deliveryHolidayService.deleteDeliveryHoliday(deliveryHolidayCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}