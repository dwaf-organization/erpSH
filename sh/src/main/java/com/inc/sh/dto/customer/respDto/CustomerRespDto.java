package com.inc.sh.dto.customer.respDto;


import com.inc.sh.entity.Customer;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRespDto {
    
    private Integer customerCode;
    private Integer hqCode;
    private Integer brandCode;
    private String customerName;
    private String ownerName;
    private String bizNum;
    private String zipCode;
    private String addr;
    private String bizType;
    private String bizSector;
    private String email;
    private String telNum;
    private String mobileNum;
    private String faxNum; // 부가세여부
    private Integer taxInvoiceYn;
    private String taxInvoiceName;
    private String regDt;
    private String closeDt;
    private String printNote;
    private String bankName;
    private String accountHolder;
    private String accountNum;
    private Integer distCenterCode;
    private String deliveryWeekday;
    private Integer depositTypeCode;
    private String virtualAccount;
    private String virtualBankName;
    private Integer balanceAmt;
    private String hqMemo;
    private Integer creditLimit;
    private Integer collectionDay;
    private Integer orderBlockYn;
    private String orderBlockReason;
    private String orderBlockDt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Entity to DTO 변환
     */
    public static CustomerRespDto fromEntity(Customer customer) {
        return CustomerRespDto.builder()
                .customerCode(customer.getCustomerCode())
                .hqCode(customer.getHqCode())
                .brandCode(customer.getBrandCode())
                .customerName(customer.getCustomerName())
                .ownerName(customer.getOwnerName())
                .bizNum(customer.getBizNum())
                .zipCode(customer.getZipCode())
                .addr(customer.getAddr())
                .bizType(customer.getBizType())
                .bizSector(customer.getBizSector())
                .email(customer.getEmail())
                .telNum(customer.getTelNum())
                .mobileNum(customer.getMobileNum())
                .faxNum(customer.getFaxNum())
                .taxInvoiceYn(customer.getTaxInvoiceYn())
                .taxInvoiceName(customer.getTaxInvoiceName())
                .regDt(customer.getRegDt())
                .closeDt(customer.getCloseDt())
                .printNote(customer.getPrintNote())
                .bankName(customer.getBankName())
                .accountHolder(customer.getAccountHolder())
                .accountNum(customer.getAccountNum())
                .distCenterCode(customer.getDistCenterCode())
                .deliveryWeekday(customer.getDeliveryWeekday())
                .depositTypeCode(customer.getDepositTypeCode())
                .virtualAccount(customer.getVirtualAccount())
                .virtualBankName(customer.getVirtualBankName())
                .balanceAmt(customer.getBalanceAmt())
                .hqMemo(customer.getHqMemo())
                .creditLimit(customer.getCreditLimit())
                .collectionDay(customer.getCollectionDay())
                .orderBlockYn(customer.getOrderBlockYn())
                .orderBlockReason(customer.getOrderBlockReason())
                .orderBlockDt(customer.getOrderBlockDt())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}