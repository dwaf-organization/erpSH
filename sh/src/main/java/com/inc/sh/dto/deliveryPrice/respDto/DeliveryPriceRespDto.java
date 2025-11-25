package com.inc.sh.dto.deliveryPrice.respDto;

import com.inc.sh.entity.Item;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPriceRespDto {
    
    private Integer itemCode;       // 품목코드
    private String itemName;        // 품명
    private String specification;   // 규격
    private String purchaseUnit;    // 단위
    private String vatType;         // 부가세
    private Integer previousPrice;  // 직전단가
    private Integer basePrice;      // 기본단가
    private Integer priceType;      // 단가유형 (1=납품싯가, 2=납품단가)
    
    /**
     * Entity to DTO 변환
     */
    public static DeliveryPriceRespDto fromEntity(Item item) {
        return DeliveryPriceRespDto.builder()
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .specification(item.getSpecification())
                .purchaseUnit(item.getPurchaseUnit())
                .vatType(item.getVatType())
                .previousPrice(item.getPreviousPrice())
                .basePrice(item.getBasePrice())
                .priceType(item.getPriceType())
                .build();
    }
}