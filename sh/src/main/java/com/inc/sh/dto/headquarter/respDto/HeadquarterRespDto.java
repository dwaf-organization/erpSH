package com.inc.sh.dto.headquarter.respDto;

import com.inc.sh.entity.Headquarter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeadquarterRespDto {
    
    private Integer hqCode;
    private String hqAccessCode;        // 본사접속코드 추가
    private String companyName;
    private String ceoName;
    private String bizNum;
    private String corpRegNum;
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
    
    /**
     * Entity -> RespDto 변환
     */
    public static HeadquarterRespDto from(Headquarter entity) {
        return HeadquarterRespDto.builder()
                .hqCode(entity.getHqCode())
                .hqAccessCode(entity.getHqAccessCode()) // 접속코드 추가
                .companyName(entity.getCompanyName())
                .ceoName(entity.getCeoName())
                .bizNum(entity.getBizNum())
                .corpRegNum(entity.getCorpRegNum())
                .zipCode(entity.getZipCode())
                .addr(entity.getAddr())
                .bizType(entity.getBizType())
                .bizItem(entity.getBizItem())
                .telNum(entity.getTelNum())
                .inquiryTelNum(entity.getInquiryTelNum())
                .faxNum(entity.getFaxNum())
                .homepage(entity.getHomepage())
                .bankName(entity.getBankName())
                .accountNum(entity.getAccountNum())
                .accountHolder(entity.getAccountHolder())
                .build();
    }
}