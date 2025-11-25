package com.inc.sh.dto.itemCustomerPrice.respDto;

import com.inc.sh.entity.Item;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemForCustomerPriceRespDto {
    
    private Integer itemCode;     // 품목코드
    private String itemName;      // 품명
    private String specification; // 규격
    private Integer basePrice;    // 기본단가
    
    /**
     * Entity to DTO 변환
     */
    public static ItemForCustomerPriceRespDto fromEntity(Item item) {
        return ItemForCustomerPriceRespDto.builder()
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .specification(item.getSpecification())
                .basePrice(item.getBasePrice())
                .build();
    }
}