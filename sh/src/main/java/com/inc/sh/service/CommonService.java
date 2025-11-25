package com.inc.sh.service;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.common.respDto.BrandSelectDto;
import com.inc.sh.dto.common.respDto.DistCenterSelectDto;
import com.inc.sh.dto.common.respDto.WarehouseSelectDto;
import com.inc.sh.dto.common.respDto.ItemCategorySelectDto;
import com.inc.sh.dto.common.respDto.RoleSelectDto;
import com.inc.sh.dto.common.respDto.VehicleSelectDto;
import com.inc.sh.dto.common.respDto.OrderSelectDto;
import com.inc.sh.entity.BrandInfo;
import com.inc.sh.entity.DistCenter;
import com.inc.sh.entity.Warehouse;
import com.inc.sh.entity.Vehicle;
import com.inc.sh.entity.Role;
import com.inc.sh.repository.BrandRepository;
import com.inc.sh.repository.DistCenterRepository;
import com.inc.sh.repository.WarehouseRepository;
import com.inc.sh.repository.VehicleRepository;
import com.inc.sh.repository.ItemCategoryRepository;
import com.inc.sh.repository.RoleRepository;
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
@Transactional(readOnly = true)
@Slf4j
public class CommonService {
    
    private final BrandRepository brandRepository;
    private final DistCenterRepository distCenterRepository;
    private final WarehouseRepository warehouseRepository;
    private final VehicleRepository vehicleRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final RoleRepository roleRepository;
    private final OrderRepository orderRepository;
    private final HeadquarterRepository headquarterRepository;
    
