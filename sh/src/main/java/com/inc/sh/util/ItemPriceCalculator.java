package com.inc.sh.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ItemPriceCalculator {
    
    /**
     * 품목 금액 계산
     * @param basePrice 기본단가
     * @param vatType 부가세 타입 (과세/면세)
     * @param vatDetail VAT 상세 (VAT포함/VAT별도)
     * @return 계산된 금액 배열 [공급가, 부가세, 과세액, 면세액, 총액]
     */
    public static int[] calculateItemPrices(Integer basePrice, String vatType, String vatDetail) {
        if (basePrice == null || basePrice <= 0) {
            return new int[]{0, 0, 0, 0, 0};
        }
        
        int supplyPrice = 0;
        int taxAmount = 0;
        int taxableAmount = 0;
        int dutyFreeAmount = 0;
        int totalAmount = 0;
        
        if ("면세".equals(vatType)) {
            // 3) 면세
            supplyPrice = basePrice;
            taxAmount = 0;
            taxableAmount = 0;
            dutyFreeAmount = supplyPrice;
            totalAmount = basePrice;
            
            log.debug("면세 계산 - 단가: {}, 공급가: {}, 부가세: {}, 과세액: {}, 면세액: {}, 총액: {}", 
                     basePrice, supplyPrice, taxAmount, taxableAmount, dutyFreeAmount, totalAmount);
                     
        } else if ("과세".equals(vatType)) {
            if ("VAT포함".equals(vatDetail)) {
                // 1) 과세 + VAT 포함
                supplyPrice = Math.round(basePrice / 1.1f); // 소수점 반올림
                taxAmount = basePrice - supplyPrice;
                taxableAmount = supplyPrice;
                dutyFreeAmount = 0;
                totalAmount = basePrice;
                
                log.debug("과세+VAT포함 계산 - 단가: {}, 공급가: {}, 부가세: {}, 과세액: {}, 면세액: {}, 총액: {}", 
                         basePrice, supplyPrice, taxAmount, taxableAmount, dutyFreeAmount, totalAmount);
                         
            } else { // VAT별도 또는 기본값
                // 2) 과세 + VAT 미포함
                supplyPrice = basePrice;
                taxAmount = Math.round(basePrice * 0.1f); // 소수점 반올림
                taxableAmount = supplyPrice;
                dutyFreeAmount = 0;
                totalAmount = supplyPrice + taxAmount;
                
                log.debug("과세+VAT별도 계산 - 단가: {}, 공급가: {}, 부가세: {}, 과세액: {}, 면세액: {}, 총액: {}", 
                         basePrice, supplyPrice, taxAmount, taxableAmount, dutyFreeAmount, totalAmount);
            }
        } else {
            // 기본값 (과세 + VAT별도로 처리)
            supplyPrice = basePrice;
            taxAmount = Math.round(basePrice * 0.1f);
            taxableAmount = supplyPrice;
            dutyFreeAmount = 0;
            totalAmount = supplyPrice + taxAmount;
            
            log.warn("알 수 없는 VAT 타입: {} / {}, 기본값으로 처리", vatType, vatDetail);
        }
        
        return new int[]{supplyPrice, taxAmount, taxableAmount, dutyFreeAmount, totalAmount};
    }
}