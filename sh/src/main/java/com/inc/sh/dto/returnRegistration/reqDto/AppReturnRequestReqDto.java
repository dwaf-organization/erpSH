package com.inc.sh.dto.returnRegistration.reqDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppReturnRequestReqDto {
    
    private Integer customerCode;      // 거래처코드
    private String orderNo;            // 주문번호
    private Integer orderItemCode;     // 주문품목코드
    private Integer itemCode;          // 품목코드
    private String itemName;           // 품목명
    private Integer warehouseCode;     // 창고코드
    private Integer returnQty;         // 반품수량
    private String returnMessage;      // 반품메시지
}