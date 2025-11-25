package com.inc.sh.service;

import com.inc.sh.dto.vehicle.reqDto.VehicleSearchDto;
import com.inc.sh.dto.vehicle.reqDto.VehicleReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.vehicle.respDto.VehicleRespDto;
import com.inc.sh.dto.vehicle.respDto.VehicleSaveRespDto;
import com.inc.sh.dto.vehicle.respDto.VehicleDeleteRespDto;
import com.inc.sh.entity.Vehicle;
import com.inc.sh.repository.VehicleRepository;
import com.inc.sh.repository.OrderRepository;
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
            log.info("차량 목록 조회 시작 - vehicleCode: {}, category: {}", 
                    searchDto.getVehicleCode(), searchDto.getCategory());
            
            List<Vehicle> vehicles = vehicleRepository.findBySearchConditions(
                    searchDto.getVehicleCode(),
                    searchDto.getCategory()
            );
            
            List<VehicleRespDto> responseList = vehicles.stream()
                    .map(VehicleRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("차량 목록 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("차량 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("차량 목록 조회 중 오류 발생", e);
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
     * 차량 저장 (신규/수정)
     * @param request 차량 정보
     * @return 저장된 차량 코드
     */
    public RespDto<VehicleSaveRespDto> saveVehicle(VehicleReqDto request) {
        try {
            // 본사 존재 확인
            if (!headquarterRepository.existsByHqCode(request.getHqCode())) {
                log.warn("본사가 존재하지 않습니다 - hqCode: {}", request.getHqCode());
                return RespDto.fail("본사가 존재하지 않습니다.");
            }
            
            // 구분 유효성 검증
            if (!isValidCategory(request.getCategory())) {
                log.warn("유효하지 않은 차량 구분입니다 - category: {}", request.getCategory());
                return RespDto.fail("유효하지 않은 차량 구분입니다. (냉장, 냉동, 상온 중 선택)");
            }
            
            Vehicle savedVehicle;
            String action;
            
            if (request.getVehicleCode() == null) {
                // 신규 등록
                log.info("차량 신규 등록 시작 - vehicleName: {}, category: {}", 
                        request.getVehicleName(), request.getCategory());
                
                Vehicle vehicle = request.toEntity();
                savedVehicle = vehicleRepository.save(vehicle);
                action = "등록";
                
            } else {
                // 수정
                log.info("차량 수정 시작 - vehicleCode: {}, vehicleName: {}", 
                        request.getVehicleCode(), request.getVehicleName());
                
                Vehicle existingVehicle = vehicleRepository.findByVehicleCode(request.getVehicleCode());
                if (existingVehicle == null) {
                    log.warn("수정할 차량을 찾을 수 없습니다 - vehicleCode: {}", request.getVehicleCode());
                    return RespDto.fail("수정할 차량을 찾을 수 없습니다.");
                }
                
                request.updateEntity(existingVehicle);
                savedVehicle = vehicleRepository.save(existingVehicle);
                action = "수정";
            }
            
            // 간소화된 응답 생성
            VehicleSaveRespDto responseDto = VehicleSaveRespDto.builder()
                    .vehicleCode(savedVehicle.getVehicleCode())
                    .build();
            
            log.info("차량 {} 완료 - vehicleCode: {}, vehicleName: {}, category: {}", 
                    action, savedVehicle.getVehicleCode(), savedVehicle.getVehicleName(), 
                    savedVehicle.getCategory());
            
            return RespDto.success("차량이 성공적으로 " + action + "되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("차량 저장 중 오류 발생 - vehicleCode: {}", request.getVehicleCode(), e);
            return RespDto.fail("차량 저장 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 차량 삭제 (하드 삭제)
     * @param vehicleCode 차량코드
     * @return 삭제 결과
     */
    public RespDto<VehicleDeleteRespDto> deleteVehicle(Integer vehicleCode) {
        try {
            log.info("차량 삭제 시작 - vehicleCode: {}", vehicleCode);
            
            Vehicle vehicle = vehicleRepository.findByVehicleCode(vehicleCode);
            if (vehicle == null) {
                log.warn("삭제할 차량을 찾을 수 없습니다 - vehicleCode: {}", vehicleCode);
                return RespDto.fail("삭제할 차량을 찾을 수 없습니다.");
            }
            
            // 배송중인 주문이 있는지 확인
            List<String> activeOrderNos = orderRepository.findActiveOrdersByVehicleCode(vehicleCode);
            if (!activeOrderNos.isEmpty()) {
                // 배송중인 주문이 있으면 삭제 중단
                log.warn("배송중인 주문이 있어 삭제할 수 없습니다 - vehicleCode: {}, activeOrders: {}", 
                        vehicleCode, activeOrderNos);
                
                String orderNosStr = String.join(", ", activeOrderNos);
                
                VehicleDeleteRespDto responseDto = VehicleDeleteRespDto.builder()
                        .vehicleCode(vehicleCode)
                        .activeOrderNos(activeOrderNos)
                        .message("차량이 배송중인 주문에 할당되어 있어 삭제할 수 없습니다. 주문번호: " + orderNosStr)
                        .build();
                
                return RespDto.fail("차량이 배송중인 주문에 할당되어 있어 삭제할 수 없습니다. 주문번호: " + orderNosStr);
            }
            
            // 하드 삭제 진행
            vehicleRepository.delete(vehicle);
            
            // 응답 생성
            VehicleDeleteRespDto responseDto = VehicleDeleteRespDto.builder()
                    .vehicleCode(vehicleCode)
                    .activeOrderNos(null)
                    .message("차량이 성공적으로 삭제되었습니다.")
                    .build();
            
            log.info("차량 삭제 완료 - vehicleCode: {}, vehicleName: {}", 
                    vehicleCode, vehicle.getVehicleName());
            
            return RespDto.success("차량이 성공적으로 삭제되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("차량 삭제 중 오류 발생 - vehicleCode: {}", vehicleCode, e);
            return RespDto.fail("차량 삭제 중 오류가 발생했습니다.");
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
}