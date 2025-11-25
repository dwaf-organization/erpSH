package com.inc.sh.service;

import com.inc.sh.dto.distCenter.reqDto.DistCenterSearchDto;
import com.inc.sh.dto.distCenter.reqDto.DistCenterReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.distCenter.respDto.DistCenterRespDto;
import com.inc.sh.dto.distCenter.respDto.DistCenterSaveRespDto;
import com.inc.sh.dto.distCenter.respDto.DistCenterDeleteRespDto;
import com.inc.sh.entity.DistCenter;
import com.inc.sh.repository.DistCenterRepository;
import com.inc.sh.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            log.info("물류센터 목록 조회 시작 - distCenterCode: {}, useYn: {}", 
                    searchDto.getDistCenterCode(), searchDto.getUseYn());
            
            List<DistCenter> distCenters = distCenterRepository.findBySearchConditions(
                    searchDto.getDistCenterCode(),
                    searchDto.getUseYn()
            );
            
            List<DistCenterRespDto> responseList = distCenters.stream()
                    .map(DistCenterRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("물류센터 목록 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("물류센터 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("물류센터 목록 조회 중 오류 발생", e);
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
     * 물류센터 저장 (신규/수정)
     * @param request 물류센터 정보
     * @return 저장된 물류센터 코드
     */
    public RespDto<DistCenterSaveRespDto> saveDistCenter(DistCenterReqDto request) {
        try {
            DistCenter savedDistCenter;
            String action;
            
            if (request.getDistCenterCode() == null) {
                // 신규 등록
                log.info("물류센터 신규 등록 시작 - distCenterName: {}", request.getDistCenterName());
                
                DistCenter distCenter = request.toEntity();
                savedDistCenter = distCenterRepository.save(distCenter);
                action = "등록";
                
            } else {
                // 수정
                log.info("물류센터 수정 시작 - distCenterCode: {}, distCenterName: {}", 
                        request.getDistCenterCode(), request.getDistCenterName());
                
                DistCenter existingDistCenter = distCenterRepository.findByDistCenterCode(request.getDistCenterCode());
                if (existingDistCenter == null) {
                    log.warn("수정할 물류센터를 찾을 수 없습니다 - distCenterCode: {}", request.getDistCenterCode());
                    return RespDto.fail("수정할 물류센터를 찾을 수 없습니다.");
                }
                
                request.updateEntity(existingDistCenter);
                savedDistCenter = distCenterRepository.save(existingDistCenter);
                action = "수정";
            }
            
            // 간소화된 응답 생성
            DistCenterSaveRespDto responseDto = DistCenterSaveRespDto.builder()
                    .distCenterCode(savedDistCenter.getDistCenterCode())
                    .build();
            
            log.info("물류센터 {} 완료 - distCenterCode: {}, distCenterName: {}", 
                    action, savedDistCenter.getDistCenterCode(), savedDistCenter.getDistCenterName());
            
            return RespDto.success("물류센터가 성공적으로 " + action + "되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("물류센터 저장 중 오류 발생 - distCenterCode: {}", request.getDistCenterCode(), e);
            return RespDto.fail("물류센터 저장 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 물류센터 삭제 (하드 삭제)
     * @param distCenterCode 물류센터코드
     * @return 삭제 결과
     */
    public RespDto<DistCenterDeleteRespDto> deleteDistCenter(Integer distCenterCode) {
        try {
            log.info("물류센터 삭제 시작 - distCenterCode: {}", distCenterCode);
            
            DistCenter distCenter = distCenterRepository.findByDistCenterCode(distCenterCode);
            if (distCenter == null) {
                log.warn("삭제할 물류센터를 찾을 수 없습니다 - distCenterCode: {}", distCenterCode);
                return RespDto.fail("삭제할 물류센터를 찾을 수 없습니다.");
            }
            
            // 연관된 창고 확인
            List<Integer> relatedWarehouseCodes = warehouseRepository.findWarehouseCodesByDistCenterCode(distCenterCode);
            
            if (!relatedWarehouseCodes.isEmpty()) {
                // 연관된 창고가 있으면 삭제 중단
                log.warn("연관된 창고가 있어 삭제할 수 없습니다 - distCenterCode: {}, relatedWarehouses: {}", 
                        distCenterCode, relatedWarehouseCodes);
                
                String warehouseCodesStr = relatedWarehouseCodes.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                
                DistCenterDeleteRespDto responseDto = DistCenterDeleteRespDto.builder()
                        .distCenterCode(distCenterCode)
                        .relatedWarehouseCodes(relatedWarehouseCodes)
                        .message("해당 물류센터에 연결된 창고가 있습니다. 창고번호: " + warehouseCodesStr)
                        .build();
                
                return RespDto.fail("해당 물류센터에 연결된 창고가 있어 삭제할 수 없습니다. 창고번호: " + warehouseCodesStr);
            }
            
            // 하드 삭제 진행
            distCenterRepository.delete(distCenter);
            
            // 응답 생성
            DistCenterDeleteRespDto responseDto = DistCenterDeleteRespDto.builder()
                    .distCenterCode(distCenterCode)
                    .relatedWarehouseCodes(null)
                    .message("물류센터가 성공적으로 삭제되었습니다.")
                    .build();
            
            log.info("물류센터 삭제 완료 - distCenterCode: {}, distCenterName: {}", 
                    distCenterCode, distCenter.getDistCenterName());
            
            return RespDto.success("물류센터가 성공적으로 삭제되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("물류센터 삭제 중 오류 발생 - distCenterCode: {}", distCenterCode, e);
            return RespDto.fail("물류센터 삭제 중 오류가 발생했습니다.");
        }
    }
}