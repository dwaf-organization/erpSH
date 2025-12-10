package com.inc.sh.service;

import com.inc.sh.dto.distCenter.reqDto.DistCenterSearchDto;
import com.inc.sh.dto.distCenter.reqDto.DistCenterDeleteReqDto;
import com.inc.sh.dto.distCenter.reqDto.DistCenterReqDto;
import com.inc.sh.dto.distCenter.reqDto.DistCenterSaveReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.distCenter.respDto.DistCenterRespDto;
import com.inc.sh.dto.distCenter.respDto.DistCenterSaveRespDto;
import com.inc.sh.dto.distCenter.respDto.DistCenterBatchResult;
import com.inc.sh.dto.distCenter.respDto.DistCenterDeleteRespDto;
import com.inc.sh.entity.DistCenter;
import com.inc.sh.repository.DistCenterRepository;
import com.inc.sh.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DistCenterService {
    
    private final DistCenterRepository distCenterRepository;
    private final WarehouseRepository warehouseRepository;
    
    /**
     * 물류센터 목록 조회
     * @param searchDto 검색 조건
     * @return 조회된 물류센터 목록
     */
    @Transactional(readOnly = true)
    public RespDto<List<DistCenterRespDto>> getDistCenterList(DistCenterSearchDto searchDto) {
        try {
            log.info("물류센터 목록 조회 시작 - distCenterCode: {}, useYn: {}, hqCode: {}", 
                    searchDto.getDistCenterCode(), searchDto.getUseYn(), searchDto.getHqCode());
            
            List<DistCenter> distCenters = distCenterRepository.findBySearchConditionsWithHqCode(
                    searchDto.getDistCenterCode(),
                    searchDto.getUseYn(),
                    searchDto.getHqCode()
            );
            
            List<DistCenterRespDto> responseList = distCenters.stream()
                    .map(DistCenterRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("물류센터 목록 조회 완료 - hqCode: {}, 조회 건수: {}", searchDto.getHqCode(), responseList.size());
            return RespDto.success("물류센터 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("물류센터 목록 조회 중 오류 발생 - hqCode: {}", searchDto.getHqCode(), e);
            return RespDto.fail("물류센터 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 물류센터 상세 조회
     * @param distCenterCode 물류센터코드
     * @return 물류센터 상세 정보
     */
    @Transactional(readOnly = true)
    public RespDto<DistCenterRespDto> getDistCenter(Integer distCenterCode) {
        try {
            log.info("물류센터 상세 조회 시작 - distCenterCode: {}", distCenterCode);
            
            DistCenter distCenter = distCenterRepository.findByDistCenterCode(distCenterCode);
            if (distCenter == null) {
                log.warn("물류센터를 찾을 수 없습니다 - distCenterCode: {}", distCenterCode);
                return RespDto.fail("물류센터를 찾을 수 없습니다.");
            }
            
            DistCenterRespDto responseDto = DistCenterRespDto.fromEntity(distCenter);
            
            log.info("물류센터 상세 조회 완료 - distCenterCode: {}, distCenterName: {}", 
                    distCenterCode, distCenter.getDistCenterName());
            return RespDto.success("물류센터 조회 성공", responseDto);
            
        } catch (Exception e) {
            log.error("물류센터 상세 조회 중 오류 발생 - distCenterCode: {}", distCenterCode, e);
            return RespDto.fail("물류센터 조회 중 오류가 발생했습니다.");
        }
    }
    /**
     * 물류센터 다중 저장 (신규/수정)
     */
    @Transactional
    public RespDto<DistCenterBatchResult> saveDistCenters(DistCenterSaveReqDto reqDto) {
        
        log.info("물류센터 다중 저장 시작 - 총 {}건", reqDto.getDistCenters().size());
        
        List<DistCenterRespDto> successData = new ArrayList<>();
        List<DistCenterBatchResult.DistCenterErrorDto> failData = new ArrayList<>();
        
        for (DistCenterSaveReqDto.DistCenterItemDto distCenter : reqDto.getDistCenters()) {
            try {
                // 개별 물류센터 저장 처리
                DistCenterRespDto savedDistCenter = saveSingleDistCenter(distCenter);
                successData.add(savedDistCenter);
                
                log.info("물류센터 저장 성공 - distCenterCode: {}, distCenterName: {}", 
                        savedDistCenter.getDistCenterCode(), savedDistCenter.getDistCenterName());
                
            } catch (Exception e) {
                log.error("물류센터 저장 실패 - distCenterName: {}, 에러: {}", distCenter.getDistCenterName(), e.getMessage());
                
                DistCenterBatchResult.DistCenterErrorDto errorDto = DistCenterBatchResult.DistCenterErrorDto.builder()
                        .distCenterCode(distCenter.getDistCenterCode())
                        .distCenterName(distCenter.getDistCenterName())
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성
        DistCenterBatchResult result = DistCenterBatchResult.builder()
                .totalCount(reqDto.getDistCenters().size())
                .successCount(successData.size())
                .failCount(failData.size())
                .successData(successData)
                .failData(failData)
                .build();
        
        String message = String.format("물류센터 저장 완료 - 성공: %d건, 실패: %d건", 
                successData.size(), failData.size());
        
        log.info("물류센터 다중 저장 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getDistCenters().size(), successData.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 물류센터 저장 처리 (본사코드 검증 포함)
     */
    private DistCenterRespDto saveSingleDistCenter(DistCenterSaveReqDto.DistCenterItemDto distCenter) {
        
        // 본사코드 존재 여부 확인
        if (distCenterRepository.countByHqCode(distCenter.getHqCode()) == 0) {
            throw new RuntimeException("존재하지 않는 본사코드입니다: " + distCenter.getHqCode());
        }
        
        DistCenter distCenterEntity;
        
        if (distCenter.getDistCenterCode() == null) {
            // 신규 등록
            distCenterEntity = DistCenter.builder()
                    .hqCode(distCenter.getHqCode())
                    .distCenterName(distCenter.getDistCenterName())
                    .zipCode(distCenter.getZipCode())
                    .addr(distCenter.getAddr())
                    .telNum(distCenter.getTelNum())
                    .managerName(distCenter.getManagerName())
                    .managerContact(distCenter.getManagerContact())
                    .useYn(distCenter.getUseYn())
                    .description(distCenter.getDescription())
                    .build();
            
            distCenterEntity = distCenterRepository.save(distCenterEntity);
            
            log.info("물류센터 신규 생성 - distCenterCode: {}, distCenterName: {}", 
                    distCenterEntity.getDistCenterCode(), distCenterEntity.getDistCenterName());
            
        } else {
            // 수정
            distCenterEntity = distCenterRepository.findById(distCenter.getDistCenterCode())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 물류센터입니다: " + distCenter.getDistCenterCode()));
            
            // 모든 필드 수정
            distCenterEntity.setHqCode(distCenter.getHqCode());
            distCenterEntity.setDistCenterName(distCenter.getDistCenterName());
            distCenterEntity.setZipCode(distCenter.getZipCode());
            distCenterEntity.setAddr(distCenter.getAddr());
            distCenterEntity.setTelNum(distCenter.getTelNum());
            distCenterEntity.setManagerName(distCenter.getManagerName());
            distCenterEntity.setManagerContact(distCenter.getManagerContact());
            distCenterEntity.setUseYn(distCenter.getUseYn());
            distCenterEntity.setDescription(distCenter.getDescription());
            
            distCenterEntity = distCenterRepository.save(distCenterEntity);
            
            log.info("물류센터 정보 수정 - distCenterCode: {}, distCenterName: {}", 
                    distCenterEntity.getDistCenterCode(), distCenterEntity.getDistCenterName());
        }
        
        return DistCenterRespDto.fromEntity(distCenterEntity);
    }

    /**
     * 물류센터 다중 삭제 (Hard Delete + 창고 연결 확인)
     */
    @Transactional
    public RespDto<DistCenterBatchResult> deleteDistCenters(DistCenterDeleteReqDto reqDto) {
        
        log.info("물류센터 다중 삭제 시작 - 총 {}건", reqDto.getDistCenterCodes().size());
        
        List<Integer> successCodes = new ArrayList<>();
        List<DistCenterBatchResult.DistCenterErrorDto> failData = new ArrayList<>();
        
        for (Integer distCenterCode : reqDto.getDistCenterCodes()) {
            try {
                // 개별 물류센터 삭제 처리
                deleteSingleDistCenter(distCenterCode);
                successCodes.add(distCenterCode);
                
                log.info("물류센터 삭제 성공 - distCenterCode: {}", distCenterCode);
                
            } catch (Exception e) {
                log.error("물류센터 삭제 실패 - distCenterCode: {}, 에러: {}", distCenterCode, e.getMessage());
                
                // 에러 시 물류센터명 조회 시도
                String distCenterName = getDistCenterNameSafely(distCenterCode);
                
                DistCenterBatchResult.DistCenterErrorDto errorDto = DistCenterBatchResult.DistCenterErrorDto.builder()
                        .distCenterCode(distCenterCode)
                        .distCenterName(distCenterName)
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성 (삭제는 successData 대신 성공 코드만)
        DistCenterBatchResult result = DistCenterBatchResult.builder()
                .totalCount(reqDto.getDistCenterCodes().size())
                .successCount(successCodes.size())
                .failCount(failData.size())
                .successData(successCodes.stream()
                        .map(code -> DistCenterRespDto.builder().distCenterCode(code).build())
                        .collect(Collectors.toList()))
                .failData(failData)
                .build();
        
        String message = String.format("물류센터 삭제 완료 - 성공: %d건, 실패: %d건", 
                successCodes.size(), failData.size());
        
        log.info("물류센터 다중 삭제 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getDistCenterCodes().size(), successCodes.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 물류센터 삭제 처리 (창고 연결 확인)
     */
    private void deleteSingleDistCenter(Integer distCenterCode) {
        
        // 물류센터 존재 확인
        DistCenter distCenter = distCenterRepository.findById(distCenterCode)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 물류센터입니다: " + distCenterCode));

        // 연결된 창고 확인
        List<Object[]> linkedWarehouses = distCenterRepository.findLinkedWarehouses(distCenterCode);
        if (!linkedWarehouses.isEmpty()) {
            // 연결된 창고 정보를 메시지에 포함
            StringBuilder warehouseInfo = new StringBuilder();
            warehouseInfo.append("연결된 창고가 존재하여 삭제할 수 없습니다. 연결된 창고: ");
            
            for (int i = 0; i < linkedWarehouses.size(); i++) {
                Object[] warehouse = linkedWarehouses.get(i);
                Integer warehouseCode = (Integer) warehouse[0];
                String warehouseName = (String) warehouse[1];
                
                warehouseInfo.append(String.format("[%d] %s", warehouseCode, warehouseName));
                
                if (i < linkedWarehouses.size() - 1) {
                    warehouseInfo.append(", ");
                }
            }
            
            throw new RuntimeException(warehouseInfo.toString());
        }

        // Hard Delete - 실제 레코드 삭제
        distCenterRepository.delete(distCenter);
        
        log.info("물류센터 삭제 완료 - distCenterCode: {}, distCenterName: {}", 
                distCenterCode, distCenter.getDistCenterName());
    }
    
    /**
     * 물류센터명 안전 조회 (에러 발생시 사용)
     */
    private String getDistCenterNameSafely(Integer distCenterCode) {
        try {
            return distCenterRepository.findById(distCenterCode)
                    .map(DistCenter::getDistCenterName)
                    .orElse("알 수 없음");
        } catch (Exception e) {
            return "조회 실패";
        }
    }
}