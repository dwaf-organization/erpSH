package com.inc.sh.dto.taxInvoice.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxInvoiceExcelRespDto {
    
    // 고정값들
    private String invoiceType;             // 전자세금계산서종류: "05" (고정)
    private String supplierBranchNum;       // 공급자종사업장번호: null (고정)
    private String receiptType;             // 영수: "01" (고정)
    
    // 요청에서 오는 값들
    private String issueDate;               // 작성일자: YYYYMMDD
    private String dayOnly;                 // 일자: DD (작성일자의 DD 부분)
    
    // 본사 정보 (Headquarter 테이블에서 조회)
    private String hqBizNum;                // 공급받는자등록번호: 본사 사업자번호
    private String supplierCompanyName;     // 공급자상호: 본사 상호
    private String supplierCeoName;         // 공급자성명: 본사 대표자명
    private String supplierAddr;            // 공급자사업장주소: 본사 주소
    private String supplierBizType;         // 공급자업태: 본사 업태
    private String supplierBizSector;       // 공급자업종: 본사 업종
    private String supplierEmail;           // 공급자이메일: 본사 이메일
    
    // 거래처 정보 (Customer 테이블에서 조회)
    private String customerBizNum;          // 공급받는자등록번호: 거래처 사업자번호
    private String customerName;			// 공급받는자거래처명
    private String customerOwnerName;		// 공급받는자성명
    private String customerAddr;			// 공급받는자주소
    private String customerBizType;         // 공급받는자업태: 거래처 업태
    private String customerBizSector;       // 공급받는자업종: 거래처 업종
    private String customerEmail;           // 공급받는자이메일1: 거래처 이메일
    
    // 계산된 값들
    private Integer supplyAmount;           // 공급가액: 면세면 면세공급가액합계, 과세면 과세공급가액합계
    private String itemNames;               // 품목1: 주문품목명
    
    /**
     * 쿼리 결과와 추가 정보로부터 DTO 생성
     */
    public static TaxInvoiceExcelRespDto fromQueryResult(
            // 본사 정보
            String hqBizNum, String supplierCompanyName, String supplierCeoName, 
            String supplierAddr, String supplierBizType, String supplierBizSector, String supplierEmail,
            // 거래처 정보
            String customerBizNum, String customerName, String customerOwnerName, String customerAddr, String customerBizType, String customerBizSector, String customerEmail,
            // 계산된 정보
            String itemNames, Integer supplyAmount, String issueDate) {
        
        // 일자 계산 (YYYYMMDD → DD)
        String dayOnly = issueDate.length() >= 8 ? issueDate.substring(6, 8) : "";
        
        return TaxInvoiceExcelRespDto.builder()
                // 고정값
                .invoiceType("05")
                .supplierBranchNum(null)
                .receiptType("01")
                // 요청값
                .issueDate(issueDate)
                .dayOnly(dayOnly)
                // 본사 정보
                .hqBizNum(hqBizNum)
                .supplierCompanyName(supplierCompanyName)
                .supplierCeoName(supplierCeoName)
                .supplierAddr(supplierAddr)
                .supplierBizType(supplierBizType)
                .supplierBizSector(supplierBizSector)
                .supplierEmail(supplierEmail)
                // 거래처 정보
                .customerBizNum(customerBizNum)
                .customerName(customerName)
                .customerOwnerName(customerOwnerName)
                .customerAddr(customerAddr)
                .customerBizType(customerBizType)
                .customerBizSector(customerBizSector)
                .customerEmail(customerEmail)
                // 계산값
                .supplyAmount(supplyAmount)
                .itemNames(itemNames)
                .build();
    }
}