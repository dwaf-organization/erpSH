package com.inc.sh.dto.returnRegistration.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppReturnHistoryRespDto {
    
    private String returnNo;           // 반품번호
    private String returnRequestDt;    // 반품요청일
    private Integer itemCode;          // 품목코드
    private String itemName;           // 품명
    private String specification;      // 규격
    private String unit;               // 단위
    private Integer priceType;         // 단가유형
    private Integer unitPrice;         // 기본단가
    private Integer qty;               // 수량
    private String returnMessage;      // 반품사유
    private String progressStatus;     // 승인여부
    private Integer totalAmt;          // 총금액
}