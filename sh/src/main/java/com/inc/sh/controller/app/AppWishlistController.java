package com.inc.sh.controller.app;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.wishlist.reqDto.WishlistAddReqDto;
import com.inc.sh.dto.wishlist.respDto.WishlistRespDto;
import com.inc.sh.service.app.AppWishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/app/wishlist")
@RequiredArgsConstructor
@Slf4j
public class AppWishlistController {
    
    private final AppWishlistService appWishlistService;
    
    /**
     * 위시리스트 추가
     * POST /api/v1/app/wishlist/add
     */
    @PostMapping("/add")
    public ResponseEntity<RespDto<WishlistRespDto>> addWishlist(@RequestBody WishlistAddReqDto request) {
        log.info("[앱] 위시리스트 추가 요청 - customerCode: {}, customerUserCode: {}, itemCode: {}", 
                request.getCustomerCode(), request.getCustomerUserCode(), request.getItemCode());
        
        RespDto<WishlistRespDto> response = appWishlistService.addWishlist(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 위시리스트 삭제
     * DELETE /api/v1/app/wishlist/{customer_wishlist_code}
     */
    @DeleteMapping("/{customer_wishlist_code}")
    public ResponseEntity<RespDto<Void>> deleteWishlist(@PathVariable("customer_wishlist_code") Integer customerWishlistCode) {
        log.info("[앱] 위시리스트 삭제 요청 - customerWishlistCode: {}", customerWishlistCode);
        
        RespDto<Void> response = appWishlistService.deleteWishlist(customerWishlistCode);
        
        return ResponseEntity.ok(response);
    }
}