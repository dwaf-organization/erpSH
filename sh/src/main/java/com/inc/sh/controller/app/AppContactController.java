package com.inc.sh.controller.app;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.contact.respDto.AppContactInfoRespDto;
import com.inc.sh.service.app.AppContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/app/contact")
@RequiredArgsConstructor
@Slf4j
public class AppContactController {
    
    private final AppContactService appContactService;
    
    /**
     * [앱전용] 문의 정보조회 (본사정보)
     * GET /api/v1/app/contact/info
     */
    @GetMapping("/info")
    public ResponseEntity<RespDto<AppContactInfoRespDto>> getContactInfo(
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("[앱] 문의 정보조회 - hqCode: {}", hqCode);
        
        RespDto<AppContactInfoRespDto> response = appContactService.getContactInfo(hqCode);
        
        return ResponseEntity.ok(response);
    }
}