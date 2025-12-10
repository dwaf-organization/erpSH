package com.inc.sh.controller;

import com.inc.sh.dto.popup.reqDto.ItemSearchPopupDto;
import com.inc.sh.dto.popup.reqDto.CustomerItemSearchDto;
import com.inc.sh.dto.popup.reqDto.CustomerSearchPopupDto;
import com.inc.sh.dto.popup.reqDto.VirtualAccountSearchPopupDto;
import com.inc.sh.dto.popup.respDto.CustomerItemRespDto;
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
    		@RequestParam("hqCode") Integer hqCode,
            @RequestParam(value = "item", required = false) String item,
            @RequestParam(value = "categoryCode", required = false) Integer categoryCode,
            @RequestParam(value = "priceType", required = false) Integer priceType) {
        
        log.info("품목 팝업 검색 요청 - item: {}, categoryCode: {}, priceType: {}", 
                item, categoryCode, priceType);
        
        ItemSearchPopupDto searchDto = ItemSearchPopupDto.builder()
                .hqCode(hqCode)
                .item(item)
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
     * 거래처 팝업 검색 (수정됨 - hqCode 추가)
     * GET /api/v1/erp/popup/customer-search
     */
    @GetMapping("/customer-search")
    public ResponseEntity<RespDto<List<CustomerRespDto>>> searchCustomers(
            @RequestParam("hqCode") Integer hqCode,
            @RequestParam(value = "customerSearch", required = false) String customerSearch,
            @RequestParam(value = "brandCode", required = false) String brandCode) {
        
        log.info("거래처 팝업 검색 요청 - hqCode: {}, customerSearch: {}, brandCode: {}", 
                hqCode, customerSearch, brandCode);
        
        CustomerSearchPopupDto searchDto = CustomerSearchPopupDto.builder()
                .hqCode(hqCode)
                .customerSearch(customerSearch)
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
     * 가상계좌 팝업 검색 (수정됨 - hqCode 추가, 파라미터명 수정)
     * GET /api/v1/erp/popup/virtual-account-search
     */
    @GetMapping("/virtual-account-search")
    public ResponseEntity<RespDto<List<VirtualAccountRespDto>>> searchVirtualAccounts(
            @RequestParam("hqCode") Integer hqCode,
            @RequestParam(value = "virtualAccountNum", required = false) String virtualAccountNum,
            @RequestParam(value = "virtualAccountStatus", required = false) String virtualAccountStatus) {
        
        log.info("가상계좌 팝업 검색 요청 - hqCode: {}, virtualAccountNum: {}, virtualAccountStatus: {}", 
                hqCode, virtualAccountNum, virtualAccountStatus);
        
        VirtualAccountSearchPopupDto searchDto = VirtualAccountSearchPopupDto.builder()
                .hqCode(hqCode)
                .virtualAccountNum(virtualAccountNum)
                .virtualAccountStatus(virtualAccountStatus)
                .build();
        
        RespDto<List<VirtualAccountRespDto>> response = popupService.searchVirtualAccounts(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 거래처품목조회 팝업
     * GET /api/v1/erp/popup/customer/item-search
     */
    @GetMapping("/customer/item-search")
    public ResponseEntity<RespDto<List<CustomerItemRespDto>>> searchCustomerItems(
            @RequestParam("hqCode") Integer hqCode,
            @RequestParam("customerCode") Integer customerCode,
            @RequestParam(value = "item", required = false) String item,
            @RequestParam(value = "warehouseCode", required = false) Integer warehouseCode,
            @RequestParam(value = "categoryCode", required = false) Integer categoryCode,
            @RequestParam(value = "priceType", required = false) Integer priceType) {
        
        log.info("거래처품목조회 팝업 요청 - hqCode: {}, customerCode: {}, item: {}, warehouseCode: {}, categoryCode: {}, priceType: {}", 
                hqCode, customerCode, item, warehouseCode, categoryCode, priceType);
        
        CustomerItemSearchDto searchDto = CustomerItemSearchDto.builder()
                .hqCode(hqCode)
                .customerCode(customerCode)
                .item(item)
                .warehouseCode(warehouseCode)
                .categoryCode(categoryCode)
                .priceType(priceType)
                .build();
        
        RespDto<List<CustomerItemRespDto>> response = popupService.searchCustomerItems(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
}