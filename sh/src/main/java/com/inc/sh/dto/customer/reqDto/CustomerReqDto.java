package com.inc.sh.dto.customer.reqDto;


import com.inc.sh.entity.Customer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerReqDto {
    
    // 수정시에만 사용 (null이면 신규 생성)
    private Integer customerCode;
    
    @NotNull(message = "본사코드는 필수입니다")
    private Integer hqCode;
    
    @NotNull(message = "브랜드코드는 필수입니다")
    private Integer brandCode;
    
    @NotBlank(message = "거래처명은 필수입니다")
    private String customerName;
    
    @NotBlank(message = "대표자는 필수입니다")
    private String ownerName;
    
    @NotBlank(message = "사업자번호는 필수입니다")
    private String bizNum;
    
    private String zipCode;
    private String addr;
    private String bizType;
    private String bizSector;
    private String email;
    private String telNum;
    private String mobileNum;
    private String faxNum; // 부가세여부
    private String taxInvoiceYn; // 세금계산서발행여부
    private String taxInvoiceName;
    private String printNote;
    private String bankName;
    private String accountHolder;
    private String accountNum;
    
    @NotNull(message = "물류센터코드는 필수입니다")
    private Integer distCenterCode;
    
    private String deliveryWeekday; // 배송요일
    private Integer depositTypeCode; // 입금유형
    private Integer virtual_account_code;
    private String virtualAccount;
    private String virtualBankName;
    private Integer balanceAmt; // 계좌잔액
    private String hqMemo;
    private Integer creditLimit; // 여신한도
    private Integer collectionDay; // 회수기일
    private Integer orderBlockYn; // 주문금지여부
    private String orderBlockReason; // 주문금지사유
    private String orderBlockDt; // 주문금지처리일시
    
    /**
     * DTO to Entity 변환 (신규 등록)
     */
    public Customer toEntity() {
        return Customer.builder()
                .hqCode(this.hqCode)
                .brandCode(this.brandCode)
                .customerName(this.customerName)
                .virtualAccountCode(virtual_account_code)
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
                .deliveryWeekday(this.deliveryWeekday)
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
                .build();
    }
    
    /**
     * 기존 Entity 업데이트
     */
    public void updateEntity(Customer customer) {
        customer.setHqCode(this.hqCode);
        customer.setBrandCode(this.brandCode);
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
        if (this.taxInvoiceYn != null) {
            customer.setTaxInvoiceYn(this.taxInvoiceYn);
        }
        customer.setTaxInvoiceName(this.taxInvoiceName);
        customer.setPrintNote(this.printNote);
        customer.setBankName(this.bankName);
        customer.setAccountHolder(this.accountHolder);
        customer.setAccountNum(this.accountNum);
        customer.setDistCenterCode(this.distCenterCode);
        customer.setDeliveryWeekday(this.deliveryWeekday);
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
    }
}