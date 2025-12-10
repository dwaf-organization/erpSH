package com.inc.sh.dto.item.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemSaveReqDto {
    
    private List<ItemItemDto> items;  // 품목 배열
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemItemDto {
        private Integer itemCode;               // null=CREATE, 값=UPDATE
        private Integer hqCode;                 // 본사코드 (필수)
        private Integer categoryCode;           // 분류코드 (Entity 구조에 맞춤)
        private String itemName;               // 품명 (필수)
        private String specification;          // 규격
        private String purchaseUnit;           // 구매단위
        private String vatType;               // VAT유형
        private String vatDetail;             // 세부VAT
        private String regDt;                 // 등록일자
        private String endDt;                 // 종료일자
        private String origin;                // 원산지
        private Integer priceType;            // 단가유형 (1=납품싯가, 2=납품단가)
        private Integer basePrice;            // 기본단가
        private Integer previousPrice;        // 이전단가
        private String hqMemo;               // 본사메모
        private Integer orderAvailableYn;     // 주문가능여부 (0=전체불가, 1=전체가능, 2=선택불가)
        private Integer minOrderQty;         // 최소주문수량
        private Integer maxOrderQty;         // 최대주문수량
        private Integer deadlineDay;         // 마감일
        private String deadlineTime;        // 마감시간
    }
}