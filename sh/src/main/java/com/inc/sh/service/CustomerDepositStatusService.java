package com.inc.sh.service;

import com.inc.sh.dto.customerDepositStatus.reqDto.CustomerDepositStatusSearchDto;
import com.inc.sh.dto.customerDepositStatus.respDto.CustomerDepositStatusRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.repository.DepositsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerDepositStatusService {
    
    private final DepositsRepository depositsRepository;
    
    /**
     * 거래처별수금현황 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<CustomerDepositStatusRespDto>> getCustomerDepositStatusList(CustomerDepositStatusSearchDto searchDto) {
        try {
            log.info("거래처별수금현황 조회 시작 - hqCode: {}, 기간: {}~{}, 거래처: {}, 브랜드: {}, 입금유형: {}", 
                    searchDto.getHqCode(), searchDto.getStartDate(), searchDto.getEndDate(), 
                    searchDto.getCustomerCode(), searchDto.getBrandCode(), searchDto.getDepositMethod());
            
            List<Object[]> results = depositsRepository.findCustomerDepositStatusWithConditionsWithHqCode(
                    searchDto.getStartDate(),
                    searchDto.getEndDate(),
                    searchDto.getCustomerCode(),
                    searchDto.getBrandCode(),
                    searchDto.getDepositMethod(),
                    searchDto.getHqCode()
            );
            
            List<CustomerDepositStatusRespDto> responseList = results.stream()
                    .map(result -> CustomerDepositStatusRespDto.builder()
                            .depositId((Integer) result[0])
                            .depositDate(formatDepositDate((String) result[1]))
                            .customerCode((Integer) result[2])
                            .customerName((String) result[3])
                            .depositAmount(((Number) result[4]).intValue())
                            .depositMethod((Integer) result[5])
                            .depositMethodName(getDepositMethodName((Integer) result[5]))
                            .note((String) result[6])
                            .build())
                    .collect(Collectors.toList());
            
            log.info("거래처별수금현황 조회 완료 - hqCode: {}, 조회 건수: {}", searchDto.getHqCode(), responseList.size());
            return RespDto.success("거래처별수금현황 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("거래처별수금현황 조회 중 오류 발생 - hqCode: {}", searchDto.getHqCode(), e);
            return RespDto.fail("거래처별수금현황 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 입금일자 포맷 변환 (YYYYMMDD -> YYYY-MM-DD)
     */
    private String formatDepositDate(String depositDate) {
        try {
            if (depositDate != null && depositDate.length() == 8) {
                LocalDate date = LocalDate.parse(depositDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            return depositDate;
        } catch (Exception e) {
            log.warn("입금일자 포맷 변환 실패: {}", depositDate);
            return depositDate;
        }
    }
    
    /**
     * 입금유형명 변환 (0=일반입금, 1=가상계좌)
     */
    private String getDepositMethodName(Integer depositMethod) {
        if (depositMethod == 0) {
            return "일반입금";
        } else if ("1".equals(depositMethod)) {
            return "가상계좌";
        } else {
            return "올바르지않은 입금유형"; // 원본 값 반환
        }
    }
}