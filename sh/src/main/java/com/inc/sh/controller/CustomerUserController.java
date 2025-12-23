package com.inc.sh.controller;

import com.inc.sh.dto.customerUser.reqDto.CustomerUserSearchDto;
import com.inc.sh.dto.customerUser.reqDto.CustomerUserSaveReqDto;
import com.inc.sh.dto.customerUser.reqDto.CustomerUserDeleteReqDto;
import com.inc.sh.dto.customerUser.respDto.CustomerUserRespDto;
import com.inc.sh.dto.customerUser.respDto.CustomerUserBatchResult;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.CustomerUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/erp/customer-user")
@RequiredArgsConstructor
@Slf4j
public class CustomerUserController {

    private final CustomerUserService customerUserService;

    /**
     * 거래처사용자 조회 (customerCode 필수)
     * GET /api/v1/erp/customer-user/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<CustomerUserRespDto>>> getCustomerUserList(
            @RequestParam("customerCode") Integer customerCode,
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
     * 거래처사용자 다중 저장 (신규/수정)
     * POST /api/v1/erp/customer-user/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<CustomerUserBatchResult>> saveCustomerUsers(@RequestBody CustomerUserSaveReqDto request) {
        
        log.info("거래처사용자 다중 저장 요청 - 총 {}건", 
                request.getCustomerUsers() != null ? request.getCustomerUsers().size() : 0);
        
        // 요청 데이터 검증
        if (request.getCustomerUsers() == null || request.getCustomerUsers().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("저장할 거래처사용자 데이터가 없습니다."));
        }
        
        // 개별 항목 필수 필드 검증
        for (int i = 0; i < request.getCustomerUsers().size(); i++) {
            CustomerUserSaveReqDto.CustomerUserSaveItemDto item = request.getCustomerUsers().get(i);
            
            if (item.getCustomerCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail(String.format("%d번째 항목: 거래처코드는 필수입니다.", i + 1)));
            }
            if (item.getCustomerUserId() == null || item.getCustomerUserId().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail(String.format("%d번째 항목: 사용자아이디는 필수입니다.", i + 1)));
            }
            if (item.getCustomerUserName() == null || item.getCustomerUserName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail(String.format("%d번째 항목: 사용자명은 필수입니다.", i + 1)));
            }
            
            // 기본값 설정
            if (item.getEndYn() == null) {
                item.setEndYn(0); // 기본값: 사용중
            }
        }
        
        RespDto<CustomerUserBatchResult> response = customerUserService.saveCustomerUsers(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 거래처사용자 다중 삭제
     * DELETE /api/v1/erp/customer-user/delete
     */
    @DeleteMapping("/delete")
    public ResponseEntity<RespDto<CustomerUserBatchResult>> deleteCustomerUsers(@RequestBody CustomerUserDeleteReqDto request) {
        
        log.info("거래처사용자 다중 삭제 요청 - 총 {}건", 
                request.getCustomerUserCodes() != null ? request.getCustomerUserCodes().size() : 0);
        
        // 요청 데이터 검증
        if (request.getCustomerUserCodes() == null || request.getCustomerUserCodes().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("삭제할 사용자코드가 없습니다."));
        }
        
        // 중복 제거 및 null 제거
        List<Integer> validUserCodes = request.getCustomerUserCodes().stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        
        if (validUserCodes.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("유효한 사용자코드가 없습니다."));
        }
        
        if (validUserCodes.size() != request.getCustomerUserCodes().size()) {
            log.info("중복/null 사용자코드 제거됨 - 원본: {}건, 제거 후: {}건", 
                    request.getCustomerUserCodes().size(), validUserCodes.size());
            request.setCustomerUserCodes(validUserCodes);
        }
        
        RespDto<CustomerUserBatchResult> response = customerUserService.deleteCustomerUsers(request);
        return ResponseEntity.ok(response);
    }
}