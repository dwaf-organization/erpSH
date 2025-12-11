package com.inc.sh.dto.order.respDto;

import com.inc.sh.entity.OrderItem;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRespDto {
    
    private Integer orderItemCode;
    private String orderNo;
    private Integer itemCode;
    private Integer releaseWarehouseCode;
    private String itemName;
    private String specification;
    private String unit;
    private Integer priceType;
    private Integer orderUnitPrice;
    private Integer currentStockQty;
    private Integer orderQty;
    private String taxTarget;
    private String warehouseName;
    private Integer supplyAmtBasic;      // 기본 공급가액 (1개당)
    private Integer taxableAmtBasic;     // 기본 과세액 (1개당)
    private Integer taxFreeAmtBasic;     // 기본 면세액 (1개당)
    private Integer vatAmtBasic;         // 기본 부가세액 (1개당)
    private Integer totalAmtBasic;       // 기본 총액 (1개당)
    private Integer taxableAmt;
    private Integer taxFreeAmt;
    private Integer supplyAmt;
    private Integer vatAmt;
    private Integer totalAmt;
    private Integer totalQty;
    
    // ✅ 기본가격 정보 (단위당, 수량 곱하기 전)

    
    /**
     * Entity to DTO 변환 (기본가격 정보 없음)
     */
    public static OrderItemRespDto fromEntity(OrderItem orderItem) {
        return OrderItemRespDto.builder()
                .orderItemCode(orderItem.getOrderItemCode())
                .orderNo(orderItem.getOrderNo())
                .itemCode(orderItem.getItemCode())
                .releaseWarehouseCode(orderItem.getReleaseWarehouseCode())
                .itemName(orderItem.getItemName())
                .specification(orderItem.getSpecification())
                .unit(orderItem.getUnit())
                .priceType(orderItem.getPriceType())
                .orderUnitPrice(orderItem.getOrderUnitPrice())
                .currentStockQty(orderItem.getCurrentStockQty())
                .orderQty(orderItem.getOrderQty())
                .taxTarget(orderItem.getTaxTarget())
                .warehouseName(orderItem.getWarehouseName())
                .taxableAmt(orderItem.getTaxableAmt())
                .taxFreeAmt(orderItem.getTaxFreeAmt())
                .supplyAmt(orderItem.getSupplyAmt())
                .vatAmt(orderItem.getVatAmt())
                .totalAmt(orderItem.getTotalAmt())
                .totalQty(orderItem.getTotalQty())
                // 기본가격 정보는 null로 설정 (Repository 조인에서 처리)
                .supplyAmtBasic(null)
                .taxableAmtBasic(null)
                .taxFreeAmtBasic(null)
                .vatAmtBasic(null)
                .totalAmtBasic(null)
                .build();
    }
    
    /**
     * Object[] to DTO 변환 (기본가격 정보 포함)
     */
    public static OrderItemRespDto fromObjectArrayWithBasicPrice(Object[] result) {
        return OrderItemRespDto.builder()
                .orderItemCode(safeIntegerCast(result[0]))
                .orderNo(safeStringCast(result[1]))
                .itemCode(safeIntegerCast(result[2]))
                .releaseWarehouseCode(safeIntegerCast(result[3]))
                .itemName(safeStringCast(result[4]))
                .specification(safeStringCast(result[5]))
                .unit(safeStringCast(result[6]))
                .priceType(safeIntegerCast(result[7]))
                .orderUnitPrice(safeIntegerCast(result[8]))
                .currentStockQty(safeIntegerCast(result[9]))
                .orderQty(safeIntegerCast(result[10]))
                .taxTarget(safeStringCast(result[11]))
                .warehouseName(safeStringCast(result[12]))
                .taxableAmt(safeIntegerCast(result[13]))
                .taxFreeAmt(safeIntegerCast(result[14]))
                .supplyAmt(safeIntegerCast(result[15]))
                .vatAmt(safeIntegerCast(result[16]))
                .totalAmt(safeIntegerCast(result[17]))
                .totalQty(safeIntegerCast(result[18]))
                // 기본가격 정보 (거래처별 or 기본)
                .supplyAmtBasic(safeIntegerCast(result[20]))      // 기본 공급가액
                .taxableAmtBasic(safeIntegerCast(result[21]))     // 기본 과세액
                .taxFreeAmtBasic(safeIntegerCast(result[22]))     // 기본 면세액
                .vatAmtBasic(safeIntegerCast(result[23]))         // 기본 부가세액
                .totalAmtBasic(safeIntegerCast(result[24]))       // 기본 총액
                .build();
    }
    
    /**
     * 안전한 String 캐스팅
     */
    private static String safeStringCast(Object obj) {
        return obj != null ? obj.toString() : null;
    }
    
    /**
     * 안전한 Integer 캐스팅
     */
    private static Integer safeIntegerCast(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try {
            return Integer.valueOf(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}