package com.inc.sh.dto.virtualAccount.reqDto;


import com.inc.sh.entity.VirtualAccount;
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
public class VirtualAccountReqDto {
    
    // 수정시에만 사용 (null이면 신규 생성)
    private Integer virtualAccountCode;
    
    @NotNull(message = "본사코드는 필수입니다")
    private Integer hqCode;
    
    private Integer linkedCustomerCode; // 연결거래처코드 (선택)
    
    @NotBlank(message = "가상계좌번호는 필수입니다")
    private String virtualAccountNum;
    
    @NotBlank(message = "가상계좌상태는 필수입니다")
    private String virtualAccountStatus; // 사용중, 미사용
    
    @NotBlank(message = "은행명은 필수입니다")
    private String bankName;
    
    private String note; // 비고
    
    /**
     * DTO to Entity 변환 (신규 등록)
     */
    public VirtualAccount toEntity() {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        return VirtualAccount.builder()
                .hqCode(this.hqCode)
                .linkedCustomerCode(this.linkedCustomerCode)
                .virtualAccountNum(this.virtualAccountNum)
                .virtualAccountStatus(this.virtualAccountStatus)
                .bankName(this.bankName)
                .openDt(currentDate) // 생성시 현재날짜
                .closeDt("미사용".equals(this.virtualAccountStatus) ? currentDate : null) // 미사용시 현재날짜
                .note(this.note)
                .build();
    }
    
    /**
     * 기존 Entity 업데이트
     */
    public void updateEntity(VirtualAccount virtualAccount) {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        virtualAccount.setHqCode(this.hqCode);
        virtualAccount.setLinkedCustomerCode(this.linkedCustomerCode);
        virtualAccount.setVirtualAccountNum(this.virtualAccountNum);
        virtualAccount.setVirtualAccountStatus(this.virtualAccountStatus);
        virtualAccount.setBankName(this.bankName);
        virtualAccount.setNote(this.note);
        
        // 상태 변경에 따른 해지일자 처리
        if ("미사용".equals(this.virtualAccountStatus)) {
            // 미사용으로 변경시 해지일자 설정 (기존에 없었다면)
            if (virtualAccount.getCloseDt() == null) {
                virtualAccount.setCloseDt(currentDate);
            }
        } else if ("사용중".equals(this.virtualAccountStatus)) {
            // 사용중으로 변경시 해지일자 삭제
            virtualAccount.setCloseDt(null);
        }
    }
}
