package com.inc.sh.service.app;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.order.respDto.AppOrderHistoryRespDto;
import com.inc.sh.entity.Order;
import com.inc.sh.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppOrderHistoryService {
    
    private final OrderRepository orderRepository;
    
    /**
     * [앱] 주문내역 조회 (페이징)
     */
    public RespDto<AppOrderHistoryRespDto> getOrderHistory(
            Integer customerCode, 
            String deliveryRequestStartDt, 
            String deliveryRequestEndDt,
            int page, 
            int size) {
        
        try {
            // 페이징 설정 (최신순 정렬)
            Pageable pageable = PageRequest.of(page, size, 
                    Sort.by(Sort.Direction.DESC, "orderDt", "orderNo"));
            
            // 주문내역 조회 (페이징)
            Page<Order> orderPage = orderRepository.findByCustomerCodeAndDeliveryRequestDtBetween(
                    customerCode, 
                    deliveryRequestStartDt, 
                    deliveryRequestEndDt, 
                    pageable);
            
            // 전체 집계 조회 (같은 조건, 페이징 없이)
            AppOrderHistoryRespDto.OrderSummary summary = getOrderSummary(
                    customerCode, deliveryRequestStartDt, deliveryRequestEndDt);
            
            // DTO 변환
            AppOrderHistoryRespDto response = AppOrderHistoryRespDto.builder()
                    .summary(summary)
                    .orders(orderPage.getContent().stream()
                            .map(this::convertToOrderDto)
                            .toList())
                    .pagination(AppOrderHistoryRespDto.PaginationInfo.builder()
                            .currentPage(page)
                            .size(size)
                            .totalElements(orderPage.getTotalElements())
                            .totalPages(orderPage.getTotalPages())
                            .hasNext(orderPage.hasNext())
                            .build())
                    .build();
            
            return RespDto.success("주문내역 조회 성공", response);
            
        } catch (Exception e) {
            log.error("주문내역 조회 실패 - customerCode: {}", customerCode, e);
            return RespDto.fail("주문내역 조회 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 주문 집계 정보 조회
     */
    private AppOrderHistoryRespDto.OrderSummary getOrderSummary(
            Integer customerCode, String startDt, String endDt) {
        
        // 총 주문건수 조회
        Long totalCount = orderRepository.countByCustomerCodeAndDeliveryRequestDtBetween(
                customerCode, startDt, endDt);
        
        // 총 주문금액 조회
        Integer totalAmount = orderRepository.sumTotalAmtByCustomerCodeAndDeliveryRequestDtBetween(
                customerCode, startDt, endDt);
        
        return AppOrderHistoryRespDto.OrderSummary.builder()
                .totalCount(totalCount != null ? totalCount.intValue() : 0)
                .totalAmount(totalAmount != null ? totalAmount : 0)
                .build();
    }
    
    /**
     * Order -> OrderDto 변환
     */
    private AppOrderHistoryRespDto.OrderDto convertToOrderDto(Order order) {
        return AppOrderHistoryRespDto.OrderDto.builder()
                .orderNo(order.getOrderNo())
                .orderDt(formatDate(order.getOrderDt()))
                .deliveryRequestDt(formatDate(order.getDeliveryRequestDt()))
                .deliveryStatus(order.getDeliveryStatus())
                .totalAmt(order.getTotalAmt())
                .build();
    }
    
    /**
     * 날짜 형식 변환 (yyyyMMdd -> yyyy-MM-dd)
     */
    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.length() != 8) {
            return dateStr;
        }
        
        try {
            String year = dateStr.substring(0, 4);
            String month = dateStr.substring(4, 6);
            String day = dateStr.substring(6, 8);
            return year + "-" + month + "-" + day;
        } catch (Exception e) {
            return dateStr; // 변환 실패시 원본 반환
        }
    }
}