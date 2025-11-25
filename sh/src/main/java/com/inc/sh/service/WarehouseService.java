package com.inc.sh.service;

import com.inc.sh.dto.warehouse.reqDto.WarehouseSearchDto;
import com.inc.sh.dto.warehouse.reqDto.WarehouseReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.warehouse.respDto.WarehouseListRespDto;
import com.inc.sh.dto.warehouse.respDto.WarehouseSaveRespDto;
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
            log.info("창고 목록 조회 시작 - warehouseCode: {}, distCenterCode: {}", 
                    searchDto.getWarehouseCode(), searchDto.getDistCenterCode());
            
            List<Object[]> warehouses = warehouseRepository.findWarehousesWithDistCenterName(
                    searchDto.getWarehouseCode(),
                    searchDto.getDistCenterCode()
            );
            
            List<WarehouseListRespDto> responseList = warehouses.stream()
                    .map(WarehouseListRespDto::of)
                    .collect(Collectors.toList());
            
            log.info("창고 목록 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("창고 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("창고 목록 조회 중 오류 발생", e);
            return RespDto.fail("창고 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 창고 저장 (신규/수정)
     * @param request 창고 정보
     * @return 저장된 창고 코드
     */
    public RespDto<WarehouseSaveRespDto> saveWarehouse(WarehouseReqDto request) {
        try {
            // 본사 존재 확인
            if (!headquarterRepository.existsByHqCode(request.getHqCode())) {
                log.warn("본사가 존재하지 않습니다 - hqCode: {}", request.getHqCode());
                return RespDto.fail("본사가 존재하지 않습니다.");
            }
            
            // 물류센터 존재 확인
            DistCenter distCenter = distCenterRepository.findByDistCenterCode(request.getDistCenterCode());
            if (distCenter == null) {
                log.warn("물류센터가 존재하지 않습니다 - distCenterCode: {}", request.getDistCenterCode());
                return RespDto.fail("물류센터가 존재하지 않습니다.");
            }
            
            // 물류센터 사용여부 확인
            if (distCenter.getUseYn() == 0) {
                log.warn("사용하지 않는 물류센터입니다 - distCenterCode: {}", request.getDistCenterCode());
                return RespDto.fail("사용하지 않는 물류센터입니다.");
            }
            
            Warehouse savedWarehouse;
            String action;
            
            if (request.getWarehouseCode() == null) {
                // 신규 등록
                log.info("창고 신규 등록 시작 - warehouseName: {}, distCenterCode: {}", 
                        request.getWarehouseName(), request.getDistCenterCode());
                
                Warehouse warehouse = request.toEntity();
                savedWarehouse = warehouseRepository.save(warehouse);
                action = "등록";
                
            } else {
                // 수정
                log.info("창고 수정 시작 - warehouseCode: {}, warehouseName: {}", 
                        request.getWarehouseCode(), request.getWarehouseName());
                
                Warehouse existingWarehouse = warehouseRepository.findByWarehouseCode(request.getWarehouseCode());
                if (existingWarehouse == null) {
                    log.warn("수정할 창고를 찾을 수 없습니다 - warehouseCode: {}", request.getWarehouseCode());
                    return RespDto.fail("수정할 창고를 찾을 수 없습니다.");
                }
                
                request.updateEntity(existingWarehouse);
                savedWarehouse = warehouseRepository.save(existingWarehouse);
                action = "수정";
            }
            
            // 간소화된 응답 생성
            WarehouseSaveRespDto responseDto = WarehouseSaveRespDto.builder()
                    .warehouseCode(savedWarehouse.getWarehouseCode())
                    .build();
            
            log.info("창고 {} 완료 - warehouseCode: {}, warehouseName: {}, distCenterName: {}", 
                    action, savedWarehouse.getWarehouseCode(), savedWarehouse.getWarehouseName(), 
                    distCenter.getDistCenterName());
            
            return RespDto.success("창고가 성공적으로 " + action + "되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("창고 저장 중 오류 발생 - warehouseCode: {}", request.getWarehouseCode(), e);
            return RespDto.fail("창고 저장 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 창고 삭제 (하드 삭제)
     * @param warehouseCode 창고코드
     * @return 삭제 결과
     */
    public RespDto<WarehouseDeleteRespDto> deleteWarehouse(Integer warehouseCode) {
        try {
            log.info("창고 삭제 시작 - warehouseCode: {}", warehouseCode);
            
            Warehouse warehouse = warehouseRepository.findByWarehouseCode(warehouseCode);
            if (warehouse == null) {
                log.warn("삭제할 창고를 찾을 수 없습니다 - warehouseCode: {}", warehouseCode);
                return RespDto.fail("삭제할 창고를 찾을 수 없습니다.");
            }
            
            // 창고에 품목이 있는지 확인
            Long itemCount = warehouseItemsRepository.countByWarehouseCode(warehouseCode);
            if (itemCount > 0) {
                // 창고에 품목이 있으면 삭제 중단
                log.warn("창고에 품목이 있어 삭제할 수 없습니다 - warehouseCode: {}, itemCount: {}", 
                        warehouseCode, itemCount);
                
                WarehouseDeleteRespDto responseDto = WarehouseDeleteRespDto.builder()
                        .warehouseCode(warehouseCode)
                        .itemCount(itemCount)
                        .message("창고에 품목이 들어있어 삭제할 수 없습니다. 현재 품목 개수: " + itemCount)
                        .build();
                
                return RespDto.fail("창고에 품목이 들어있어 삭제할 수 없습니다. 현재 품목 개수: " + itemCount);
            }
            
            // 하드 삭제 진행
            warehouseRepository.delete(warehouse);
            
            // 응답 생성
            WarehouseDeleteRespDto responseDto = WarehouseDeleteRespDto.builder()
                    .warehouseCode(warehouseCode)
                    .itemCount(0L)
                    .message("창고가 성공적으로 삭제되었습니다.")
                    .build();
            
            log.info("창고 삭제 완료 - warehouseCode: {}, warehouseName: {}", 
                    warehouseCode, warehouse.getWarehouseName());
            
            return RespDto.success("창고가 성공적으로 삭제되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("창고 삭제 중 오류 발생 - warehouseCode: {}", warehouseCode, e);
            return RespDto.fail("창고 삭제 중 오류가 발생했습니다.");
        }
    }
}