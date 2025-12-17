package com.inc.sh.dto.order.respDto;

import com.inc.sh.entity.Order;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRespDto {
    
    private String orderNo;
    private Integer hqCode;           // 본사코드
    private Integer customerCode;
    private String customerName;
    private String bizNum;
    private String zipCode;
    private String addr;
    private String orderDt;
    private String deliveryRequestDt;
    private Integer depositTypeCode;
    private String deliveryStatus;
    private Integer deliveryAmt;
    private String deliveryDt;
    private Integer distCenterCode;
    private String distCenterName;
    private Integer vehicleCode;
    private String vehicleName;
    private String orderMessage;
    private Integer taxableAmt;
    private Integer taxFreeAmt;
    private Integer supplyAmt;
    private Integer vatAmt;
    private Integer totalAmt;
    private Integer totalQty;
    
    /**
     * Object[] to DTO 변환 (dist_center 조인 결과용)
     */
    public static OrderRespDto fromObjectArrayWithDistCenter(Object[] result) {
        return OrderRespDto.builder()
                .orderNo(safeStringCast(result[0]))
                .hqCode(safeIntegerCast(result[1]))
                .customerCode(safeIntegerCast(result[2]))
                .customerName(safeStringCast(result[3]))
                .bizNum(safeStringCast(result[4]))
                .zipCode(safeStringCast(result[24]))
                .addr(safeStringCast(result[5]))
                .orderDt(safeStringCast(result[6]))
                .deliveryRequestDt(safeStringCast(result[7]))
                .depositTypeCode(safeIntegerCast(result[8]))
                .deliveryStatus(safeStringCast(result[9]))
                .deliveryAmt(safeIntegerCast(result[10]))
                .orderMessage(safeStringCast(result[11]))
                .taxableAmt(safeIntegerCast(result[12]))
                .taxFreeAmt(safeIntegerCast(result[13]))
                .supplyAmt(safeIntegerCast(result[14]))
                .vatAmt(safeIntegerCast(result[15]))
                .totalAmt(safeIntegerCast(result[16]))
                .totalQty(safeIntegerCast(result[17]))
                // 조인으로 가져온 추가 정보들
                .distCenterCode(safeIntegerCast(result[20]))     // 물류센터코드
                .distCenterName(safeStringCast(result[21]))      // 물류센터명
                .vehicleCode(safeIntegerCast(result[22]))         // 차량코드
                .vehicleName(safeStringCast(result[23]))         // 차량명
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

    /**
     * Entity to DTO 변환 (조회용) - 기존 메서드 유지
     */
    public static OrderRespDto fromEntity(Order order) {
        return OrderRespDto.builder()
                .orderNo(order.getOrderNo())
                .hqCode(order.getHqCode())
                .customerCode(order.getCustomerCode())
                .customerName(order.getCustomerName())
                .bizNum(order.getBizNum())
                .zipCode(order.getZipCode())
                .addr(order.getAddr())
                .orderDt(order.getOrderDt())
                .deliveryRequestDt(order.getDeliveryRequestDt())
                .depositTypeCode(order.getDepositTypeCode())
                .deliveryStatus(order.getDeliveryStatus())
                .deliveryAmt(order.getDeliveryAmt())
                .deliveryDt(order.getDeliveryDt())
                .distCenterName(order.getDistCenterName())
                .orderMessage(order.getOrderMessage())
                .taxableAmt(order.getTaxableAmt())
                .taxFreeAmt(order.getTaxFreeAmt())
                .supplyAmt(order.getSupplyAmt())
                .vatAmt(order.getVatAmt())
                .totalAmt(order.getTotalAmt())
                .totalQty(order.getTotalQty())
                .build();
    }
}