package com.inc.sh.dto.item.respDto;

import com.inc.sh.entity.Item;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRespDto {
    
    private Integer itemCode;
    private Integer categoryCode;
    private Integer hqCode;
    private String itemName;
    private String specification;
    private String purchaseUnit;
    private String vatType;
    private String vatDetail;
    private String regDt;
    private String endDt;
    private String origin;
    private Integer priceType;
    private Integer basePrice;
    private Integer previousPrice;
    private String hqMemo;
    private Integer orderAvailableYn;
    private Integer minOrderQty;
    private Integer maxOrderQty;
    private Integer deadlineDay;
    private String deadlineTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Entity to DTO 변환
     */
    public static ItemRespDto fromEntity(Item item) {
        return ItemRespDto.builder()
                .itemCode(item.getItemCode())
                .categoryCode(item.getCategoryCode())
                .hqCode(item.getHqCode())
                .itemName(item.getItemName())
                .specification(item.getSpecification())
                .purchaseUnit(item.getPurchaseUnit())
                .vatType(item.getVatType())
                .vatDetail(item.getVatDetail())
                .regDt(item.getRegDt())
                .endDt(item.getEndDt())
                .origin(item.getOrigin())
                .priceType(item.getPriceType())
                .basePrice(item.getBasePrice())
                .previousPrice(item.getPreviousPrice())
                .hqMemo(item.getHqMemo())
                .orderAvailableYn(item.getOrderAvailableYn())
                .minOrderQty(item.getMinOrderQty())
                .maxOrderQty(item.getMaxOrderQty())
                .deadlineDay(item.getDeadlineDay())
                .deadlineTime(item.getDeadlineTime())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}