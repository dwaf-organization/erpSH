package com.inc.sh.controller;

import com.inc.sh.dto.customerUser.reqDto.CustomerUserSearchDto;
import com.inc.sh.dto.customerUser.reqDto.CustomerUserSaveDto;
import com.inc.sh.dto.customerUser.respDto.CustomerUserRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.CustomerUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/customer-user")
@RequiredArgsConstructor
@Slf4j
public class CustomerUserController {

    private final CustomerUserService customerUserService;

    /**
     * 거래처사용자 조회
     * GET /api/v1/erp/customer-user/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<CustomerUserRespDto>>> getCustomerUserList(
            @RequestParam(value = "customerCode", required = false) Integer customerCode,
            @RequestParam(value = "customerUserId", required = false) String customerUserId) {
        
        log.info("거래처사용자 조회 요청 - 거래처코드: {}, 아이디: {}", customerCode, customerUserId);
        
        CustomerUserSearchDto searchDto = CustomerUserSearchDto.builder()
                .customerCode(customerCode)
                .customerUserId(customerUserId)
                .build();
        
        RespDto<List<CustomerUserRespDto>> response = customerUserService.getCustomerUserList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 거래처사용자 저장 (신규/수정)
     * POST /api/v1/erp/customer-user/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<String>> saveCustomerUser(@RequestBody CustomerUserSaveDto saveDto) {
        
        log.info("거래처사용자 저장 요청 - 사용자코드: {}, 아이디: {}", saveDto.getCustomerUserCode(), saveDto.getCustomerUserId());
        
        RespDto<String> response = customerUserService.saveCustomerUser(saveDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 거래처사용자 삭제
     * DELETE /api/v1/erp/customer-user/{customerUserCode}
     */
    @DeleteMapping("/{customerUserCode}")
    public ResponseEntity<RespDto<String>> deleteCustomerUser(@PathVariable("customerUserCode") Integer customerUserCode) {
        
        log.info("거래처사용자 삭제 요청 - 사용자코드: {}", customerUserCode);
        
        RespDto<String> response = customerUserService.deleteCustomerUser(customerUserCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}