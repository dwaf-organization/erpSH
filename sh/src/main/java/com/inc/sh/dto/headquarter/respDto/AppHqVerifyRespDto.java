package com.inc.sh.dto.headquarter.respDto;

import com.inc.sh.entity.Headquarter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * [앱전용] 본사접속코드 검증 응답 DTO
 * - hqCode: 본사코드
 * - companyName: 회사명
 * - inquiryTelNum: 고객센터 전화번호
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppHqVerifyRespDto {
    
    private Integer hqCode;
    private String companyName;
    private String inquiryTelNum;
    
    /**
     * Entity -> AppHqVerifyRespDto 변환
     */
    public static AppHqVerifyRespDto from(Headquarter entity) {
        return AppHqVerifyRespDto.builder()
                .hqCode(entity.getHqCode())
                .companyName(entity.getCompanyName())
                .inquiryTelNum(entity.getInquiryTelNum())
                .build();
    }
}