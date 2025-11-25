package com.inc.sh.service.app;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.wishlist.reqDto.WishlistAddReqDto;
import com.inc.sh.dto.wishlist.respDto.WishlistRespDto;
import com.inc.sh.entity.CustomerWishlist;
import com.inc.sh.entity.Item;
import com.inc.sh.entity.ItemCustomerPrice;
import com.inc.sh.entity.Warehouse;
import com.inc.sh.repository.CustomerWishlistRepository;
import com.inc.sh.repository.ItemCustomerPriceRepository;
import com.inc.sh.repository.ItemRepository;
import com.inc.sh.repository.WarehouseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppWishlistService {
    
    private final CustomerWishlistRepository customerWishlistRepository;
    private final ItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;
    private final ItemCustomerPriceRepository itemCustomerPriceRepository;
    
    /**
     * 위시리스트 추가
     */
    @Transactional
    public RespDto<WishlistRespDto> addWishlist(WishlistAddReqDto request) {
        try {
            // 중복 확인 (기존 Repository 메서드 사용)
            boolean exists = customerWishlistRepository.existsByCustomerCodeAndCustomerUserCodeAndItemCode(
                    request.getCustomerCode(), 
                    request.getCustomerUserCode(), 
                    request.getItemCode()
            );
            
            if (exists) {
                return RespDto.fail("이미 위시리스트에 등록된 품목입니다");
            }
            
            // Item 테이블에서 품목 정보 조회
            Item item = itemRepository.findByItemCode(request.getItemCode());
            if (item == null) {
                return RespDto.fail("존재하지 않는 품목입니다");
            }
            
            // Warehouse 테이블에서 창고 정보 조회
            Warehouse warehouse = warehouseRepository.findByWarehouseCode(request.getWarehouseCode());
            if (warehouse == null) {
                return RespDto.fail("존재하지 않는 창고입니다");
            }
            
            // 거래처별 단가 확인 (item_customer_price 우선)
            Integer orderUnitPrice = item.getBasePrice(); // 기본값
            List<ItemCustomerPrice> customerPrices = itemCustomerPriceRepository.findByItemCode(request.getItemCode());
            for (ItemCustomerPrice cp : customerPrices) {
                if (cp.getCustomerCode().equals(request.getCustomerCode())) {
                    orderUnitPrice = cp.getCustomerSupplyPrice();
                    break;
                }
            }
            
            CustomerWishlist wishlist = CustomerWishlist.builder()
                    .customerCode(request.getCustomerCode())
                    .customerUserCode(request.getCustomerUserCode())
                    .itemCode(request.getItemCode())
                    // Item 테이블에서 가져온 정보
                    .itemName(item.getItemName())
                    .specification(item.getSpecification())
                    .unit(item.getPurchaseUnit())
                    .priceType(item.getPriceType())
                    .orderUnitPrice(orderUnitPrice) // 거래처별 단가 또는 기본 단가
                    .currentStockQty(request.getCurrentStockQty())
                    .taxTarget(item.getVatType())
                    // Warehouse 테이블에서 가져온 정보
                    .warehouseCode(request.getWarehouseCode())
                    .warehouseName(warehouse.getWarehouseName())
                    .description(null) // null 처리
                    .build();
            
            CustomerWishlist savedWishlist = customerWishlistRepository.save(wishlist);
            
            WishlistRespDto responseData = WishlistRespDto.builder()
                    .customerWishlistCode(savedWishlist.getCustomerWishlistCode())
                    .build();
            
            log.info("[앱] 위시리스트 추가 성공 - customerWishlistCode: {}, itemName: {}, 거래처별단가: {}", 
                    savedWishlist.getCustomerWishlistCode(), item.getItemName(), orderUnitPrice);
            
            return RespDto.success("위시리스트 추가 성공", responseData);
            
        } catch (Exception e) {
            log.error("위시리스트 추가 실패", e);
            return RespDto.fail("위시리스트 추가 실패");
        }
    }
    
    /**
     * 위시리스트 삭제
     */
    @Transactional
    public RespDto<Void> deleteWishlist(Integer customerWishlistCode) {
        try {
            if (!customerWishlistRepository.existsById(customerWishlistCode)) {
                return RespDto.fail("존재하지 않는 위시리스트입니다");
            }
            
            customerWishlistRepository.deleteById(customerWishlistCode);
            
            return RespDto.success("위시리스트 삭제 성공", null);
            
        } catch (Exception e) {
            log.error("위시리스트 삭제 실패", e);
            return RespDto.fail("위시리스트 삭제 실패");
        }
    }
}