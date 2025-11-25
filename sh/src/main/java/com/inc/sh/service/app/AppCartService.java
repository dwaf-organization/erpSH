package com.inc.sh.service.app;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.cart.reqDto.CartAddReqDto;
import com.inc.sh.dto.cart.respDto.CartRespDto;
import com.inc.sh.entity.*;
import com.inc.sh.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppCartService {
    
    private final CustomerCartRepository customerCartRepository;
    private final ItemRepository itemRepository;
    private final ItemCustomerPriceRepository itemCustomerPriceRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseItemsRepository warehouseItemsRepository;
    
    /**
     * 장바구니 담기
     */
    @Transactional
    public RespDto<Integer> addToCart(CartAddReqDto request) {
        try {
            // 중복 확인
            boolean exists = customerCartRepository.existsByCustomerCodeAndCustomerUserCodeAndItemCodeAndWarehouseCode(
                request.getCustomerCode(),
                request.getCustomerUserCode(),
                request.getItemCode(),
                request.getWarehouseCode()
            );
            
            if (exists) {
                return RespDto.fail("장바구니에 품목이 존재합니다");
            }
            
            // Item 정보 조회
            Item item = itemRepository.findByItemCode(request.getItemCode());
            if (item == null) {
                return RespDto.fail("존재하지 않는 품목입니다");
            }
            
            // Warehouse 정보 조회
            Warehouse warehouse = warehouseRepository.findByWarehouseCode(request.getWarehouseCode());
            if (warehouse == null) {
                return RespDto.fail("존재하지 않는 창고입니다");
            }
            
            // WarehouseItems에서 현재고량 조회
            WarehouseItems warehouseItems = warehouseItemsRepository.findByWarehouseCodeAndItemCode(
                request.getWarehouseCode(), request.getItemCode()).orElse(null);
            Integer currentStockQty = (warehouseItems != null) ? warehouseItems.getCurrentQuantity() : 0;
            
            // 거래처별 단가 확인 (item_customer_price 우선)
            Integer orderUnitPrice = item.getBasePrice(); // 기본값
            List<ItemCustomerPrice> customerPrices = itemCustomerPriceRepository.findByItemCode(request.getItemCode());
            for (ItemCustomerPrice cp : customerPrices) {
                if (cp.getCustomerCode().equals(request.getCustomerCode())) {
                    orderUnitPrice = cp.getCustomerSupplyPrice();
                    break;
                }
            }
            
            // 장바구니 추가
            CustomerCart cart = CustomerCart.builder()
                .customerUserCode(request.getCustomerUserCode())
                .customerCode(request.getCustomerCode())
                .itemCode(request.getItemCode())
                .virtualAccountCode(null) // null 처리
                .warehouseCode(request.getWarehouseCode())
                .itemName(item.getItemName())
                .specification(item.getSpecification())
                .unit(item.getPurchaseUnit())
                .priceType(item.getPriceType())
                .orderUnitPrice(orderUnitPrice)
                .currentStockQty(currentStockQty)
                .orderQty(request.getOrderQty())
                .taxTarget(item.getVatType())
                .warehouseName(warehouse.getWarehouseName())
                .description(null)
                .build();
            
            CustomerCart savedCart = customerCartRepository.save(cart);
            
            log.info("[앱] 장바구니 담기 성공 - customerCode: {}, itemCode: {}, customerCartCode: {}", 
                request.getCustomerCode(), request.getItemCode(), savedCart.getCustomerCartCode());
            
            return RespDto.success("장바구니 담기 성공", savedCart.getCustomerCartCode());
            
        } catch (Exception e) {
            log.error("장바구니 담기 실패", e);
            return RespDto.fail("장바구니 담기 실패");
        }
    }
    
    /**
     * 장바구니 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<CartRespDto>> getCartList(Integer customerCode, Integer customerUserCode) {
        try {
            List<CustomerCart> cartItems = customerCartRepository.findByCustomerCodeAndCustomerUserCode(
                customerCode, customerUserCode);
            
            List<CartRespDto> responseList = cartItems.stream()
                .map(cart -> CartRespDto.builder()
                    .customerCartCode(cart.getCustomerCartCode())
                    .customerUserCode(cart.getCustomerUserCode())
                    .customerCode(cart.getCustomerCode())
                    .itemCode(cart.getItemCode())
                    .virtualAccountCode(cart.getVirtualAccountCode())
                    .warehouseCode(cart.getWarehouseCode())
                    .itemName(cart.getItemName())
                    .specification(cart.getSpecification())
                    .unit(cart.getUnit())
                    .priceType(cart.getPriceType())
                    .orderUnitPrice(cart.getOrderUnitPrice())
                    .currentStockQty(cart.getCurrentStockQty())
                    .orderQty(cart.getOrderQty())
                    .taxTarget(cart.getTaxTarget())
                    .warehouseName(cart.getWarehouseName())
                    .description(cart.getDescription())
                    .createdAt(cart.getCreatedAt())
                    .updatedAt(cart.getUpdatedAt())
                    .build())
                .collect(Collectors.toList());
            
            log.info("[앱] 장바구니 조회 성공 - customerCode: {}, 조회건수: {}", 
                customerCode, responseList.size());
            
            return RespDto.success("장바구니 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("장바구니 조회 실패", e);
            return RespDto.fail("장바구니 조회 실패");
        }
    }
    
    /**
     * 장바구니 삭제
     */
    @Transactional
    public RespDto<Void> deleteCartItem(Integer customerCartCode) {
        try {
            if (!customerCartRepository.existsById(customerCartCode)) {
                return RespDto.fail("존재하지 않는 장바구니 항목입니다");
            }
            
            customerCartRepository.deleteById(customerCartCode);
            
            log.info("[앱] 장바구니 삭제 성공 - customerCartCode: {}", customerCartCode);
            
            return RespDto.success("장바구니 삭제 성공", null);
            
        } catch (Exception e) {
            log.error("장바구니 삭제 실패", e);
            return RespDto.fail("장바구니 삭제 실패");
        }
    }
}