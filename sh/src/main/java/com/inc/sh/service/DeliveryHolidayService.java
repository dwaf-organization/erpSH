package com.inc.sh.service;

import com.inc.sh.dto.deliveryHoliday.reqDto.DeliveryHolidaySearchDto;
import com.inc.sh.dto.deliveryHoliday.reqDto.BasicHolidayReqDto;
import com.inc.sh.dto.deliveryHoliday.reqDto.DeliveryHolidayDeleteReqDto;
import com.inc.sh.dto.deliveryHoliday.reqDto.RegularHolidayReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.deliveryHoliday.respDto.DeliveryHolidayRespDto;
import com.inc.sh.dto.deliveryHoliday.respDto.DeliveryHolidaySaveRespDto;
import com.inc.sh.dto.deliveryHoliday.respDto.DeliveryHolidayBatchResult;
import com.inc.sh.dto.deliveryHoliday.respDto.DeliveryHolidayDeleteRespDto;
import com.inc.sh.entity.DeliveryHoliday;
import com.inc.sh.entity.BrandInfo;
import com.inc.sh.repository.BrandRepository;
import com.inc.sh.repository.DeliveryHolidayRepository;
import com.inc.sh.repository.HeadquarterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DeliveryHolidayService {
    
    private final DeliveryHolidayRepository deliveryHolidayRepository;
    private final BrandRepository brandRepository;
    private final HeadquarterRepository headquarterRepository;
    
    // 날짜 패턴 정규식
    private static final Pattern BASIC_DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final Pattern REGULAR_DATE_PATTERN = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{2}~\\d{2}\\.\\d{2}\\.\\d{2}");
    
    /**
     * 배송휴일 목록 조회 (브랜드명 포함)
     * @param searchDto 검색 조건
     * @return 조회된 배송휴일 목록
     */
    @Transactional(readOnly = true)
    public RespDto<List<DeliveryHolidayRespDto>> getDeliveryHolidayList(DeliveryHolidaySearchDto searchDto) {
        try {
            log.info("배송휴일 목록 조회 시작 - brandCode: {}, hqCode: {}", 
                    searchDto.getBrandCode(), searchDto.getHqCode());
            
            List<Object[]> holidays = deliveryHolidayRepository.findHolidaysWithBrandNameAndHqCode(
                    searchDto.getBrandCode(), 
                    searchDto.getHqCode()
            );
            
            List<DeliveryHolidayRespDto> responseList = holidays.stream()
                    .map(DeliveryHolidayRespDto::of)
                    .collect(Collectors.toList());
            
            log.info("배송휴일 목록 조회 완료 - hqCode: {}, 조회 건수: {}", searchDto.getHqCode(), responseList.size());
            return RespDto.success("배송휴일 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("배송휴일 목록 조회 중 오류 발생 - hqCode: {}", searchDto.getHqCode(), e);
            return RespDto.fail("배송휴일 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 기본휴일 등록
     * @param request 기본휴일 정보
     * @return 등록된 휴일 코드
     */
    public RespDto<DeliveryHolidaySaveRespDto> saveBasicHoliday(BasicHolidayReqDto request) {
        try {
            log.info("기본휴일 등록 시작 - brandCode: {}, holidayDt: {}, holidayName: {}", 
                    request.getBrandCode(), request.getHolidayDt(), request.getHolidayName());
            
            // 브랜드 존재 확인
            BrandInfo brandInfo = brandRepository.findByBrandCode(request.getBrandCode());
            if (brandInfo == null) {
                log.warn("브랜드가 존재하지 않습니다 - brandCode: {}", request.getBrandCode());
                return RespDto.fail("브랜드가 존재하지 않습니다.");
            }
            
            // 본사 존재 확인
            if (!headquarterRepository.existsByHqCode(request.getHqCode())) {
                log.warn("본사가 존재하지 않습니다 - hqCode: {}", request.getHqCode());
                return RespDto.fail("본사가 존재하지 않습니다.");
            }
            
            // 기본휴일 날짜 형식 검증 (YYYY-MM-DD)
            if (!BASIC_DATE_PATTERN.matcher(request.getHolidayDt()).matches()) {
                log.warn("기본휴일 날짜 형식이 올바르지 않습니다 - holidayDt: {}", request.getHolidayDt());
                return RespDto.fail("기본휴일 날짜 형식이 올바르지 않습니다. (YYYY-MM-DD)");
            }
            
            // 요일 검증
            if (!isValidWeekday(request.getWeekday())) {
                log.warn("요일이 올바르지 않습니다 - weekday: {}", request.getWeekday());
                return RespDto.fail("요일이 올바르지 않습니다. (월, 화, 수, 목, 금, 토, 일 중 선택)");
            }
            
            DeliveryHoliday holiday = request.toEntity();
            DeliveryHoliday savedHoliday = deliveryHolidayRepository.save(holiday);
            
            DeliveryHolidaySaveRespDto responseDto = DeliveryHolidaySaveRespDto.builder()
                    .deliveryHolidayCode(savedHoliday.getDeliveryHolidayCode())
                    .holidayType("기본휴일")
                    .build();
            
            log.info("기본휴일 등록 완료 - deliveryHolidayCode: {}, brandName: {}, holidayName: {}", 
                    savedHoliday.getDeliveryHolidayCode(), brandInfo.getBrandName(), savedHoliday.getHolidayName());
            
            return RespDto.success("기본휴일이 성공적으로 등록되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("기본휴일 등록 중 오류 발생 - brandCode: {}", request.getBrandCode(), e);
            return RespDto.fail("기본휴일 등록 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 정기휴일 등록
     * @param request 정기휴일 정보
     * @return 등록된 휴일 코드
     */
    public RespDto<DeliveryHolidaySaveRespDto> saveRegularHoliday(RegularHolidayReqDto request) {
        try {
            log.info("정기휴일 등록 시작 - brandCode: {}, holidayDt: {}, holidayName: {}", 
                    request.getBrandCode(), request.getHolidayDt(), request.getHolidayName());
            
            // 브랜드 존재 확인
            BrandInfo brandInfo = brandRepository.findByBrandCode(request.getBrandCode());
            if (brandInfo == null) {
                log.warn("브랜드가 존재하지 않습니다 - brandCode: {}", request.getBrandCode());
                return RespDto.fail("브랜드가 존재하지 않습니다.");
            }
            
            // 본사 존재 확인
            if (!headquarterRepository.existsByHqCode(request.getHqCode())) {
                log.warn("본사가 존재하지 않습니다 - hqCode: {}", request.getHqCode());
                return RespDto.fail("본사가 존재하지 않습니다.");
            }
            
            // 정기휴일 날짜 형식 검증 (00.00.00~00.00.00)
            if (!REGULAR_DATE_PATTERN.matcher(request.getHolidayDt()).matches()) {
                log.warn("정기휴일 날짜 형식이 올바르지 않습니다 - holidayDt: {}", request.getHolidayDt());
                return RespDto.fail("정기휴일 날짜 형식이 올바르지 않습니다. (MM.DD.YY~MM.DD.YY)");
            }
            
            // 요일 검증
            if (!isValidWeekday(request.getWeekday())) {
                log.warn("요일이 올바르지 않습니다 - weekday: {}", request.getWeekday());
                return RespDto.fail("요일이 올바르지 않습니다. (월, 화, 수, 목, 금, 토, 일 중 선택)");
            }
            
            DeliveryHoliday holiday = request.toEntity();
            DeliveryHoliday savedHoliday = deliveryHolidayRepository.save(holiday);
            
            DeliveryHolidaySaveRespDto responseDto = DeliveryHolidaySaveRespDto.builder()
                    .deliveryHolidayCode(savedHoliday.getDeliveryHolidayCode())
                    .holidayType("정기휴일")
                    .build();
            
            log.info("정기휴일 등록 완료 - deliveryHolidayCode: {}, brandName: {}, holidayName: {}", 
                    savedHoliday.getDeliveryHolidayCode(), brandInfo.getBrandName(), savedHoliday.getHolidayName());
            
            return RespDto.success("정기휴일이 성공적으로 등록되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("정기휴일 등록 중 오류 발생 - brandCode: {}", request.getBrandCode(), e);
            return RespDto.fail("정기휴일 등록 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 배송휴일 다중 삭제 (Hard Delete)
     */
    @Transactional
    public RespDto<DeliveryHolidayBatchResult> deleteDeliveryHolidays(DeliveryHolidayDeleteReqDto reqDto) {
        
        log.info("배송휴일 다중 삭제 시작 - 총 {}건", reqDto.getDeliveryHolidayCodes().size());
        
        List<DeliveryHolidayDeleteRespDto> successData = new ArrayList<>();
        List<DeliveryHolidayBatchResult.DeliveryHolidayErrorDto> failData = new ArrayList<>();
        
        for (Integer deliveryHolidayCode : reqDto.getDeliveryHolidayCodes()) {
            try {
                // 개별 배송휴일 삭제 처리
                DeliveryHolidayDeleteRespDto deletedHoliday = deleteSingleDeliveryHoliday(deliveryHolidayCode);
                successData.add(deletedHoliday);
                
                log.info("배송휴일 삭제 성공 - deliveryHolidayCode: {}", deliveryHolidayCode);
                
            } catch (Exception e) {
                log.error("배송휴일 삭제 실패 - deliveryHolidayCode: {}, 에러: {}", deliveryHolidayCode, e.getMessage());
                
                // 에러 시 휴일명 조회 시도
                String holidayName = getHolidayNameSafely(deliveryHolidayCode);
                
                DeliveryHolidayBatchResult.DeliveryHolidayErrorDto errorDto = DeliveryHolidayBatchResult.DeliveryHolidayErrorDto.builder()
                        .deliveryHolidayCode(deliveryHolidayCode)
                        .holidayName(holidayName)
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성
        DeliveryHolidayBatchResult result = DeliveryHolidayBatchResult.builder()
                .totalCount(reqDto.getDeliveryHolidayCodes().size())
                .successCount(successData.size())
                .failCount(failData.size())
                .successData(successData)
                .failData(failData)
                .build();
        
        String message = String.format("배송휴일 삭제 완료 - 성공: %d건, 실패: %d건", 
                successData.size(), failData.size());
        
        log.info("배송휴일 다중 삭제 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getDeliveryHolidayCodes().size(), successData.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 배송휴일 삭제 처리 (Hard Delete)
     */
    private DeliveryHolidayDeleteRespDto deleteSingleDeliveryHoliday(Integer deliveryHolidayCode) {
        
        // 배송휴일 존재 확인
        DeliveryHoliday deliveryHoliday = deliveryHolidayRepository.findById(deliveryHolidayCode)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 배송휴일입니다: " + deliveryHolidayCode));

        // Hard Delete - 실제 레코드 삭제
        deliveryHolidayRepository.delete(deliveryHoliday);
        
        log.info("배송휴일 삭제 완료 - deliveryHolidayCode: {}, holidayName: {}", 
                deliveryHolidayCode, deliveryHoliday.getHolidayName());
        
        // 삭제 응답 생성
        return DeliveryHolidayDeleteRespDto.builder()
                .deliveryHolidayCode(deliveryHolidayCode)
                .message(String.format("'%s' 휴일이 삭제되었습니다.", deliveryHoliday.getHolidayName()))
                .build();
    }
    
    /**
     * 휴일명 안전 조회 (에러 발생시 사용)
     */
    private String getHolidayNameSafely(Integer deliveryHolidayCode) {
        try {
            return deliveryHolidayRepository.findById(deliveryHolidayCode)
                    .map(DeliveryHoliday::getHolidayName)
                    .orElse("알 수 없음");
        } catch (Exception e) {
            return "조회 실패";
        }
    }
    
    /**
     * 요일 유효성 검증
     */
    private boolean isValidWeekday(String weekday) {
        if (weekday == null || weekday.trim().isEmpty()) {
            return false;
        }
        return weekday.equals("월") || weekday.equals("화") || weekday.equals("수") || 
               weekday.equals("목") || weekday.equals("금") || weekday.equals("토") || weekday.equals("일");
    }
}