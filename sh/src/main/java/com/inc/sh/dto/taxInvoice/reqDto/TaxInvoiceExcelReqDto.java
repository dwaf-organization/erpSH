package com.inc.sh.dto.taxInvoice.reqDto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxInvoiceExcelReqDto {
    
    @NotBlank(message = "조회시작일자는 필수입니다")
    private String startDate;               // 조회 시작일 (YYYYMMDD)
    
    @NotBlank(message = "조회종료일자는 필수입니다")
    private String endDate;                 // 조회 종료일 (YYYYMMDD)
    
    @NotNull(message = "본사코드는 필수입니다")
    private Integer hqCode;                 // 본사코드 (필수)
    
    private Integer customerCode;           // 거래처코드 (선택, null이면 전체 거래처)
    
    private List<Integer> itemCodes;        // 품목코드 리스트 (선택, null이면 전체 품목)
    
    @NotBlank(message = "세금계산서 구분은 필수입니다")
    @Pattern(regexp = "^(면세|과세)$", message = "세금계산서 구분은 '면세' 또는 '과세'만 가능합니다")
    private String taxType;                 // 세금계산서 구분 ("면세" 또는 "과세") - 필수
    
    @NotBlank(message = "작성일자는 필수입니다")
    @Pattern(regexp = "^\\d{8}$", message = "작성일자는 YYYYMMDD 형식이어야 합니다")
    private String issueDate;               // 작성일자 (YYYYMMDD) - 필수
}