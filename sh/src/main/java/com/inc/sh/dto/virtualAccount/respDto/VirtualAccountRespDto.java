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
    private String virtualAccountNum;
    private String virtualAccountStatus;
    private String bankName;
    private String openDt;
    private String closeDt;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
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
                .createdAt(virtualAccount.getCreatedAt())
                .updatedAt(virtualAccount.getUpdatedAt())
                .build();
    }
}