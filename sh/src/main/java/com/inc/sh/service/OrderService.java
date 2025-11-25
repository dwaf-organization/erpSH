package com.inc.sh.service;

import com.inc.sh.dto.order.reqDto.*;
import com.inc.sh.dto.order.respDto.*;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.entity.*;
import com.inc.sh.repository.*;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAccountTransactionsRepository customerAccountTransactionsRepository;
    private final DeliveryHolidayRepository deliveryHolidayRepository;
    private final OrderLimitSetRepository orderLimitSetRepository;
    private final VehicleRepository vehicleRepository;
    private final WarehouseItemsRepository warehouseItemsRepository;
    private final InventoryTransactionsRepository inventoryTransactionsRepository;
    private final MonthlyInventoryClosingRepository monthlyInventoryClosingRepository;
    
    /**
     * 주문 저장 (신규/수정)
     */
    @Transactional
    public RespDto<String> saveOrder(OrderSaveDto saveDto) {
        try {
            log.info("주문 저장 시작 - orderNo: {}, customerCode: {}", 
                    saveDto.getOrderNo(), saveDto.getCustomerCode());

            // 1. 거래처 정보 조회
            Customer customer = customerRepository.findByCustomerCode(saveDto.getCustomerCode());
            if (customer == null) {
                return RespDto.fail("존재하지 않는 거래처입니다.");
            }

            // 2. 배송휴일 체크
            RespDto<String> holidayCheck = checkDeliveryHoliday(saveDto.getDeliveryRequestDt(), customer.getBrandCode());
            if (holidayCheck.getCode() != 1) {
                return holidayCheck;
            }

            // 3. 배송요일 체크
            RespDto<String> dayCheck = checkDeliveryDay(saveDto.getDeliveryRequestDt(), customer.getDeliveryWeekday());
            if (dayCheck.getCode() != 1) {
                return dayCheck;
            }

            // 4. 주문시간 제한 체크
            RespDto<String> timeCheck = checkOrderTimeLimit(customer.getBrandCode());
            if (timeCheck.getCode() != 1) {
                return timeCheck;
            }

            String orderNo;
            
            if (saveDto.getOrderNo() == null || saveDto.getOrderNo().trim().isEmpty()) {
                // 신규 주문 생성
                orderNo = generateOrderNo();
                
                Order newOrder = Order.builder()
                        .orderNo(orderNo)
                        .customerCode(saveDto.getCustomerCode())
                        .vehicleCode(saveDto.getVehicleCode())
                        .distCenterCode(saveDto.getDistCenterCode())
                        .customerName(customer.getCustomerName())
                        .bizNum(customer.getBizNum())
                        .addr(customer.getAddr())
                        .ownerName(customer.getOwnerName())
                        .telNum(customer.getTelNum())
                        .orderDt(saveDto.getOrderDt())
                        .deliveryRequestDt(saveDto.getDeliveryRequestDt())
                        .deliveryAmt(saveDto.getDeliveryAmt())
                        .deliveryStatus(saveDto.getDeliveryStatus())
                        .paymentStatus("결제대기")
                        .depositTypeCode(customer.getDepositTypeCode())
                        .orderMessage(saveDto.getOrderMessage())
                        .taxableAmt(0)
                        .taxFreeAmt(0)
                        .supplyAmt(0)
                        .vatAmt(0)
                        .totalAmt(0)
                        .totalQty(0)
                        .build();
                
                orderRepository.save(newOrder);
                
            } else {
                // 기존 주문 수정
                orderNo = saveDto.getOrderNo();
                Order existingOrder = orderRepository.findByOrderNo(orderNo);
                if (existingOrder == null) {
                    return RespDto.fail("존재하지 않는 주문입니다.");
                }

                existingOrder.setDeliveryRequestDt(saveDto.getDeliveryRequestDt());
                existingOrder.setDeliveryStatus(saveDto.getDeliveryStatus());
                existingOrder.setDeliveryAmt(saveDto.getDeliveryAmt());
                existingOrder.setOrderMessage(saveDto.getOrderMessage());
                
                orderRepository.save(existingOrder);
            }
            
            return RespDto.success("주문이 저장되었습니다.", orderNo);

        } catch (Exception e) {
            log.error("주문 저장 중 오류 발생", e);
            return RespDto.fail("주문 저장 중 오류가 발생했습니다.");
        }
    }

    /**
     * 주문품목 업데이트 (orderItemCode 기반 생성/수정)
     */
    @Transactional
    public RespDto<String> updateOrderItems(OrderItemUpdateDto updateDto) {
        try {
            log.info("주문품목 업데이트 시작 - orderNo: {}", updateDto.getOrderNo());
            
            // 주문 존재 확인
            Order order = orderRepository.findByOrderNo(updateDto.getOrderNo());
            if (order == null) {
                return RespDto.fail("존재하지 않는 주문입니다.");
            }
            
            // 배송중일 경우 수정 불가
            if ("배송중".equals(order.getDeliveryStatus()) || "배송완료".equals(order.getDeliveryStatus())) {
                return RespDto.fail("배송 중이거나 완료된 주문은 수정할 수 없습니다.");
            }
            
            // 충전형인 경우 차액 기반 거래내역 처리
            if (order.getDepositTypeCode() == 1) {
                Customer customer = customerRepository.findByCustomerCode(order.getCustomerCode());
                
                // 1. 기존 주문 관련 거래내역 총액 계산
                List<CustomerAccountTransactions> existingTransactions = customerAccountTransactionsRepository
                        .findByReferenceId(updateDto.getOrderNo());
                
                Integer currentOrderTotal = existingTransactions.stream()
                        .filter(t -> "주문".equals(t.getReferenceType()) || "주문품목수정".equals(t.getReferenceType()))
                        .mapToInt(t -> {
                            // 출금은 음수로, 입금은 양수로 계산
                            if ("출금".equals(t.getTransactionType())) {
                                return -t.getAmount();
                            } else if ("입금".equals(t.getTransactionType())) {
                                return t.getAmount();
                            }
                            return 0;
                        })
                        .sum();
                
                // 현재까지 실제 결제 금액 (음수이므로 절댓값)
                Integer actualCurrentTotal = Math.abs(currentOrderTotal);
                
                log.info("기존 주문 총액: {}, 새로운 주문 총액: {}", actualCurrentTotal, updateDto.getTotalAmt());
                
                // 2. 차액 계산
                Integer difference = updateDto.getTotalAmt() - actualCurrentTotal;
                
                // 3. 차액이 있는 경우에만 거래내역 생성
                if (difference != 0) {
                    // 차액만큼 잔액 확인 (추가 결제가 필요한 경우)
                    if (difference > 0 && customer.getBalanceAmt() < difference) {
                        return RespDto.fail("잔액이 부족합니다. 추가 필요 금액: " + difference + "원");
                    }
                    
                    // 잔액 업데이트 (차액만 반영)
                    customer.setBalanceAmt(customer.getBalanceAmt() - difference);
                    customerRepository.save(customer);
                    
                    // 거래내역 생성
                    String transactionType = difference > 0 ? "출금" : "입금";
                    String note = difference > 0 ? 
                            "주문품목수정 - 추가결제 (" + difference + "원)" : 
                            "주문품목수정 - 환불 (" + Math.abs(difference) + "원)";
                    
                    CustomerAccountTransactions transaction = CustomerAccountTransactions.builder()
                            .customerCode(customer.getCustomerCode())
                            .virtualAccountCode(customer.getVirtualAccountCode())
                            .transactionDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                            .transactionType(transactionType)
                            .amount(Math.abs(difference))
                            .balanceAfter(customer.getBalanceAmt())
                            .referenceType("주문품목수정")
                            .referenceId(order.getOrderNo())
                            .note(note)
                            .build();
                    
                    customerAccountTransactionsRepository.save(transaction);
                    
                    log.info("거래내역 생성 - 유형: {}, 금액: {}, 잔액: {}", transactionType, Math.abs(difference), customer.getBalanceAmt());
                }
                
                // 결제완료 처리
                order.setPaymentStatus("결제완료");
                order.setPaymentAt(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            }
            
            // 기존 모든 주문품목의 재고 복원 (배송요청 상태에서만)
            if ("배송요청".equals(order.getDeliveryStatus())) {
                List<OrderItem> existingItems = orderItemRepository.findByOrderNo(updateDto.getOrderNo());
                for (OrderItem item : existingItems) {
                    restoreInventoryForItem(item);
                }
            }
            
            // 기존 주문품목 전체 삭제
            orderItemRepository.deleteByOrderNo(updateDto.getOrderNo());
            
            // 새로운 주문품목들 생성 및 재고 처리
            for (OrderItemUpdateDto.OrderItemSaveDto itemDto : updateDto.getOrderItems()) {
                // 새 주문품목 생성 (orderItemCode는 무시됨)
                createNewOrderItem(updateDto.getOrderNo(), itemDto);
                
                // 배송요청 상태에서만 재고 차감
                if ("배송요청".equals(order.getDeliveryStatus())) {
                    updateInventoryForOrderItem(itemDto);
                }
            }
            
            // 주문 금액 업데이트
            order.setTaxableAmt(updateDto.getTaxableAmt());
            order.setTaxFreeAmt(updateDto.getTaxFreeAmt());
            order.setSupplyAmt(updateDto.getSupplyAmt());
            order.setVatAmt(updateDto.getVatAmt());
            order.setTotalAmt(updateDto.getTotalAmt());
            order.setTotalQty(updateDto.getTotalQty());
            orderRepository.save(order);
            
            return RespDto.success("주문품목 업데이트가 완료되었습니다.", null);
            
        } catch (Exception e) {
            log.error("주문품목 업데이트 중 오류 발생", e);
            return RespDto.fail("주문품목 업데이트 중 오류가 발생했습니다.");
        }
    }

    /**
     * 신규 주문품목 생성
     */
    private void createNewOrderItem(String orderNo, OrderItemUpdateDto.OrderItemSaveDto itemDto) {
        OrderItem orderItem = OrderItem.builder()
                .orderNo(orderNo)
                .itemCode(itemDto.getItemCode())
                .releaseWarehouseCode(itemDto.getReleaseWarehouseCode())
                .itemName(itemDto.getItemName())
                .specification(itemDto.getSpecification())
                .unit(itemDto.getUnit())
                .priceType(itemDto.getPriceType())
                .orderUnitPrice(itemDto.getOrderUnitPrice())
                .currentStockQty(itemDto.getCurrentStockQty())
                .orderQty(itemDto.getOrderQty())
                .taxTarget(itemDto.getTaxTarget())
                .warehouseName(itemDto.getWarehouseName())
                .taxableAmt(itemDto.getTaxableAmt())
                .taxFreeAmt(itemDto.getTaxFreeAmt())
                .supplyAmt(itemDto.getSupplyAmt())
                .vatAmt(itemDto.getVatAmt())
                .totalAmt(itemDto.getTotalAmt())
                .totalQty(itemDto.getTotalQty())
                .description(itemDto.getDescription())
                .build();
        
        orderItemRepository.save(orderItem);
    }

    /**
     * 재고 차감 처리
     */
    private void updateInventoryForOrderItem(OrderItemUpdateDto.OrderItemSaveDto itemDto) {
        // 창고재고 차감
        WarehouseItems warehouseItem = warehouseItemsRepository
                .findByWarehouseCodeAndItemCode(itemDto.getReleaseWarehouseCode(), itemDto.getItemCode())
                .orElse(null);
        
        if (warehouseItem != null) {
            warehouseItem.setCurrentQuantity(warehouseItem.getCurrentQuantity() - itemDto.getOrderQty());
            warehouseItemsRepository.save(warehouseItem);
            
            // 재고수불부 기록 (출고)
            createInventoryTransaction(itemDto.getReleaseWarehouseCode(), itemDto.getItemCode(), 
                    -itemDto.getOrderQty(), itemDto.getOrderUnitPrice(), "출고", "주문품목 등록");
            
            // 월별재고마감 업데이트 (출고량 증가)
            updateMonthlyInventoryClosing(itemDto.getReleaseWarehouseCode(), itemDto.getItemCode(), 
                    itemDto.getOrderQty(), itemDto.getOrderQty() * itemDto.getOrderUnitPrice());
        }
    }

    /**
     * 개별 품목 재고 복원
     */
    private void restoreInventoryForItem(OrderItem item) {
        // 창고재고 복원
        WarehouseItems warehouseItem = warehouseItemsRepository
                .findByWarehouseCodeAndItemCode(item.getReleaseWarehouseCode(), item.getItemCode())
                .orElse(null);
        
        if (warehouseItem != null) {
            warehouseItem.setCurrentQuantity(warehouseItem.getCurrentQuantity() + item.getOrderQty());
            warehouseItemsRepository.save(warehouseItem);
            
            // 재고수불부 기록 (입고)
            createInventoryTransaction(item.getReleaseWarehouseCode(), item.getItemCode(), 
                    item.getOrderQty(), item.getOrderUnitPrice(), "입고", "주문품목 수정으로 인한 재고 복원");
            
            // 월별재고마감 업데이트 (출고량 감소)
            updateMonthlyInventoryClosing(item.getReleaseWarehouseCode(), item.getItemCode(), 
                    -item.getOrderQty(), -(item.getOrderQty() * item.getOrderUnitPrice()));
        }
    }

    /**
     * 재고수불부 기록 생성
     */
    private void createInventoryTransaction(Integer warehouseCode, Integer itemCode, 
            Integer quantity, Integer unitPrice, String transactionType, String description) {
        
        // warehouse_item_code 조회
        WarehouseItems warehouseItem = warehouseItemsRepository
                .findByWarehouseCodeAndItemCode(warehouseCode, itemCode)
                .orElse(null);
        
        if (warehouseItem == null) {
            log.warn("창고품목을 찾을 수 없습니다. warehouseCode: {}, itemCode: {}", warehouseCode, itemCode);
            return; // 창고품목이 없으면 재고수불부 기록 생성하지 않음
        }
        
        InventoryTransactions transaction = InventoryTransactions.builder()
                .warehouseCode(warehouseCode)
                .warehouseItemCode(warehouseItem.getWarehouseItemCode()) // warehouse_item_code 설정
                .itemCode(itemCode)
                .transactionDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                .transactionType(transactionType)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .amount(quantity * unitPrice)
                .description(description)
                .build();
        
        inventoryTransactionsRepository.save(transaction);
    }

    /**
     * 월별재고마감 업데이트
     */
    private void updateMonthlyInventoryClosing(Integer warehouseCode, Integer itemCode, 
            Integer outQuantity, Integer outAmount) {
        
        String closingYm = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        
        MonthlyInventoryClosing closing = monthlyInventoryClosingRepository
                .findByWarehouseCodeAndItemCodeAndClosingYm(warehouseCode, itemCode, closingYm)
                .orElse(null);
        
        if (closing != null) {
            closing.setOutQuantity(closing.getOutQuantity() + outQuantity);
            closing.setOutAmount(closing.getOutAmount() + outAmount);
            
            // 계산수량, 계산금액 업데이트 (이월+입고-출고)
            Integer calQuantity = closing.getOpeningQuantity() + closing.getInQuantity() - closing.getOutQuantity();
            Integer calAmount = closing.getOpeningAmount() + closing.getInAmount() - closing.getOutAmount();
            
            closing.setCalQuantity(calQuantity);
            closing.setCalAmount(calAmount);
            
            monthlyInventoryClosingRepository.save(closing);
        }
    }

    /**
     * 주문 목록 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<OrderRespDto>> getOrderList(OrderSearchDto searchDto) {
        try {
            log.info("주문 목록 조회 시작");
            
            List<Order> orders = orderRepository.findBySearchConditions(
                    searchDto.getOrderDtStart(),
                    searchDto.getOrderDtEnd(),
                    searchDto.getCustomerName(),
                    searchDto.getDeliveryStatus()
            );
            
            List<OrderRespDto> responseList = orders.stream()
                    .map(OrderRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            return RespDto.success("주문 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("주문 목록 조회 중 오류 발생", e);
            return RespDto.fail("주문 목록 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 주문품목 목록 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<OrderItemRespDto>> getOrderItemList(String orderNo) {
        try {
            log.info("주문품목 목록 조회 시작 - orderNo: {}", orderNo);
            
            List<OrderItem> orderItems = orderItemRepository.findByOrderNo(orderNo);
            
            List<OrderItemRespDto> responseList = orderItems.stream()
                    .map(OrderItemRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            return RespDto.success("주문품목 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("주문품목 목록 조회 중 오류 발생", e);
            return RespDto.fail("주문품목 목록 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 주문 삭제 (주문취소)
     */
    @Transactional
    public RespDto<String> deleteOrder(String orderNo) {
        try {
            log.info("주문 삭제 시작 - orderNo: {}", orderNo);
            
            // 주문 존재 확인
            Order order = orderRepository.findByOrderNo(orderNo);
            if (order == null) {
                return RespDto.fail("존재하지 않는 주문입니다.");
            }
            
            // 배송요청 상태에서만 삭제 가능
            if (!"배송요청".equals(order.getDeliveryStatus())) {
                return RespDto.fail("배송요청 상태의 주문만 삭제할 수 있습니다.");
            }
            
            // 충전형인 경우 전액 환불 처리
            if (order.getDepositTypeCode() == 1) {
                Customer customer = customerRepository.findByCustomerCode(order.getCustomerCode());
                
                // 1. 기존 주문 관련 거래내역 총액 계산 (환불할 금액)
                List<CustomerAccountTransactions> existingTransactions = customerAccountTransactionsRepository
                        .findByReferenceId(orderNo);
                
                Integer totalRefundAmount = existingTransactions.stream()
                        .filter(t -> "주문".equals(t.getReferenceType()) || "주문품목수정".equals(t.getReferenceType()))
                        .mapToInt(t -> {
                            // 출금은 환불 대상, 입금은 차감
                            if ("출금".equals(t.getTransactionType())) {
                                return t.getAmount();
                            } else if ("입금".equals(t.getTransactionType())) {
                                return -t.getAmount();
                            }
                            return 0;
                        })
                        .sum();
                
                // 2. 환불할 금액이 있는 경우에만 거래내역 생성
                if (totalRefundAmount > 0) {
                    // 잔액 증가 (환불)
                    customer.setBalanceAmt(customer.getBalanceAmt() + totalRefundAmount);
                    customerRepository.save(customer);
                    
                    // 주문취소 거래내역 생성
                    CustomerAccountTransactions cancelTransaction = CustomerAccountTransactions.builder()
                            .customerCode(customer.getCustomerCode())
                            .virtualAccountCode(customer.getVirtualAccountCode())
                            .transactionDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                            .transactionType("입금")
                            .amount(totalRefundAmount)
                            .balanceAfter(customer.getBalanceAmt())
                            .referenceType("주문취소")
                            .referenceId(orderNo)
                            .note("주문취소 - 전액환불 (" + totalRefundAmount + "원)")
                            .build();
                    
                    customerAccountTransactionsRepository.save(cancelTransaction);
                    
                    log.info("주문취소 환불 처리 완료 - 환불금액: {}, 잔액: {}", totalRefundAmount, customer.getBalanceAmt());
                }
            }
            
            // 주문품목 먼저 삭제
            orderItemRepository.deleteByOrderNo(orderNo);
            log.info("주문품목 삭제 완료 - orderNo: {}", orderNo);
            
            // 주문 삭제
            orderRepository.deleteById(orderNo);
            log.info("주문 삭제 완료 - orderNo: {}", orderNo);
            
            return RespDto.success("주문이 취소되었습니다.", null);
            
        } catch (Exception e) {
            log.error("주문 삭제 중 오류 발생", e);
            return RespDto.fail("주문 삭제 중 오류가 발생했습니다.");
        }
    }

    /**
     * 주문품목 삭제
     */
    @Transactional
    public RespDto<String> deleteOrderItem(Integer orderItemCode) {
        try {
            log.info("주문품목 삭제 시작 - orderItemCode: {}", orderItemCode);
            
            orderItemRepository.deleteById(orderItemCode);
            
            return RespDto.success("주문품목이 삭제되었습니다.", null);
            
        } catch (Exception e) {
            log.error("주문품목 삭제 중 오류 발생", e);
            return RespDto.fail("주문품목 삭제 중 오류가 발생했습니다.");
        }
    }

    /**
     * 배송휴일 체크
     */
    private RespDto<String> checkDeliveryHoliday(String deliveryRequestDt, Integer brandCode) {
        try {
            List<DeliveryHoliday> holidays = deliveryHolidayRepository.findByBrandCodeAndHolidayDt(brandCode, deliveryRequestDt);
            if (holidays != null && !holidays.isEmpty()) {
                return RespDto.fail("해당 날짜는 배송휴일입니다. 다른 날짜를 선택해주세요.");
            }
            return RespDto.success("배송휴일 체크 통과", null);
        } catch (Exception e) {
            log.error("배송휴일 체크 중 오류 발생", e);
            return RespDto.fail("배송휴일 체크 중 오류가 발생했습니다.");
        }
    }

    /**
     * 배송요일 체크
     */
    private RespDto<String> checkDeliveryDay(String deliveryRequestDt, String deliveryWeekday) {
        try {
            LocalDate date = LocalDate.parse(deliveryRequestDt, DateTimeFormatter.ofPattern("yyyyMMdd"));
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            int dayIndex = (dayOfWeek.getValue() % 7); // 일=0, 월=1, ..., 토=6
            
            if (deliveryWeekday.length() != 7 || deliveryWeekday.charAt(dayIndex) != '1') {
                return RespDto.fail("해당 요일은 배송이 불가능합니다. 다른 날짜를 선택해주세요.");
            }
            return RespDto.success("배송요일 체크 통과", null);
        } catch (Exception e) {
            log.error("배송요일 체크 중 오류 발생", e);
            return RespDto.fail("배송요일 체크 중 오류가 발생했습니다.");
        }
    }

    /**
     * 주문시간 제한 체크
     */
    private RespDto<String> checkOrderTimeLimit(Integer brandCode) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm"));
            
            String[] dayNames = {"월", "화", "수", "목", "금", "토", "일"};
            int dayIndex = now.getDayOfWeek().getValue() - 1;
            if (now.getDayOfWeek() == DayOfWeek.SUNDAY) dayIndex = 6;
            String today = dayNames[dayIndex];
            
            List<OrderLimitSet> limits = orderLimitSetRepository.findByBrandCodeAndDayName(brandCode, today);
            
            for (OrderLimitSet limit : limits) {
                if (isTimeInRange(currentTime, limit.getLimitStartTime(), limit.getLimitEndTime())) {
                    return RespDto.fail("주문 제한 시간입니다. " + limit.getLimitStartTime() + "~" + limit.getLimitEndTime() + " 시간에는 주문할 수 없습니다.");
                }
            }
            
            return RespDto.success("주문시간 체크 통과", null);
            
        } catch (Exception e) {
            log.error("주문시간 체크 중 오류 발생", e);
            return RespDto.fail("주문시간 체크 중 오류가 발생했습니다.");
        }
    }

    /**
     * 시간 범위 체크
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
     * 결제 처리 (충전형)
     */
    private void processPayment(Customer customer, Order order, Integer amount) {
        try {
            // 잔액 차감
            customer.setBalanceAmt(customer.getBalanceAmt() - amount);
            customerRepository.save(customer);
            
            // 거래내역 생성
            CustomerAccountTransactions transaction = CustomerAccountTransactions.builder()
                    .customerCode(customer.getCustomerCode())
                    .virtualAccountCode(customer.getVirtualAccountCode())
                    .transactionDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                    .transactionType("출금")
                    .amount(amount)
                    .balanceAfter(customer.getBalanceAmt())
                    .referenceType("주문")
                    .referenceId(order.getOrderNo())
                    .note("주문결제 - " + order.getOrderNo())
                    .build();
            
            customerAccountTransactionsRepository.save(transaction);
            
        } catch (Exception e) {
            log.error("결제 처리 중 오류 발생", e);
            throw new RuntimeException("결제 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 주문번호 생성 (YYYYMMDD001 형태)
     */
    private String generateOrderNo() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String latestOrderNo = orderRepository.findLatestOrderNoByDate(today);

        int sequence = 1;
        if (latestOrderNo != null && latestOrderNo.length() >= 11) {
            String sequencePart = latestOrderNo.substring(8);
            sequence = Integer.parseInt(sequencePart) + 1;
        }

        return String.format("%s%03d", today, sequence);
    }
}