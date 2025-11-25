package com.inc.sh.service;

import com.inc.sh.dto.delivery.reqDto.*;
import com.inc.sh.dto.order.respDto.*;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.entity.*;
import com.inc.sh.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WarehouseItemsRepository warehouseItemsRepository;
    private final InventoryTransactionsRepository inventoryTransactionsRepository;
    private final MonthlyInventoryClosingRepository monthlyInventoryClosingRepository;
    
    /**
     * 배송 주문 목록 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<OrderRespDto>> getDeliveryList(DeliverySearchDto searchDto) {
        try {
            log.info("배송 주문 목록 조회 시작 - 조건: {}", searchDto);
            
            List<Order> orders = orderRepository.findByDeliverySearchConditions(
                    searchDto.getDeliveryRequestDt(),
                    searchDto.getCustomerCode(),
                    searchDto.getOrderNo(),
                    searchDto.getDeliveryStatus()
            );
            
            List<OrderRespDto> responseList = orders.stream()
                    .map(OrderRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("배송 주문 목록 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("배송 주문 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("배송 주문 목록 조회 중 오류 발생", e);
            return RespDto.fail("배송 주문 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 배송 주문품목 목록 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<OrderItemRespDto>> getDeliveryItemList(String orderNo) {
        try {
            log.info("배송 주문품목 목록 조회 시작 - orderNo: {}", orderNo);
            
            List<OrderItem> orderItems = orderItemRepository.findByOrderNo(orderNo);
            
            List<OrderItemRespDto> responseList = orderItems.stream()
                    .map(OrderItemRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("배송 주문품목 목록 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("배송 주문품목 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("배송 주문품목 목록 조회 중 오류 발생 - orderNo: {}", orderNo, e);
            return RespDto.fail("배송 주문품목 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 배송시작 (배송요청 → 배송중) + 재고 차감
     */
    @Transactional
    public RespDto<List<String>> startDelivery(DeliveryStartDto startDto) {
        try {
            log.info("배송시작 처리 시작 - 주문수: {}", startDto.getOrders().size());
            
            List<String> successOrders = new ArrayList<>();
            List<String> failedOrders = new ArrayList<>();
            
            for (DeliveryStartDto.DeliveryOrderDto orderDto : startDto.getOrders()) {
                try {
                    Order order = orderRepository.findByOrderNo(orderDto.getOrderNo());
                    if (order == null) {
                        failedOrders.add(orderDto.getOrderNo() + " (존재하지 않는 주문)");
                        continue;
                    }
                    
                    // 배송요청 상태 확인
                    if (!"배송요청".equals(order.getDeliveryStatus())) {
                        failedOrders.add(orderDto.getOrderNo() + " (배송요청 상태가 아님: " + order.getDeliveryStatus() + ")");
                        continue;
                    }
                    
                    // ✅ 재고 차감 처리 (주문품목별로)
                    List<OrderItem> orderItems = orderItemRepository.findByOrderNo(orderDto.getOrderNo());
                    for (OrderItem item : orderItems) {
                        processInventoryDeduction(item);
                    }
                    
                    // 주문 정보 업데이트
                    order.setVehicleCode(orderDto.getVehicleCode());
                    order.setVehicleName(orderDto.getVehicleName());
                    order.setDeliveryAmt(orderDto.getDeliveryAmt());
                    order.setDeliveryDt(orderDto.getDeliveryDt());
                    order.setDeliveryStatus("배송중");
                    
                    orderRepository.save(order);
                    successOrders.add(orderDto.getOrderNo());
                    
                    log.info("배송시작 처리 완료 (재고차감 포함) - orderNo: {}", orderDto.getOrderNo());
                    
                } catch (Exception e) {
                    log.error("배송시작 처리 중 오류 발생 - orderNo: {}", orderDto.getOrderNo(), e);
                    failedOrders.add(orderDto.getOrderNo() + " (처리 오류: " + e.getMessage() + ")");
                }
            }
            
            String message = String.format("배송시작 처리 완료 - 성공: %d건, 실패: %d건", 
                    successOrders.size(), failedOrders.size());
            
            if (!failedOrders.isEmpty()) {
                message += " | 실패 주문: " + String.join(", ", failedOrders);
            }
            
            log.info(message);
            return RespDto.success(message, successOrders);
            
        } catch (Exception e) {
            log.error("배송시작 처리 중 오류 발생", e);
            return RespDto.fail("배송시작 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 배송취소 (배송중 → 배송요청) + 재고 복원
     */
    @Transactional
    public RespDto<List<String>> cancelDelivery(DeliveryCancelDto cancelDto) {
        try {
            log.info("배송취소 처리 시작 - 주문수: {}", cancelDto.getOrderNos().size());
            
            List<String> successOrders = new ArrayList<>();
            List<String> failedOrders = new ArrayList<>();
            
            for (String orderNo : cancelDto.getOrderNos()) {
                try {
                    Order order = orderRepository.findByOrderNo(orderNo);
                    if (order == null) {
                        failedOrders.add(orderNo + " (존재하지 않는 주문)");
                        continue;
                    }
                    
                    // 배송중 상태 확인
                    if (!"배송중".equals(order.getDeliveryStatus())) {
                        failedOrders.add(orderNo + " (배송중 상태가 아님: " + order.getDeliveryStatus() + ")");
                        continue;
                    }
                    
                    // ✅ 재고 복원 처리 (주문품목별로)
                    List<OrderItem> orderItems = orderItemRepository.findByOrderNo(orderNo);
                    for (OrderItem item : orderItems) {
                        processInventoryRestoration(item);
                    }
                    
                    // 배송상태 변경
                    order.setDeliveryStatus("배송요청");
                    order.setDeliveryDt(null); // 배송일자 초기화
                    
                    orderRepository.save(order);
                    successOrders.add(orderNo);
                    
                    log.info("배송취소 처리 완료 (재고복원 포함) - orderNo: {}", orderNo);
                    
                } catch (Exception e) {
                    log.error("배송취소 처리 중 오류 발생 - orderNo: {}", orderNo, e);
                    failedOrders.add(orderNo + " (처리 오류: " + e.getMessage() + ")");
                }
            }
            
            String message = String.format("배송취소 처리 완료 - 성공: %d건, 실패: %d건", 
                    successOrders.size(), failedOrders.size());
            
            if (!failedOrders.isEmpty()) {
                message += " | 실패 주문: " + String.join(", ", failedOrders);
            }
            
            log.info(message);
            return RespDto.success(message, successOrders);
            
        } catch (Exception e) {
            log.error("배송취소 처리 중 오류 발생", e);
            return RespDto.fail("배송취소 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 배송완료 (배송요청/배송중 → 배송완료)
     * 재고처리 없음 (이미 배송시작에서 처리됨)
     */
    @Transactional
    public RespDto<List<String>> completeDelivery(DeliveryCompleteDto completeDto) {
        try {
            log.info("배송완료 처리 시작 - 주문수: {}", completeDto.getOrderNos().size());
            
            List<String> successOrders = new ArrayList<>();
            List<String> failedOrders = new ArrayList<>();
            
            for (String orderNo : completeDto.getOrderNos()) {
                try {
                    Order order = orderRepository.findByOrderNo(orderNo);
                    if (order == null) {
                        failedOrders.add(orderNo + " (존재하지 않는 주문)");
                        continue;
                    }
                    
                    // 배송요청 또는 배송중 상태 확인
                    if (!"배송요청".equals(order.getDeliveryStatus()) && !"배송중".equals(order.getDeliveryStatus())) {
                        failedOrders.add(orderNo + " (배송요청/배송중 상태가 아님: " + order.getDeliveryStatus() + ")");
                        continue;
                    }
                    
                    // 배송상태 변경 (재고처리 없음)
                    order.setDeliveryStatus("배송완료");
                    if (order.getDeliveryDt() == null) {
                        order.setDeliveryDt(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                    }
                    
                    orderRepository.save(order);
                    successOrders.add(orderNo);
                    
                    log.info("배송완료 처리 완료 - orderNo: {}", orderNo);
                    
                } catch (Exception e) {
                    log.error("배송완료 처리 중 오류 발생 - orderNo: {}", orderNo, e);
                    failedOrders.add(orderNo + " (처리 오류: " + e.getMessage() + ")");
                }
            }
            
            String message = String.format("배송완료 처리 완료 - 성공: %d건, 실패: %d건", 
                    successOrders.size(), failedOrders.size());
            
            if (!failedOrders.isEmpty()) {
                message += " | 실패 주문: " + String.join(", ", failedOrders);
            }
            
            log.info(message);
            return RespDto.success(message, successOrders);
            
        } catch (Exception e) {
            log.error("배송완료 처리 중 오류 발생", e);
            return RespDto.fail("배송완료 처리 중 오류가 발생했습니다.");
        }
    }
    
    // =================== 재고처리 전용 메서드들 ===================
    
    /**
     * 재고 차감 처리 (배송시작 시)
     */
    private void processInventoryDeduction(OrderItem item) {
        // 1. 창고재고 차감
        Optional<WarehouseItems> warehouseItemOpt = warehouseItemsRepository
                .findByWarehouseCodeAndItemCode(item.getReleaseWarehouseCode(), item.getItemCode());
        
        if (warehouseItemOpt.isPresent()) {
            WarehouseItems warehouseItem = warehouseItemOpt.get();
            
            // 재고 부족 체크
            if (warehouseItem.getCurrentQuantity() < item.getOrderQty()) {
                throw new RuntimeException(String.format(
                    "재고 부족: 품목코드 %d, 주문수량 %d, 현재고 %d", 
                    item.getItemCode(), item.getOrderQty(), warehouseItem.getCurrentQuantity()
                ));
            }
            
            // 창고재고 차감
            warehouseItem.setCurrentQuantity(warehouseItem.getCurrentQuantity() - item.getOrderQty());
            warehouseItemsRepository.save(warehouseItem);
            
            // 2. 재고수불부 기록 (출고)
            createInventoryTransaction(item.getReleaseWarehouseCode(), item.getItemCode(), 
                    warehouseItem.getWarehouseItemCode(), item.getOrderQty(), item.getOrderUnitPrice(), 
                    "출고", "배송시작 - 재고출고");
            
            // 3. 월별재고마감 업데이트 (출고량 증가)
            updateMonthlyInventoryClosing(item.getReleaseWarehouseCode(), item.getItemCode(), 
                    item.getOrderQty(), item.getOrderQty() * item.getOrderUnitPrice());
            
            log.info("재고 차감 완료 - 품목코드: {}, 차감수량: {}", item.getItemCode(), item.getOrderQty());
            
        } else {
            log.warn("창고품목을 찾을 수 없어 재고 차감을 스킵합니다 - 창고코드: {}, 품목코드: {}", 
                    item.getReleaseWarehouseCode(), item.getItemCode());
        }
    }
    
    /**
     * 재고 복원 처리 (배송취소 시)
     */
    private void processInventoryRestoration(OrderItem item) {
        // 1. 창고재고 복원
        Optional<WarehouseItems> warehouseItemOpt = warehouseItemsRepository
                .findByWarehouseCodeAndItemCode(item.getReleaseWarehouseCode(), item.getItemCode());
        
        if (warehouseItemOpt.isPresent()) {
            WarehouseItems warehouseItem = warehouseItemOpt.get();
            
            // 창고재고 복원
            warehouseItem.setCurrentQuantity(warehouseItem.getCurrentQuantity() + item.getOrderQty());
            warehouseItemsRepository.save(warehouseItem);
            
            // 2. 재고수불부 기록 (입고)
            createInventoryTransaction(item.getReleaseWarehouseCode(), item.getItemCode(), 
                    warehouseItem.getWarehouseItemCode(), item.getOrderQty(), item.getOrderUnitPrice(), 
                    "입고", "배송취소 - 재고복원");
            
            // 3. 월별재고마감 업데이트 (출고량 감소)
            updateMonthlyInventoryClosing(item.getReleaseWarehouseCode(), item.getItemCode(), 
                    -item.getOrderQty(), -(item.getOrderQty() * item.getOrderUnitPrice()));
            
            log.info("재고 복원 완료 - 품목코드: {}, 복원수량: {}", item.getItemCode(), item.getOrderQty());
            
        } else {
            log.warn("창고품목을 찾을 수 없어 재고 복원을 스킵합니다 - 창고코드: {}, 품목코드: {}", 
                    item.getReleaseWarehouseCode(), item.getItemCode());
        }
    }
    
    /**
     * 재고수불부 기록 생성
     */
    private void createInventoryTransaction(Integer warehouseCode, Integer itemCode, 
            Integer warehouseItemCode, Integer quantity, Integer unitPrice, 
            String transactionType, String description) {
        
        InventoryTransactions transaction = InventoryTransactions.builder()
                .warehouseCode(warehouseCode)
                .warehouseItemCode(warehouseItemCode)
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
            Integer outQuantityChange, Integer outAmountChange) {
        
        String closingYm = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        
        Optional<MonthlyInventoryClosing> closingOpt = monthlyInventoryClosingRepository
                .findByWarehouseCodeAndItemCodeAndClosingYm(warehouseCode, itemCode, closingYm);
        
        if (closingOpt.isPresent()) {
            MonthlyInventoryClosing closing = closingOpt.get();
            
            // 출고량/출고금액 업데이트
            closing.setOutQuantity(closing.getOutQuantity() + outQuantityChange);
            closing.setOutAmount(closing.getOutAmount() + outAmountChange);
            
            // 계산수량/계산금액 재계산 (이월+입고-출고)
            Integer calQuantity = closing.getOpeningQuantity() + closing.getInQuantity() - closing.getOutQuantity();
            Integer calAmount = closing.getOpeningAmount() + closing.getInAmount() - closing.getOutAmount();
            
            closing.setCalQuantity(calQuantity);
            closing.setCalAmount(calAmount);
            
            monthlyInventoryClosingRepository.save(closing);
            
        } else {
            log.warn("월별재고마감 데이터를 찾을 수 없습니다 - 창고코드: {}, 품목코드: {}, 마감년월: {}", 
                    warehouseCode, itemCode, closingYm);
        }
    }
}