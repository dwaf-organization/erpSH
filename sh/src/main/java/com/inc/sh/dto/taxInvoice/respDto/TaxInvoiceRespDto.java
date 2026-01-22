package com.inc.sh.dto.taxInvoice.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxInvoiceRespDto {
    
    private Integer customerCode;           // 거래처코드
    private String customerName;            // 거래처명
    private String ownerName;               // 대표자명
    private String bizNum;                  // 사업자번호
    private String addr;                    // 주소
    
    private String itemNames;               // 품목명 ("사과 외 2개" 형식)
    
    private Integer taxFreeSupplyAmt;       // 면세공급가
    private Integer taxableSupplyAmt;       // 과세공급가
    private Integer vatAmt;                 // 부가세
    private Integer taxableTotalAmt;        // 과세합계금액 (과세공급가 + 부가세)
    private Integer totalAmt;               // 합계금액 (면세공급가 + 과세합계금액)
    
    /**
     * Entity 데이터로부터 DTO 생성
     */
    public static TaxInvoiceRespDto fromQueryResult(
            Integer customerCode, String customerName, String ownerName, 
            String bizNum, String addr, String itemNames,
            Integer taxFreeSupplyAmt, Integer taxableSupplyAmt, Integer vatAmt) {
        
        Integer taxableTotalAmt = taxableSupplyAmt + vatAmt;
        Integer totalAmt = taxFreeSupplyAmt + taxableTotalAmt;
        
        return TaxInvoiceRespDto.builder()
                .customerCode(customerCode)
                .customerName(customerName)
                .ownerName(ownerName)
                .bizNum(bizNum)
                .addr(addr)
                .itemNames(itemNames)
                .taxFreeSupplyAmt(taxFreeSupplyAmt)
                .taxableSupplyAmt(taxableSupplyAmt)
                .vatAmt(vatAmt)
                .taxableTotalAmt(taxableTotalAmt)
                .totalAmt(totalAmt)
                .build();
    }
}