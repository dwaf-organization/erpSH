package com.inc.sh.service;

import com.inc.sh.dto.delivery.reqDto.*;
import com.inc.sh.dto.delivery.respDto.DeliveryBatchResult;
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
    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;
    
    /**
     * 배송 주문 목록 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<OrderRespDto>> getDeliveryList(DeliverySearchDto searchDto) {
        try {
            log.info("배송 주문 목록 조회 시작 - deliveryRequestDt: {}, customerCode: {}, orderNo: {}, deliveryStatus: {}, hqCode: {}", 
                    searchDto.getDeliveryRequestDt(), searchDto.getCustomerCode(), 
                    searchDto.getOrderNo(), searchDto.getDeliveryStatus(), searchDto.getHqCode());
            
            List<Order> orders = orderRepository.findByDeliverySearchConditionsWithHqCode(
                    searchDto.getDeliveryRequestDt(),
                    searchDto.getCustomerCode(),
                    searchDto.getOrderNo(),
                    searchDto.getDeliveryStatus(),
                    searchDto.getHqCode()
            );
            
            List<OrderRespDto> responseList = orders.stream()
                    .map(OrderRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("배송 주문 목록 조회 완료 - hqCode: {}, 조회 건수: {}", searchDto.getHqCode(), responseList.size());
            return RespDto.success("배송 주문 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("배송 주문 목록 조회 중 오류 발생 - hqCode: {}", searchDto.getHqCode(), e);
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
    public RespDto<DeliveryBatchResult> startDelivery(DeliveryStartDto startDto) {
        try {
            log.info("배송시작 처리 시작 - 주문수: {}", startDto.getOrders().size());
            
            List<DeliveryBatchResult.DeliverySuccessResult> successList = new ArrayList<>();
            List<DeliveryBatchResult.DeliveryFailureResult> failureList = new ArrayList<>();
            
            for (DeliveryStartDto.DeliveryOrderDto orderDto : startDto.getOrders()) {
                try {
                    Order order = orderRepository.findByOrderNo(orderDto.getOrderNo());
                    if (order == null) {
                        failureList.add(DeliveryBatchResult.DeliveryFailureResult.builder()
                                .orderNo(orderDto.getOrderNo())
                                .reason("존재하지 않는 주문입니다")
                                .build());
                        continue;
                    }
                    
                    // 배송요청 상태 확인
                    if (!"배송요청".equals(order.getDeliveryStatus())) {
                        failureList.add(DeliveryBatchResult.DeliveryFailureResult.builder()
                                .orderNo(orderDto.getOrderNo())
                                .reason("배송요청 상태가 아닙니다. 현재상태: " + order.getDeliveryStatus())
                                .build());
                        continue;
                    }
                    
                    // 재고 차감 처리 (주문품목별로)
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
                    
                    successList.add(DeliveryBatchResult.DeliverySuccessResult.builder()
                            .orderNo(orderDto.getOrderNo())
                            .customerName(order.getCustomerName())
                            .deliveryStatus("배송중")
                            .message("배송시작 완료 (재고차감 포함)")
                            .build());
                    
                    log.info("배송시작 처리 완료 (재고차감 포함) - orderNo: {}", orderDto.getOrderNo());
                    
                } catch (Exception e) {
                    log.error("배송시작 처리 중 오류 발생 - orderNo: {}", orderDto.getOrderNo(), e);
                    failureList.add(DeliveryBatchResult.DeliveryFailureResult.builder()
                            .orderNo(orderDto.getOrderNo())
                            .reason("처리 중 오류 발생: " + e.getMessage())
                            .build());
                }
            }
            
            // 배치 결과 생성
            DeliveryBatchResult batchResult = DeliveryBatchResult.builder()
                    .totalCount(startDto.getOrders().size())
                    .successCount(successList.size())
                    .failCount(failureList.size())
                    .successData(successList)
                    .failData(failureList)
                    .build();
            
            String message = String.format("배송시작 처리 완료 - 성공: %d건, 실패: %d건", 
                    batchResult.getSuccessCount(), batchResult.getFailCount());
            
            log.info("배송시작 배치 처리 완료 - 총:{}건, 성공:{}건, 실패:{}건", 
                    batchResult.getTotalCount(), batchResult.getSuccessCount(), batchResult.getFailCount());
            
            return RespDto.success(message, batchResult);
            
        } catch (Exception e) {
            log.error("배송시작 처리 중 오류 발생", e);
            return RespDto.fail("배송시작 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 배송취소 (배송중 → 배송요청) + 재고 복원
     */
    @Transactional
    public RespDto<DeliveryBatchResult> cancelDelivery(DeliveryCancelDto cancelDto) {
        try {
            log.info("배송취소 처리 시작 - 주문수: {}", cancelDto.getOrderNos().size());
            
            List<DeliveryBatchResult.DeliverySuccessResult> successList = new ArrayList<>();
            List<DeliveryBatchResult.DeliveryFailureResult> failureList = new ArrayList<>();
            
            for (String orderNo : cancelDto.getOrderNos()) {
                try {
                    Order order = orderRepository.findByOrderNo(orderNo);
                    if (order == null) {
                        failureList.add(DeliveryBatchResult.DeliveryFailureResult.builder()
                                .orderNo(orderNo)
                                .reason("존재하지 않는 주문입니다")
                                .build());
                        continue;
                    }
                    
                    // 배송중 상태 확인
                    if (!"배송중".equals(order.getDeliveryStatus())) {
                        failureList.add(DeliveryBatchResult.DeliveryFailureResult.builder()
                                .orderNo(orderNo)
                                .reason("배송중 상태가 아닙니다. 현재상태: " + order.getDeliveryStatus())
                                .build());
                        continue;
                    }
                    
                    // 재고 복원 처리 (주문품목별로)
                    List<OrderItem> orderItems = orderItemRepository.findByOrderNo(orderNo);
                    for (OrderItem item : orderItems) {
                        processInventoryRestoration(item);
                    }
                    
                    // 배송상태 변경
                    order.setDeliveryStatus("배송요청");
                    order.setDeliveryDt(null); // 배송일자 초기화
                    
                    orderRepository.save(order);
                    
                    successList.add(DeliveryBatchResult.DeliverySuccessResult.builder()
                            .orderNo(orderNo)
                            .customerName(order.getCustomerName())
                            .deliveryStatus("배송요청")
                            .message("배송취소 완료 (재고복원 포함)")
                            .build());
                    
                    log.info("배송취소 처리 완료 (재고복원 포함) - orderNo: {}", orderNo);
                    
                } catch (Exception e) {
                    log.error("배송취소 처리 중 오류 발생 - orderNo: {}", orderNo, e);
                    failureList.add(DeliveryBatchResult.DeliveryFailureResult.builder()
                            .orderNo(orderNo)
                            .reason("처리 중 오류 발생: " + e.getMessage())
                            .build());
                }
            }
            
            // 배치 결과 생성
            DeliveryBatchResult batchResult = DeliveryBatchResult.builder()
                    .totalCount(cancelDto.getOrderNos().size())
                    .successCount(successList.size())
                    .failCount(failureList.size())
                    .successData(successList)
                    .failData(failureList)
                    .build();
            
            String message = String.format("배송취소 처리 완료 - 성공: %d건, 실패: %d건", 
                    batchResult.getSuccessCount(), batchResult.getFailCount());
            
            log.info("배송취소 배치 처리 완료 - 총:{}건, 성공:{}건, 실패:{}건", 
                    batchResult.getTotalCount(), batchResult.getSuccessCount(), batchResult.getFailCount());
            
            return RespDto.success(message, batchResult);
            
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
    public RespDto<DeliveryBatchResult> completeDelivery(DeliveryCompleteDto completeDto) {
        try {
            log.info("배송완료 처리 시작 - 주문수: {}", completeDto.getOrderNos().size());
            
            List<DeliveryBatchResult.DeliverySuccessResult> successList = new ArrayList<>();
            List<DeliveryBatchResult.DeliveryFailureResult> failureList = new ArrayList<>();
            
            for (String orderNo : completeDto.getOrderNos()) {
                try {
                    Order order = orderRepository.findByOrderNo(orderNo);
                    if (order == null) {
                        failureList.add(DeliveryBatchResult.DeliveryFailureResult.builder()
                                .orderNo(orderNo)
                                .reason("존재하지 않는 주문입니다")
                                .build());
                        continue;
                    }
                    
                    // 배송 가능 상태 확인 (배송요청 또는 배송중만 배송완료로 변경 가능)
                    if (!"배송요청".equals(order.getDeliveryStatus()) && !"배송중".equals(order.getDeliveryStatus())) {
                        failureList.add(DeliveryBatchResult.DeliveryFailureResult.builder()
                                .orderNo(orderNo)
                                .reason("배송완료로 변경할 수 없는 상태입니다. 현재상태: " + order.getDeliveryStatus())
                                .build());
                        continue;
                    }
                    
                    // 배송상태 변경
                    order.setDeliveryStatus("배송완료");
                    
                    // 배송일자가 없으면 오늘 날짜로 설정
                    if (order.getDeliveryDt() == null) {
                        order.setDeliveryDt(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                    }
                    
                    orderRepository.save(order);
                    
                    successList.add(DeliveryBatchResult.DeliverySuccessResult.builder()
                            .orderNo(orderNo)
                            .customerName(order.getCustomerName())
                            .deliveryStatus("배송완료")
                            .message("배송완료 처리 완료")
                            .build());
                    
                    log.info("배송완료 처리 완료 - orderNo: {}", orderNo);
                    
                } catch (Exception e) {
                    log.error("배송완료 처리 중 오류 발생 - orderNo: {}", orderNo, e);
                    failureList.add(DeliveryBatchResult.DeliveryFailureResult.builder()
                            .orderNo(orderNo)
                            .reason("처리 중 오류 발생: " + e.getMessage())
                            .build());
                }
            }
            
            // 배치 결과 생성
            DeliveryBatchResult batchResult = DeliveryBatchResult.builder()
                    .totalCount(completeDto.getOrderNos().size())
                    .successCount(successList.size())
                    .failCount(failureList.size())
                    .successData(successList)
                    .failData(failureList)
                    .build();
            
            String message = String.format("배송완료 처리 완료 - 성공: %d건, 실패: %d건", 
                    batchResult.getSuccessCount(), batchResult.getFailCount());
            
            log.info("배송완료 배치 처리 완료 - 총:{}건, 성공:{}건, 실패:{}건", 
                    batchResult.getTotalCount(), batchResult.getSuccessCount(), batchResult.getFailCount());
            
            return RespDto.success(message, batchResult);
            
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
     * 월별재고마감 업데이트 (✅ 재고등록 안된 품목은 배송처리 불가)
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
            
            log.info("월별재고마감 업데이트 완료 - 창고코드: {}, 품목코드: {}, 출고량 변화: {}", 
                    warehouseCode, itemCode, outQuantityChange);
            
        } else {
            // ✅ 재고등록 안된 품목은 배송처리 불가
            String warehouseName = getWarehouseNameSafely(warehouseCode);
            String itemName = getItemNameSafely(itemCode);
            
            throw new RuntimeException(String.format(
                "재고등록이 필요합니다. 품목명: %s, 창고: %s (품목코드: %d, 창고코드: %d, 마감년월: %s)", 
                itemName, warehouseName, itemCode, warehouseCode, closingYm));
        }
    }
    
    /**
     * 창고명 안전 조회
     */
    private String getWarehouseNameSafely(Integer warehouseCode) {
        try {
            return warehouseRepository.findById(warehouseCode)
                    .map(warehouse -> warehouse.getWarehouseName())
                    .orElse("알 수 없는 창고");
        } catch (Exception e) {
            return "창고코드: " + warehouseCode;
        }
    }
    
    /**
     * 품목명 안전 조회
     */
    private String getItemNameSafely(Integer itemCode) {
        try {
            return itemRepository.findById(itemCode)
                    .map(item -> item.getItemName())
                    .orElse("알 수 없는 품목");
        } catch (Exception e) {
            return "품목코드: " + itemCode;
        }
    }
}