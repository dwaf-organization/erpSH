package com.inc.sh.dto.picking.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickingListRespDto {
    
    private String distCenterName;    // 물류센터명
    private Integer itemCode;         // 품목코드
    private String itemName;          // 품명
    private String specification;     // 규격
    private String unit;              // 단위
    private Integer totalQty;         // 출고량 (품목별 주문수량 합계)
}