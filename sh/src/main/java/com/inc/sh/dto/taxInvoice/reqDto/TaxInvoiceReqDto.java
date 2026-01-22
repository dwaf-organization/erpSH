package com.inc.sh.dto.taxInvoice.reqDto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxInvoiceReqDto {
    
    @NotBlank(message = "조회시작일자는 필수입니다")
    private String startDate;               // 조회 시작일 (YYYYMMDD)
    
    @NotBlank(message = "조회종료일자는 필수입니다")
    private String endDate;                 // 조회 종료일 (YYYYMMDD)
    
    @NotNull(message = "본사코드는 필수입니다")
    private Integer hqCode;                 // 본사코드 (필수)
    
    private Integer customerCode;           // 거래처코드 (선택, null이면 전체 거래처)
    
    private List<Integer> itemCodes;        // 품목코드 리스트 (선택, null이면 전체 품목)
}