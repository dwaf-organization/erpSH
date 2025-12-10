package com.inc.sh.service;

import java.util.ArrayList;
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
import com.inc.sh.dto.orderLimitSet.reqDto.OrderLimitDeleteReqDto;
import com.inc.sh.dto.orderLimitSet.respDto.OrderLimitRespDto;
import com.inc.sh.entity.Headquarter;
import com.inc.sh.entity.OrderLimitSet;
import com.inc.sh.repository.HeadquarterRepository;
import com.inc.sh.repository.OrderLimitSetRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingService {

	private final HeadquarterRepository headquarterRepository;
	private final OrderLimitSetRepository orderLimitSetRepository;
	
    /**
     * 주문관리 조회
     */
    @Transactional(readOnly = true)
	public RespDto<OrderConfigRespDto> getOrderConfigByHqCode(Integer hqCode) {
        
        Optional<Headquarter> headquarterOptional = headquarterRepository.findById(hqCode);

        if (headquarterOptional.isEmpty()) {
            return RespDto.fail("존재하지않는 본사코드입니다.");
        }
        
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
     */
    @Transactional
    public RespDto<OrderConfigRespDto> updateOrderConfig(OrderConfigUpdateReqDto reqDto) {
        
        Optional<Headquarter> headquarterOptional = headquarterRepository.findById(reqDto.getHqCode());
        
        if (headquarterOptional.isEmpty()) {
            return RespDto.fail("본사코드가 존재하지 않습니다.");
        }
        
        Headquarter headquarter = headquarterOptional.get();
        
        headquarter.setLogisticsType(reqDto.getLogisticsType());
        headquarter.setPriceDisplayType(reqDto.getPriceDisplayType());

        OrderConfigRespDto updatedDto = OrderConfigRespDto.builder()
                .hqCode(headquarter.getHqCode())
                .logisticsType(headquarter.getLogisticsType())
                .priceDisplayType(headquarter.getPriceDisplayType())
                .build();
                
        return RespDto.success("주문 설정 정보 업데이트 성공", updatedDto);
    }
    
    /**
     * 주문 제한 설정 목록 조회 (브랜드코드 + 본사코드)
     */
    @Transactional(readOnly = true)
    public RespDto<List<OrderLimitRespDto>> getOrderLimitListByBrandCodeAndHqCode(Integer brandCode, Integer hqCode) {
        
        List<OrderLimitSet> entityList = orderLimitSetRepository.findByBrandCodeAndHqCode(brandCode, hqCode);
        
        List<OrderLimitRespDto> respDtoList = entityList.stream()
                .map(OrderLimitRespDto::fromEntity)
                .collect(Collectors.toList());
        
        if (respDtoList.isEmpty()) {
            return RespDto.fail("해당 브랜드 코드(" + brandCode + "), 본사 코드(" + hqCode + ")에 설정된 주문 제한 정보가 없습니다.");
        }
        
        return RespDto.success("주문 제한 설정 목록 조회 성공", respDtoList);
    }
    
    /**
     * 주문 제한 설정 목록 조회 (기존 메소드 - 브랜드코드만)
     */
    @Transactional(readOnly = true)
    public RespDto<List<OrderLimitRespDto>> getOrderLimitListByBrandCode(Integer brandCode) {
        
        List<OrderLimitSet> entityList = orderLimitSetRepository.findByBrandCode(brandCode);
        
        List<OrderLimitRespDto> respDtoList = entityList.stream()
                .map(OrderLimitRespDto::fromEntity)
                .collect(Collectors.toList());
        
        if (respDtoList.isEmpty()) {
            return RespDto.fail("해당 브랜드 코드(" + brandCode + ")에 설정된 주문 제한 정보가 없습니다.");
        }
        
        return RespDto.success("주문 제한 설정 목록 조회 성공", respDtoList);
    }
    
    /**
     * 주문 제한 설정 등록 또는 수정 (기존 단일 저장 메서드 - 호환성 유지)
     */
    @Transactional
    public OrderLimitRespDto saveOrUpdateOrderLimit(OrderLimitSaveReqDto.OrderLimitItemDto itemDto, 
                                                     Integer brandCode, Integer hqCode) {
        
        OrderLimitSet savedEntity;

        if (itemDto.getLimitCode() != null) {
            // UPDATE 로직
            OrderLimitSet existingLimit = orderLimitSetRepository.findById(itemDto.getLimitCode())
                    .orElseThrow(() -> new IllegalArgumentException("업데이트할 주문 제한 코드(" + itemDto.getLimitCode() + ")를 찾을 수 없습니다."));

            existingLimit.setBrandCode(brandCode);
            existingLimit.setHqCode(hqCode);
            existingLimit.setDayName(itemDto.getDayName());
            existingLimit.setLimitStartTime(itemDto.getLimitStartTime());
            existingLimit.setLimitEndTime(itemDto.getLimitEndTime());
            
            savedEntity = existingLimit;
            
        } else {
            // CREATE 로직
            OrderLimitSet newLimit = OrderLimitSet.builder()
                    .brandCode(brandCode)
                    .hqCode(hqCode)
                    .dayName(itemDto.getDayName())
                    .limitStartTime(itemDto.getLimitStartTime())
                    .limitEndTime(itemDto.getLimitEndTime())
                    .build();
            
            savedEntity = orderLimitSetRepository.save(newLimit);
        }

        return OrderLimitRespDto.fromEntity(savedEntity);
    }
    
    /**
     * 주문 제한 설정 다중 등록/수정
     */
    @Transactional
    public RespDto<List<OrderLimitRespDto>> saveOrUpdateOrderLimitMultiple(OrderLimitSaveReqDto reqDto) {
        
        try {
            log.info("주문 제한 설정 다중 저장 시작 - brandCode: {}, 처리 건수: {}", 
                    reqDto.getBrandCode(), reqDto.getLimits().size());
            
            List<OrderLimitRespDto> savedItems = new ArrayList<>();
            int createCount = 0;
            int updateCount = 0;
            
            for (OrderLimitSaveReqDto.OrderLimitItemDto itemDto : reqDto.getLimits()) {
                try {
                    OrderLimitRespDto savedItem = saveOrUpdateOrderLimit(itemDto, reqDto.getBrandCode(), reqDto.getHqCode());
                    savedItems.add(savedItem);
                    
                    if (itemDto.getLimitCode() == null) {
                        createCount++;
                    } else {
                        updateCount++;
                    }
                    
                    log.info("주문 제한 설정 처리 완료 - 요일: {}, 타입: {}", 
                            itemDto.getDayName(), itemDto.getLimitCode() == null ? "생성" : "수정");
                    
                } catch (Exception e) {
                    log.error("주문 제한 설정 처리 중 오류 발생 - 요일: {}, 에러: {}", 
                            itemDto.getDayName(), e.getMessage());
                    throw e;
                }
            }
            
            String message = String.format("주문 제한 설정 저장 완료 - 생성: %d건, 수정: %d건", createCount, updateCount);
            
            log.info("주문 제한 설정 다중 저장 완료 - brandCode: {}, 총 처리: {}건", 
                    reqDto.getBrandCode(), savedItems.size());
            
            return RespDto.success(message, savedItems);
            
        } catch (Exception e) {
            log.error("주문 제한 설정 다중 저장 중 오류 발생 - brandCode: {}", reqDto.getBrandCode(), e);
            return RespDto.fail("주문 제한 설정 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 주문 제한 설정 다중 삭제 (새로 추가된 메서드)
     */
    @Transactional
    public RespDto<String> deleteOrderLimitMultiple(OrderLimitDeleteReqDto deleteDto) {
        
        try {
            log.info("주문 제한 설정 다중 삭제 시작 - 삭제 대상: {} 건", deleteDto.getLimitCodes().size());
            
            List<Integer> successDeletes = new ArrayList<>();
            List<String> failedDeletes = new ArrayList<>();
            
            // limitCodes 배열 순회하며 개별 삭제 처리
            for (Integer limitCode : deleteDto.getLimitCodes()) {
                try {
                    // 삭제 대상 존재 여부 확인
                    if (!orderLimitSetRepository.existsById(limitCode)) {
                        failedDeletes.add(limitCode + "(존재하지 않음)");
                        continue;
                    }
                    
                    // 삭제 실행
                    orderLimitSetRepository.deleteById(limitCode);
                    successDeletes.add(limitCode);
                    
                    log.info("주문 제한 설정 삭제 완료 - limitCode: {}", limitCode);
                    
                } catch (Exception e) {
                    log.error("주문 제한 설정 삭제 중 오류 발생 - limitCode: {}, 에러: {}", limitCode, e.getMessage());
                    failedDeletes.add(limitCode + "(삭제 오류)");
                }
            }
            
            // 성공 메시지 생성
            String message = String.format("주문 제한 설정 삭제 완료 - 성공: %d건, 실패: %d건", 
                    successDeletes.size(), failedDeletes.size());
            
            if (!failedDeletes.isEmpty()) {
                message += " | 실패 목록: " + String.join(", ", failedDeletes);
            }
            
            log.info("주문 제한 설정 다중 삭제 완료 - 총 처리: {}/{}건", 
                    successDeletes.size(), deleteDto.getLimitCodes().size());
            
            return RespDto.success(message, "삭제 완료");
            
        } catch (Exception e) {
            log.error("주문 제한 설정 다중 삭제 중 오류 발생", e);
            return RespDto.fail("주문 제한 설정 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 주문 제한 설정 삭제 (기존 단일 삭제 메서드 - 호환성 유지)
     */
    @Transactional
    public RespDto<Void> deleteOrderLimit(Integer limitCode) {
        
        if (!orderLimitSetRepository.existsById(limitCode)) {
            return RespDto.fail("존재하지 않는 주문제한설정코드입니다.");
        }
        
        orderLimitSetRepository.deleteById(limitCode);
        return RespDto.success("주문 제한 설정 삭제 완료", null);
    }
}