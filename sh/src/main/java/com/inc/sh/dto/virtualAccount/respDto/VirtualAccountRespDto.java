package com.inc.sh.dto.virtualAccount.respDto;

import com.inc.sh.entity.VirtualAccount;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualAccountRespDto {
    
    private Integer virtualAccountCode;
    private Integer hqCode;
    private Integer linkedCustomerCode;
    private String customerName;
    private String virtualAccountNum;
    private String virtualAccountStatus;
    private String bankName;
    private String openDt;
    private String closeDt;
    private String note;
    
    
    /**
     * Entity to DTO 변환
     */
    public static VirtualAccountRespDto fromEntity(VirtualAccount virtualAccount) {
        return VirtualAccountRespDto.builder()
                .virtualAccountCode(virtualAccount.getVirtualAccountCode())
                .hqCode(virtualAccount.getHqCode())
                .linkedCustomerCode(virtualAccount.getLinkedCustomerCode())
                .virtualAccountNum(virtualAccount.getVirtualAccountNum())
                .virtualAccountStatus(virtualAccount.getVirtualAccountStatus())
                .bankName(virtualAccount.getBankName())
                .openDt(virtualAccount.getOpenDt())
                .closeDt(virtualAccount.getCloseDt())
                .note(virtualAccount.getNote())
                .build();
    }
    
    /**
     * Object[]와 조인 데이터로 DTO 생성 (팝업용)
     */
    public static VirtualAccountRespDto fromObjectArrayWithJoin(Object[] result) {
        return VirtualAccountRespDto.builder()
                .virtualAccountCode(convertToInteger(result[0]))
                .hqCode(convertToInteger(result[1]))
                .virtualAccountNum(convertToString(result[2]))
                .virtualAccountStatus(convertToString(result[3]))
                .bankName(convertToString(result[4]))
                .linkedCustomerCode(convertToInteger(result[5]))
                .openDt(convertToString(result[6]))
                .closeDt(convertToString(result[7]))
                .note(convertToString(result[8]))
                .customerName(convertToString(result[9]))    // customer_name
                .build();
    }
    
    // 안전한 변환 메서드들
    private static String convertToString(Object obj) {
        return obj != null ? obj.toString() : null;
    }
    
    private static Integer convertToInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try {
            return Integer.valueOf(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private static LocalDateTime convertToLocalDateTime(Object obj) {
        if (obj == null) return null;
        if (obj instanceof LocalDateTime) return (LocalDateTime) obj;
        if (obj instanceof java.sql.Timestamp) return ((java.sql.Timestamp) obj).toLocalDateTime();
        return null;
    }
    
}