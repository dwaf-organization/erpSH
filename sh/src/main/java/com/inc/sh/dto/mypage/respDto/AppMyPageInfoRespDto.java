package com.inc.sh.dto.mypage.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppMyPageInfoRespDto {
    
    // 거래처 정보
    private String customerName;        // 상호명
    private String ownerName;           // 대표자
    private String bizNum;              // 사업자번호
    private String bizType;             // 업태
    private String bizSector;           // 업종
    private String accountInfo;         // 입금계좌 (은행명 계좌번호 예금주)
    private String mobileNum;           // 연락처
    private String zipCode;             // 우편번호
    private String addr;                // 주소
    private String email;               // 이메일
    private String telNum;              // 전화
    
    // 사용자 정보
    private String customerUserId;      // 아이디
}