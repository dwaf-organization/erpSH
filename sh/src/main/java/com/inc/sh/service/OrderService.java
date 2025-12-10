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
import java.util.ArrayList;
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
     * 주문 다중 저장 (신규/수정)
     */
    @Transactional
    public RespDto<OrderBatchResult> saveOrders(OrderSaveReqDto reqDto) {
        
        log.info("주문 다중 저장 시작 - 총 {}건", reqDto.getOrders().size());
        
        List<OrderRespDto> successData = new ArrayList<>();
        List<OrderBatchResult.OrderErrorDto> failData = new ArrayList<>();
        
        for (OrderSaveReqDto.OrderSaveItemDto order : reqDto.getOrders()) {
            try {
                // 개별 주문 저장 처리
                OrderRespDto savedOrder = saveSingleOrder(order);
                successData.add(savedOrder);
                
                log.info("주문 저장 성공 - orderNo: {}, customerCode: {}", 
                        savedOrder.getOrderNo(), savedOrder.getCustomerCode());
                
            } catch (Exception e) {
                log.error("주문 저장 실패 - customerCode: {}, 에러: {}", order.getCustomerCode(), e.getMessage());
                
                // 에러 시 거래처명 조회 시도
                String customerName = getCustomerNameSafely(order.getCustomerCode());
                
                OrderBatchResult.OrderErrorDto errorDto = OrderBatchResult.OrderErrorDto.builder()
                        .orderNo(order.getOrderNo())
                        .customerName(customerName)
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성
        OrderBatchResult result = OrderBatchResult.builder()
                .totalCount(reqDto.getOrders().size())
                .successCount(successData.size())
                .failCount(failData.size())
                .successData(successData)
                .failData(failData)
                .build();
        
        String message = String.format("주문 저장 완료 - 성공: %d건, 실패: %d건", 
                successData.size(), failData.size());
        
        log.info("주문 다중 저장 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getOrders().size(), successData.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 주문 저장 처리 (기존 검증 로직 포함)
     */
    private OrderRespDto saveSingleOrder(OrderSaveReqDto.OrderSaveItemDto saveDto) {
        
        // 1. 거래처 정보 조회 및 검증
        Customer customer = customerRepository.findByCustomerCode(saveDto.getCustomerCode());
        if (customer == null) {
            throw new RuntimeException("존재하지 않는 거래처입니다: " + saveDto.getCustomerCode());
        }

        // 2. 배송휴일 체크
        List<DeliveryHoliday> holidays = deliveryHolidayRepository.findByBrandCodeAndHolidayDt(
                customer.getBrandCode(), saveDto.getDeliveryRequestDt());
        if (holidays != null && !holidays.isEmpty()) {
            throw new RuntimeException("해당 날짜는 배송휴일입니다. 다른 날짜를 선택해주세요.");
        }

        // 3. 배송요일 체크
        LocalDate date = LocalDate.parse(saveDto.getDeliveryRequestDt(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        int dayIndex = (dayOfWeek.getValue() % 7); // 일=0, 월=1, ..., 토=6
        
        if (customer.getDeliveryWeekday().length() != 7 || customer.getDeliveryWeekday().charAt(dayIndex) != '1') {
            throw new RuntimeException("해당 요일은 배송이 불가능합니다. 다른 날짜를 선택해주세요.");
        }

        // 4. 주문시간 제한 체크
        LocalDateTime now = LocalDateTime.now();
        String currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        String[] dayNames = {"월", "화", "수", "목", "금", "토", "일"};
        int dayIdx = now.getDayOfWeek().getValue() - 1;
        if (now.getDayOfWeek() == DayOfWeek.SUNDAY) dayIdx = 6;
        String today = dayNames[dayIdx];
        
        List<OrderLimitSet> limits = orderLimitSetRepository.findByBrandCodeAndDayName(customer.getBrandCode(), today);
        for (OrderLimitSet limit : limits) {
            if (isTimeInRange(currentTime, limit.getLimitStartTime(), limit.getLimitEndTime())) {
                throw new RuntimeException("주문 제한 시간입니다. " + limit.getLimitStartTime() + "~" + limit.getLimitEndTime() + " 시간에는 주문할 수 없습니다.");
            }
        }

        // 5. 충전형 주문시 잔액 확인
        if (saveDto.getDepositTypeCode() != null && saveDto.getDepositTypeCode() == 1 && saveDto.getTotalAmt() != null) {
            if (customer.getBalanceAmt() < saveDto.getTotalAmt()) {
                throw new RuntimeException("잔액이 부족합니다. 현재 잔액: " + customer.getBalanceAmt() + "원, 주문금액: " + saveDto.getTotalAmt() + "원");
            }
        }

        String orderNo;
        Order orderEntity;
        
        if (saveDto.getOrderNo() == null || saveDto.getOrderNo().trim().isEmpty()) {
            // 신규 주문 생성
            orderNo = generateOrderNo();
            
            orderEntity = Order.builder()
                    .orderNo(orderNo)
                    .hqCode(saveDto.getHqCode())
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
            
            orderEntity = orderRepository.save(orderEntity);
            
            log.info("주문 신규 생성 - orderNo: {}, customerName: {}", orderNo, customer.getCustomerName());
            
        } else {
            // 주문 수정
            orderEntity = orderRepository.findByOrderNo(saveDto.getOrderNo());
            if (orderEntity == null) {
                throw new RuntimeException("존재하지 않는 주문입니다: " + saveDto.getOrderNo());
            }
            
            // 배송요청 상태에서만 수정 가능
            if (!"배송요청".equals(orderEntity.getDeliveryStatus())) {
                throw new RuntimeException("배송요청 상태의 주문만 수정할 수 있습니다.");
            }
            
            // 주문 정보 수정
            orderEntity.setDeliveryRequestDt(saveDto.getDeliveryRequestDt());
            orderEntity.setOrderMessage(saveDto.getOrderMessage());
            orderEntity.setDeliveryAmt(saveDto.getDeliveryAmt());
            orderEntity.setDeliveryStatus(saveDto.getDeliveryStatus());
            
            if (saveDto.getVehicleCode() != null) {
                orderEntity.setVehicleCode(saveDto.getVehicleCode());
            }
            
            orderEntity = orderRepository.save(orderEntity);
            orderNo = orderEntity.getOrderNo();
            
            log.info("주문 정보 수정 - orderNo: {}, customerName: {}", orderNo, customer.getCustomerName());
        }
        
        return OrderRespDto.fromEntity(orderEntity);
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
            log.info("주문 목록 조회 시작 - orderDtStart: {}, orderDtEnd: {}, customerName: {}, deliveryStatus: {}, hqCode: {}",
                    searchDto.getOrderDtStart(), searchDto.getOrderDtEnd(), 
                    searchDto.getCustomerName(), searchDto.getDeliveryStatus(), searchDto.getHqCode());
            
            List<Order> orders = orderRepository.findBySearchConditionsWithHqCode(
                    searchDto.getOrderDtStart(),
                    searchDto.getOrderDtEnd(),
                    searchDto.getCustomerName(),
                    searchDto.getDeliveryStatus(),
                    searchDto.getHqCode()
            );
            
            List<OrderRespDto> responseList = orders.stream()
                    .map(OrderRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("주문 목록 조회 완료 - hqCode: {}, 조회 건수: {}", searchDto.getHqCode(), responseList.size());
            return RespDto.success("주문 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("주문 목록 조회 중 오류 발생 - hqCode: {}", searchDto.getHqCode(), e);
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
     * 주문 다중 삭제 (Hard Delete + 기존 검증 로직)
     */
    @Transactional
    public RespDto<OrderBatchResult> deleteOrders(OrderDeleteReqDto reqDto) {
        
        log.info("주문 다중 삭제 시작 - 총 {}건", reqDto.getOrderNos().size());
        
        List<String> successOrderNos = new ArrayList<>();
        List<OrderBatchResult.OrderErrorDto> failData = new ArrayList<>();
        
        for (String orderNo : reqDto.getOrderNos()) {
            try {
                // 개별 주문 삭제 처리
                deleteSingleOrder(orderNo);
                successOrderNos.add(orderNo);
                
                log.info("주문 삭제 성공 - orderNo: {}", orderNo);
                
            } catch (Exception e) {
                log.error("주문 삭제 실패 - orderNo: {}, 에러: {}", orderNo, e.getMessage());
                
                // 에러 시 거래처명 조회 시도
                String customerName = getCustomerNameByOrderNoSafely(orderNo);
                
                OrderBatchResult.OrderErrorDto errorDto = OrderBatchResult.OrderErrorDto.builder()
                        .orderNo(orderNo)
                        .customerName(customerName)
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성 (삭제는 successData 대신 성공 주문번호만)
        OrderBatchResult result = OrderBatchResult.builder()
                .totalCount(reqDto.getOrderNos().size())
                .successCount(successOrderNos.size())
                .failCount(failData.size())
                .successData(successOrderNos.stream()
                        .map(orderNo -> OrderRespDto.builder().orderNo(orderNo).build())
                        .collect(Collectors.toList()))
                .failData(failData)
                .build();
        
        String message = String.format("주문 삭제 완료 - 성공: %d건, 실패: %d건", 
                successOrderNos.size(), failData.size());
        
        log.info("주문 다중 삭제 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getOrderNos().size(), successOrderNos.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 주문 삭제 처리 (기존 검증 로직 포함)
     */
    private void deleteSingleOrder(String orderNo) {
        
        // 주문 존재 확인
        Order order = orderRepository.findByOrderNo(orderNo);
        if (order == null) {
            throw new RuntimeException("존재하지 않는 주문입니다: " + orderNo);
        }
        
        // 배송요청 상태에서만 삭제 가능
        if (!"배송요청".equals(order.getDeliveryStatus())) {
            throw new RuntimeException("배송요청 상태의 주문만 삭제할 수 있습니다. 현재 상태: " + order.getDeliveryStatus());
        }
        
        // 충전형인 경우 전액 환불 처리
        if (order.getDepositTypeCode() == 1) {
            Customer customer = customerRepository.findByCustomerCode(order.getCustomerCode());
            
            // 기존 주문 관련 거래내역 총액 계산 (환불할 금액)
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
            
            // 환불할 금액이 있는 경우에만 거래내역 생성
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
        
        // 주문 삭제 (Hard Delete)
        orderRepository.deleteById(orderNo);
        log.info("주문 삭제 완료 - orderNo: {}", orderNo);
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
     * 거래처명 안전 조회 (에러 발생시 사용)
     */
    private String getCustomerNameSafely(Integer customerCode) {
        try {
            Customer customer = customerRepository.findByCustomerCode(customerCode);
            return customer != null ? customer.getCustomerName() : "알 수 없음";
        } catch (Exception e) {
            return "조회 실패";
        }
    }

    /**
     * 주문번호로 거래처명 안전 조회 (에러 발생시 사용)
     */
    private String getCustomerNameByOrderNoSafely(String orderNo) {
        try {
            Order order = orderRepository.findByOrderNo(orderNo);
            return order != null ? order.getCustomerName() : "알 수 없음";
        } catch (Exception e) {
            return "조회 실패";
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