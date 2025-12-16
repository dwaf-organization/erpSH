package com.inc.sh.service;

import com.inc.sh.dto.analysisTest.respDto.AnalysisTestRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.entity.DistrictAnalysis;
import com.inc.sh.repository.AnalysisTestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisTestService {
    
    private final AnalysisTestRepository analysisTestRepository;
    
    /**
     * 구코드로 상권분석 조회
     */
    @Transactional(readOnly = true)
    public RespDto<AnalysisTestRespDto> getDistrictAnalysis(Integer districtCode) {
        try {
            log.info("상권분석 조회 시작 - districtCode: {}", districtCode);
            
            DistrictAnalysis analysis = analysisTestRepository.findByDistrictCode(districtCode);
            if (analysis == null) {
                return RespDto.fail("존재하지 않는 구코드입니다.");
            }
            
            AnalysisTestRespDto responseData = AnalysisTestRespDto.from(analysis);
            
            log.info("상권분석 조회 완료 - districtCode: {}, districtName: {}", 
                    districtCode, analysis.getDistrictName());
            
            return RespDto.success("상권분석 조회 성공", responseData);
            
        } catch (Exception e) {
            log.error("상권분석 조회 중 오류 발생 - districtCode: {}", districtCode, e);
            return RespDto.fail("상권분석 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 전체 상권분석 목록 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<AnalysisTestRespDto>> getAllDistrictAnalysis() {
        try {
            log.info("전체 상권분석 목록 조회 시작");
            
            List<DistrictAnalysis> analysisList = analysisTestRepository.findAllOrderByDistrictCode();
            
            List<AnalysisTestRespDto> responseList = analysisList.stream()
                    .map(AnalysisTestRespDto::from)
                    .collect(Collectors.toList());
            
            log.info("전체 상권분석 목록 조회 완료 - 조회 건수: {}", responseList.size());
            
            return RespDto.success("전체 상권분석 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("전체 상권분석 목록 조회 중 오류 발생", e);
            return RespDto.fail("전체 상권분석 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 성장 구역 조회 (상승/하락별)
     */
    @Transactional(readOnly = true)
    public RespDto<List<AnalysisTestRespDto>> getDistrictAnalysisByGrowth(Integer growthCode) {
        try {
            log.info("성장 구역별 상권분석 조회 시작 - growthCode: {}", growthCode);
            
            if (growthCode != 0 && growthCode != 1) {
                return RespDto.fail("올바르지 않은 성장 코드입니다. (0=하락, 1=상승)");
            }
            
            List<DistrictAnalysis> analysisList = analysisTestRepository.findByGrowthCodeOrderByGrowthRateDesc(growthCode);
            
            List<AnalysisTestRespDto> responseList = analysisList.stream()
                    .map(AnalysisTestRespDto::from)
                    .collect(Collectors.toList());
            
            String growthType = growthCode == 1 ? "상승" : "하락";
            log.info("성장 구역별 상권분석 조회 완료 - growthType: {}, 조회 건수: {}", 
                    growthType, responseList.size());
            
            return RespDto.success(growthType + " 구역 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("성장 구역별 상권분석 조회 중 오류 발생 - growthCode: {}", growthCode, e);
            return RespDto.fail("성장 구역별 상권분석 조회 중 오류가 발생했습니다.");
        }
    }
}