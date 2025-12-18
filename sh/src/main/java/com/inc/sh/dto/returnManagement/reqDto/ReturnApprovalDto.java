package com.inc.sh.dto.returnManagement.reqDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnApprovalDto {
    
    private List<String> returnNos;     // 반품번호 목록
    private String approvalAction;      // "승인" 또는 "미승인"
    private String approvalNote;        // 승인/미승인 사유 (선택사항)
}