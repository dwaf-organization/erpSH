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
    private String categoryName;
    
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
                .categoryName(null)
                .build();
    }
    
    /**
     * Entity를 DTO로 변환하고 categoryName 설정
     */
    public static ItemRespDto fromEntityWithCategoryName(Item item, String categoryName) {
        ItemRespDto dto = fromEntity(item);
        dto.setCategoryName(categoryName);
        return dto;
    }
}