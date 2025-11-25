package com.inc.sh.controller;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.headquarter.reqDto.HeadquarterReqDto;
import com.inc.sh.dto.headquarter.respDto.HeadquarterRespDto;
import com.inc.sh.service.HeadquarterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/hq")
@RequiredArgsConstructor
@Slf4j
public class HeadquarterController {
    
    private final HeadquarterService headquarterService;
    
    /**
     * 본사 등록
     * POST /api/v1/admin/hq/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<HeadquarterRespDto>> saveHeadquarter(
            @Valid @RequestBody HeadquarterReqDto request) {
        
        return ResponseEntity.ok(headquarterService.saveHeadquarter(request));
    }
}