    /**
     * 브랜드 셀렉트박스 목록 조회
     * @param hqCode 본사코드
     * @return 브랜드 목록 (value: 브랜드코드, label: 브랜드명)
     */
    public RespDto<List<BrandSelectDto>> getBrandSelectList(Integer hqCode) {
        try {
            log.info("브랜드 셀렉트박스 목록 조회 시작 - hqCode: {}", hqCode);
            
            // 본사 존재 확인
            if (!headquarterRepository.existsByHqCode(hqCode)) {
                log.warn("본사가 존재하지 않습니다 - hqCode: {}", hqCode);
                return RespDto.fail("본사가 존재하지 않습니다.");
            }
            
            // 본사별 브랜드 조회
            List<BrandInfo> brands = brandRepository.findByHqCode(hqCode);
            
            // DTO 변환
            List<BrandSelectDto> selectList = brands.stream()
                    .map(BrandSelectDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("브랜드 셀렉트박스 목록 조회 완료 - hqCode: {}, 조회 건수: {}", hqCode, selectList.size());
            return RespDto.success("브랜드 목록 조회 성공", selectList);
            
        } catch (Exception e) {
            log.error("브랜드 셀렉트박스 목록 조회 중 오류 발생 - hqCode: {}", hqCode, e);
            return RespDto.fail("브랜드 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 물류센터 셀렉트박스 목록 조회
     * @param hqCode 본사코드
     * @return 물류센터 목록 (value: 물류센터코드, label: 물류센터명)
     */
    public RespDto<List<DistCenterSelectDto>> getDistCenterSelectList(Integer hqCode) {
        try {
            log.info("물류센터 셀렉트박스 목록 조회 시작 - hqCode: {}", hqCode);
            
            // 본사 존재 확인
            if (!headquarterRepository.existsByHqCode(hqCode)) {
                log.warn("본사가 존재하지 않습니다 - hqCode: {}", hqCode);
                return RespDto.fail("본사가 존재하지 않습니다.");
            }
            
            // 본사별 물류센터 조회 (사용중인 것만)
            List<DistCenter> distCenters = distCenterRepository.findByHqCode(hqCode).stream()
                    .filter(dc -> dc.getUseYn() == 1) // 사용중인 물류센터만
                    .collect(Collectors.toList());
            
            // DTO 변환
            List<DistCenterSelectDto> selectList = distCenters.stream()
                    .map(DistCenterSelectDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("물류센터 셀렉트박스 목록 조회 완료 - hqCode: {}, 조회 건수: {}", hqCode, selectList.size());
            return RespDto.success("물류센터 목록 조회 성공", selectList);
            
        } catch (Exception e) {
            log.error("물류센터 셀렉트박스 목록 조회 중 오류 발생 - hqCode: {}", hqCode, e);
            return RespDto.fail("물류센터 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 창고 셀렉트박스 목록 조회 (본사별)
     * @param hqCode 본사코드
     * @return 창고 목록 (value: 창고코드, label: 창고명)
     */
    public RespDto<List<WarehouseSelectDto>> getWarehouseSelectList(Integer hqCode) {
        try {
            log.info("창고 셀렉트박스 목록 조회 시작 - hqCode: {}", hqCode);
            
            // 본사 존재 확인
            if (!headquarterRepository.existsByHqCode(hqCode)) {
                log.warn("본사가 존재하지 않습니다 - hqCode: {}", hqCode);
                return RespDto.fail("본사가 존재하지 않습니다.");
            }
            
            // 본사별 창고 조회 (사용중인 것만)
            List<Warehouse> warehouses = warehouseRepository.findByHqCode(hqCode).stream()
                    .filter(w -> w.getUseYn() == 1) // 사용중인 창고만
                    .collect(Collectors.toList());
            
            // DTO 변환
            List<WarehouseSelectDto> selectList = warehouses.stream()
                    .map(WarehouseSelectDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("창고 셀렉트박스 목록 조회 완료 - hqCode: {}, 조회 건수: {}", hqCode, selectList.size());
            return RespDto.success("창고 목록 조회 성공", selectList);
            
        } catch (Exception e) {
            log.error("창고 셀렉트박스 목록 조회 중 오류 발생 - hqCode: {}", hqCode, e);
            return RespDto.fail("창고 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 창고 셀렉트박스 목록 조회 (물류센터별)
     * @param distCenterCode 물류센터코드
     * @return 창고 목록 (value: 창고코드, label: 창고명)
     */
    public RespDto<List<WarehouseSelectDto>> getWarehouseSelectListByDistCenter(Integer distCenterCode) {
        try {
            log.info("창고 셀렉트박스 목록 조회(물류센터별) 시작 - distCenterCode: {}", distCenterCode);
            
            // 물류센터 존재 확인
            DistCenter distCenter = distCenterRepository.findByDistCenterCode(distCenterCode);
            if (distCenter == null) {
                log.warn("물류센터가 존재하지 않습니다 - distCenterCode: {}", distCenterCode);
                return RespDto.fail("물류센터가 존재하지 않습니다.");
            }
            
            // 물류센터별 창고 조회 (사용중인 것만)
            List<Warehouse> warehouses = warehouseRepository.findByDistCenterCode(distCenterCode).stream()
                    .filter(w -> w.getUseYn() == 1) // 사용중인 창고만
                    .collect(Collectors.toList());
            
            // DTO 변환
            List<WarehouseSelectDto> selectList = warehouses.stream()
                    .map(WarehouseSelectDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("창고 셀렉트박스 목록 조회(물류센터별) 완료 - distCenterCode: {}, 조회 건수: {}", 
                    distCenterCode, selectList.size());
            return RespDto.success("창고 목록 조회 성공", selectList);
            
        } catch (Exception e) {
            log.error("창고 셀렉트박스 목록 조회(물류센터별) 중 오류 발생 - distCenterCode: {}", distCenterCode, e);
            return RespDto.fail("창고 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 품목분류 셀렉트박스 목록 조회 (중분류만, 대분류명 포함)
     * @param hqCode 본사코드
     * @return 품목분류 목록 (value: 중분류코드, label: "대분류명-중분류명")
     */
    public RespDto<List<ItemCategorySelectDto>> getItemCategorySelectList(Integer hqCode) {
        try {
            log.info("품목분류 셀렉트박스 목록 조회 시작 - hqCode: {}", hqCode);
            
            // 본사 존재 확인
            if (!headquarterRepository.existsByHqCode(hqCode)) {
                log.warn("본사가 존재하지 않습니다 - hqCode: {}", hqCode);
                return RespDto.fail("본사가 존재하지 않습니다.");
            }
            
            // 중분류와 대분류명 조회 (상위분류코드 != 0)
            List<Object[]> categories = itemCategoryRepository.findSubCategoriesWithMajorName(hqCode);
            
            // DTO 변환
            List<ItemCategorySelectDto> selectList = categories.stream()
                    .map(result -> ItemCategorySelectDto.of(
                            (Integer) result[0],  // 중분류코드
                            (String) result[1],   // 대분류명
                            (String) result[2]    // 중분류명
                    ))
                    .collect(Collectors.toList());
            
            log.info("품목분류 셀렉트박스 목록 조회 완료 - hqCode: {}, 조회 건수: {}", hqCode, selectList.size());
            return RespDto.success("품목분류 목록 조회 성공", selectList);
            
        } catch (Exception e) {
            log.error("품목분류 셀렉트박스 목록 조회 중 오류 발생 - hqCode: {}", hqCode, e);
            return RespDto.fail("품목분류 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 권한 셀렉트박스 목록 조회
     * @param hqCode 본사코드
     * @return 권한 목록 (value: 권한코드, label: 권한명)
     */
    public RespDto<List<RoleSelectDto>> getRoleSelectList(Integer hqCode) {
        try {
            log.info("권한 셀렉트박스 목록 조회 시작 - hqCode: {}", hqCode);
            
            // 본사 존재 확인
            if (!headquarterRepository.existsByHqCode(hqCode)) {
                log.warn("본사가 존재하지 않습니다 - hqCode: {}", hqCode);
                return RespDto.fail("본사가 존재하지 않습니다.");
            }
            
            // 본사별 권한 조회 (권한코드 순)
            List<Role> roles = roleRepository.findByHqCodeOrderByRoleCode(hqCode);
            
            // DTO 변환
            List<RoleSelectDto> selectList = roles.stream()
                    .map(RoleSelectDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("권한 셀렉트박스 목록 조회 완료 - hqCode: {}, 조회 건수: {}", hqCode, selectList.size());
            return RespDto.success("권한 목록 조회 성공", selectList);
            
        } catch (Exception e) {
            log.error("권한 셀렉트박스 목록 조회 중 오류 발생 - hqCode: {}", hqCode, e);
            return RespDto.fail("권한 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 차량 목록 조회 (셀렉트박스용)
     */
    @Transactional(readOnly = true)
    public RespDto<List<VehicleSelectDto>> getVehicleList(Integer hqCode) {
        try {
            log.info("차량 목록 조회 시작 - hqCode: {}", hqCode);
            
            List<Vehicle> vehicles = vehicleRepository.findByHqCodeOrderByVehicleCode(hqCode);
            
            List<VehicleSelectDto> vehicleList = vehicles.stream()
                    .map(vehicle -> VehicleSelectDto.builder()
                            .value(vehicle.getVehicleCode())
                            .label(vehicle.getVehicleName())
                            .build())
                    .collect(Collectors.toList());
            
            log.info("차량 목록 조회 완료 - hqCode: {}, 조회 건수: {}", hqCode, vehicleList.size());
            return RespDto.success("차량 목록 조회 성공", vehicleList);
            
        } catch (Exception e) {
            log.error("차량 목록 조회 중 오류 발생 - hqCode: {}", hqCode, e);
            return RespDto.fail("차량 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 주문번호 목록 조회 (반품등록용 셀렉트박스)
     */
    @Transactional(readOnly = true)
    public RespDto<List<OrderSelectDto>> getOrderList(Integer customerCode, Integer hqCode) {
        try {
            log.info("주문번호 목록 조회 시작 - customerCode: {}, hqCode: {}", customerCode, hqCode);
            
            // 30일 전 날짜 계산
            String thirtyDaysAgo = java.time.LocalDate.now()
                    .minusDays(30)
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            List<String> orderNumbers = orderRepository.findRecentCompletedOrdersByCustomer(customerCode, thirtyDaysAgo);
            
            List<OrderSelectDto> orderList = orderNumbers.stream()
                    .map(orderNo -> OrderSelectDto.builder()
                            .value(orderNo)
                            .label(orderNo)
                            .build())
                    .collect(Collectors.toList());
            
            log.info("주문번호 목록 조회 완료 - customerCode: {}, 조회 건수: {}", customerCode, orderList.size());
            return RespDto.success("주문번호 목록 조회 성공", orderList);
            
        } catch (Exception e) {
            log.error("주문번호 목록 조회 중 오류 발생 - customerCode: {}", customerCode, e);
            return RespDto.fail("주문번호 목록 조회 중 오류가 발생했습니다.");
        }
    }
}