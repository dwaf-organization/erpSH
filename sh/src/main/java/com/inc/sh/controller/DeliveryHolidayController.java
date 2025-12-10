package com.inc.sh.controller;

import com.inc.sh.dto.deliveryHoliday.reqDto.DeliveryHolidaySearchDto;
import com.inc.sh.dto.deliveryHoliday.reqDto.BasicHolidayReqDto;
import com.inc.sh.dto.deliveryHoliday.reqDto.DeliveryHolidayDeleteReqDto;
import com.inc.sh.dto.deliveryHoliday.reqDto.RegularHolidayReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.deliveryHoliday.respDto.DeliveryHolidayRespDto;
import com.inc.sh.dto.deliveryHoliday.respDto.DeliveryHolidaySaveRespDto;
import com.inc.sh.dto.deliveryHoliday.respDto.DeliveryHolidayBatchResult;
import com.inc.sh.dto.deliveryHoliday.respDto.DeliveryHolidayDeleteRespDto;
import com.inc.sh.service.DeliveryHolidayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
            @RequestParam(value = "brandCode", required = false) Integer brandCode,
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("배송휴일 목록 조회 요청 - brandCode: {}, hqCode: {}", brandCode, hqCode);
        
        DeliveryHolidaySearchDto searchDto = DeliveryHolidaySearchDto.builder()
                .brandCode(brandCode)
                .hqCode(hqCode)
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
     * 배송휴일 다중 삭제 (Hard Delete)
     * DELETE /api/v1/erp/delivery-holiday/delete
     */
    @DeleteMapping("/delete")
    public ResponseEntity<RespDto<DeliveryHolidayBatchResult>> deleteDeliveryHolidays(@RequestBody DeliveryHolidayDeleteReqDto request) {
        
        log.info("배송휴일 다중 삭제 요청 - 총 {}건", 
                request.getDeliveryHolidayCodes() != null ? request.getDeliveryHolidayCodes().size() : 0);
        
        // 요청 데이터 검증
        if (request.getDeliveryHolidayCodes() == null || request.getDeliveryHolidayCodes().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("삭제할 배송휴일 코드가 없습니다."));
        }
        
        // 중복 제거
        List<Integer> uniqueCodes = request.getDeliveryHolidayCodes().stream()
                .distinct()
                .collect(Collectors.toList());
        
        if (uniqueCodes.size() != request.getDeliveryHolidayCodes().size()) {
            log.info("중복된 배송휴일 코드 제거됨 - 원본: {}건, 제거 후: {}건", 
                    request.getDeliveryHolidayCodes().size(), uniqueCodes.size());
            request.setDeliveryHolidayCodes(uniqueCodes);
        }
        
        RespDto<DeliveryHolidayBatchResult> response = deliveryHolidayService.deleteDeliveryHolidays(request);
        return ResponseEntity.ok(response);
    }
}