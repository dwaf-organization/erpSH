package com.inc.sh.dto.orderLimit.respDto;

import com.inc.sh.entity.Item;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemForOrderLimitRespDto {
    
    private Integer itemCode;         // 품목코드
    private String itemName;          // 품명
    private String specification;     // 규격
    private Integer basePrice;        // 기본단가
    private Integer orderAvailableYn; // 주문제한(0=전체불가, 1=전체가능, 2=선택불가)
    
    /**
     * Entity to DTO 변환
     */
    public static ItemForOrderLimitRespDto fromEntity(Item item) {
        return ItemForOrderLimitRespDto.builder()
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .specification(item.getSpecification())
                .basePrice(item.getBasePrice())
                .orderAvailableYn(item.getOrderAvailableYn())
                .build();
    }
}