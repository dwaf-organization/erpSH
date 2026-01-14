package com.inc.sh.controller.platform;

import com.inc.sh.service.platform.DeliveryAppConnectionService;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.platform.reqDto.DeliveryAppConnectionReqDto;
import com.inc.sh.dto.platform.reqDto.DeliveryAppDeleteReqDto;
import com.inc.sh.dto.platform.reqDto.DeliveryAppInfoReqDto;
import com.inc.sh.dto.platform.reqDto.DeliveryAppSaveReqDto;
import com.inc.sh.dto.platform.respDto.DeliveryAppConnectionRespDto;
import com.inc.sh.dto.platform.respDto.DeliveryAppInfoRespDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/platform")
@RequiredArgsConstructor
@Slf4j
public class DeliveryAppConnectionController {
    
    private final DeliveryAppConnectionService deliveryAppConnectionService;
    
    /**
     * 배달앱 연결 관리 조회
     * 
     * GET /api/v1/platform/delivery-app-connections?hqCode=2&brandCode=0&page=0
     * 
     * @param hqCode 거래처코드 (본사코드)
     * @param brandCode 브랜드코드 (0: 전체, 특정값: 해당 브랜드만)
     * @param page 페이지 번호 (0부터 시작)
     * @return 배달앱 연결 관리 정보 (페이징)
     */
    @GetMapping("/delivery-app-connections")
    public RespDto<DeliveryAppConnectionRespDto> getDeliveryAppConnections(
            @RequestParam("hqCode") Integer hqCode,
            @RequestParam(value = "brandCode", defaultValue = "0") Integer brandCode) {
        
        try {
            log.info("배달앱 연결 관리 조회 API 호출 - 거래처: {}, 브랜드: {}", 
                    hqCode, brandCode);
            
            DeliveryAppConnectionReqDto reqDto = DeliveryAppConnectionReqDto.builder()
                    .hqCode(hqCode)
                    .brandCode(brandCode)
                    .build();
            
            return deliveryAppConnectionService.getDeliveryAppConnections(reqDto);
            
        } catch (Exception e) {
            log.error("배달앱 연결 관리 조회 API 오류", e);
            return RespDto.fail("배달앱 연결 관리 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 거래처별 배달앱 정보 상세 조회
     * 
     * GET /api/v1/platform/delivery-app-info?hqCode=2&customerCode=123
     * 
     * @param hqCode 거래처코드 (본사코드)
     * @param customerCode 거래처코드
     * @return 거래처별 배달앱 상세 정보
     */
    @GetMapping("/delivery-app-info")
    public RespDto<DeliveryAppInfoRespDto> getDeliveryAppInfo(
            @RequestParam("hqCode") Integer hqCode,
            @RequestParam("customerCode") Integer customerCode) {
        
        try {
            log.info("거래처별 배달앱 정보 조회 API 호출 - 본사: {}, 거래처: {}", 
                    hqCode, customerCode);
            
            DeliveryAppInfoReqDto reqDto = DeliveryAppInfoReqDto.builder()
                    .hqCode(hqCode)
                    .customerCode(customerCode)
                    .build();
            
            return deliveryAppConnectionService.getDeliveryAppInfo(reqDto);
            
        } catch (Exception e) {
            log.error("거래처별 배달앱 정보 조회 API 오류", e);
            return RespDto.fail("거래처별 배달앱 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 배달앱 정보 저장 (등록/수정)
     * 
     * POST /api/v1/platform/delivery-app-info
     * 
     * @param reqDto 배달앱 정보 저장 요청
     * @return 저장 결과
     */
    @PostMapping("/delivery-app-info")
    public RespDto<String> saveDeliveryAppInfo(@RequestBody DeliveryAppSaveReqDto reqDto) {
        
        try {
            log.info("배달앱 정보 저장 API 호출 - 본사: {}, 거래처: {}, 플랫폼: {}", 
                    reqDto.getHqCode(), reqDto.getCustomerCode(), reqDto.getPlatform());
            
            return deliveryAppConnectionService.saveDeliveryAppInfo(reqDto);
            
        } catch (Exception e) {
            log.error("배달앱 정보 저장 API 오류", e);
            return RespDto.fail("배달앱 정보 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 배달앱 정보 삭제 (하드 딜리트)
     * 
     * DELETE /api/v1/platform/delivery-app-info
     * 
     * @param reqDto 배달앱 정보 삭제 요청
     * @return 삭제 결과
     */
    @DeleteMapping("/delivery-app-info")
    public RespDto<String> deleteDeliveryAppInfo(@RequestBody DeliveryAppDeleteReqDto reqDto) {
        
        try {
            log.info("배달앱 정보 삭제 API 호출 - 본사: {}, 거래처: {}, 플랫폼: {}", 
                    reqDto.getHqCode(), reqDto.getCustomerCode(), reqDto.getPlatform());
            
            return deliveryAppConnectionService.deleteDeliveryAppInfo(reqDto);
            
        } catch (Exception e) {
            log.error("배달앱 정보 삭제 API 오류", e);
            return RespDto.fail("배달앱 정보 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}