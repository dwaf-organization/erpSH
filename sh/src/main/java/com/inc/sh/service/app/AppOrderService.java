package com.inc.sh.service.app;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.order.respDto.AppOrderItemListRespDto;
import com.inc.sh.repository.ItemRepository;
import com.inc.sh.repository.CustomerWishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppOrderService {
    
    private final ItemRepository itemRepository;
    private final CustomerWishlistRepository customerWishlistRepository;
    
    /**
     * [앱전용] 주문가능 품목 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<AppOrderItemListRespDto>> findOrderableItemsForApp(
            Integer customerCode, 
            Integer customerUserCode,
            String itemType, 
            String categoryCode, 
            String itemName) {
        try {
            List<Object[]> itemResults;
            
            // 위시리스트 품목코드 목록 조회
            List<Integer> wishlistItemCodes = customerWishlistRepository
                    .findItemCodesByCustomerCodeAndCustomerUserCode(customerCode, customerUserCode);
            
            // itemType에 따라 다른 쿼리 호출
            if ("위시리스트".equals(itemType)) {
                // 위시리스트 품목만 조회
                itemResults = itemRepository.findWishlistOrderableItemsForApp(
                        customerCode, customerUserCode, categoryCode, itemName);
            } else {
                // 전체 주문가능 품목 조회
                itemResults = itemRepository.findAllOrderableItemsForApp(
                        customerCode, categoryCode, itemName);
            }
            
            // Object[] 결과를 DTO로 변환 (warehouse_code 포함)
            List<AppOrderItemListRespDto> responseList = itemResults.stream()
                .map(result -> {
                    Integer itemCode = (Integer) result[0];
                    
                    return AppOrderItemListRespDto.builder()
                            .itemCode(itemCode)
                            .itemName((String) result[1])
                            .specification((String) result[2])
                            .unit((String) result[3])
                            .vatType((String) result[4])
                            .vatDetail((String) result[5])
                            .categoryCode((Integer) result[6])
                            .origin((String) result[7])
                            .priceType((Integer) result[8])
                            .customerPrice((Integer) result[9])     // base_price
                            .supplyPrice((Integer) result[10])      // supply_price  
                            .taxAmount((Integer) result[11])        // tax_amount
                            .taxableAmount((Integer) result[12])    // taxable_amount
                            .dutyFreeAmount((Integer) result[13])   // duty_free_amount
                            .totalAmt((Integer) result[14])         // total_amt
                            .orderAvailableYn((Integer) result[15])
                            .minOrderQty((Integer) result[16])
                            .maxOrderQty((Integer) result[17])
                            .deadlineDay((Integer) result[18])
                            .deadlineTime((String) result[19])
                            .currentQuantity((Integer) result[20])
                            .warehouseCode((Integer) result[21])    // 새로 추가된 warehouse_code
                            .isWishlist(wishlistItemCodes.contains(itemCode))
                            .build();
                })
                .collect(Collectors.toList());
            
            log.info("[앱] 주문가능품목 조회 완료 - customerCode: {}, itemType: {}, 조회건수: {}", 
                    customerCode, itemType, responseList.size());
            
            return RespDto.success("조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("[앱] 주문가능품목 조회 실패 - customerCode: {}, itemType: {}", 
                    customerCode, itemType, e);
            return RespDto.fail("조회 중 오류가 발생했습니다");
        }
    }
}