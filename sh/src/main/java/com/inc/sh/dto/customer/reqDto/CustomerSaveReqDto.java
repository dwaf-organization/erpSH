package com.inc.sh.dto.customer.reqDto;

import com.inc.sh.entity.Customer;
import lombok.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSaveReqDto {
    
    private List<CustomerItemDto> customers;  // 거래처 배열
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerItemDto {
        // 수정시에만 사용 (null이면 신규 생성)
        private Integer customerCode;
        
        private Integer hqCode;                 // 본사코드 (필수)
        private Integer brandCode;              // 브랜드코드 (필수)
        private Integer virtualAccountCode;     // 가상계좌코드
        private String customerName;            // 거래처명 (필수)
        private String ownerName;               // 대표자 (필수)
        private String bizNum;                  // 사업자번호 (필수)
        private String zipCode;                 // 우편번호
        private String addr;                    // 주소
        private String bizType;                 // 업태
        private String bizSector;               // 업종
        private String email;                   // 이메일
        private String telNum;                  // 전화번호
        private String mobileNum;               // 핸드폰번호
        private String faxNum;                  // 부가세여부
        private String taxInvoiceYn;           // 세금계산서발행여부
        private String taxInvoiceName;          // 세금계산서명
        private String printNote;               // 출력비고
        private String bankName;                // 은행명
        private String accountHolder;           // 예금주
        private String accountNum;              // 계좌번호
        private Integer distCenterCode;         // 물류센터코드 (필수)
        private String deliveryWeekday;         // 배송요일
        private Integer depositTypeCode;        // 입금유형
        private String virtualAccount;          // 가상계좌
        private String virtualBankName;         // 가상계좌은행명
        private Integer balanceAmt;             // 계좌잔액
        private String hqMemo;                  // 본사메모
        private Integer creditLimit;            // 여신한도
        private Integer collectionDay;          // 회수기일
        private Integer orderBlockYn;           // 주문금지여부
        private String orderBlockReason;        // 주문금지사유
        private String orderBlockDt;            // 주문금지처리일시
        
        /**
         * DTO to Entity 변환 (신규 등록)
         */
        public Customer toEntity() {
            return Customer.builder()
                    .hqCode(this.hqCode)
                    .brandCode(this.brandCode)
                    .virtualAccountCode(this.virtualAccountCode)
                    .customerName(this.customerName)
                    .ownerName(this.ownerName)
                    .bizNum(this.bizNum)
                    .zipCode(this.zipCode)
                    .addr(this.addr)
                    .bizType(this.bizType)
                    .bizSector(this.bizSector)
                    .email(this.email)
                    .telNum(this.telNum)
                    .mobileNum(this.mobileNum)
                    .faxNum(this.faxNum)
                    .taxInvoiceYn(this.taxInvoiceYn)
                    .taxInvoiceName(this.taxInvoiceName)
                    .regDt(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                    .printNote(this.printNote)
                    .bankName(this.bankName)
                    .accountHolder(this.accountHolder)
                    .accountNum(this.accountNum)
                    .distCenterCode(this.distCenterCode)
                    .deliveryWeekday(this.deliveryWeekday != null ? this.deliveryWeekday : "1111111")
                    .depositTypeCode(this.depositTypeCode != null ? this.depositTypeCode : 0)
                    .virtualAccount(this.virtualAccount)
                    .virtualBankName(this.virtualBankName)
                    .balanceAmt(this.balanceAmt != null ? this.balanceAmt : 0)
                    .hqMemo(this.hqMemo)
                    .creditLimit(this.creditLimit != null ? this.creditLimit : 0)
                    .collectionDay(this.collectionDay != null ? this.collectionDay : 0)
                    .orderBlockYn(this.orderBlockYn != null ? this.orderBlockYn : 0)
                    .orderBlockReason(this.orderBlockReason)
                    .orderBlockDt(this.orderBlockDt)
                    .description("거래처등록")
                    .build();
        }
        
        /**
         * 기존 Entity 업데이트
         */
        public void updateEntity(Customer customer) {
            customer.setHqCode(this.hqCode);
            customer.setBrandCode(this.brandCode);
            customer.setVirtualAccountCode(this.virtualAccountCode);
            customer.setCustomerName(this.customerName);
            customer.setOwnerName(this.ownerName);
            customer.setBizNum(this.bizNum);
            customer.setZipCode(this.zipCode);
            customer.setAddr(this.addr);
            customer.setBizType(this.bizType);
            customer.setBizSector(this.bizSector);
            customer.setEmail(this.email);
            customer.setTelNum(this.telNum);
            customer.setMobileNum(this.mobileNum);
            customer.setFaxNum(this.faxNum);
            customer.setTaxInvoiceYn(this.taxInvoiceYn);
            customer.setTaxInvoiceName(this.taxInvoiceName);
            customer.setPrintNote(this.printNote);
            customer.setBankName(this.bankName);
            customer.setAccountHolder(this.accountHolder);
            customer.setAccountNum(this.accountNum);
            customer.setDistCenterCode(this.distCenterCode);
            if (this.deliveryWeekday != null) {
                customer.setDeliveryWeekday(this.deliveryWeekday);
            }
            if (this.depositTypeCode != null) {
                customer.setDepositTypeCode(this.depositTypeCode);
            }
            customer.setVirtualAccount(this.virtualAccount);
            customer.setVirtualBankName(this.virtualBankName);
            if (this.balanceAmt != null) {
                customer.setBalanceAmt(this.balanceAmt);
            }
            customer.setHqMemo(this.hqMemo);
            if (this.creditLimit != null) {
                customer.setCreditLimit(this.creditLimit);
            }
            if (this.collectionDay != null) {
                customer.setCollectionDay(this.collectionDay);
            }
            if (this.orderBlockYn != null) {
                customer.setOrderBlockYn(this.orderBlockYn);
            }
            customer.setOrderBlockReason(this.orderBlockReason);
            customer.setOrderBlockDt(this.orderBlockDt);
            customer.setDescription("거래처수정");
        }
    }
}