package com.inc.sh.controller.app;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.mypage.respDto.AppMyPageInfoRespDto;
import com.inc.sh.service.app.AppMyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/app/mypage")
@RequiredArgsConstructor
@Slf4j
public class AppMyPageController {
    
    private final AppMyPageService appMyPageService;
    
    /**
     * [앱전용] 내정보조회
     * GET /api/v1/app/mypage/info
     */
    @GetMapping("/info")
    public ResponseEntity<RespDto<AppMyPageInfoRespDto>> getMyPageInfo(
            @RequestParam("customerCode") Integer customerCode,
            @RequestParam("customerUserCode") Integer customerUserCode) {
        
        log.info("[앱] 내정보조회 - customerCode: {}, customerUserCode: {}", customerCode, customerUserCode);
        
        RespDto<AppMyPageInfoRespDto> response = appMyPageService.getMyPageInfo(customerCode, customerUserCode);
        
        return ResponseEntity.ok(response);
    }
}