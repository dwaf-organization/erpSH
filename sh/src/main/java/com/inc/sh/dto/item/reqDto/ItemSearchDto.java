package com.inc.sh.dto.item.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemSearchDto {
    
    private String itemCode;          // 품목코드 (부분일치)
    private String itemName;          // 품명 (부분일치)
    private Integer categoryCode;     // 대분류코드 (완전일치)
    private Integer priceType;        // 단가설정 (완전일치, 1=납품싯가, 2=납품단가)
    private Integer endDtYn;          // 종료여부 (1=종료, 0=미종료, null=전체)
    private Integer orderAvailableYn; // 주문가능여부 (완전일치, 0=전체불가, 1=전체가능, 2=선택불가)
}