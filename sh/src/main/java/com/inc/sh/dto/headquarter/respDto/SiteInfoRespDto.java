package com.inc.sh.dto.headquarter.respDto;

import com.inc.sh.entity.Headquarter;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteInfoRespDto {
    
    private Integer hqCode;
    private String companyName;
    private String corpRegNum;
    private String bizNum;
    private String ceoName;
    private String zipCode;
    private String addr;
    private String bizType;
    private String bizItem;
    private String telNum;
    private String inquiryTelNum;
    private String faxNum;
    private String homepage;
    private String bankName;
    private String accountNum;
    private String accountHolder;
    private Integer logisticsType;
    private String priceDisplayType;
    
    /**
     * Entity to DTO 변환
     */
    public static SiteInfoRespDto fromEntity(Headquarter headquarter) {
        return SiteInfoRespDto.builder()
                .hqCode(headquarter.getHqCode())
                .companyName(headquarter.getCompanyName())
                .corpRegNum(headquarter.getCorpRegNum())
                .bizNum(headquarter.getBizNum())
                .ceoName(headquarter.getCeoName())
                .zipCode(headquarter.getZipCode())
                .addr(headquarter.getAddr())
                .bizType(headquarter.getBizType())
                .bizItem(headquarter.getBizItem())
                .telNum(headquarter.getTelNum())
                .inquiryTelNum(headquarter.getInquiryTelNum())
                .faxNum(headquarter.getFaxNum())
                .homepage(headquarter.getHomepage())
                .bankName(headquarter.getBankName())
                .accountNum(headquarter.getAccountNum())
                .accountHolder(headquarter.getAccountHolder())
                .logisticsType(headquarter.getLogisticsType())
                .priceDisplayType(headquarter.getPriceDisplayType())
                .build();
    }
}