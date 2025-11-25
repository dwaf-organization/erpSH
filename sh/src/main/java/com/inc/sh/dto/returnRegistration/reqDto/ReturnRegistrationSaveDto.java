package com.inc.sh.dto.returnRegistration.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnRegistrationSaveDto {
    
    // 기본 정보 (기존 유지)
    private Integer customerCode;           // 거래처코드
    private String customerName;            // 거래처명
    private Integer itemCode;               // 품목코드
    private String itemName;                // 품명
    private String specification;           // 규격
    private String unit;                    // 단위
    private Integer warehouseCode;          // 창고코드
    private String warehouseName;           // 창고명
    
    // 추가 필요한 필드들 (실제 테이블 구조에 맞춤)
    private Integer orderItemCode;          // 주문품목코드 (필수)
    private String orderNo;                 // 주문번호 (필수)
    
    // 주문 관련 정보
    private Integer orderPrice;             // 주문단가
    private Integer orderQuantity;          // 주문수량
    private Integer priceType;              // 단가유형 (Integer 타입)
    
    // 반품 관련 정보
    private Integer returnQuantity;         // 반품수량
    private String returnRequestDate;       // 반품요청일자 (YYYYMMDD 형태)
    private String returnMessage;           // 반품메시지 (return_message 컬럼)
    private String replyMessage;            // 회신메시지 (reply_message 컬럼)
    private String note;                    // 비고
    private String progressStatus;          // 진행상태 (기본값: "미승인")
    private String returnApprovalDate;      // 반품승인일자
}