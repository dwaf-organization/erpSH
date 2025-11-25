package com.inc.sh.service;

import com.inc.sh.dto.deliveryHoliday.reqDto.DeliveryHolidaySearchDto;
import com.inc.sh.dto.deliveryHoliday.reqDto.BasicHolidayReqDto;
import com.inc.sh.dto.deliveryHoliday.reqDto.RegularHolidayReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.deliveryHoliday.respDto.DeliveryHolidayRespDto;
import com.inc.sh.dto.deliveryHoliday.respDto.DeliveryHolidaySaveRespDto;
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
            log.info("배송휴일 목록 조회 시작 - brandCode: {}", searchDto.getBrandCode());
            
            List<Object[]> holidays = deliveryHolidayRepository.findHolidaysWithBrandName(searchDto.getBrandCode());
            
            List<DeliveryHolidayRespDto> responseList = holidays.stream()
                    .map(DeliveryHolidayRespDto::of)
                    .collect(Collectors.toList());
            
            log.info("배송휴일 목록 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("배송휴일 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("배송휴일 목록 조회 중 오류 발생", e);
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
     * 배송휴일 삭제 (하드 삭제)
     * @param deliveryHolidayCode 배송휴일코드
     * @return 삭제 결과
     */
    public RespDto<DeliveryHolidayDeleteRespDto> deleteDeliveryHoliday(Integer deliveryHolidayCode) {
        try {
            log.info("배송휴일 삭제 시작 - deliveryHolidayCode: {}", deliveryHolidayCode);
            
            DeliveryHoliday holiday = deliveryHolidayRepository.findByDeliveryHolidayCode(deliveryHolidayCode);
            if (holiday == null) {
                log.warn("삭제할 배송휴일을 찾을 수 없습니다 - deliveryHolidayCode: {}", deliveryHolidayCode);
                return RespDto.fail("삭제할 배송휴일을 찾을 수 없습니다.");
            }
            
            // 하드 삭제 진행
            deliveryHolidayRepository.delete(holiday);
            
            // 응답 생성
            DeliveryHolidayDeleteRespDto responseDto = DeliveryHolidayDeleteRespDto.builder()
                    .deliveryHolidayCode(deliveryHolidayCode)
                    .message("배송휴일이 성공적으로 삭제되었습니다.")
                    .build();
            
            log.info("배송휴일 삭제 완료 - deliveryHolidayCode: {}, holidayName: {}", 
                    deliveryHolidayCode, holiday.getHolidayName());
            
            return RespDto.success("배송휴일이 성공적으로 삭제되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("배송휴일 삭제 중 오류 발생 - deliveryHolidayCode: {}", deliveryHolidayCode, e);
            return RespDto.fail("배송휴일 삭제 중 오류가 발생했습니다.");
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