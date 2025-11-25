package com.inc.sh.service.app;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.order.reqDto.AppOrderConfirmReqDto;
import com.inc.sh.dto.order.respDto.AppOrderDetailRespDto;
import com.inc.sh.dto.order.respDto.AppOrderConfirmRespDto;
import com.inc.sh.entity.Order;
import com.inc.sh.entity.OrderItem;
import com.inc.sh.repository.OrderRepository;
import com.inc.sh.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppOrderDetailService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    
    /**
     * [앱] 주문상세 조회
     */
    public RespDto<AppOrderDetailRespDto> getOrderDetail(String orderNo, Integer customerCode) {
        try {
            // 1. 주문 정보 조회 및 권한 확인
            Order order = orderRepository.findByOrderNoAndCustomerCode(orderNo, customerCode);
            if (order == null) {
                return RespDto.fail("잘못된 주문번호입니다");
            }
            
            // 2. 주문품목 조회
            List<OrderItem> orderItems = orderItemRepository.findByOrderNo(orderNo);
            
            // 3. 총 품목 수 계산
            int totalItemCount = orderItems.size();
            
            // 4. DTO 변환
            AppOrderDetailRespDto response = AppOrderDetailRespDto.builder()
                    .order(convertToOrderDto(order, totalItemCount))
                    .items(orderItems.stream()
                            .map(this::convertToOrderItemDto)
                            .toList())
                    .build();
            
            return RespDto.success("주문상세 조회 성공", response);
            
        } catch (Exception e) {
            log.error("주문상세 조회 실패 - orderNo: {}, customerCode: {}", orderNo, customerCode, e);
            return RespDto.fail("주문상세 조회 중 오류가 발생했습니다");
        }
    }
    
    /**
     * [앱] 수령확인 처리
     */
    @Transactional
    public RespDto<AppOrderConfirmRespDto> confirmDelivery(AppOrderConfirmReqDto request) {
        try {
            // 1. 주문 조회 및 권한 확인
            Order order = orderRepository.findByOrderNoAndCustomerCode(request.getOrderNo(), request.getCustomerCode());
            if (order == null) {
                return RespDto.fail("주문번호가 혹은 거래처코드가 일치하지않습니다");
            }
            
            // 2. 결제상태 확인
            if (!"결제완료".equals(order.getPaymentStatus())) {
                return RespDto.fail("결제가 완료되지않은 주문입니다");
            }
            
            // 3. 현재 배송상태 확인
            if ("배송완료".equals(order.getDeliveryStatus())) {
                return RespDto.fail("이미 배송완료 처리된 주문입니다");
            }
            
            // 4. 배송상태 업데이트
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            order.setDeliveryStatus("배송완료");
            order.setDeliveryDt(currentDate);
            
            orderRepository.save(order);
            
            // 5. 응답 데이터 생성
            AppOrderConfirmRespDto responseData = AppOrderConfirmRespDto.builder()
                    .orderNo(request.getOrderNo())
                    .deliveryStatus("배송완료")
                    .deliveryDt(formatDate(currentDate))
                    .build();
            
            log.info("[앱] 수령확인 완료 - orderNo: {}, customerCode: {}", request.getOrderNo(), request.getCustomerCode());
            
            return RespDto.success("수령확인이 완료되었습니다", responseData);
            
        } catch (Exception e) {
            log.error("수령확인 처리 실패 - orderNo: {}, customerCode: {}", request.getOrderNo(), request.getCustomerCode(), e);
            return RespDto.fail("수령확인 처리 중 오류가 발생했습니다");
        }
    }
    
    /**
     * Order -> OrderDto 변환
     */
    private AppOrderDetailRespDto.OrderDto convertToOrderDto(Order order, int totalItemCount) {
        return AppOrderDetailRespDto.OrderDto.builder()
                .orderNo(order.getOrderNo())
                .orderDt(formatDate(order.getOrderDt()))
                .deliveryRequestDt(formatDate(order.getDeliveryRequestDt()))
                .deliveryDt(formatDate(order.getDeliveryDt()))
                .deliveryStatus(order.getDeliveryStatus())
                .orderMessage(order.getOrderMessage())
                .totalAmt(order.getTotalAmt())
                .totalItemCount(totalItemCount)
                .build();
    }
    
    /**
     * OrderItem -> OrderItemDto 변환
     */
    private AppOrderDetailRespDto.OrderItemDto convertToOrderItemDto(OrderItem orderItem) {
        return AppOrderDetailRespDto.OrderItemDto.builder()
                .itemCode(orderItem.getItemCode())
                .itemName(orderItem.getItemName())
                .specification(orderItem.getSpecification())
                .unit(orderItem.getUnit())
                .orderQty(orderItem.getOrderQty())
                .priceType(convertPriceType(orderItem.getPriceType()))
                .orderUnitPrice(orderItem.getOrderUnitPrice())
                .totalAmt(orderItem.getTotalAmt())
                .build();
    }
    
    /**
     * 단가유형 변환 (1=납품싯가, 2=납품단가)
     */
    private String convertPriceType(Integer priceType) {
        if (priceType == null) return "";
        
        return switch (priceType) {
            case 1 -> "납품싯가";
            case 2 -> "납품단가";
            default -> "";
        };
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