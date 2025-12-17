package com.inc.sh.service.app;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.order.reqDto.OrderCreateReqDto;
import com.inc.sh.dto.order.respDto.AppOrderRespDto;
import com.inc.sh.entity.*;
import com.inc.sh.repository.*;
import com.inc.sh.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppOrderCreateService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final CustomerCartRepository customerCartRepository;
    private final CustomerAccountTransactionsRepository customerAccountTransactionsRepository;
    private final DeliveryHolidayRepository deliveryHolidayRepository;
    private final OrderLimitSetRepository orderLimitSetRepository;
    private final NotificationService notificationService;
    
    /**
     * 주문 생성
     */
    @Transactional
    public RespDto<AppOrderRespDto> createOrder(OrderCreateReqDto request) {
        try {
            // 1. 거래처 정보 조회
            Customer customer = customerRepository.findByCustomerCode(request.getCustomerCode());
            if (customer == null) {
                return RespDto.fail("존재하지 않는 거래처입니다");
            }
            
            // 2. 납기요청일 요일 체크
            RespDto<String> deliveryDayCheck = checkDeliveryDay(request.getDeliveryRequestDt(), customer);
            if (deliveryDayCheck.getCode() != 1) {
                return RespDto.fail(deliveryDayCheck.getMessage());
            }
            
            // 3. 배송휴일 체크
            RespDto<String> holidayCheck = checkDeliveryHoliday(request.getDeliveryRequestDt(), customer.getBrandCode());
            if (holidayCheck.getCode() != 1) {
                return RespDto.fail(holidayCheck.getMessage());
            }
            
            // 4. 주문제한시간 체크
//            RespDto<String> timeLimitCheck = checkOrderTimeLimit(customer.getBrandCode());
//            if (timeLimitCheck.getCode() != 1) {
//                return RespDto.fail(timeLimitCheck.getMessage());
//            }
            
            // 5. 잔액 체크 및 주문 처리
            RespDto<AppOrderRespDto> orderResult = processOrderByDepositType(request, customer);
            if (orderResult.getCode() != 1) {
                // 주문 실패시 장바구니 수량 복원
                restoreCartQuantities(request);
                return orderResult;
            }
            
            // 6. 주문 성공시 장바구니 전체 삭제
            deleteAllCartItems(request.getCustomerCode(), request.getCustomerUserCode());
            
            log.info("[앱] 주문 생성 완료 - orderNo: {}, customerCode: {}", 
                    orderResult.getData().getOrderNo(), request.getCustomerCode());
            
            return orderResult;
            
        } catch (Exception e) {
            log.error("주문 생성 실패", e);
            return RespDto.fail("주문 처리 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 납기요청일 요일 체크
     */
    private RespDto<String> checkDeliveryDay(String deliveryRequestDt, Customer customer) {
        try {
            LocalDate deliveryDate = LocalDate.parse(deliveryRequestDt, DateTimeFormatter.ofPattern("yyyyMMdd"));
            DayOfWeek dayOfWeek = deliveryDate.getDayOfWeek();
            
            // 월=1, 화=2, ... 일=7 → 배열 인덱스로 변환 (월=0, 화=1, ... 일=6)
            int dayIndex = dayOfWeek.getValue() - 1; // 월=0, 화=1, ... 토=5
            if (dayOfWeek == DayOfWeek.SUNDAY) dayIndex = 6; // 일=6
            
            String deliveryDays = customer.getDeliveryWeekday(); // "1111111" 형태
            if (deliveryDays != null && deliveryDays.length() > dayIndex) {
                char dayFlag = deliveryDays.charAt(dayIndex);
                if (dayFlag == '0') {
                    return RespDto.fail("납기요청이 불가능한 요일입니다");
                }
            }
            
            return RespDto.success("요일 체크 통과", "통과");
            
        } catch (Exception e) {
            return RespDto.fail("납기요청일 형식이 올바르지 않습니다");
        }
    }
    
    /**
     * 배송휴일 체크
     */
    private RespDto<String> checkDeliveryHoliday(String deliveryRequestDt, Integer brandCode) {
        try {
            // 브랜드별 특정 날짜 배송휴일 조회
            List<DeliveryHoliday> holidays = deliveryHolidayRepository.findByBrandCodeAndHolidayDt(brandCode, deliveryRequestDt);
            
            if (holidays != null && !holidays.isEmpty()) {
                return RespDto.fail("납기요청이 불가능한 휴일입니다");
            }
            
            return RespDto.success("배송휴일 체크 통과", "통과");
            
        } catch (Exception e) {
            return RespDto.fail("배송휴일 체크 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 주문제한시간 체크
     */
    private RespDto<String> checkOrderTimeLimit(Integer brandCode) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm"));
            
            // 현재 요일을 한글로 변환 (월화수목금토일)
            String[] dayNames = {"월", "화", "수", "목", "금", "토", "일"};
            int dayIndex = now.getDayOfWeek().getValue() - 1; // 월=0, 화=1, ... 토=5
            if (now.getDayOfWeek() == DayOfWeek.SUNDAY) dayIndex = 6; // 일=6
            String today = dayNames[dayIndex];
            
            // 브랜드별 해당 요일 주문제한 설정 조회
            List<OrderLimitSet> limits = orderLimitSetRepository.findByBrandCodeAndDayName(brandCode, today);
            
            // 해당 요일과 매칭하여 시간 체크
            for (OrderLimitSet limit : limits) {
                if (limit.getDayName().equals(today)) {
                    if (isTimeInRange(currentTime, limit.getLimitStartTime(), limit.getLimitEndTime())) {
                        return RespDto.fail("주문이 제한된 시간입니다");
                    }
                }
            }
            
            return RespDto.success("주문시간 체크 통과", "통과");
            
        } catch (Exception e) {
            return RespDto.fail("주문시간 체크 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 시간 범위 체크 (HH:mm 형식)
     */
    private boolean isTimeInRange(String currentTime, String startTime, String endTime) {
        try {
            LocalTime current = LocalTime.parse(currentTime);
            LocalTime start = LocalTime.parse(startTime);
            LocalTime end = LocalTime.parse(endTime);
            
            return !current.isBefore(start) && !current.isAfter(end);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 입금유형별 주문 처리
     */
    private RespDto<AppOrderRespDto> processOrderByDepositType(OrderCreateReqDto request, Customer customer) {
        if (customer.getDepositTypeCode() == 0) {
            // 후입금: 바로 주문 생성
            return createOrderRecord(request, customer, "결제대기", "배송요청", null);
        } else {
            // 충전형: 잔액 체크
            if (customer.getBalanceAmt() < request.getTotalAmt()) {
                return RespDto.fail("잔액이 부족하여 주문처리가 되지않습니다");
            }
            
            // 잔액 차감
            customer.setBalanceAmt(customer.getBalanceAmt() - request.getTotalAmt());
            customerRepository.save(customer);
            
            // 주문 생성
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            RespDto<AppOrderRespDto> orderResult = createOrderRecord(request, customer, "결제완료", "배송요청", today);
            
            if (orderResult.getCode() == 1) {
                // 거래내역 추가
                CustomerAccountTransactions transaction = CustomerAccountTransactions.builder()
                        .customerCode(customer.getCustomerCode())
                        .virtualAccountCode(customer.getVirtualAccountCode())
                        .transactionDate(today)
                        .transactionType("출금")
                        .amount(request.getTotalAmt())
                        .balanceAfter(customer.getBalanceAmt())
                        .referenceType("주문")
                        .referenceId(orderResult.getData().getOrderNo())
                        .note("주문결제")
                        .build();
                
                customerAccountTransactionsRepository.save(transaction);
            }
            
            return orderResult;
        }
    }
    
    /**
     * 주문 레코드 생성
     */
    private RespDto<AppOrderRespDto> createOrderRecord(OrderCreateReqDto request, Customer customer, 
                                                   String paymentStatus, String deliveryStatus, String paymentAt) {
        try {
            // 주문번호 생성
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String latestOrderNo = orderRepository.findLatestOrderNoByDate(today);
            String orderNo = generateOrderNo(today, latestOrderNo);
            
            // Order 생성
            Order order = Order.builder()
                    .orderNo(orderNo)
                    .hqCode(customer.getHqCode())
                    .customerCode(customer.getCustomerCode())
                    .vehicleCode(0) // 기본값
                    .distCenterCode(customer.getDistCenterCode())
                    .customerName(customer.getCustomerName())
                    .bizNum(customer.getBizNum())
                    .addr(customer.getAddr())
                    .ownerName(customer.getOwnerName())
                    .telNum(customer.getTelNum())
                    .orderDt(today)
                    .deliveryRequestDt(request.getDeliveryRequestDt())
                    .orderMessage(request.getOrderMessage())
                    .depositTypeCode(customer.getDepositTypeCode())
                    .taxableAmt(request.getTaxableAmt())
                    .taxFreeAmt(request.getTaxFreeAmt())
                    .supplyAmt(request.getSupplyAmt())
                    .vatAmt(request.getVatAmt())
                    .totalAmt(request.getTotalAmt())
                    .totalQty(request.getTotalQty())
                    .paymentStatus(paymentStatus)
                    .deliveryStatus(deliveryStatus)
                    .paymentAt(paymentAt)
                    .build();
            
            orderRepository.save(order);
            
            // OrderItem 생성
            for (OrderCreateReqDto.OrderItemReqDto itemReq : request.getOrderItems()) {
                OrderItem orderItem = OrderItem.builder()
                        .orderNo(orderNo)
                        .itemCode(itemReq.getItemCode())
                        .releaseWarehouseCode(itemReq.getWarehouseCode())
                        .itemName(itemReq.getItemName())
                        .specification(itemReq.getSpecification())
                        .unit(itemReq.getUnit())
                        .priceType(itemReq.getPriceType())
                        .orderUnitPrice(itemReq.getOrderUnitPrice())
                        .currentStockQty(itemReq.getCurrentStockQty())
                        .orderQty(itemReq.getOrderQty())
                        .taxTarget(itemReq.getTaxTarget())
                        .taxableAmt(itemReq.getTaxableAmt())
                        .taxFreeAmt(itemReq.getTaxFreeAmt())
                        .supplyAmt(itemReq.getSupplyAmt())
                        .vatAmt(itemReq.getVatAmt())
                        .totalAmt(itemReq.getTotalAmt())
                        .totalQty(itemReq.getTotalQty())
                        .build();
                
                orderItemRepository.save(orderItem);
            }
            
            // 성공시 데이터와 함께 리턴
            AppOrderRespDto responseData = AppOrderRespDto.builder()
                    .orderNo(orderNo)
                    .totalAmt(request.getTotalAmt())
                    .message("주문이 완료되었습니다")
                    .build();
            
            try {
                notificationService.createOrderNotification(
                    customer.getHqCode(),
                    customer.getCustomerCode(),
                    customer.getCustomerName(),
                    orderNo
                );
            } catch (Exception e) {
                log.warn("주문 알림 생성 실패 - orderNo: {}", orderNo, e);
            }
            
            return RespDto.success("주문 생성 성공", responseData);
            
        } catch (Exception e) {
            log.error("주문 레코드 생성 실패", e);
            return RespDto.fail("주문 생성 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 주문번호 생성
     */
    private String generateOrderNo(String datePrefix, String latestOrderNo) {
        if (latestOrderNo == null || latestOrderNo.isEmpty()) {
            return datePrefix + "001";
        }
        
        String sequencePart = latestOrderNo.substring(8); // 날짜 이후 순번 부분
        int nextSequence = Integer.parseInt(sequencePart) + 1;
        return datePrefix + String.format("%03d", nextSequence);
    }
    
    /**
     * 주문 실패시 장바구니 수량 복원
     */
    private void restoreCartQuantities(OrderCreateReqDto request) {
        try {
            List<CustomerCart> cartItems = customerCartRepository.findByCustomerCodeAndCustomerUserCode(
                    request.getCustomerCode(), request.getCustomerUserCode());
            
            // 주문 요청의 품목들과 매칭해서 원래 수량으로 복원
            for (CustomerCart cart : cartItems) {
                for (OrderCreateReqDto.OrderItemReqDto orderItem : request.getOrderItems()) {
                    if (cart.getItemCode().equals(orderItem.getItemCode()) && 
                        cart.getWarehouseCode().equals(orderItem.getWarehouseCode())) {
                        cart.setOrderQty(orderItem.getOrderQty()); // 원래 수량으로 복원
                        customerCartRepository.save(cart);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("장바구니 수량 복원 실패", e);
        }
    }
    
    /**
     * 주문 성공시 장바구니 전체 삭제
     */
    private void deleteAllCartItems(Integer customerCode, Integer customerUserCode) {
        try {
            List<CustomerCart> cartItems = customerCartRepository.findByCustomerCodeAndCustomerUserCode(
                    customerCode, customerUserCode);
            
            customerCartRepository.deleteAll(cartItems);
            
        } catch (Exception e) {
            log.error("장바구니 삭제 실패", e);
        }
    }
}