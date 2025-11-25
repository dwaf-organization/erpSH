package com.inc.sh.controller;

import com.inc.sh.dto.user.reqDto.UserSearchDto;
import com.inc.sh.dto.user.reqDto.UserSaveDto;
import com.inc.sh.dto.user.respDto.UserRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 사용자 조회
     * GET /api/v1/erp/user/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<UserRespDto>>> getUserList(
            @RequestParam(value = "userCode", required = false) String userCode,
            @RequestParam(value = "userName", required = false) String userName) {
        
        log.info("사용자 조회 요청 - 사번: {}, 성명: {}", userCode, userName);
        
        UserSearchDto searchDto = UserSearchDto.builder()
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
     * 사용자 저장 (신규/수정)
     * POST /api/v1/erp/user/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<String>> saveUser(@RequestBody UserSaveDto saveDto) {
        
        log.info("사용자 저장 요청 - 사번: {}, 성명: {}", saveDto.getUserCode(), saveDto.getUserName());
        
        RespDto<String> response = userService.saveUser(saveDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 사용자 삭제
     * DELETE /api/v1/erp/user/{userCode}
     */
    @DeleteMapping("/{userCode}")
    public ResponseEntity<RespDto<String>> deleteUser(@PathVariable("userCode") String userCode) {
        
        log.info("사용자 삭제 요청 - 사번: {}", userCode);
        
        RespDto<String> response = userService.deleteUser(userCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}