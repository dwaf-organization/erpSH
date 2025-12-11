package com.inc.sh.dto.customerAdjustment.reqDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAdjustmentDeleteReqDto {
    
    private List<Integer> transactionCodes;    // 삭제할 거래내역코드 배열
}