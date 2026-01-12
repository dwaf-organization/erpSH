package com.inc.sh.service.platform;

import com.inc.sh.dto.platform.reqDto.DailySalesReqDto;
import com.inc.sh.dto.platform.reqDto.MonthlySalesReqDto;
import com.inc.sh.dto.platform.respDto.DailySalesRespDto;
import com.inc.sh.dto.platform.respDto.MonthlySalesRespDto;
import com.inc.sh.repository.SalesRepository;
import com.inc.sh.common.dto.RespDto;

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
public class SalesService {
    
    private final SalesRepository salesRepository;
    
    /**
     * 일별매출 조회
     */
    public RespDto<DailySalesRespDto> getDailySales(DailySalesReqDto reqDto) {
        try {
            log.info("일별매출 조회 시작 - 본사: {}, 브랜드: {}, 조회월: {}", 
                    reqDto.getHqCode(), reqDto.getBrandCode(), reqDto.getSearchDate());
            
            // 날짜 범위 계산
            String startDate = reqDto.getSearchDate() + "01"; // 202501 -> 20250101
            String endDate = calculateMonthEndDate(reqDto.getSearchDate()); // 202501 -> 20250131
            
            log.info("조회 기간: {} ~ {}", startDate, endDate);
            
            // 월의 총 일수 계산
            int totalDays = Integer.parseInt(endDate.substring(6, 8));
            
            // 일별매출 데이터 조회
            List<Object[]> dailyResults = salesRepository.findDailySalesByMonth(
                    reqDto.getHqCode(), 
                    reqDto.getBrandCode(), 
                    startDate, 
                    endDate
            );
            
            // 매장별 월 총매출 조회
            List<Object[]> monthlyTotals = salesRepository.findMonthlyTotalByStore(
                    reqDto.getHqCode(), 
                    reqDto.getBrandCode(), 
                    startDate, 
                    endDate
            );
            
            // 응답 데이터 구성
            Map<String, DailySalesRespDto.StoreDailySales> customerMap = new HashMap<>();
            
            // 거래처별 월 총매출 설정
            for (Object[] total : monthlyTotals) {
                Integer customerCode = ((Number) total[0]).intValue();
                String customerName = (String) total[1];
                Integer monthlyTotal = ((Number) total[2]).intValue();
                
                String customerKey = String.valueOf(customerCode);
                
                // 일별매출 배열 초기화 (1일~31일, 모두 0으로)
                Integer[] dailySalesArray = new Integer[totalDays];
                Arrays.fill(dailySalesArray, 0);
                
                DailySalesRespDto.StoreDailySales customerSales = DailySalesRespDto.StoreDailySales.builder()
                        .customerCode(customerCode)
                        .customerName(customerName)
                        .monthlyTotal(monthlyTotal)
                        .dailySales(Arrays.asList(dailySalesArray))
                        .build();
                
                customerMap.put(customerKey, customerSales);
            }
            
            // 일별매출 데이터 설정
            for (Object[] result : dailyResults) {
                Integer customerCode = ((Number) result[0]).intValue();
                String dayStr = (String) result[2];
                Integer dailySales = ((Number) result[3]).intValue();
                
                if (dayStr != null) {
                    String customerKey = String.valueOf(customerCode);
                    int day = Integer.parseInt(dayStr) - 1; // 배열 인덱스는 0부터
                    
                    DailySalesRespDto.StoreDailySales customerSales = customerMap.get(customerKey);
                    if (customerSales != null && day >= 0 && day < totalDays) {
                        // 기존 값과 더해서 처리 (여러 매장 합계)
                        Integer currentSales = customerSales.getDailySales().get(day);
                        customerSales.getDailySales().set(day, currentSales + dailySales);
                    }
                }
            }
            
            // 응답 데이터 구성
            DailySalesRespDto response = DailySalesRespDto.builder()
                    .searchMonth(reqDto.getSearchDate())
                    .totalDays(totalDays)
                    .stores(new ArrayList<>(customerMap.values()))
                    .build();
            
            log.info("일별매출 조회 완료 - 거래처수: {}, 조회일수: {}", customerMap.size(), totalDays);
            
            return RespDto.success("일별매출 조회 완료", response);
            
        } catch (Exception e) {
            log.error("일별매출 조회 중 오류 발생", e);
            return RespDto.fail("일별매출 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 월간매출 조회
     */
    public RespDto<MonthlySalesRespDto> getMonthlySales(MonthlySalesReqDto reqDto) {
        try {
            log.info("월간매출 조회 시작 - 본사: {}, 브랜드: {}, 기간: {} ~ {}", 
                    reqDto.getHqCode(), reqDto.getBrandCode(), reqDto.getStartDate(), reqDto.getEndDate());
            
            // 월간매출 데이터 조회
            List<Object[]> monthlyResults = salesRepository.findMonthlySalesByStore(
                    reqDto.getHqCode(), 
                    reqDto.getBrandCode(), 
                    reqDto.getStartDate(), 
                    reqDto.getEndDate()
            );
            
            // 응답 데이터 구성
            List<MonthlySalesRespDto.StoreMonthlySales> storeList = new ArrayList<>();
            int totalSales = 0;
            int totalOrderCount = 0;
            Set<String> uniqueCustomers = new HashSet<>();
            
            for (Object[] result : monthlyResults) {
                Integer customerCode = ((Number) result[0]).intValue();
                String customerName = (String) result[1];
                String month = (String) result[2];
                Integer sales = ((Number) result[3]).intValue();
                Integer orderCount = ((Number) result[4]).intValue();
                
                // 평균매출 계산 (총매출 ÷ 주문수)
                Integer avgSales = orderCount > 0 ? sales / orderCount : 0;
                
                MonthlySalesRespDto.StoreMonthlySales customerSales = MonthlySalesRespDto.StoreMonthlySales.builder()
                        .customerCode(customerCode)
                        .customerName(customerName)
                        .month(month)
                        .totalSales(sales)
                        .orderCount(orderCount)
                        .avgSales(avgSales)
                        .build();
                
                storeList.add(customerSales);
                
                // 전체 합계 계산
                totalSales += sales;
                totalOrderCount += orderCount;
                uniqueCustomers.add(String.valueOf(customerCode));
            }
            
            // 거래처평균매출 계산 (총매출 ÷ 거래처수)
            int customerCount = uniqueCustomers.size();
            Integer averageSalesPerCustomer = customerCount > 0 ? totalSales / customerCount : 0;
            
            // 전체 합계 정보
            MonthlySalesRespDto.TotalSummary summary = MonthlySalesRespDto.TotalSummary.builder()
                    .totalSales(totalSales)
                    .totalOrderCount(totalOrderCount)
                    .averageSalesPerStore(averageSalesPerCustomer)
                    .storeCount(customerCount)
                    .build();
            
            // 응답 데이터 구성
            MonthlySalesRespDto response = MonthlySalesRespDto.builder()
                    .startMonth(reqDto.getStartDate())
                    .endMonth(reqDto.getEndDate())
                    .summary(summary)
                    .stores(storeList)
                    .build();
            
            log.info("월간매출 조회 완료 - 거래처수: {}, 총매출: {}, 총주문수: {}", 
                    customerCount, totalSales, totalOrderCount);
            
            return RespDto.success("월간매출 조회 완료", response);
            
        } catch (Exception e) {
            log.error("월간매출 조회 중 오류 발생", e);
            return RespDto.fail("월간매출 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 월의 마지막 날짜 계산
     */
    private String calculateMonthEndDate(String yearMonth) {
        try {
            int year = Integer.parseInt(yearMonth.substring(0, 4));
            int month = Integer.parseInt(yearMonth.substring(4, 6));
            
            LocalDate firstDay = LocalDate.of(year, month, 1);
            LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
            
            return lastDay.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            log.error("월 마지막 날짜 계산 오류 - 입력값: {}", yearMonth, e);
            return yearMonth + "31"; // 기본값
        }
    }
}