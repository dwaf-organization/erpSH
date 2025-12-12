package com.inc.sh.dto.headquarter.respDto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminHeadquarterDetailRespDto {
    
    private Integer hqCode;                 // 본사코드
    private String hqAccessCode;            // 본사접속코드
    private String companyName;             // 회사명
    private String corpRegNum;              // 법인등록번호
    private String bizNum;                  // 사업자번호
    private String ceoName;                 // 대표자명
    private String zipCode;                 // 우편번호
    private String addr;                    // 주소
    private String bizType;                 // 업태
    private String bizItem;                 // 종목
    private String telNum;                  // 전화번호
    private String inquiryTelNum;           // 문의전화번호
    private String faxNum;                  // 팩스번호
    private String homepage;                // 홈페이지
    private String bankName;                // 은행명
    private String accountNum;              // 계좌번호
    private String accountHolder;           // 예금주
    private Integer logisticsType;          // 물류유형
    private String priceDisplayType;        // 가격표시유형
    private String description;             // 설명
    private LocalDateTime createdAt;        // 생성일시
    private LocalDateTime updatedAt;        // 수정일시
}