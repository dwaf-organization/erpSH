package com.inc.sh.service;

import com.inc.sh.dto.vehicle.reqDto.VehicleSearchDto;
import com.inc.sh.dto.vehicle.reqDto.VehicleDeleteReqDto;
import com.inc.sh.dto.vehicle.reqDto.VehicleReqDto;
import com.inc.sh.dto.vehicle.reqDto.VehicleSaveReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.vehicle.respDto.VehicleRespDto;
import com.inc.sh.dto.vehicle.respDto.VehicleSaveRespDto;
import com.inc.sh.dto.vehicle.respDto.VehicleBatchResult;
import com.inc.sh.dto.vehicle.respDto.VehicleDeleteRespDto;
import com.inc.sh.entity.Vehicle;
import com.inc.sh.repository.VehicleRepository;
import com.inc.sh.repository.OrderRepository;
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
public class VehicleService {
    
    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final HeadquarterRepository headquarterRepository;
    
    /**
     * 차량 목록 조회
     * @param searchDto 검색 조건
     * @return 조회된 차량 목록
     */
    @Transactional(readOnly = true)
    public RespDto<List<VehicleRespDto>> getVehicleList(VehicleSearchDto searchDto) {
        try {
            log.info("차량 목록 조회 시작 - vehicleCode: {}, category: {}, hqCode: {}", 
                    searchDto.getVehicleCode(), searchDto.getCategory(), searchDto.getHqCode());
            
            List<Vehicle> vehicles = vehicleRepository.findBySearchConditionsWithHqCode(
                    searchDto.getVehicleCode(),
                    searchDto.getCategory(),
                    searchDto.getHqCode()
            );
            
            List<VehicleRespDto> responseList = vehicles.stream()
                    .map(VehicleRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("차량 목록 조회 완료 - hqCode: {}, 조회 건수: {}", searchDto.getHqCode(), responseList.size());
            return RespDto.success("차량 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("차량 목록 조회 중 오류 발생 - hqCode: {}", searchDto.getHqCode(), e);
            return RespDto.fail("차량 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 차량 상세 조회
     * @param vehicleCode 차량코드
     * @return 차량 상세 정보
     */
    @Transactional(readOnly = true)
    public RespDto<VehicleRespDto> getVehicle(Integer vehicleCode) {
        try {
            log.info("차량 상세 조회 시작 - vehicleCode: {}", vehicleCode);
            
            Vehicle vehicle = vehicleRepository.findByVehicleCode(vehicleCode);
            if (vehicle == null) {
                log.warn("차량을 찾을 수 없습니다 - vehicleCode: {}", vehicleCode);
                return RespDto.fail("차량을 찾을 수 없습니다.");
            }
            
            VehicleRespDto responseDto = VehicleRespDto.fromEntity(vehicle);
            
            log.info("차량 상세 조회 완료 - vehicleCode: {}, vehicleName: {}", 
                    vehicleCode, vehicle.getVehicleName());
            return RespDto.success("차량 조회 성공", responseDto);
            
        } catch (Exception e) {
            log.error("차량 상세 조회 중 오류 발생 - vehicleCode: {}", vehicleCode, e);
            return RespDto.fail("차량 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 차량 구분 유효성 검증
     */
    private boolean isValidCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return false;
        }
        return category.equals("냉장") || category.equals("냉동") || category.equals("상온");
    }
    

    /**
     * 차량 다중 저장 (신규/수정)
     */
    @Transactional
    public RespDto<VehicleBatchResult> saveVehicles(VehicleSaveReqDto reqDto) {
        
        log.info("차량 다중 저장 시작 - 총 {}건", reqDto.getVehicles().size());
        
        List<VehicleRespDto> successData = new ArrayList<>();
        List<VehicleBatchResult.VehicleErrorDto> failData = new ArrayList<>();
        
        for (VehicleSaveReqDto.VehicleItemDto vehicle : reqDto.getVehicles()) {
            try {
                // 개별 차량 저장 처리
                VehicleRespDto savedVehicle = saveSingleVehicle(vehicle);
                successData.add(savedVehicle);
                
                log.info("차량 저장 성공 - vehicleCode: {}, vehicleName: {}", 
                        savedVehicle.getVehicleCode(), savedVehicle.getVehicleName());
                
            } catch (Exception e) {
                log.error("차량 저장 실패 - vehicleName: {}, 에러: {}", vehicle.getVehicleName(), e.getMessage());
                
                VehicleBatchResult.VehicleErrorDto errorDto = VehicleBatchResult.VehicleErrorDto.builder()
                        .vehicleCode(vehicle.getVehicleCode())
                        .vehicleName(vehicle.getVehicleName())
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성
        VehicleBatchResult result = VehicleBatchResult.builder()
                .totalCount(reqDto.getVehicles().size())
                .successCount(successData.size())
                .failCount(failData.size())
                .successData(successData)
                .failData(failData)
                .build();
        
        String message = String.format("차량 저장 완료 - 성공: %d건, 실패: %d건", 
                successData.size(), failData.size());
        
        log.info("차량 다중 저장 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getVehicles().size(), successData.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 차량 저장 처리 (유효성 검증 추가)
     */
    private VehicleRespDto saveSingleVehicle(VehicleSaveReqDto.VehicleItemDto vehicle) {
        
        // 1. 본사코드 존재 여부 확인
        if (!headquarterRepository.existsByHqCode(vehicle.getHqCode())) {
            throw new RuntimeException("존재하지 않는 본사코드입니다: " + vehicle.getHqCode());
        }
        
        // 2. 구분 유효성 검증
        if (!isValidCategory(vehicle.getCategory())) {
            throw new RuntimeException("유효하지 않은 차량 구분입니다. (냉장, 냉동, 상온 중 선택)");
        }
        
        Vehicle vehicleEntity;
        
        if (vehicle.getVehicleCode() == null) {
            // 신규 등록
            vehicleEntity = Vehicle.builder()
                    .hqCode(vehicle.getHqCode())
                    .vehicleName(vehicle.getVehicleName())
                    .category(vehicle.getCategory())
                    .vehicleType(vehicle.getVehicleType())
                    .capacitySpec(vehicle.getCapacitySpec())
                    .build();
            
            vehicleEntity = vehicleRepository.save(vehicleEntity);
            
            log.info("차량 신규 생성 - vehicleCode: {}, vehicleName: {}", 
                    vehicleEntity.getVehicleCode(), vehicleEntity.getVehicleName());
            
        } else {
            // 수정
            vehicleEntity = vehicleRepository.findById(vehicle.getVehicleCode())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 차량입니다: " + vehicle.getVehicleCode()));
            
            // 모든 필드 수정
            vehicleEntity.setHqCode(vehicle.getHqCode());
            vehicleEntity.setVehicleName(vehicle.getVehicleName());
            vehicleEntity.setCategory(vehicle.getCategory());
            vehicleEntity.setVehicleType(vehicle.getVehicleType());
            vehicleEntity.setCapacitySpec(vehicle.getCapacitySpec());
            
            vehicleEntity = vehicleRepository.save(vehicleEntity);
            
            log.info("차량 정보 수정 - vehicleCode: {}, vehicleName: {}", 
                    vehicleEntity.getVehicleCode(), vehicleEntity.getVehicleName());
        }
        
        return VehicleRespDto.fromEntity(vehicleEntity);
    }

    /**
     * 차량 다중 삭제 (Hard Delete)
     */
    @Transactional
    public RespDto<VehicleBatchResult> deleteVehicles(VehicleDeleteReqDto reqDto) {
        
        log.info("차량 다중 삭제 시작 - 총 {}건", reqDto.getVehicleCodes().size());
        
        List<Integer> successCodes = new ArrayList<>();
        List<VehicleBatchResult.VehicleErrorDto> failData = new ArrayList<>();
        
        for (Integer vehicleCode : reqDto.getVehicleCodes()) {
            try {
                // 개별 차량 삭제 처리
                deleteSingleVehicle(vehicleCode);
                successCodes.add(vehicleCode);
                
                log.info("차량 삭제 성공 - vehicleCode: {}", vehicleCode);
                
            } catch (Exception e) {
                log.error("차량 삭제 실패 - vehicleCode: {}, 에러: {}", vehicleCode, e.getMessage());
                
                // 에러 시 차량명 조회 시도
                String vehicleName = getVehicleNameSafely(vehicleCode);
                
                VehicleBatchResult.VehicleErrorDto errorDto = VehicleBatchResult.VehicleErrorDto.builder()
                        .vehicleCode(vehicleCode)
                        .vehicleName(vehicleName)
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성 (삭제는 successData 대신 성공 코드만)
        VehicleBatchResult result = VehicleBatchResult.builder()
                .totalCount(reqDto.getVehicleCodes().size())
                .successCount(successCodes.size())
                .failCount(failData.size())
                .successData(successCodes.stream()
                        .map(code -> VehicleRespDto.builder().vehicleCode(code).build())
                        .collect(Collectors.toList()))
                .failData(failData)
                .build();
        
        String message = String.format("차량 삭제 완료 - 성공: %d건, 실패: %d건", 
                successCodes.size(), failData.size());
        
        log.info("차량 다중 삭제 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getVehicleCodes().size(), successCodes.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 차량 삭제 처리 (배송중인 주문 확인 추가)
     */
    private void deleteSingleVehicle(Integer vehicleCode) {
        
        // 차량 존재 확인
        Vehicle vehicle = vehicleRepository.findById(vehicleCode)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 차량입니다: " + vehicleCode));

        // 차량에 배송중인 주문이 있는지 확인
        List<String> activeOrderNos = orderRepository.findActiveOrdersByVehicleCode(vehicleCode);
        if (!activeOrderNos.isEmpty()) {
            String orderNosStr = String.join(", ", activeOrderNos);
            throw new RuntimeException("차량이 배송중인 주문에 할당되어 있어 삭제할 수 없습니다. 주문번호: " + orderNosStr);
        }

        // Hard Delete - 실제 레코드 삭제
        vehicleRepository.delete(vehicle);
        
        log.info("차량 삭제 완료 - vehicleCode: {}, vehicleName: {}", 
                vehicleCode, vehicle.getVehicleName());
    }
    
    /**
     * 차량명 안전 조회 (에러 발생시 사용)
     */
    private String getVehicleNameSafely(Integer vehicleCode) {
        try {
            return vehicleRepository.findById(vehicleCode)
                    .map(Vehicle::getVehicleName)
                    .orElse("알 수 없음");
        } catch (Exception e) {
            return "조회 실패";
        }
    }
}