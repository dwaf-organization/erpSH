package com.inc.sh.service;

import com.inc.sh.dto.warehouse.reqDto.WarehouseSearchDto;
import com.inc.sh.dto.warehouse.reqDto.WarehouseDeleteReqDto;
import com.inc.sh.dto.warehouse.reqDto.WarehouseReqDto;
import com.inc.sh.dto.warehouse.reqDto.WarehouseSaveReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.warehouse.respDto.WarehouseListRespDto;
import com.inc.sh.dto.warehouse.respDto.WarehouseRespDto;
import com.inc.sh.dto.warehouse.respDto.WarehouseSaveRespDto;
import com.inc.sh.dto.warehouse.respDto.WarehouseBatchResult;
import com.inc.sh.dto.warehouse.respDto.WarehouseDeleteRespDto;
import com.inc.sh.entity.Warehouse;
import com.inc.sh.entity.DistCenter;
import com.inc.sh.repository.WarehouseRepository;
import com.inc.sh.repository.WarehouseItemsRepository;
import com.inc.sh.repository.DistCenterRepository;
import com.inc.sh.repository.HeadquarterRepository;
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
public class WarehouseService {
    
    private final WarehouseRepository warehouseRepository;
    private final WarehouseItemsRepository warehouseItemsRepository;
    private final DistCenterRepository distCenterRepository;
    private final HeadquarterRepository headquarterRepository;
    
