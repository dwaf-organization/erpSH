package com.inc.sh.controller.platform;

import com.inc.sh.dto.platform.reqDto.DailySalesReqDto;
import com.inc.sh.dto.platform.reqDto.MonthlySalesReqDto;
import com.inc.sh.dto.platform.respDto.DailySalesRespDto;
import com.inc.sh.dto.platform.respDto.MonthlySalesRespDto;
import com.inc.sh.service.platform.SalesService;
import com.inc.sh.common.dto.RespDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/platform/sales")
@RequiredArgsConstructor
@Slf4j
public class SalesController {
    
    private final SalesService salesService;
    
    /**
     * 일별매출 조회
     * GET /api/v1/platform/sales/daily?hqCode=HQ002&brandCode=2&searchDate=202501
     */
    @GetMapping("/daily")
    public RespDto<DailySalesRespDto> getDailySales(
            @RequestParam("hqCode") Integer hqCode,
            @RequestParam("brandCode") Integer brandCode,
            @RequestParam("searchDate") String searchDate) {
        
        try {
            log.info("일별매출 조회 API 호출 - 본사: {}, 브랜드: {}, 조회월: {}", 
                    hqCode, brandCode, searchDate);
            
            // 파라미터 검증
            if (searchDate == null || searchDate.length() != 6) {
                return RespDto.fail("조회월은 YYYYMM 형식이어야 합니다. (예: 202501)");
            }
            
            DailySalesReqDto reqDto = DailySalesReqDto.builder()
                    .hqCode(hqCode)
                    .brandCode(brandCode)
                    .searchDate(searchDate)
                    .build();
            
            return salesService.getDailySales(reqDto);
            
        } catch (Exception e) {
            log.error("일별매출 조회 API 오류", e);
            return RespDto.fail("일별매출 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 월간매출 조회
     * GET /api/v1/platform/sales/monthly?hqCode=HQ002&brandCode=2&startDate=202401&endDate=202412
     */
    @GetMapping("/monthly")
    public RespDto<MonthlySalesRespDto> getMonthlySales(
            @RequestParam("hqCode") Integer hqCode,
            @RequestParam("brandCode") Integer brandCode,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        
        try {
            log.info("월간매출 조회 API 호출 - 본사: {}, 브랜드: {}, 기간: {} ~ {}", 
                    hqCode, brandCode, startDate, endDate);
            
            // 파라미터 검증
            if (startDate == null || startDate.length() != 6) {
                return RespDto.fail("시작월은 YYYYMM 형식이어야 합니다. (예: 202401)");
            }
            
            if (endDate == null || endDate.length() != 6) {
                return RespDto.fail("종료월은 YYYYMM 형식이어야 합니다. (예: 202412)");
            }
            
            if (startDate.compareTo(endDate) > 0) {
                return RespDto.fail("시작월은 종료월보다 이전이어야 합니다.");
            }
            
            MonthlySalesReqDto reqDto = MonthlySalesReqDto.builder()
                    .hqCode(hqCode)
                    .brandCode(brandCode)
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();
            
            return salesService.getMonthlySales(reqDto);
            
        } catch (Exception e) {
            log.error("월간매출 조회 API 오류", e);
            return RespDto.fail("월간매출 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}