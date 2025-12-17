package com.inc.sh.controller;

import com.inc.sh.dto.user.reqDto.UserSearchDto;
import com.inc.sh.dto.user.reqDto.UserSaveReqDto;
import com.inc.sh.dto.user.reqDto.UserDeleteReqDto;
import com.inc.sh.dto.user.respDto.UserRespDto;
import com.inc.sh.dto.user.respDto.UserBatchResult;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/erp/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 사용자 조회 (hqCode 필수)
     * GET /api/v1/erp/user/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<UserRespDto>>> getUserList(
            @RequestParam("hqCode") Integer hqCode,
            @RequestParam(value = "userCode", required = false) String userCode,
            @RequestParam(value = "userName", required = false) String userName) {
        
        log.info("사용자 조회 요청 - hqCode: {}, 사번: {}, 성명: {}", hqCode, userCode, userName);
        
        UserSearchDto searchDto = UserSearchDto.builder()
                .hqCode(hqCode)
                .userCode(userCode)
                .userName(userName)
                .build();
        
        RespDto<List<UserRespDto>> response = userService.getUserList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 사용자 다중 저장 (신규/수정)
     * POST /api/v1/erp/user/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<UserBatchResult>> saveUsers(@RequestBody UserSaveReqDto request) {
        
        log.info("사용자 다중 저장 요청 - 총 {}건", 
                request.getUsers() != null ? request.getUsers().size() : 0);
        
        // 요청 데이터 검증
        if (request.getUsers() == null || request.getUsers().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("저장할 사용자 데이터가 없습니다."));
        }
        
        // 개별 항목 필수 필드 검증
        for (int i = 0; i < request.getUsers().size(); i++) {
            UserSaveReqDto.UserSaveItemDto item = request.getUsers().get(i);
            
            if (item.getUserName() == null || item.getUserName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail(String.format("%d번째 항목: 성명은 필수입니다.", i + 1)));
            }
            if (item.getHqCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail(String.format("%d번째 항목: 본사코드는 필수입니다.", i + 1)));
            }
            if (item.getRoleCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail(String.format("%d번째 항목: 사용자권한코드는 필수입니다.", i + 1)));
            }
            
            // 신규 시 비밀번호 필수 체크
            boolean isUpdate = item.getUserCode() != null && !item.getUserCode().trim().isEmpty();
            if (!isUpdate && (item.getUserPw() == null || item.getUserPw().trim().isEmpty())) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail(String.format("%d번째 항목: 신규 사용자는 비밀번호가 필수입니다.", i + 1)));
            }
        }
        
        RespDto<UserBatchResult> response = userService.saveUsers(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 다중 삭제
     * DELETE /api/v1/erp/user/delete
     */
    @DeleteMapping("/delete")
    public ResponseEntity<RespDto<UserBatchResult>> deleteUsers(@RequestBody UserDeleteReqDto request) {
        
        log.info("사용자 다중 삭제 요청 - 총 {}건", 
                request.getUserCodes() != null ? request.getUserCodes().size() : 0);
        
        // 요청 데이터 검증
        if (request.getUserCodes() == null || request.getUserCodes().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("삭제할 사번이 없습니다."));
        }
        
        // 중복 제거 및 null/빈값 제거
        List<String> validUserCodes = request.getUserCodes().stream()
                .filter(Objects::nonNull)
                .filter(userCode -> !userCode.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
        
        if (validUserCodes.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("유효한 사번이 없습니다."));
        }
        
        if (validUserCodes.size() != request.getUserCodes().size()) {
            log.info("중복/null 사번 제거됨 - 원본: {}건, 제거 후: {}건", 
                    request.getUserCodes().size(), validUserCodes.size());
            request.setUserCodes(validUserCodes);
        }
        
        RespDto<UserBatchResult> response = userService.deleteUsers(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 장부대장 사용여부 조회
     * GET /api/v1/erp/user/ledger-usage
     */
    @GetMapping("/ledger-usage")
    public ResponseEntity<RespDto<Integer>> getLedgerUsage(
            @RequestParam("hqCode") Integer hqCode,
            @RequestParam("userCode") String userCode) {
        
        log.info("장부대장 사용여부 조회 요청 - hqCode: {}, userCode: {}", hqCode, userCode);
        
        RespDto<Integer> response = userService.getLedgerUsage(hqCode, userCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}