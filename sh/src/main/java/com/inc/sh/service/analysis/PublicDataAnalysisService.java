package com.inc.sh.service.analysis;

import com.inc.sh.dto.publicDataAnalysis.respDto.PublicDataAnalysisRespDto;
import com.inc.sh.entity.PublicDataAnalysis;
import com.inc.sh.repository.PublicDataAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicDataAnalysisService {

    private final PublicDataAnalysisRepository repository;

    /**
     * 동코드로 분석 데이터 조회
     */
    public PublicDataAnalysisRespDto.PublicDataAnalysisDetailResp getAnalysisData(Integer adminDongCode) {
        log.info("분석 데이터 조회 - 동코드: {}", adminDongCode);
        
        PublicDataAnalysis entity = repository.findByAdminDongCode(adminDongCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 동코드입니다: " + adminDongCode));
        
        log.info("분석 데이터 조회 성공 - 동코드: {}, 동이름: {}", adminDongCode, entity.getAdminDongName());
        return PublicDataAnalysisRespDto.PublicDataAnalysisDetailResp.fromEntity(entity);
    }

}