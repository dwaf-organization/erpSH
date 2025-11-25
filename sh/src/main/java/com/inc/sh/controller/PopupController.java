package com.inc.sh.controller;

import com.inc.sh.dto.popup.reqDto.ItemSearchPopupDto;
import com.inc.sh.dto.popup.reqDto.CustomerSearchPopupDto;
import com.inc.sh.dto.popup.reqDto.VirtualAccountSearchPopupDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.item.respDto.ItemRespDto;
import com.inc.sh.dto.customer.respDto.CustomerRespDto;
import com.inc.sh.dto.virtualAccount.respDto.VirtualAccountRespDto;
import com.inc.sh.service.PopupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/popup")
@RequiredArgsConstructor
@Slf4j
public class PopupController {

    private final PopupService popupService;

    /**
     * 품목 팝업 검색
     * GET /api/v1/erp/popup/item-search
     */
    @GetMapping("/item-search")
    public ResponseEntity<RespDto<List<ItemRespDto>>> searchItems(
            @RequestParam(value = "itemCode", required = false) String itemCode,
            @RequestParam(value = "itemName", required = false) String itemName,
            @RequestParam(value = "categoryCode", required = false) Integer categoryCode,
            @RequestParam(value = "priceType", required = false) Integer priceType) {
        
        log.info("품목 팝업 검색 요청 - itemCode: {}, itemName: {}, categoryCode: {}, priceType: {}", 
                itemCode, itemName, categoryCode, priceType);
        
        ItemSearchPopupDto searchDto = ItemSearchPopupDto.builder()
                .itemCode(itemCode)
                .itemName(itemName)
                .categoryCode(categoryCode)
                .priceType(priceType)
                .build();
        
        RespDto<List<ItemRespDto>> response = popupService.searchItems(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 거래처 팝업 검색
     * GET /api/v1/erp/popup/customer-search
     */
    @GetMapping("/customer-search")
    public ResponseEntity<RespDto<List<CustomerRespDto>>> searchCustomers(
            @RequestParam(value = "customerCode", required = false) String customerCode,
            @RequestParam(value = "customerName", required = false) String customerName,
            @RequestParam(value = "brandCode", required = false) Integer brandCode) {
        
        log.info("거래처 팝업 검색 요청 - customerCode: {}, customerName: {}, brandCode: {}", 
                customerCode, customerName, brandCode);
        
        CustomerSearchPopupDto searchDto = CustomerSearchPopupDto.builder()
                .customerCode(customerCode)
                .customerName(customerName)
                .brandCode(brandCode)
                .build();
        
        RespDto<List<CustomerRespDto>> response = popupService.searchCustomers(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 가상계좌 팝업 검색
     * GET /api/v1/erp/popup/virtual-account-search
     */
    @GetMapping("/virtual-account-search")
    public ResponseEntity<RespDto<List<VirtualAccountRespDto>>> searchVirtualAccounts(
            @RequestParam(value = "virtualAccountCode", required = false) String virtualAccountCode,
            @RequestParam(value = "virtualAccount", required = false) String virtualAccount,
            @RequestParam(value = "accountStatus", required = false) String accountStatus) {
        
        log.info("가상계좌 팝업 검색 요청 - virtualAccountCode: {}, virtualAccount: {}, accountStatus: {}", 
                virtualAccountCode, virtualAccount, accountStatus);
        
        VirtualAccountSearchPopupDto searchDto = VirtualAccountSearchPopupDto.builder()
                .virtualAccountCode(virtualAccountCode)
                .virtualAccount(virtualAccount)
                .accountStatus(accountStatus)
                .build();
        
        RespDto<List<VirtualAccountRespDto>> response = popupService.searchVirtualAccounts(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}