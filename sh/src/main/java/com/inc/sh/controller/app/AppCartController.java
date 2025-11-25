package com.inc.sh.controller.app;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.cart.reqDto.CartAddReqDto;
import com.inc.sh.dto.cart.respDto.CartRespDto;
import com.inc.sh.service.app.AppCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/app/cart")
@RequiredArgsConstructor
@Slf4j
public class AppCartController {
    
    private final AppCartService appCartService;
    
    /**
     * 장바구니 담기
     * POST /api/v1/app/cart/add
     */
    @PostMapping("/add")
    public ResponseEntity<RespDto<Integer>> addToCart(@RequestBody CartAddReqDto request) {
        
        log.info("[앱] 장바구니 담기 요청 - customerCode: {}, itemCode: {}, warehouseCode: {}", 
                request.getCustomerCode(), request.getItemCode(), request.getWarehouseCode());
        
        RespDto<Integer> response = appCartService.addToCart(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 장바구니 조회
     * GET /api/v1/app/cart/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<CartRespDto>>> getCartList(
            @RequestParam("customerCode") Integer customerCode,
            @RequestParam("customerUserCode") Integer customerUserCode) {
        
        log.info("[앱] 장바구니 조회 요청 - customerCode: {}, customerUserCode: {}", 
                customerCode, customerUserCode);
        
        RespDto<List<CartRespDto>> response = appCartService.getCartList(customerCode, customerUserCode);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 장바구니 삭제
     * DELETE /api/v1/app/cart/{customerCartCode}
     */
    @DeleteMapping("/{customerCartCode}")
    public ResponseEntity<RespDto<Void>> deleteCartItem(@PathVariable("customerCartCode") Integer customerCartCode) {
        
        log.info("[앱] 장바구니 삭제 요청 - customerCartCode: {}", customerCartCode);
        
        RespDto<Void> response = appCartService.deleteCartItem(customerCartCode);
        
        return ResponseEntity.ok(response);
    }
}