package com.inc.sh.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.headquarter.reqDto.OrderConfigUpdateReqDto;
import com.inc.sh.dto.headquarter.respDto.OrderConfigRespDto;
import com.inc.sh.dto.orderLimitSet.reqDto.OrderLimitSaveReqDto;
import com.inc.sh.dto.orderLimitSet.respDto.OrderLimitRespDto;
import com.inc.sh.entity.Headquarter;
import com.inc.sh.entity.OrderLimitSet;
import com.inc.sh.repository.HeadquarterRepository;
import com.inc.sh.repository.OrderLimitSetRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettingService {

	private final HeadquarterRepository headquarterRepository;
	private final OrderLimitSetRepository orderLimitSetRepository;
	
    /**
     * 주문관리 조회
     */
    @Transactional(readOnly = true)
	public RespDto<OrderConfigRespDto> getOrderConfigByHqCode(Integer hqCode) {
        
        // 1. Repository를 통해 Headquarter Entity를 Optional로 조회
        Optional<Headquarter> headquarterOptional = headquarterRepository.findById(hqCode);

        // 2. Optional을 확인하여 데이터 존재 여부에 따라 분기
        if (headquarterOptional.isEmpty()) {
            // 데이터가 없을 경우: RespDto.fail() 반환
            // 요청하신 code: -1, message: "없는 본사코드입니다.", data: null 응답이 생성됩니다.
            return RespDto.fail("존재하지않는 본사코드입니다.");
        }
        
        // 3. 데이터가 있을 경우: 정상적인 DTO 변환 및 RespDto.success() 반환
        Headquarter headquarter = headquarterOptional.get();

        OrderConfigRespDto respDtoData = OrderConfigRespDto.builder()
                .hqCode(headquarter.getHqCode())
                .logisticsType(headquarter.getLogisticsType())
                .priceDisplayType(headquarter.getPriceDisplayType())
                .build();
        
        return RespDto.success("주문 설정 정보 조회 성공", respDtoData);
    }
    
    /**
     * 주문관리 설정 업데이트
     * Headquarter Entity의 logistics_type, price_display_type 필드 업데이트
     */
    @Transactional
    public RespDto<OrderConfigRespDto> updateOrderConfig(OrderConfigUpdateReqDto reqDto) {
        
        // 1. hqCode로 Headquarter Entity를 Optional로 조회
        Optional<Headquarter> headquarterOptional = headquarterRepository.findById(reqDto.getHqCode());
        
        if (headquarterOptional.isEmpty()) {
            return RespDto.fail("본사코드가 존재하지 않습니다.");
        }
        
        Headquarter headquarter = headquarterOptional.get();
        
        // 2. Entity 필드 업데이트
        // Headquarter Entity의 Setter를 사용합니다.
        headquarter.setLogisticsType(reqDto.getLogisticsType());
        headquarter.setPriceDisplayType(reqDto.getPriceDisplayType());

        // 3. 업데이트된 Entity 정보를 DTO로 변환하여 성공 응답에 담아 반환
        OrderConfigRespDto updatedDto = OrderConfigRespDto.builder()
                .hqCode(headquarter.getHqCode())
                .logisticsType(headquarter.getLogisticsType())
                .priceDisplayType(headquarter.getPriceDisplayType())
                .build();
                
        return RespDto.success("주문 설정 정보 업데이트 성공", updatedDto);
    }
    
    /**
     * 주문 제한 설정 목록 조회
     * @param brandCode 브랜드 코드
     * @return 해당 브랜드의 요일별 주문 제한 설정 목록
     */
    @Transactional(readOnly = true)
    public RespDto<List<OrderLimitRespDto>> getOrderLimitListByBrandCode(Integer brandCode) {
        
        // 1. Repository를 통해 OrderLimitSet Entity 목록 조회
        List<OrderLimitSet> entityList = orderLimitSetRepository.findByBrandCode(brandCode);
        
        // 2. Entity 목록을 DTO 목록으로 변환
        List<OrderLimitRespDto> respDtoList = entityList.stream()
                .map(OrderLimitRespDto::fromEntity) // DTO의 fromEntity 메서드 사용
                .collect(Collectors.toList());
        
        // 3. 응답 처리 (데이터가 없을 경우 빈 리스트 반환)
        if (respDtoList.isEmpty()) {
            return RespDto.fail("해당 브랜드 코드(" + brandCode + ")에 설정된 주문 제한 정보가 없습니다.");
        }
        
        return RespDto.success("주문 제한 설정 목록 조회 성공", respDtoList);
    }
    
    /**
     * 주문 제한 설정 등록 또는 수정 (limitCode 값으로 분기)
     */
    @Transactional
    public OrderLimitRespDto saveOrUpdateOrderLimit(OrderLimitSaveReqDto reqDto) {
        
        OrderLimitSet savedEntity;

        // 🚨 핵심 분기 로직 🚨
        if (reqDto.getLimitCode() != null) {
            // 1. UPDATE 로직: limitCode가 있을 경우
            
            // 1-1. 기존 Entity 조회 (없으면 예외 발생)
            OrderLimitSet existingLimit = orderLimitSetRepository.findById(reqDto.getLimitCode())
                    .orElseThrow(() -> new IllegalArgumentException("업데이트할 주문 제한 코드(" + reqDto.getLimitCode() + ")를 찾을 수 없습니다."));

            // 1-2. 필드 업데이트
            existingLimit.setBrandCode(reqDto.getBrandCode());
            existingLimit.setHqCode(reqDto.getHqCode());
            existingLimit.setDayName(reqDto.getDayName());
            existingLimit.setLimitStartTime(reqDto.getLimitStartTime());
            existingLimit.setLimitEndTime(reqDto.getLimitEndTime());
            
            savedEntity = existingLimit;
            
        } else {
            // 2. CREATE 로직: limitCode가 null일 경우
            
            // 2-1. DTO를 Entity로 변환
            OrderLimitSet newLimit = OrderLimitSet.builder()
                    .brandCode(reqDto.getBrandCode())
                    .hqCode(reqDto.getHqCode())
                    .dayName(reqDto.getDayName())
                    .limitStartTime(reqDto.getLimitStartTime())
                    .limitEndTime(reqDto.getLimitEndTime())
                    .build();
            
            // 2-2. 저장
            savedEntity = orderLimitSetRepository.save(newLimit);
        }

        // 3. 저장/업데이트된 Entity를 응답 DTO로 변환
        return OrderLimitRespDto.fromEntity(savedEntity);
    }
    
    /**
     * 주문 제한 설정 삭제
     */
    @Transactional
    public RespDto<Void> deleteOrderLimit(Integer limitCode) {
        
        // 1. 삭제 대상 존재 여부 확인
        if (!orderLimitSetRepository.existsById(limitCode)) {
            // 데이터가 존재하지 않을 경우, RespDto.fail() 반환
            return RespDto.fail("존재하지 않는 주문제한설정코드입니다.");
        }
        
        // 2. 삭제 실행
        orderLimitSetRepository.deleteById(limitCode);
        
        // 3. 삭제 성공 시 RespDto.success() 반환
        return RespDto.success("주문 제한 설정 삭제 완료", null);
    }
}
