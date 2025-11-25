package com.inc.sh.util;

import com.inc.sh.entity.Item;
import com.inc.sh.entity.ItemCustomerPrice;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ItemPriceResolver {
    
    /**
     * 품목 가격 정보 우선순위 조회
     * 1순위: 거래처별 단가 (값이 0이 아니고 null이 아닌 경우)
     * 2순위: 품목 기본 단가
     * 
     * @param item 품목 정보
     * @param customerPrice 거래처별 단가 정보 (null 가능)
     * @return 최종 사용할 가격 정보 Map
     */
    public static Map<String, Integer> resolvePrices(Item item, ItemCustomerPrice customerPrice) {
        if (customerPrice != null && 
            customerPrice.getCustomerSupplyPrice() != null && 
            customerPrice.getCustomerSupplyPrice() > 0) {
            
            // 1순위: 거래처별 단가 사용
            log.debug("거래처별 단가 사용 - itemCode: {}, customerCode: {}, price: {}", 
                     item.getItemCode(), customerPrice.getCustomerCode(), customerPrice.getCustomerSupplyPrice());
                     
            return Map.of(
                "basePrice", customerPrice.getCustomerSupplyPrice(),
                "supplyPrice", customerPrice.getSupplyPrice() != null ? customerPrice.getSupplyPrice() : 0,
                "taxAmount", customerPrice.getTaxAmount() != null ? customerPrice.getTaxAmount() : 0,
                "taxableAmount", customerPrice.getTaxableAmount() != null ? customerPrice.getTaxableAmount() : 0,
                "dutyFreeAmount", customerPrice.getDutyFreeAmount() != null ? customerPrice.getDutyFreeAmount() : 0,
                "totalAmount", customerPrice.getTotalAmount() != null ? customerPrice.getTotalAmount() : 0
            );
        } else {
            // 2순위: 품목 기본 단가 사용
            log.debug("품목 기본 단가 사용 - itemCode: {}, price: {}", 
                     item.getItemCode(), item.getBasePrice());
                     
            return Map.of(
                "basePrice", item.getBasePrice() != null ? item.getBasePrice() : 0,
                "supplyPrice", item.getSupplyPrice() != null ? item.getSupplyPrice() : 0,
                "taxAmount", item.getTaxAmount() != null ? item.getTaxAmount() : 0,
                "taxableAmount", item.getTaxableAmount() != null ? item.getTaxableAmount() : 0,
                "dutyFreeAmount", item.getDutyFreeAmount() != null ? item.getDutyFreeAmount() : 0,
                "totalAmount", item.getTotalAmount() != null ? item.getTotalAmount() : 0
            );
        }
    }
    
    /**
     * 기본 단가 조회 (우선순위 적용)
     */
    public static Integer resolveBasePrice(Item item, ItemCustomerPrice customerPrice) {
        if (customerPrice != null && 
            customerPrice.getCustomerSupplyPrice() != null && 
            customerPrice.getCustomerSupplyPrice() > 0) {
            return customerPrice.getCustomerSupplyPrice();
        }
        return item.getBasePrice() != null ? item.getBasePrice() : 0;
    }
    
    /**
     * 공급가액 조회 (우선순위 적용)
     */
    public static Integer resolveSupplyPrice(Item item, ItemCustomerPrice customerPrice) {
        if (customerPrice != null && 
            customerPrice.getCustomerSupplyPrice() != null && 
            customerPrice.getCustomerSupplyPrice() > 0) {
            return customerPrice.getSupplyPrice() != null ? customerPrice.getSupplyPrice() : 0;
        }
        return item.getSupplyPrice() != null ? item.getSupplyPrice() : 0;
    }
    
    /**
     * 총액 조회 (우선순위 적용)
     */
    public static Integer resolveTotalAmount(Item item, ItemCustomerPrice customerPrice) {
        if (customerPrice != null && 
            customerPrice.getCustomerSupplyPrice() != null && 
            customerPrice.getCustomerSupplyPrice() > 0) {
            return customerPrice.getTotalAmount() != null ? customerPrice.getTotalAmount() : 0;
        }
        return item.getTotalAmount() != null ? item.getTotalAmount() : 0;
    }
}