package com.inc.sh.dto.picking.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickingListSearchDto {
    
    private String deliveryRequestDtStart;  // 납기일자 시작
    private String deliveryRequestDtEnd;    // 납기일자 끝
    private Integer itemCode;               // 품목코드 (전체/완전)
    private Integer distCenterCode;         // 물류센터코드 (전체/완전)
    private Integer brandCode;              // 브랜드코드 (전체/완전)
    private Integer hqCode;                 // 본사코드
}