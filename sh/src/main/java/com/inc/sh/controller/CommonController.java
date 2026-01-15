package com.inc.sh.controller;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.common.respDto.BrandSelectDto;
import com.inc.sh.dto.common.respDto.DistCenterSelectDto;
import com.inc.sh.dto.common.respDto.HqSelectDto;
import com.inc.sh.dto.common.respDto.WarehouseSelectDto;
import com.inc.sh.dto.common.respDto.ItemCategorySelectDto;
import com.inc.sh.dto.common.respDto.RoleSelectDto;
import com.inc.sh.dto.common.respDto.VehicleSelectDto;
import com.inc.sh.dto.common.respDto.OrderSelectDto;
import com.inc.sh.service.CommonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/common")
@RequiredArgsConstructor
@Slf4j
public class CommonController {

    private final CommonService commonService;

    /**
     * 본사 셀렉트박스 목록 조회
     * GET /api/v1/erp/common/hq-list
     */
    @GetMapping("/hq-list")
    public ResponseEntity<RespDto<List<HqSelectDto>>> getHqSelectList() {
        
        log.info("본사 셀렉트박스 목록 조회 요청");
        
        RespDto<List<HqSelectDto>> response = commonService.getHqSelectList();
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 브랜드 셀렉트박스 목록 조회
     * GET /api/v1/erp/common/brand-list?hqCode=1
     */
    @GetMapping("/brand-list")
    public ResponseEntity<RespDto<List<BrandSelectDto>>> getBrandSelectList(
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("브랜드 셀렉트박스 목록 조회 요청 - hqCode: {}", hqCode);
        
        RespDto<List<BrandSelectDto>> response = commonService.getBrandSelectList(hqCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 물류센터 셀렉트박스 목록 조회
     * GET /api/v1/erp/common/dist-center-list?hqCode=1
     */
    @GetMapping("/dist-center-list")
    public ResponseEntity<RespDto<List<DistCenterSelectDto>>> getDistCenterSelectList(
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("물류센터 셀렉트박스 목록 조회 요청 - hqCode: {}", hqCode);
        
        RespDto<List<DistCenterSelectDto>> response = commonService.getDistCenterSelectList(hqCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 창고 셀렉트박스 목록 조회 (본사별)
     * GET /api/v1/erp/common/warehouse-list?hqCode=1
     */
    @GetMapping("/warehouse-list")
    public ResponseEntity<RespDto<List<WarehouseSelectDto>>> getWarehouseSelectList(
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("창고 셀렉트박스 목록 조회 요청 - hqCode: {}", hqCode);
        
        RespDto<List<WarehouseSelectDto>> response = commonService.getWarehouseSelectList(hqCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 창고 셀렉트박스 목록 조회 (물류센터별)
     * GET /api/v1/erp/common/warehouse-list-by-dist-center?distCenterCode=1
     */
    @GetMapping("/warehouse-list-by-dist-center")
    public ResponseEntity<RespDto<List<WarehouseSelectDto>>> getWarehouseSelectListByDistCenter(
            @RequestParam("distCenterCode") Integer distCenterCode) {
        
        log.info("창고 셀렉트박스 목록 조회(물류센터별) 요청 - distCenterCode: {}", distCenterCode);
        
        RespDto<List<WarehouseSelectDto>> response = commonService.getWarehouseSelectListByDistCenter(distCenterCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 품목분류 셀렉트박스 목록 조회 (중분류만, 대분류명 포함)
     * GET /api/v1/erp/common/item-category-list?hqCode=1
     */
    @GetMapping("/item-category-list")
    public ResponseEntity<RespDto<List<ItemCategorySelectDto>>> getItemCategorySelectList(
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("품목분류 셀렉트박스 목록 조회 요청 - hqCode: {}", hqCode);
        
        RespDto<List<ItemCategorySelectDto>> response = commonService.getItemCategorySelectList(hqCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 권한 셀렉트박스 목록 조회
     * GET /api/v1/erp/common/role-list?hqCode=1
     */
    @GetMapping("/role-list")
    public ResponseEntity<RespDto<List<RoleSelectDto>>> getRoleSelectList(
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("권한 셀렉트박스 목록 조회 요청 - hqCode: {}", hqCode);
        
        RespDto<List<RoleSelectDto>> response = commonService.getRoleSelectList(hqCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 차량 셀렉트박스 목록 조회
     * GET /api/v1/erp/common/vehicle-list?hqCode=1
     */
    @GetMapping("/vehicle-list")
    public ResponseEntity<RespDto<List<VehicleSelectDto>>> getVehicleSelectList(
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("차량 셀렉트박스 목록 조회 요청 - hqCode: {}", hqCode);
        
        RespDto<List<VehicleSelectDto>> response = commonService.getVehicleList(hqCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 주문번호 셀렉트박스 목록 조회 (반품등록용)
     * GET /api/v1/erp/common/order-list?customerCode=1&hqCode=1
     */
    @GetMapping("/order-list")
    public ResponseEntity<RespDto<List<OrderSelectDto>>> getOrderSelectList(
            @RequestParam("customerCode") Integer customerCode,
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("주문번호 셀렉트박스 목록 조회 요청 - customerCode: {}, hqCode: {}", customerCode, hqCode);
        
        RespDto<List<OrderSelectDto>> response = commonService.getOrderList(customerCode, hqCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}