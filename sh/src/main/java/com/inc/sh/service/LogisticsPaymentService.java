package com.inc.sh.service;

import com.inc.sh.dto.logisticsPayment.reqDto.LogisticsPaymentSearchDto;
import com.inc.sh.dto.logisticsPayment.respDto.LogisticsPaymentRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogisticsPaymentService {
    
    private final OrderRepository orderRepository;
    
    /**
     * 물류대금마감현황 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<LogisticsPaymentRespDto>> getLogisticsPaymentStatus(LogisticsPaymentSearchDto searchDto) {
        try {
            log.info("물류대금마감현황 조회 시작 - 조건: {}", searchDto);
            
            List<Object[]> results = orderRepository.findLogisticsPaymentStatus(
                    searchDto.getOrderNo(),
                    searchDto.getCustomerCode(),
                    searchDto.getCollectionDate()
            );
            
            List<LogisticsPaymentRespDto> paymentStatusList = results.stream()
                    .map(result -> LogisticsPaymentRespDto.builder()
                            .orderNo((String) result[0])
                            .customerCode((Integer) result[1])
                            .customerName((String) result[2])
                            .orderDate((String) result[3])
                            .collectionDay((Integer) result[4])
                            .paymentDate((String) result[5])    // payment_at (납부일자)
                            .supplyAmount((Integer) result[6])
                            .vatAmount((Integer) result[7])
                            .totalAmount((Integer) result[8])
                            .paymentStatus((String) result[9])
                            .collectionDueDate((String) result[10]) // 계산된 회수기일
                            .build())
                    .collect(Collectors.toList());
            
            log.info("물류대금마감현황 조회 완료 - 조회 건수: {}", paymentStatusList.size());
            return RespDto.success("물류대금마감현황 조회 성공", paymentStatusList);
            
        } catch (Exception e) {
            log.error("물류대금마감현황 조회 중 오류 발생", e);
            return RespDto.fail("물류대금마감현황 조회 중 오류가 발생했습니다.");
        }
    }
}