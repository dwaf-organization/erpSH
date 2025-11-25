package com.inc.sh.dto.headquarter.reqDto;

import com.inc.sh.entity.Headquarter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteInfoReqDto {
    
    @NotNull(message = "본사코드는 필수입니다")
    private Integer hqCode;
    
    @NotBlank(message = "법인명은 필수입니다")
    private String companyName;
    
    private String corpRegNum;
    
    @NotBlank(message = "사업자번호는 필수입니다")
    private String bizNum;
    
    @NotBlank(message = "대표자는 필수입니다")
    private String ceoName;
    
    private String zipCode;
    private String addr;
    private String bizType;
    private String bizItem;
    private String telNum;
    
    @NotBlank(message = "고객센터 전화번호는 필수입니다")
    private String inquiryTelNum;
    
    private String faxNum;
    private String homepage;
    private String bankName;
    private String accountNum;
    private String accountHolder;
    
    /**
     * DTO to Entity 변환 (신규 등록)
     */
    public Headquarter toEntity() {
        return Headquarter.builder()
                .hqCode(this.hqCode)
                .companyName(this.companyName)
                .corpRegNum(this.corpRegNum)
                .bizNum(this.bizNum)
                .ceoName(this.ceoName)
                .zipCode(this.zipCode)
                .addr(this.addr)
                .bizType(this.bizType)
                .bizItem(this.bizItem)
                .telNum(this.telNum)
                .inquiryTelNum(this.inquiryTelNum)
                .faxNum(this.faxNum)
                .homepage(this.homepage)
                .bankName(this.bankName)
                .accountNum(this.accountNum)
                .accountHolder(this.accountHolder)
                .build();
    }
    
    /**
     * 기존 Entity 업데이트
     */
    public void updateEntity(Headquarter headquarter) {
        headquarter.setCompanyName(this.companyName);
        headquarter.setCorpRegNum(this.corpRegNum);
        headquarter.setBizNum(this.bizNum);
        headquarter.setCeoName(this.ceoName);
        headquarter.setZipCode(this.zipCode);
        headquarter.setAddr(this.addr);
        headquarter.setBizType(this.bizType);
        headquarter.setBizItem(this.bizItem);
        headquarter.setTelNum(this.telNum);
        headquarter.setInquiryTelNum(this.inquiryTelNum);
        headquarter.setFaxNum(this.faxNum);
        headquarter.setHomepage(this.homepage);
        headquarter.setBankName(this.bankName);
        headquarter.setAccountNum(this.accountNum);
        headquarter.setAccountHolder(this.accountHolder);
    }
}