package com.inc.sh.dto.headquarter.reqDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeadquarterReqDto {
    
    @NotBlank(message = "법인명은 필수입니다")
    private String companyName;

    @NotBlank(message = "대표자는 필수입니다")
    private String ceoName;

    @NotBlank(message = "사업자번호는 필수입니다")
    private String bizNum;

    private String corpRegNum;

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
}