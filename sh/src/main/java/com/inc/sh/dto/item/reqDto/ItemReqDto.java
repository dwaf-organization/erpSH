package com.inc.sh.dto.item.reqDto;

import com.inc.sh.entity.Item;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemReqDto {
    
    // 수정시에만 사용 (null이면 신규 생성)
    private Integer itemCode;
    
    @NotNull(message = "분류코드는 필수입니다")
    private Integer categoryCode;
    
    @NotNull(message = "본사코드는 필수입니다")
    private Integer hqCode;
    
    @NotBlank(message = "품명은 필수입니다")
    private String itemName;
    
    private String specification; // 규격
    
    @NotBlank(message = "구매단위는 필수입니다")
    private String purchaseUnit;
    
    @NotBlank(message = "부가세는 필수입니다")
    private String vatType;
    
    private String vatDetail; // 세부부가세
    
    private String regDt; // 등록일자 (null이면 현재날짜)
    private String endDt; // 종료일자
    private String origin; // 원산지
    
    @NotNull(message = "단가유형은 필수입니다")
    private Integer priceType; // 1=납품싯가, 2=납품단가
    
    @NotNull(message = "기본단가는 필수입니다")
    private Integer basePrice;
    
    private Integer previousPrice; // 이전단가
    private String hqMemo; // 본사메모
    
    private Integer orderAvailableYn; // 주문가능여부
    private Integer minOrderQty; // 최소주문수량
    private Integer maxOrderQty; // 최대주문수량
    private Integer deadlineDay; // 마감일
    private String deadlineTime; // 마감시간
    
    /**
     * DTO to Entity 변환 (신규 등록)
     */
    public Item toEntity() {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        return Item.builder()
                .categoryCode(this.categoryCode)
                .hqCode(this.hqCode)
                .itemName(this.itemName)
                .specification(this.specification)
                .purchaseUnit(this.purchaseUnit)
                .vatType(this.vatType)
                .vatDetail(this.vatDetail)
                .regDt(this.regDt != null ? this.regDt : currentDate)
                .endDt(this.endDt)
                .origin(this.origin)
                .priceType(this.priceType)
                .basePrice(this.basePrice)
                .previousPrice(this.previousPrice)
                .hqMemo(this.hqMemo)
                .orderAvailableYn(this.orderAvailableYn != null ? this.orderAvailableYn : 1)
                .minOrderQty(this.minOrderQty != null ? this.minOrderQty : 1)
                .maxOrderQty(this.maxOrderQty != null ? this.maxOrderQty : 9999)
                .deadlineDay(this.deadlineDay != null ? this.deadlineDay : 1)
                .deadlineTime(this.deadlineTime != null ? this.deadlineTime : "18:00")
                .build();
    }
    
    /**
     * 기존 Entity 업데이트
     */
    public void updateEntity(Item item) {
        item.setCategoryCode(this.categoryCode);
        item.setHqCode(this.hqCode);
        item.setItemName(this.itemName);
        item.setSpecification(this.specification);
        item.setPurchaseUnit(this.purchaseUnit);
        item.setVatType(this.vatType);
        item.setVatDetail(this.vatDetail);
        if (this.regDt != null) {
            item.setRegDt(this.regDt);
        }
        item.setEndDt(this.endDt);
        item.setOrigin(this.origin);
        item.setPriceType(this.priceType);
        item.setBasePrice(this.basePrice);
        item.setPreviousPrice(this.previousPrice);
        item.setHqMemo(this.hqMemo);
        if (this.orderAvailableYn != null) {
            item.setOrderAvailableYn(this.orderAvailableYn);
        }
        if (this.minOrderQty != null) {
            item.setMinOrderQty(this.minOrderQty);
        }
        if (this.maxOrderQty != null) {
            item.setMaxOrderQty(this.maxOrderQty);
        }
        if (this.deadlineDay != null) {
            item.setDeadlineDay(this.deadlineDay);
        }
        if (this.deadlineTime != null) {
            item.setDeadlineTime(this.deadlineTime);
        }
    }
}