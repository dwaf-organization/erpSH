package com.inc.sh.service.app;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.returnRegistration.respDto.AppReturnOrderItemRespDto;
import com.inc.sh.dto.returnRegistration.reqDto.AppReturnRequestReqDto;
import com.inc.sh.dto.returnRegistration.respDto.AppReturnRequestRespDto;
import com.inc.sh.dto.returnRegistration.reqDto.AppReturnHistoryReqDto;
import com.inc.sh.dto.returnRegistration.respDto.AppReturnHistoryRespDto;
import com.inc.sh.dto.returnRegistration.respDto.AppReturnCancelRespDto;
import com.inc.sh.dto.returnRegistration.respDto.AppReturnHistoryListRespDto;
import com.inc.sh.entity.Customer;
import com.inc.sh.entity.Order;
import com.inc.sh.entity.OrderItem;
import com.inc.sh.entity.OrderItemReturnStatus;
import com.inc.sh.entity.Return;
import com.inc.sh.entity.Warehouse;
import com.inc.sh.repository.OrderRepository;
import com.inc.sh.repository.CustomerRepository;
import com.inc.sh.repository.DistCenterRepository;
import com.inc.sh.repository.OrderItemRepository;
import com.inc.sh.repository.OrderItemReturnStatusRepository;
import com.inc.sh.repository.ReturnRepository;
import com.inc.sh.repository.WarehouseRepository;

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
public class AppReturnService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReturnRepository returnRepository;
    private final WarehouseRepository warehouseRepository;
    private final CustomerRepository customerRepository;
    private final DistCenterRepository distCenterRepository;
    
    // ✅ 반품가능 주문품목 뷰 조회용 Repository 추가
    private final OrderItemReturnStatusRepository orderItemReturnStatusRepository;
    
    /**
     * [앱] 반품가능한 주문번호 조회 (✅ 뷰 사용)
     */
    public RespDto<List<String>> getAvailableOrders(Integer customerCode) {
        try {
            log.info("[앱] 반품가능 주문번호 조회 시작 - customerCode: {}", customerCode);
            
            // ✅ 뷰에서 반품가능한 주문번호 직접 조회
            List<String> orderNumbers = orderItemReturnStatusRepository
                    .findAvailableOrderNumbers(customerCode);
            
            log.info("[앱] 반품가능 주문번호 조회 완료 - customerCode: {}, 조회건수: {}", 
                    customerCode, orderNumbers.size());
            
            return RespDto.success("반품가능 주문 조회 성공", orderNumbers);
            
        } catch (Exception e) {
            log.error("반품가능 주문번호 조회 실패 - customerCode: {}", customerCode, e);
            return RespDto.fail("반품가능 주문 조회 중 오류가 발생했습니다");
        }
    }
    
    /**
     * [앱] 반품가능한 주문품목 조회 (✅ 뷰 사용)
     */
    public RespDto<List<AppReturnOrderItemRespDto>> getOrderItems(String orderNo, Integer customerCode) {
        try {
            // 1. 주문 권한 확인 (Order 조회로 대체)
            Order order = orderRepository.findByOrderNoAndCustomerCode(orderNo, customerCode);
            if (order == null) {
                return RespDto.fail("주문번호 또는 거래처코드가 일치하지 않습니다");
            }
            
            // ✅ 2. 뷰에서 반품가능한 주문품목 직접 조회
            List<OrderItemReturnStatus> returnableItems = orderItemReturnStatusRepository
                    .findByOrderNoAndCustomerCodeOrderByItemCode(orderNo, customerCode);
            
            // 3. DTO 변환
            List<AppReturnOrderItemRespDto> response = returnableItems.stream()
                    .map(this::convertViewToOrderItemDto)
                    .toList();
            
            log.info("[앱] 반품가능 주문품목 조회 완료 - orderNo: {}, customerCode: {}, 조회건수: {}", 
                    orderNo, customerCode, response.size());
            
            return RespDto.success("주문품목 조회 성공", response);
            
        } catch (Exception e) {
            log.error("반품가능 주문품목 조회 실패 - orderNo: {}, customerCode: {}", orderNo, customerCode, e);
            return RespDto.fail("주문품목 조회 중 오류가 발생했습니다");
        }
    }
    
    /**
     * [앱] 반품신청
     */
    @Transactional
    public RespDto<AppReturnRequestRespDto> requestReturn(AppReturnRequestReqDto request) {
        try {
            // 1. 주문 권한 확인
            Order order = orderRepository.findByOrderNoAndCustomerCode(request.getOrderNo(), request.getCustomerCode());
            if (order == null) {
                return RespDto.fail("주문번호 또는 거래처코드가 일치하지 않습니다");
            }
            
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // 2. 중복 반품 체크 (임시로 주석 처리 - Repository 메서드 구현 후 활성화)
            // if (returnRepository.existsByOrderNoAndItemCodeAndReceiveWarehouseCode(
            //         request.getOrderNo(), request.getItemCode(), request.getWarehouseCode())) {
            //     return RespDto.fail("이미 반품신청된 품목입니다: " + request.getItemName());
            // }
            
            // 3. 주문품목 정보 조회 (orderNo와 itemCode로 조회 후 창고코드 확인)
            List<OrderItem> orderItemList = orderItemRepository.findByOrderNo(request.getOrderNo());
            OrderItem orderItem = orderItemList.stream()
                    .filter(item -> item.getItemCode().equals(request.getItemCode()) 
                                 && item.getReleaseWarehouseCode().equals(request.getWarehouseCode()))
                    .findFirst()
                    .orElse(null);
            
            if (orderItem == null) {
                return RespDto.fail("주문품목 정보를 찾을 수 없습니다: " + request.getItemName());
            }
            
            // 4. 반품번호 생성 (RET-YYYYMMDD-0001 형식)
            String returnNo = generateReturnNo();
            
            Warehouse warehouse = warehouseRepository.findByWarehouseCode(request.getWarehouseCode());
            
            
            // 5. Return 엔티티 생성 (OrderItem 기존 금액으로 수량 비율 계산)
            Return returnEntity = Return.builder()
                    .returnNo(returnNo)
                    .returnCustomerCode(request.getCustomerCode())
                    .orderNo(request.getOrderNo())
                    .orderItemCode(request.getOrderItemCode())
                    .itemCode(request.getItemCode())
                    .receiveWarehouseCode(request.getWarehouseCode())
                    .warehouseName(warehouse.getWarehouseName())
                    .returnCustomerName(order.getCustomerName())
                    .returnRequestDt(today)
                    .itemName(orderItem.getItemName())
                    .specification(orderItem.getSpecification())
                    .unit(orderItem.getUnit())
                    .qty(request.getReturnQty())
                    .priceType(orderItem.getPriceType())
                    .unitPrice(orderItem.getOrderUnitPrice())
                    .supplyPrice(calculateAmountByRatio(orderItem.getSupplyAmt(), request.getReturnQty(), orderItem.getOrderQty()))
                    .vatAmt(calculateAmountByRatio(orderItem.getVatAmt(), request.getReturnQty(), orderItem.getOrderQty()))
                    .totalAmt(calculateAmountByRatio(orderItem.getTotalAmt(), request.getReturnQty(), orderItem.getOrderQty()))
                    .returnMessage(request.getReturnMessage())
                    .progressStatus("미승인")
                    .build();
            
            returnRepository.save(returnEntity);
            
            // ✅ 6. order_item.returned_qty 업데이트 (반품신청 즉시 반영)
            orderItemRepository.updateReturnedQty(request.getOrderItemCode(), request.getReturnQty());
            
            // 7. 응답 데이터 생성
            AppReturnRequestRespDto response = AppReturnRequestRespDto.builder()
                    .returnNo(returnNo)
                    .returnMessage("반품신청이 완료되었습니다")
                    .build();
            
            log.info("[앱] 반품신청 완료 - orderNo: {}, customerCode: {}, returnNo: {}", 
                    request.getOrderNo(), request.getCustomerCode(), returnNo);
            
            return RespDto.success("반품신청이 완료되었습니다", response);
            
        } catch (Exception e) {
            log.error("반품신청 실패 - orderNo: {}, customerCode: {}", request.getOrderNo(), request.getCustomerCode(), e);
            return RespDto.fail("반품신청 중 오류가 발생했습니다");
        }
    }
    
    /**
     * [앱] 반품내역조회 (페이징)
     */
    public RespDto<AppReturnHistoryListRespDto> getReturnHistory(AppReturnHistoryReqDto request) {
        try {
            // 1. 거래처별 반품 전체 조회
            List<Return> allReturns = returnRepository.findByReturnCustomerCode(request.getCustomerCode());
            
            // 2. 날짜 범위 필터링 및 정렬
            List<Return> filteredReturns = allReturns.stream()
                    .filter(returnEntity -> {
                        String returnDate = returnEntity.getReturnRequestDt();
                        return returnDate.compareTo(request.getStartDate()) >= 0 
                            && returnDate.compareTo(request.getEndDate()) <= 0;
                    })
                    .sorted((r1, r2) -> {
                        // 반품요청일 최신순 정렬
                        int dateCompare = r2.getReturnRequestDt().compareTo(r1.getReturnRequestDt());
                        if (dateCompare != 0) return dateCompare;
                        return r2.getReturnNo().compareTo(r1.getReturnNo());
                    })
                    .toList();
            
            // 3. 총 건수
            long totalCount = filteredReturns.size();
            
            // 4. 페이징 처리
            int startIndex = request.getPage() * request.getSize();
            int endIndex = Math.min(startIndex + request.getSize(), (int) totalCount);
            
            List<Return> pagedReturns = (startIndex < totalCount) 
                    ? filteredReturns.subList(startIndex, endIndex)
                    : List.of();
            
            // 5. DTO 변환
            List<AppReturnHistoryRespDto> returnList = pagedReturns.stream()
                    .map(this::convertToReturnHistoryDto)
                    .toList();
            
            // 6. 페이징 정보 계산
            int totalPages = (int) Math.ceil((double) totalCount / request.getSize());
            boolean hasNext = (request.getPage() + 1) < totalPages;
            
            // 7. 응답 데이터 생성
            AppReturnHistoryListRespDto response = AppReturnHistoryListRespDto.builder()
                    .totalCount(totalCount)
                    .currentPage(request.getPage())
                    .totalPages(totalPages)
                    .hasNext(hasNext)
                    .returnList(returnList)
                    .build();
            
            log.info("[앱] 반품내역조회 완료 - customerCode: {}, 기간: {} ~ {}, 조회건수: {}, 총건수: {}", 
                    request.getCustomerCode(), request.getStartDate(), request.getEndDate(), 
                    returnList.size(), totalCount);
            
            return RespDto.success("반품내역 조회 성공", response);
            
        } catch (Exception e) {
            log.error("반품내역조회 실패 - customerCode: {}, 기간: {} ~ {}", 
                    request.getCustomerCode(), request.getStartDate(), request.getEndDate(), e);
            return RespDto.fail("반품내역 조회 중 오류가 발생했습니다");
        }
    }
    
    /**
     * ✅ OrderItemReturnStatus(뷰) -> AppReturnOrderItemRespDto 변환
     */
    private AppReturnOrderItemRespDto convertViewToOrderItemDto(OrderItemReturnStatus viewItem) {
        // 실제 OrderItem에서 추가 정보 조회 (필요한 필드들)
        OrderItem orderItem = orderItemRepository.findByOrderItemCode(viewItem.getOrderItemCode());
        
        // 창고명 조회
        String warehouseName = null;
        if (orderItem != null && orderItem.getReleaseWarehouseCode() != null) {
            warehouseName = warehouseRepository.findById(orderItem.getReleaseWarehouseCode())
                    .map(warehouse -> warehouse.getWarehouseName())
                    .orElse(null);
        }
        
        // 거래처로부터 물류센터 정보 조회
        Integer distCenterCode = null;
        String distCenterName = null;
        if (viewItem.getCustomerCode() != null) {
            Customer customer = customerRepository.findByCustomerCode(viewItem.getCustomerCode());
            if (customer != null && customer.getDistCenterCode() != null) {
                distCenterCode = customer.getDistCenterCode();
                distCenterName = distCenterRepository.findById(distCenterCode)
                        .map(distCenter -> distCenter.getDistCenterName())
                        .orElse(null);
            }
        }
        
        return AppReturnOrderItemRespDto.builder()
                .customerCode(viewItem.getCustomerCode())
                .customerName(viewItem.getCustomerName())
                .orderItemCode(viewItem.getOrderItemCode())
                .orderNo(viewItem.getOrderNo())
                .itemCode(viewItem.getItemCode())
                .releaseWarehouseCode(orderItem != null ? orderItem.getReleaseWarehouseCode() : null)
                .itemName(viewItem.getItemName())
                .specification(orderItem != null ? orderItem.getSpecification() : null)
                .unit(orderItem != null ? orderItem.getUnit() : null)
                .priceType(orderItem != null ? orderItem.getPriceType() : null)
                .orderUnitPrice(orderItem != null ? orderItem.getOrderUnitPrice() : null)
                .currentStockQty(orderItem != null ? orderItem.getCurrentStockQty() : null)
                .orderQty(viewItem.getOrderQty()) // 뷰에서 가져옴
                .taxTarget(orderItem != null ? orderItem.getTaxTarget() : null)
                .warehouseName(warehouseName)
                .taxableAmt(orderItem != null ? orderItem.getTaxableAmt() : null)
                .taxFreeAmt(orderItem != null ? orderItem.getTaxFreeAmt() : null)
                .supplyAmt(orderItem != null ? orderItem.getSupplyAmt() : null)
                .vatAmt(orderItem != null ? orderItem.getVatAmt() : null)
                .totalAmt(orderItem != null ? orderItem.getTotalAmt() : null)
                .totalQty(orderItem != null ? orderItem.getTotalQty() : null)
                // ✅ 물류센터 정보 추가 (AppReturnOrderItemRespDto에 필드가 있다면)
                .distCenterCode(distCenterCode)
                .distCenterName(distCenterName)
                .availableReturnQty(viewItem.getAvailableReturnQty())
                .build();
    }
    
    /**
     * OrderItem -> AppReturnOrderItemRespDto 변환 (기존 메서드 유지)
     */
    private AppReturnOrderItemRespDto convertToOrderItemDto(OrderItem orderItem) {
        return AppReturnOrderItemRespDto.builder()
                .orderItemCode(orderItem.getOrderItemCode())
                .orderNo(orderItem.getOrderNo())
                .itemCode(orderItem.getItemCode())
                .releaseWarehouseCode(orderItem.getReleaseWarehouseCode())
                .itemName(orderItem.getItemName())
                .specification(orderItem.getSpecification())
                .unit(orderItem.getUnit())
                .priceType(orderItem.getPriceType())
                .orderUnitPrice(orderItem.getOrderUnitPrice())
                .currentStockQty(orderItem.getCurrentStockQty())
                .orderQty(orderItem.getOrderQty())
                .taxTarget(orderItem.getTaxTarget())
                .warehouseName(orderItem.getWarehouseName())
                .taxableAmt(orderItem.getTaxableAmt())
                .taxFreeAmt(orderItem.getTaxFreeAmt())
                .supplyAmt(orderItem.getSupplyAmt())
                .vatAmt(orderItem.getVatAmt())
                .totalAmt(orderItem.getTotalAmt())
                .totalQty(orderItem.getTotalQty())
                .build();
    }
    
    /**
     * Return -> AppReturnHistoryRespDto 변환
     */
    private AppReturnHistoryRespDto convertToReturnHistoryDto(Return returnEntity) {
        return AppReturnHistoryRespDto.builder()
                .returnNo(returnEntity.getReturnNo())
                .returnRequestDt(returnEntity.getReturnRequestDt())
                .itemCode(returnEntity.getItemCode())
                .itemName(returnEntity.getItemName())
                .specification(returnEntity.getSpecification())
                .unit(returnEntity.getUnit())
                .priceType(returnEntity.getPriceType())
                .unitPrice(returnEntity.getUnitPrice())
                .qty(returnEntity.getQty())
                .returnMessage(returnEntity.getReturnMessage())
                .progressStatus(returnEntity.getProgressStatus())
                .totalAmt(returnEntity.getTotalAmt())
                .build();
    }
    
    /**
     * 반품번호 생성 (RET + YYYYMMDD + 001 형태)
     */
    private String generateReturnNo() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String datePrefix = "RET" + today;
        String latestReturnNo = returnRepository.findLatestReturnNoByDate(datePrefix);

        int sequence = 1;
        if (latestReturnNo != null && latestReturnNo.length() >= 14) {
            String sequencePart = latestReturnNo.substring(11);
            sequence = Integer.parseInt(sequencePart) + 1;
        }

        return String.format("%s%03d", datePrefix, sequence);
    }
    
    /**
     * [앱] 반품취소
     */
    @Transactional
    public RespDto<AppReturnCancelRespDto> cancelReturn(String returnNo, Integer customerCode) {
        try {
            // 1. 반품 존재 여부 및 권한 확인
            Return returnEntity = returnRepository.findByReturnNo(returnNo);
            if (returnEntity == null) {
                return RespDto.fail("존재하지 않는 반품번호입니다");
            }
            
            // 2. 거래처 권한 확인
            if (!returnEntity.getReturnCustomerCode().equals(customerCode)) {
                return RespDto.fail("반품취소 권한이 없습니다");
            }
            
            // 3. 진행상태 확인
            if ("승인".equals(returnEntity.getProgressStatus())) {
                return RespDto.fail("이미 승인되어 취소가 불가합니다");
            }
            
            // 4. 미승인 상태인 경우에만 삭제 처리
            if ("미승인".equals(returnEntity.getProgressStatus())) {
                
                // ✅ 4-1. order_item.returned_qty 복원 (반품신청했던 수량을 차감)
                orderItemRepository.updateReturnedQty(returnEntity.getOrderItemCode(), -returnEntity.getQty());
                
                returnRepository.delete(returnEntity);
                
                // 5. 응답 데이터 생성
                AppReturnCancelRespDto response = AppReturnCancelRespDto.builder()
                        .returnNo(returnNo)
                        .cancelMessage("반품취소가 완료되었습니다")
                        .build();
                
                log.info("[앱] 반품취소 완료 - returnNo: {}, customerCode: {}", returnNo, customerCode);
                
                return RespDto.success("반품취소가 완료되었습니다", response);
            } else {
                // 6. 기타 상태값인 경우
                return RespDto.fail("현재 상태에서는 취소할 수 없습니다. 상태: " + returnEntity.getProgressStatus());
            }
            
        } catch (Exception e) {
            log.error("반품취소 실패 - returnNo: {}, customerCode: {}", returnNo, customerCode, e);
            return RespDto.fail("반품취소 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 기존 금액에서 수량 비율로 계산
     */
    private Integer calculateAmountByRatio(Integer originalAmount, Integer returnQty, Integer orderQty) {
        if (originalAmount == null || orderQty == null || orderQty == 0) {
            return 0;
        }
        return Math.round((float) originalAmount * returnQty / orderQty);
    }
}