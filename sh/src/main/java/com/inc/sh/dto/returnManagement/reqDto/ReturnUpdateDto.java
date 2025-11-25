package com.inc.sh.dto.returnManagement.reqDto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnUpdateDto {
    
    private Integer returnCode;             // 반품코드 (필수)
    private Integer customerCode;           // 거래처코드
    private Integer itemCode;               // 품목코드
    private Integer warehouseCode;          // 창고코드
    private String customerName;            // 거래처명
    private String returnRequestDate;       // 반품요청일자 (YYYYMMDD)
    private String itemName;                // 품명
    private String specification;           // 규격
    private String unit;                    // 단위
    private Integer quantity;               // 수량
    private String priceType;               // 단가유형
    private BigDecimal supplyPrice;         // 공급가액
    private BigDecimal vat;                 // 부가세
    private BigDecimal totalAmount;         // 합계금액
    private String returnMessage;                 // 메시지
    private String replyMessage;                 // 메시지
    private String note;                    // 비고
    private String status;                  // 진행상태
    private String warehouseName;           // 창고명
    private String returnApprovalDate;      // 반품승인일자 (YYYYMMDD)
}