    /**
     * 창고 목록 조회 (물류센터명 포함)
     * @param searchDto 검색 조건
     * @return 조회된 창고 목록
     */
    @Transactional(readOnly = true)
    public RespDto<List<WarehouseListRespDto>> getWarehouseList(WarehouseSearchDto searchDto) {
        try {
            log.info("창고 목록 조회 시작 - warehouseCode: {}, distCenterCode: {}, hqCode: {}", 
                    searchDto.getWarehouseCode(), searchDto.getDistCenterCode(), searchDto.getHqCode());
            
            List<Object[]> warehouses = warehouseRepository.findWarehousesWithDistCenterNameAndHqCodeAll(
                    searchDto.getWarehouseCode(),
                    searchDto.getDistCenterCode(),
                    searchDto.getHqCode()
            );
            
            List<WarehouseListRespDto> responseList = warehouses.stream()
                    .map(WarehouseListRespDto::of)
                    .collect(Collectors.toList());
            
            log.info("창고 목록 조회 완료 - hqCode: {}, 조회 건수: {}", searchDto.getHqCode(), responseList.size());
            return RespDto.success("창고 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("창고 목록 조회 중 오류 발생 - hqCode: {}", searchDto.getHqCode(), e);
            return RespDto.fail("창고 목록 조회 중 오류가 발생했습니다.");
        }
    }
    /**
     * 창고 다중 저장 (신규/수정)
     */
    @Transactional
    public RespDto<WarehouseBatchResult> saveWarehouses(WarehouseSaveReqDto reqDto) {
        
        log.info("창고 다중 저장 시작 - 총 {}건", reqDto.getWarehouses().size());
        
        List<WarehouseRespDto> successData = new ArrayList<>();
        List<WarehouseBatchResult.WarehouseErrorDto> failData = new ArrayList<>();
        
        for (WarehouseSaveReqDto.WarehouseItemDto warehouse : reqDto.getWarehouses()) {
            try {
                // 개별 창고 저장 처리
                WarehouseRespDto savedWarehouse = saveSingleWarehouse(warehouse);
                successData.add(savedWarehouse);
                
                log.info("창고 저장 성공 - warehouseCode: {}, warehouseName: {}", 
                        savedWarehouse.getWarehouseCode(), savedWarehouse.getWarehouseName());
                
            } catch (Exception e) {
                log.error("창고 저장 실패 - warehouseName: {}, 에러: {}", warehouse.getWarehouseName(), e.getMessage());
                
                WarehouseBatchResult.WarehouseErrorDto errorDto = WarehouseBatchResult.WarehouseErrorDto.builder()
                        .warehouseCode(warehouse.getWarehouseCode())
                        .warehouseName(warehouse.getWarehouseName())
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성
        WarehouseBatchResult result = WarehouseBatchResult.builder()
                .totalCount(reqDto.getWarehouses().size())
                .successCount(successData.size())
                .failCount(failData.size())
                .successData(successData)
                .failData(failData)
                .build();
        
        String message = String.format("창고 저장 완료 - 성공: %d건, 실패: %d건", 
                successData.size(), failData.size());
        
        log.info("창고 다중 저장 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getWarehouses().size(), successData.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 창고 저장 처리 (유효성 검증 추가)
     */
    private WarehouseRespDto saveSingleWarehouse(WarehouseSaveReqDto.WarehouseItemDto warehouse) {
        
        // 1. 본사코드 존재 여부 확인
        if (warehouseRepository.countByHqCode(warehouse.getHqCode()) == 0) {
            throw new RuntimeException("존재하지 않는 본사코드입니다: " + warehouse.getHqCode());
        }
        
        // 2. 물류센터코드 존재 여부 확인
        if (warehouseRepository.countByDistCenterCode(warehouse.getDistCenterCode()) == 0) {
            throw new RuntimeException("존재하지 않는 물류센터코드입니다: " + warehouse.getDistCenterCode());
        }
        
        Warehouse warehouseEntity;
        
        if (warehouse.getWarehouseCode() == null) {
            // 신규 등록
            warehouseEntity = Warehouse.builder()
                    .distCenterCode(warehouse.getDistCenterCode())
                    .hqCode(warehouse.getHqCode())
                    .warehouseName(warehouse.getWarehouseName())
                    .zipCode(warehouse.getZipCode())
                    .addr(warehouse.getAddr())
                    .telNum(warehouse.getTelNum())
                    .managerName(warehouse.getManagerName())
                    .managerContact(warehouse.getManagerContact())
                    .useYn(warehouse.getUseYn())
                    .description(warehouse.getDescription())
                    .build();
            
            warehouseEntity = warehouseRepository.save(warehouseEntity);
            
            log.info("창고 신규 생성 - warehouseCode: {}, warehouseName: {}", 
                    warehouseEntity.getWarehouseCode(), warehouseEntity.getWarehouseName());
            
        } else {
            // 수정
            warehouseEntity = warehouseRepository.findById(warehouse.getWarehouseCode())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 창고입니다: " + warehouse.getWarehouseCode()));
            
            // 모든 필드 수정
            warehouseEntity.setDistCenterCode(warehouse.getDistCenterCode());
            warehouseEntity.setWarehouseName(warehouse.getWarehouseName());
            warehouseEntity.setZipCode(warehouse.getZipCode());
            warehouseEntity.setAddr(warehouse.getAddr());
            warehouseEntity.setTelNum(warehouse.getTelNum());
            warehouseEntity.setManagerName(warehouse.getManagerName());
            warehouseEntity.setManagerContact(warehouse.getManagerContact());
            warehouseEntity.setUseYn(warehouse.getUseYn());
            warehouseEntity.setDescription(warehouse.getDescription());
            
            warehouseEntity = warehouseRepository.save(warehouseEntity);
            
            log.info("창고 정보 수정 - warehouseCode: {}, warehouseName: {}", 
                    warehouseEntity.getWarehouseCode(), warehouseEntity.getWarehouseName());
        }
        
        return WarehouseRespDto.fromEntity(warehouseEntity);
    }

    /**
     * 창고 다중 삭제 (Hard Delete)
     */
    @Transactional
    public RespDto<WarehouseBatchResult> deleteWarehouses(WarehouseDeleteReqDto reqDto) {
        
        log.info("창고 다중 삭제 시작 - 총 {}건", reqDto.getWarehouseCodes().size());
        
        List<Integer> successCodes = new ArrayList<>();
        List<WarehouseBatchResult.WarehouseErrorDto> failData = new ArrayList<>();
        
        for (Integer warehouseCode : reqDto.getWarehouseCodes()) {
            try {
                // 개별 창고 삭제 처리
                deleteSingleWarehouse(warehouseCode);
                successCodes.add(warehouseCode);
                
                log.info("창고 삭제 성공 - warehouseCode: {}", warehouseCode);
                
            } catch (Exception e) {
                log.error("창고 삭제 실패 - warehouseCode: {}, 에러: {}", warehouseCode, e.getMessage());
                
                // 에러 시 창고명 조회 시도
                String warehouseName = getWarehouseNameSafely(warehouseCode);
                
                WarehouseBatchResult.WarehouseErrorDto errorDto = WarehouseBatchResult.WarehouseErrorDto.builder()
                        .warehouseCode(warehouseCode)
                        .warehouseName(warehouseName)
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성 (삭제는 successData 대신 성공 코드만)
        WarehouseBatchResult result = WarehouseBatchResult.builder()
                .totalCount(reqDto.getWarehouseCodes().size())
                .successCount(successCodes.size())
                .failCount(failData.size())
                .successData(successCodes.stream()
                        .map(code -> WarehouseRespDto.builder().warehouseCode(code).build())
                        .collect(Collectors.toList()))
                .failData(failData)
                .build();
        
        String message = String.format("창고 삭제 완료 - 성공: %d건, 실패: %d건", 
                successCodes.size(), failData.size());
        
        log.info("창고 다중 삭제 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getWarehouseCodes().size(), successCodes.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 창고 삭제 처리 (재고 확인 추가)
     */
    private void deleteSingleWarehouse(Integer warehouseCode) {
        
        // 창고 존재 확인
        Warehouse warehouse = warehouseRepository.findById(warehouseCode)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 창고입니다: " + warehouseCode));

        // 창고에 재고가 있는 품목 확인
        Long inventoryCount = warehouseRepository.countInventoryByWarehouseCode(warehouseCode);
        if (inventoryCount > 0) {
            throw new RuntimeException("창고에 재고가 있는 품목이 " + inventoryCount + "개 존재하여 삭제할 수 없습니다. 재고를 먼저 처리해주세요.");
        }

        // Hard Delete - 실제 레코드 삭제
        warehouseRepository.delete(warehouse);
        
        log.info("창고 삭제 완료 - warehouseCode: {}, warehouseName: {}", 
                warehouseCode, warehouse.getWarehouseName());
    }
    
    /**
     * 창고명 안전 조회 (에러 발생시 사용)
     */
    private String getWarehouseNameSafely(Integer warehouseCode) {
        try {
            return warehouseRepository.findById(warehouseCode)
                    .map(Warehouse::getWarehouseName)
                    .orElse("알 수 없음");
        } catch (Exception e) {
            return "조회 실패";
        }
    }
}