package com.inc.sh.service.platform;

import com.inc.sh.repository.DeliveryAppConnectionRepository;
import com.inc.sh.repository.StorePlatformsRepository;
import com.inc.sh.entity.Customer;
import com.inc.sh.entity.StorePlatforms;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.platform.reqDto.DeliveryAppConnectionReqDto;
import com.inc.sh.dto.platform.reqDto.DeliveryAppDeleteReqDto;
import com.inc.sh.dto.platform.reqDto.DeliveryAppInfoReqDto;
import com.inc.sh.dto.platform.reqDto.DeliveryAppSaveReqDto;
import com.inc.sh.dto.platform.respDto.DeliveryAppConnectionRespDto;
import com.inc.sh.dto.platform.respDto.DeliveryAppInfoRespDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DeliveryAppConnectionService {
    
    private final DeliveryAppConnectionRepository deliveryAppConnectionRepository;
    private final StorePlatformsRepository storePlatformsRepository;
    
    private static final int PAGE_SIZE = 10; // 고정 사이즈
    
    /**
     * 배달앱 연결 관리 조회
     */
    public RespDto<DeliveryAppConnectionRespDto> getDeliveryAppConnections(DeliveryAppConnectionReqDto reqDto) {
        try {
            log.info("배달앱 연결 관리 조회 시작 - 거래처: {}, 브랜드: {}, 페이지: {}", 
                    reqDto.getHqCode(), reqDto.getBrandCode(), reqDto.getPage());
            
            // 1. 입력값 검증
            if (reqDto.getHqCode() == null) {
                return RespDto.fail("거래처코드는 필수입니다.");
            }
            if (reqDto.getBrandCode() == null) {
                return RespDto.fail("브랜드코드는 필수입니다.");
            }
            
            // 3. 배달앱 연결 정보 조회
            List<Map<String, Object>> rawConnections = deliveryAppConnectionRepository
                    .findDeliveryAppConnections(reqDto.getHqCode(), reqDto.getBrandCode());
            
            // 6. 데이터 변환
            List<DeliveryAppConnectionRespDto.DeliveryAppConnection> connections = rawConnections.stream()
                    .map(this::convertToDeliveryAppConnection)
                    .collect(Collectors.toList());
            
            DeliveryAppConnectionRespDto response = DeliveryAppConnectionRespDto.builder()
                    .deliveryAppConnections(connections)
                    .build();
            
            String brandInfo = reqDto.getBrandCode() == 0 ? "전체" : "브랜드(" + reqDto.getBrandCode() + ")";
            log.info("배달앱 연결 관리 조회 완료 - 거래처: {}, {}", 
                    reqDto.getHqCode(), brandInfo);
            
            return RespDto.success("배달앱 연결 관리 조회 완료", response);
            
        } catch (Exception e) {
            log.error("배달앱 연결 관리 조회 중 오류 발생", e);
            return RespDto.fail("배달앱 연결 관리 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * Map 데이터를 DeliveryAppConnection DTO로 변환
     */
    private DeliveryAppConnectionRespDto.DeliveryAppConnection convertToDeliveryAppConnection(Map<String, Object> data) {
        
        return DeliveryAppConnectionRespDto.DeliveryAppConnection.builder()
                .customerCode((Integer) data.get("customer_code"))
                .customerName((String) data.get("customer_name"))
                .businessNumber((String) data.get("biz_num"))  // 필드명 수정
                .baeminStatus((String) data.get("baemin_status"))
                .yogiyoStatus((String) data.get("yogiyo_status"))
                .coupangStatus((String) data.get("coupang_status"))
                .build();
    }
    
    /**
     * 거래처별 배달앱 정보 상세 조회
     */
    public RespDto<DeliveryAppInfoRespDto> getDeliveryAppInfo(DeliveryAppInfoReqDto reqDto) {
        try {
            log.info("거래처별 배달앱 정보 조회 시작 - 본사: {}, 거래처: {}", 
                    reqDto.getHqCode(), reqDto.getCustomerCode());
            
            // 1. 입력값 검증
            if (reqDto.getHqCode() == null) {
                return RespDto.fail("본사코드는 필수입니다.");
            }
            if (reqDto.getCustomerCode() == null) {
                return RespDto.fail("거래처코드는 필수입니다.");
            }
            
            // 2. 배달앱 정보 조회
            List<Map<String, Object>> rawData = deliveryAppConnectionRepository
                    .findDeliveryAppInfo(reqDto.getHqCode(), reqDto.getCustomerCode());
            
            if (rawData.isEmpty()) {
                return RespDto.fail("해당 거래처를 찾을 수 없습니다.");
            }
            
            // 3. 데이터 변환
            DeliveryAppInfoRespDto response = convertToDeliveryAppInfoResp(rawData);
            
            log.info("거래처별 배달앱 정보 조회 완료 - 거래처: {}, 거래처명: {}", 
                    response.getCustomerCode(), response.getCustomerName());
            
            return RespDto.success("거래처별 배달앱 정보 조회 완료", response);
            
        } catch (Exception e) {
            log.error("거래처별 배달앱 정보 조회 중 오류 발생", e);
            return RespDto.fail("거래처별 배달앱 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 배달앱 정보 저장 (등록/수정)
     */
    @Transactional
    public RespDto<String> saveDeliveryAppInfo(DeliveryAppSaveReqDto reqDto) {
        try {
            log.info("배달앱 정보 저장 시작 - 본사: {}, 거래처: {}, 플랫폼: {}", 
                    reqDto.getHqCode(), reqDto.getCustomerCode(), reqDto.getPlatform());
            
            // 1. 입력값 검증
            if (reqDto.getHqCode() == null) {
                return RespDto.fail("본사코드는 필수입니다.");
            }
            if (reqDto.getCustomerCode() == null) {
                return RespDto.fail("거래처코드는 필수입니다.");
            }
            if (reqDto.getPlatform() == null || reqDto.getPlatform().trim().isEmpty()) {
                return RespDto.fail("플랫폼은 필수입니다.");
            }
            if (!isValidPlatform(reqDto.getPlatform())) {
                return RespDto.fail("플랫폼은 '배민', '요기요', '쿠팡이츠' 중 하나여야 합니다.");
            }
            
            // 2. 기존 데이터 확인
            StorePlatforms existingEntity = storePlatformsRepository
                    .findByCustomerCodeAndPlatform(reqDto.getCustomerCode(), reqDto.getPlatform());
            
            String action;
            if (existingEntity != null) {
                // 수정
                action = updateDeliveryAppInfo(existingEntity, reqDto);
            } else {
                // 신규 등록
                action = createDeliveryAppInfo(reqDto);
            }
            
            log.info("배달앱 정보 저장 완료 - 거래처: {}, 플랫폼: {}, 작업: {}", 
                    reqDto.getCustomerCode(), reqDto.getPlatform(), action);
            
            return RespDto.success("배달앱 정보가 " + action + "되었습니다.", action);
            
        } catch (Exception e) {
            log.error("배달앱 정보 저장 중 오류 발생", e);
            return RespDto.fail("배달앱 정보 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 배달앱 정보 데이터 변환
     */
    private DeliveryAppInfoRespDto convertToDeliveryAppInfoResp(List<Map<String, Object>> rawData) {
        
        // 첫 번째 행에서 거래처 기본 정보 추출
        Map<String, Object> firstRow = rawData.get(0);
        Integer customerCode = (Integer) firstRow.get("customer_code");
        String customerName = (String) firstRow.get("customer_name");
        
        // 플랫폼별 정보 맵 생성
        Map<String, DeliveryAppInfoRespDto.PlatformInfo> platformMap = new HashMap<>();
        
        for (Map<String, Object> row : rawData) {
            String platform = (String) row.get("platform");
            if (platform != null) {
                String storeId = (String) row.get("platform_store_id");
                String loginId = (String) row.get("login_id");
                String loginPassword = (String) row.get("login_password");
                
                // 등록 상태 체크 (NULL이거나 빈값이면 미등록)
                String status = isCompleteInfo(storeId, loginId, loginPassword) ? "등록완료" : "미등록";
                
                DeliveryAppInfoRespDto.PlatformInfo platformInfo = DeliveryAppInfoRespDto.PlatformInfo.builder()
                        .platformStoreId(storeId)
                        .loginId(loginId)
                        .loginPassword(loginPassword)
                        .status(status)
                        .build();
                
                platformMap.put(platform, platformInfo);
            }
        }
        
        // 기본값 설정 (데이터가 없는 플랫폼)
        DeliveryAppInfoRespDto.PlatformInfo emptyInfo = DeliveryAppInfoRespDto.PlatformInfo.builder()
                .platformStoreId(null)
                .loginId(null)
                .loginPassword(null)
                .status("미등록")
                .build();
        
        return DeliveryAppInfoRespDto.builder()
                .customerCode(customerCode)
                .customerName(customerName)
                .baemin(platformMap.getOrDefault("배민", emptyInfo))
                .yogiyo(platformMap.getOrDefault("요기요", emptyInfo))
                .coupang(platformMap.getOrDefault("쿠팡이츠", emptyInfo))
                .build();
    }
    
    /**
     * 완전한 정보인지 체크
     */
    private boolean isCompleteInfo(String storeId, String loginId, String loginPassword) {
        return storeId != null && !storeId.trim().isEmpty() &&
               loginId != null && !loginId.trim().isEmpty() &&
               loginPassword != null && !loginPassword.trim().isEmpty();
    }
    
    /**
     * 유효한 플랫폼인지 체크
     */
    private boolean isValidPlatform(String platform) {
        return "배민".equals(platform) || "요기요".equals(platform) || "쿠팡이츠".equals(platform);
    }
    
    /**
     * 배달앱 정보 수정
     */
    private String updateDeliveryAppInfo(StorePlatforms existingEntity, DeliveryAppSaveReqDto reqDto) {
        
        log.info("기존 데이터 수정 시작 - storePlatformCode: {}, 새로운 정보: storeId={}, loginId={}", 
                existingEntity.getStorePlatformCode(), reqDto.getStoreId(), reqDto.getLoginId());
        
        // customer 테이블에서 customerName 조회
        Customer customer = deliveryAppConnectionRepository.findCustomerByCustomerCode(reqDto.getCustomerCode());
        
        // 데이터 업데이트
        existingEntity.setPlatformStoreName(customer.getCustomerName());
        existingEntity.setPlatformStoreId(reqDto.getStoreId());
        existingEntity.setLoginId(reqDto.getLoginId());
        existingEntity.setLoginPassword(reqDto.getLoginPassword());
        existingEntity.setLastSyncedAt(LocalDateTime.now().toString());
        existingEntity.setUpdatedAt(LocalDateTime.now());
        
        // 데이터베이스에 저장
        storePlatformsRepository.save(existingEntity);
        
        log.info("기존 데이터 수정 완료 - storePlatformCode: {}", existingEntity.getStorePlatformCode());
        
        return "수정";
    }
    
    /**
     * 배달앱 정보 신규 등록
     */
    private String createDeliveryAppInfo(DeliveryAppSaveReqDto reqDto) {
        
        log.info("신규 데이터 생성 시작 - 거래처: {}, 플랫폼: {}, storeId: {}, loginId: {}", 
                reqDto.getCustomerCode(), reqDto.getPlatform(), reqDto.getStoreId(), reqDto.getLoginId());
        
        // customer 테이블에서 brandCode 조회
        Integer brandCode = deliveryAppConnectionRepository.findBrandCodeByCustomerCode(reqDto.getCustomerCode());
        if (brandCode == null) {
            throw new RuntimeException("해당 거래처를 찾을 수 없습니다. customerCode: " + reqDto.getCustomerCode());
        }
        
        
        // customer 테이블에서 customerName 조회
        Customer customer = deliveryAppConnectionRepository.findCustomerByCustomerCode(reqDto.getCustomerCode());
        
        log.info("거래처 {} 의 brandCode: {} 조회 완료", reqDto.getCustomerCode(), brandCode);
        
        // 새로운 엔티티 생성
        StorePlatforms entity = new StorePlatforms();
        entity.setHqCode(reqDto.getHqCode());
        entity.setCustomerCode(reqDto.getCustomerCode());
        entity.setBrandCode(brandCode);  // customer 테이블에서 조회한 brandCode 설정
        entity.setPlatform(reqDto.getPlatform());
        entity.setPlatformStoreName(customer.getCustomerName());
        entity.setPlatformStoreId(reqDto.getStoreId());
        entity.setLoginId(reqDto.getLoginId());
        entity.setLoginPassword(reqDto.getLoginPassword());
        entity.setIsActive(1);
        entity.setLastSyncedAt(LocalDateTime.now().toString());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        
        // 데이터베이스에 저장
        StorePlatforms savedEntity = storePlatformsRepository.save(entity);
        
        log.info("신규 데이터 생성 완료 - storePlatformCode: {}, brandCode: {}", 
                savedEntity.getStorePlatformCode(), savedEntity.getBrandCode());
        
        return "등록";
    }
    
    /**
     * 배달앱 정보 삭제 (하드 딜리트)
     */
    @Transactional
    public RespDto<String> deleteDeliveryAppInfo(DeliveryAppDeleteReqDto reqDto) {
        try {
            log.info("배달앱 정보 삭제 시작 - 본사: {}, 거래처: {}, 플랫폼: {}", 
                    reqDto.getHqCode(), reqDto.getCustomerCode(), reqDto.getPlatform());
            
            // 1. 입력값 검증
            if (reqDto.getHqCode() == null) {
                return RespDto.fail("본사코드는 필수입니다.");
            }
            if (reqDto.getCustomerCode() == null) {
                return RespDto.fail("거래처코드는 필수입니다.");
            }
            if (reqDto.getPlatform() == null || reqDto.getPlatform().trim().isEmpty()) {
                return RespDto.fail("플랫폼은 필수입니다.");
            }
            if (!isValidPlatform(reqDto.getPlatform())) {
                return RespDto.fail("플랫폼은 '배민', '요기요', '쿠팡이츠' 중 하나여야 합니다.");
            }
            
            // 2. 삭제할 데이터 존재 여부 확인
            StorePlatforms existingEntity = storePlatformsRepository
                    .findByCustomerCodeAndPlatform(reqDto.getCustomerCode(), reqDto.getPlatform());
            
            if (existingEntity == null) {
                return RespDto.fail("삭제할 데이터가 존재하지 않습니다.");
            }
            
            // 3. 하드 딜리트 실행
            int deletedCount = storePlatformsRepository
                    .deleteByCustomerCodeAndPlatform(reqDto.getCustomerCode(), reqDto.getPlatform());
            
            if (deletedCount > 0) {
                log.info("배달앱 정보 삭제 완료 - 거래처: {}, 플랫폼: {}, 삭제된 건수: {}건", 
                        reqDto.getCustomerCode(), reqDto.getPlatform(), deletedCount);
                
                return RespDto.success("배달앱 정보가 삭제되었습니다.", "삭제 완료");
            } else {
                log.warn("배달앱 정보 삭제 실패 - 거래처: {}, 플랫폼: {}", 
                        reqDto.getCustomerCode(), reqDto.getPlatform());
                
                return RespDto.fail("배달앱 정보 삭제에 실패했습니다.");
            }
            
        } catch (Exception e) {
            log.error("배달앱 정보 삭제 중 오류 발생", e);
            return RespDto.fail("배달앱 정보 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}