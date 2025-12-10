package com.inc.sh.service;

import com.inc.sh.dto.deliveryPrice.reqDto.DeliveryPriceSearchDto;
import com.inc.sh.dto.deliveryPrice.reqDto.DeliveryPriceUpdateReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.deliveryPrice.respDto.DeliveryPriceBatchResult;
import com.inc.sh.dto.deliveryPrice.respDto.DeliveryPriceRespDto;
import com.inc.sh.dto.deliveryPrice.respDto.DeliveryPriceUpdateRespDto;
import com.inc.sh.entity.Item;
import com.inc.sh.repository.ItemRepository;
import com.inc.sh.util.ItemPriceCalculator;

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
public class DeliveryPriceService {
    
    private final ItemRepository itemRepository;
    
    /**
     * 납품단가관리용 품목 조회
     * @param searchDto 검색 조건
     * @return 조회된 품목 목록
     */
    @Transactional(readOnly = true)
    public RespDto<List<DeliveryPriceRespDto>> getItemsForPriceManagement(DeliveryPriceSearchDto searchDto) {
        try {
            log.info("납품단가관리 품목 조회 시작 - itemCode: {}, categoryCode: {}, priceType: {}, hqCode: {}", 
                    searchDto.getItemCode(), searchDto.getCategoryCode(), searchDto.getPriceType(), searchDto.getHqCode());
            
            List<Item> items = itemRepository.findForDeliveryPriceManagementWithHqCode(
                    searchDto.getItemCode(),
                    searchDto.getCategoryCode(),
                    searchDto.getPriceType(),
                    searchDto.getHqCode()
            );
            
            List<DeliveryPriceRespDto> responseList = items.stream()
                    .map(DeliveryPriceRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("납품단가관리 품목 조회 완료 - hqCode: {}, 조회 건수: {}", searchDto.getHqCode(), responseList.size());
            return RespDto.success("납품단가관리 품목 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("납품단가관리 품목 조회 중 오류 발생 - hqCode: {}", searchDto.getHqCode(), e);
            return RespDto.fail("납품단가관리 품목 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 납품단가 다중 수정
     */
    @Transactional
    public RespDto<DeliveryPriceBatchResult> updateDeliveryPrices(DeliveryPriceUpdateReqDto reqDto) {
        
        log.info("납품단가 다중 수정 시작 - 총 {}건", reqDto.getItems().size());
        
        List<DeliveryPriceUpdateRespDto> successData = new ArrayList<>();
        List<DeliveryPriceBatchResult.DeliveryPriceErrorDto> failData = new ArrayList<>();
        
        for (DeliveryPriceUpdateReqDto.DeliveryPriceItemDto item : reqDto.getItems()) {
            try {
                // 개별 납품단가 수정 처리
                DeliveryPriceUpdateRespDto updatedPrice = updateSingleDeliveryPrice(item);
                successData.add(updatedPrice);
                
                log.info("납품단가 수정 성공 - itemCode: {}, basePrice: {}", 
                        updatedPrice.getItemCode(), updatedPrice.getBasePrice());
                
            } catch (Exception e) {
                log.error("납품단가 수정 실패 - itemCode: {}, 에러: {}", item.getItemCode(), e.getMessage());
                
                // 에러 시 품목명 조회 시도
                String itemName = getItemNameSafely(item.getItemCode());
                
                DeliveryPriceBatchResult.DeliveryPriceErrorDto errorDto = DeliveryPriceBatchResult.DeliveryPriceErrorDto.builder()
                        .itemCode(item.getItemCode())
                        .itemName(itemName)
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성
        DeliveryPriceBatchResult result = DeliveryPriceBatchResult.builder()
                .totalCount(reqDto.getItems().size())
                .successCount(successData.size())
                .failCount(failData.size())
                .successData(successData)
                .failData(failData)
                .build();
        
        String message = String.format("납품단가 수정 완료 - 성공: %d건, 실패: %d건", 
                successData.size(), failData.size());
        
        log.info("납품단가 다중 수정 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getItems().size(), successData.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    /**
     * 개별 납품단가 수정 처리 (VAT 계산 포함)
     */
    private DeliveryPriceUpdateRespDto updateSingleDeliveryPrice(DeliveryPriceUpdateReqDto.DeliveryPriceItemDto item) {
        
        // 품목 존재 확인
        Item itemEntity = itemRepository.findById(item.getItemCode())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 품목입니다: " + item.getItemCode()));

        // 품목 종료 여부 확인
        if (itemEntity.getEndDt() != null && !itemEntity.getEndDt().trim().isEmpty()) {
            throw new RuntimeException("종료된 품목은 단가를 수정할 수 없습니다.");
        }
        
        // 기존 단가 정보 저장
        Integer originalBasePrice = itemEntity.getBasePrice();
        Integer newPreviousPrice = itemEntity.getPreviousPrice();
        String processMessage;
        String priceTypeDesc;
        
        if (itemEntity.getPriceType() == 1) {
            // 납품단가: 기본단가만 수정
            itemEntity.setBasePrice(item.getBasePrice());
            priceTypeDesc = "납품단가";
            processMessage = "기본단가만 수정했습니다.";
            
            log.info("납품단가 수정 - itemCode: {}, 기존: {}, 신규: {}", 
                    item.getItemCode(), originalBasePrice, item.getBasePrice());
            
        } else if (itemEntity.getPriceType() == 2) {
            // 납품싯가: 현재 기본단가를 직전단가로, 입력값을 기본단가로
            newPreviousPrice = originalBasePrice;
            itemEntity.setPreviousPrice(newPreviousPrice);
            itemEntity.setBasePrice(item.getBasePrice());
            priceTypeDesc = "납품싯가";
            processMessage = "기존 기본단가를 직전단가로 이동하고 새로운 기본단가를 설정했습니다.";
            
            log.info("납품싯가 수정 - itemCode: {}, 기존 기본단가->직전단가: {}, 새 기본단가: {}", 
                    item.getItemCode(), originalBasePrice, item.getBasePrice());
            
        } else {
            log.warn("유효하지 않은 단가유형입니다 - itemCode: {}, priceType: {}", 
                    item.getItemCode(), itemEntity.getPriceType());
            throw new RuntimeException("유효하지 않은 단가유형입니다.");
        }

        // VAT 금액 재계산 및 설정
        calculateAndSetPrices(itemEntity);

        // 품목 저장
        itemRepository.save(itemEntity);
        
        // 응답 데이터 생성
        return DeliveryPriceUpdateRespDto.builder()
                .itemCode(itemEntity.getItemCode())
                .previousPrice(newPreviousPrice)
                .basePrice(itemEntity.getBasePrice())
                .priceTypeDesc(priceTypeDesc)
                .message(processMessage)
                .build();
    }
    
    /**
     * 품목 VAT 금액 계산 및 설정
     */
    private void calculateAndSetPrices(Item item) {
        int[] calculatedPrices = ItemPriceCalculator.calculateItemPrices(
                item.getBasePrice(),
                item.getVatType(),
                item.getVatDetail()
        );

        item.setSupplyPrice(calculatedPrices[0]);       // 공급가액
        item.setTaxAmount(calculatedPrices[1]);         // 부가세액
        item.setTaxableAmount(calculatedPrices[2]);     // 과세액
        item.setDutyFreeAmount(calculatedPrices[3]);    // 면세액
        item.setTotalAmount(calculatedPrices[4]);       // 총액

        log.debug("품목 VAT 재계산 완료 - itemCode: {}, basePrice: {}, supplyPrice: {}, taxAmount: {}, totalAmount: {}",
                item.getItemCode(), item.getBasePrice(), item.getSupplyPrice(), item.getTaxAmount(), item.getTotalAmount());
    }
    /**
     * 품목명 안전 조회 (에러 발생시 사용)
     */
    private String getItemNameSafely(Integer itemCode) {
        try {
            return itemRepository.findById(itemCode)
                    .map(Item::getItemName)
                    .orElse("알 수 없음");
        } catch (Exception e) {
            return "조회 실패";
        }
    }
}