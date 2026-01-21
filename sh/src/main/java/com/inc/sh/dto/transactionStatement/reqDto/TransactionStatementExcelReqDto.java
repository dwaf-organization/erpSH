package com.inc.sh.dto.transactionStatement.reqDto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionStatementExcelReqDto {
    
    private List<String> orderNumbers;  // 주문번호 리스트 (필수)
    private Integer hqCode;             // 본사코드 (필수)
}