package com.inc.sh.dto.returnManagement.respDto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnRespDto {
    
    // 반품 기본정보
    private String returnNo;             // 반품코드
    private String orderNo;                 // 주문번호
    private Integer customerCode;           // 거래처코드
    private String customerName;            // 거래처명 (JOIN)
    private String returnRequestDate;       // 반품요청일
    private String returnApprovalDate;      // 반품승인일자
    
    // 품목정보
    private Integer itemCode;               // 품목코드
    private String itemName;                // 품명
    private String specification;           // 규격
    private String unit;                    // 단위
    private String priceType;               // 단가유형
    private Integer unitPrice;              // 납품단가
    
    // 수량/금액정보
    private Integer quantity;               // 수량
    private BigDecimal supplyPrice;         // 공급가액
    private BigDecimal vat;                 // 부가세
    private BigDecimal totalAmount;         // 합계금액
    
    // 창고/물류센터 정보
    private Integer warehouseCode;          // 입고창고코드
    private String warehouseName;           // 창고명 (JOIN)
    private Integer distCenterCode;         // 물류센터코드
    private String distCenterName;          // 물류센터명 (JOIN)
    
    // 상태/메모
    private String status;                  // 진행상태
    private String message;                 // 메시지
    private String note;                    // 비고
    private String returnMessage;           // 반품사유
    
    // 기타 반품테이블 항목들
    private String returnType;              // 반품유형
    private String processedBy;             // 처리자
}