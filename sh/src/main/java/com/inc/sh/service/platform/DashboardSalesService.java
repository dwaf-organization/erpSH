package com.inc.sh.service.platform;

import com.inc.sh.repository.DashboardSalesRepository;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.platform.reqDto.DashboardSalesReqDto;
import com.inc.sh.dto.platform.respDto.DashboardSalesRespDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardSalesService {
    
    private final DashboardSalesRepository dashboardSalesRepository;
    
    /**
     * 대시보드 매출 그래프 조회
     */
    public RespDto<DashboardSalesRespDto> getDashboardSales(DashboardSalesReqDto reqDto) {
        try {
            log.info("대시보드 매출 그래프 조회 시작 - 본사: {}, 브랜드: {}", 
                    reqDto.getHqCode(), reqDto.getBrandCode());
            
            // 현재 날짜 기준 월 계산
            LocalDate now = LocalDate.now();
            String currentMonth = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
            String month1Ago = now.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
            String month2Ago = now.minusMonths(2).format(DateTimeFormatter.ofPattern("yyyyMM"));
            String month11Ago = now.minusMonths(11).format(DateTimeFormatter.ofPattern("yyyyMM"));
            
            log.info("조회 기간 - 3개월: {} ~ {}, 12개월: {} ~ {}", 
                    month2Ago, currentMonth, month11Ago, currentMonth);
            
            // 1. 3개월 매출액 조회
            List<Object[]> sales3MonthResults = dashboardSalesRepository.find3MonthSales(
                    reqDto.getHqCode(), reqDto.getBrandCode(), 
                    currentMonth, month1Ago, month2Ago);
            
            // 2. 3개월 주문수 조회
            List<Object[]> order3MonthResults = dashboardSalesRepository.find3MonthOrders(
                    reqDto.getHqCode(), reqDto.getBrandCode(), 
                    currentMonth, month1Ago, month2Ago);
            
            // 3. 12개월 매출액 추이 조회
            List<Object[]> sales12MonthResults = dashboardSalesRepository.find12MonthSales(
                    reqDto.getHqCode(), reqDto.getBrandCode(), 
                    month11Ago, currentMonth);
            
            // 4. 12개월 주문수 추이 조회
            List<Object[]> order12MonthResults = dashboardSalesRepository.find12MonthOrders(
                    reqDto.getHqCode(), reqDto.getBrandCode(), 
                    month11Ago, currentMonth);
            
            // 응답 데이터 구성
            DashboardSalesRespDto response = DashboardSalesRespDto.builder()
                    .sales3Month(convertToMonthlyData(sales3MonthResults, Arrays.asList(month2Ago, month1Ago, currentMonth)))
                    .order3Month(convertToMonthlyData(order3MonthResults, Arrays.asList(month2Ago, month1Ago, currentMonth)))
                    .sales12Month(convertToMonthlyData(sales12MonthResults, generate12MonthList(now)))
                    .order12Month(convertToMonthlyData(order12MonthResults, generate12MonthList(now)))
                    .build();
            
            log.info("대시보드 매출 그래프 조회 완료 - 3개월: {}건, 12개월: {}건", 
                    sales3MonthResults.size(), sales12MonthResults.size());
            
            return RespDto.success("대시보드 매출 그래프 조회 완료", response);
            
        } catch (Exception e) {
            log.error("대시보드 매출 그래프 조회 중 오류 발생", e);
            return RespDto.fail("대시보드 매출 그래프 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 쿼리 결과를 MonthlyData 리스트로 변환 (과거월 먼저, 현재월 마지막)
     */
    private List<DashboardSalesRespDto.MonthlyData> convertToMonthlyData(List<Object[]> results, List<String> monthList) {
        
        // 쿼리 결과를 Map으로 변환 (월 → 값)
        Map<String, Long> resultMap = results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],  // 월
                        result -> ((Number) result[1]).longValue(),  // 값
                        (existing, replacement) -> existing
                ));
        
        // monthList 순서대로 데이터 생성 (없으면 0)
        List<DashboardSalesRespDto.MonthlyData> monthlyDataList = new ArrayList<>();
        
        for (String month : monthList) {
            Long value = resultMap.getOrDefault(month, 0L);
            String koreanMonth = convertToKoreanMonth(month);
            
            DashboardSalesRespDto.MonthlyData monthlyData = DashboardSalesRespDto.MonthlyData.builder()
                    .month(koreanMonth)
                    .value(value)
                    .build();
            
            monthlyDataList.add(monthlyData);
        }
        
        return monthlyDataList;
    }
    
    /**
     * 12개월 리스트 생성 (과거 11개월 + 현재월)
     */
    private List<String> generate12MonthList(LocalDate currentDate) {
        List<String> monthList = new ArrayList<>();
        
        // 11개월 전부터 현재월까지
        for (int i = 11; i >= 0; i--) {
            String month = currentDate.minusMonths(i).format(DateTimeFormatter.ofPattern("yyyyMM"));
            monthList.add(month);
        }
        
        return monthList;
    }
    
    /**
     * YYYYMM 형식을 한글 형식으로 변환
     * "202601" → "2026년 1월"
     */
    private String convertToKoreanMonth(String yyyyMM) {
        try {
            String year = yyyyMM.substring(0, 4);
            String month = yyyyMM.substring(4, 6);
            
            // 앞의 0 제거
            int monthInt = Integer.parseInt(month);
            
            return year + "년 " + monthInt + "월";
        } catch (Exception e) {
            log.warn("월 형식 변환 오류 - 입력값: {}", yyyyMM, e);
            return yyyyMM; // 변환 실패시 원본 반환
        }
    }
}
