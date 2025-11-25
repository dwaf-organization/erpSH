package com.inc.sh.dto.contact.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppContactInfoRespDto {
    
    private String companyName;         // 법인명
    private String ceoName;             // 대표자
    private String bizType;             // 업태
    private String bizItem;             // 업종
    private String homepage;            // 홈페이지주소
    private String faxNum;              // 팩스번호
    private String inquiryTelNum;       // 고객센터전화번호
}