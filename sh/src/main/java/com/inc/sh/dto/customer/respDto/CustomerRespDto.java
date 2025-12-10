package com.inc.sh.dto.customer.respDto;

import com.inc.sh.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRespDto {
    
    private Integer customerCode;
    private Integer hqCode;
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
    private String faxNum;
    private String taxInvoiceYn;
    private String taxInvoiceName;
    private String regDt;
    private String closeDt;
    private String printNote;
    private String bankName;
    private String accountHolder;
    private String accountNum;
    private Integer distCenterCode;
    private String distCenterName;   // 물류센터명 (dist_center.dist_center_name)
    private Integer brandCode;
    private String brandName;        // 브랜드명 (brand_info.brand_name)
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
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 조인으로 추가되는 필드들


    
    /**
     * Entity를 DTO로 변환 (기본 변환 - 조인 필드 null)
     */
    public static CustomerRespDto fromEntity(Customer customer) {
        return CustomerRespDto.builder()
                .customerCode(customer.getCustomerCode())
                .hqCode(customer.getHqCode())
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
                .brandCode(customer.getBrandCode())
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
                .description(customer.getDescription())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                // 조인 필드는 null로 설정
                .brandName(null)
                .distCenterName(null)
                .build();
    }
}