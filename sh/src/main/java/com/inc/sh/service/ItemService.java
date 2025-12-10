package com.inc.sh.service;

import com.inc.sh.dto.item.reqDto.ItemDeleteReqDto;
import com.inc.sh.dto.item.reqDto.ItemReqDto;
import com.inc.sh.dto.item.reqDto.ItemSaveReqDto;
import com.inc.sh.dto.item.reqDto.ItemSearchDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.item.respDto.ItemRespDto;
import com.inc.sh.dto.item.respDto.ItemSaveRespDto;
import com.inc.sh.dto.item.respDto.ItemBatchResult;
import com.inc.sh.dto.item.respDto.ItemDeleteRespDto;
import com.inc.sh.entity.Item;
import com.inc.sh.entity.ItemCategory;
import com.inc.sh.repository.ItemCategoryRepository;
import com.inc.sh.repository.ItemRepository;
import com.inc.sh.util.ItemPriceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ItemService {
    
    private final ItemRepository itemRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    
    /**
     * 품목 조회 (검색 조건)
     * @param searchDto 검색 조건
     * @return 조회된 품목 목록
     */
    @Transactional(readOnly = true)
    public RespDto<List<ItemRespDto>> getItemList(ItemSearchDto searchDto) {
        try {
            log.info("품목 목록 조회 시작 - itemCode: {}, itemName: {}, categoryCode: {}, priceType: {}, endDtYn: {}, orderAvailableYn: {}", 
                    searchDto.getItemCode(), searchDto.getItemName(), searchDto.getCategoryCode(), 
                    searchDto.getPriceType(), searchDto.getEndDtYn(), searchDto.getOrderAvailableYn());
            
            List<Item> items = itemRepository.findBySearchConditions(
                    searchDto.getItemCode(),
                    searchDto.getItemName(),
                    searchDto.getCategoryCode(),
                    searchDto.getPriceType(),
                    searchDto.getEndDtYn(),
                    searchDto.getOrderAvailableYn()
            );
            
            List<ItemRespDto> responseList = items.stream()
                    .map(ItemRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("품목 목록 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("품목 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("품목 목록 조회 중 오류 발생", e);
            return RespDto.fail("품목 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 품목 상세 조회
     * @param itemCode 품목코드
     * @return 품목 상세 정보
     */
    @Transactional(readOnly = true)
    public RespDto<ItemRespDto> getItem(Integer itemCode) {
        try {
            log.info("품목 상세 조회 시작 - itemCode: {}", itemCode);
            
            Item item = itemRepository.findByItemCode(itemCode);
            if (item == null) {
                log.warn("품목을 찾을 수 없습니다 - itemCode: {}", itemCode);
                return RespDto.fail("품목을 찾을 수 없습니다.");
            }
            
            ItemRespDto responseDto = ItemRespDto.fromEntity(item);
            
            log.info("품목 상세 조회 완료 - itemCode: {}, itemName: {}", 
                    itemCode, item.getItemName());
            return RespDto.success("품목 조회 성공", responseDto);
            
        } catch (Exception e) {
            log.error("품목 상세 조회 중 오류 발생 - itemCode: {}", itemCode, e);
            return RespDto.fail("품목 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 품목 목록 조회 (분류명 포함)
     */
    public RespDto<List<ItemRespDto>> getItemList(String item, String categoryCode, String priceType, String endDtYn, String orderAvailableYn, Integer hqCode) {
        try {
            log.info("품목 목록 조회 시작 - item: {}, categoryCode: {}, priceType: {}, endDtYn: {}, orderAvailableYn: {}, hqCode: {}", 
                    item, categoryCode, priceType, endDtYn, orderAvailableYn, hqCode);
            
            List<Item> items = itemRepository.findBySearchConditionsUpdatedWithHqCode(
                    item, categoryCode, priceType, endDtYn, orderAvailableYn, hqCode);
            
            List<ItemRespDto> responseList = new ArrayList<>();
            
            for (Item itemEntity : items) {
                // 분류명 조회
                String categoryName = getCategoryNameSafely(itemEntity.getCategoryCode());
                
                // DTO 생성 (분류명 포함)
                ItemRespDto itemDto = ItemRespDto.fromEntityWithCategoryName(itemEntity, categoryName);
                responseList.add(itemDto);
            }
            
            log.info("품목 목록 조회 완료 - hqCode: {}, 조회 건수: {}", hqCode, responseList.size());
            return RespDto.success("품목 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("품목 목록 조회 중 오류 발생 - hqCode: {}", hqCode, e);
            return RespDto.fail("품목 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 품목 다중 저장 (신규/수정)
     */
    @Transactional
    public RespDto<ItemBatchResult> saveItems(ItemSaveReqDto reqDto) {
        
        log.info("품목 다중 저장 시작 - 총 {}건", reqDto.getItems().size());
        
        List<ItemRespDto> successData = new ArrayList<>();
        List<ItemBatchResult.ItemErrorDto> failData = new ArrayList<>();
        
        for (ItemSaveReqDto.ItemItemDto item : reqDto.getItems()) {
            try {
                // 개별 품목 저장 처리
                ItemRespDto savedItem = saveSingleItem(item);
                successData.add(savedItem);
                
                log.info("품목 저장 성공 - itemCode: {}, itemName: {}", 
                        savedItem.getItemCode(), savedItem.getItemName());
                
            } catch (Exception e) {
                log.error("품목 저장 실패 - itemName: {}, 에러: {}", item.getItemName(), e.getMessage());
                
                ItemBatchResult.ItemErrorDto errorDto = ItemBatchResult.ItemErrorDto.builder()
                        .itemCode(item.getItemCode())
                        .itemName(item.getItemName())
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성
        ItemBatchResult result = ItemBatchResult.builder()
                .totalCount(reqDto.getItems().size())
                .successCount(successData.size())
                .failCount(failData.size())
                .successData(successData)
                .failData(failData)
                .build();
        
        String message = String.format("품목 저장 완료 - 성공: %d건, 실패: %d건", 
                successData.size(), failData.size());
        
        log.info("품목 다중 저장 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getItems().size(), successData.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 품목 저장 처리 (분류명 포함)
     */
    private ItemRespDto saveSingleItem(ItemSaveReqDto.ItemItemDto item) {
        
        Item itemEntity;
        
        if (item.getItemCode() == null) {
            // 신규 등록
            itemEntity = Item.builder()
                    .hqCode(item.getHqCode())
                    .categoryCode(item.getCategoryCode())
                    .itemName(item.getItemName())
                    .specification(item.getSpecification())
                    .purchaseUnit(item.getPurchaseUnit())
                    .vatType(item.getVatType())
                    .vatDetail(item.getVatDetail())
                    .regDt(item.getRegDt())
                    .endDt(item.getEndDt())
                    .origin(item.getOrigin())
                    .priceType(item.getPriceType())
                    .basePrice(item.getBasePrice())
                    .previousPrice(item.getPreviousPrice())
                    .hqMemo(item.getHqMemo())
                    .orderAvailableYn(item.getOrderAvailableYn())
                    .minOrderQty(item.getMinOrderQty())
                    .maxOrderQty(item.getMaxOrderQty())
                    .deadlineDay(item.getDeadlineDay())
                    .deadlineTime(item.getDeadlineTime())
                    .build();
            
            // 금액 계산 및 설정
            calculateAndSetPrices(itemEntity);
            
            itemEntity = itemRepository.save(itemEntity);
            
        } else {
            // 수정
            itemEntity = itemRepository.findById(item.getItemCode())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 품목입니다: " + item.getItemCode()));
            
            // 모든 필드 수정
            itemEntity.setCategoryCode(item.getCategoryCode());
            itemEntity.setItemName(item.getItemName());
            itemEntity.setSpecification(item.getSpecification());
            itemEntity.setPurchaseUnit(item.getPurchaseUnit());
            itemEntity.setVatType(item.getVatType());
            itemEntity.setVatDetail(item.getVatDetail());
            itemEntity.setRegDt(item.getRegDt());
            itemEntity.setEndDt(item.getEndDt());
            itemEntity.setOrigin(item.getOrigin());
            itemEntity.setPriceType(item.getPriceType());
            itemEntity.setBasePrice(item.getBasePrice());
            itemEntity.setPreviousPrice(item.getPreviousPrice());
            itemEntity.setHqMemo(item.getHqMemo());
            itemEntity.setOrderAvailableYn(item.getOrderAvailableYn());
            itemEntity.setMinOrderQty(item.getMinOrderQty());
            itemEntity.setMaxOrderQty(item.getMaxOrderQty());
            itemEntity.setDeadlineDay(item.getDeadlineDay());
            itemEntity.setDeadlineTime(item.getDeadlineTime());
            
            // 금액 계산 및 설정
            calculateAndSetPrices(itemEntity);
            
            itemEntity = itemRepository.save(itemEntity);
        }
        
        // 분류명 조회
        String categoryName = getCategoryNameSafely(itemEntity.getCategoryCode());
        
        // DTO 생성 (분류명 포함)
        return ItemRespDto.fromEntityWithCategoryName(itemEntity, categoryName);
    }

    /**
     * 분류명 안전 조회 (대분류명-중분류명 조합)
     */
    private String getCategoryNameSafely(Integer categoryCode) {
        try {
            if (categoryCode == null) {
                return "분류없음";
            }
            
            // ItemCategoryRepository에서 분류 정보 조회
            Optional<ItemCategory> categoryOpt = itemCategoryRepository.findById(categoryCode);
            if (!categoryOpt.isPresent()) {
                return "분류없음";
            }
            
            ItemCategory category = categoryOpt.get();
            
            // 부모 분류가 있는지 확인 (중분류인 경우)
            if (category.getParentsCategoryCode() != null && category.getParentsCategoryCode() > 0) {
                Optional<ItemCategory> parentOpt = itemCategoryRepository.findById(category.getParentsCategoryCode());
                if (parentOpt.isPresent()) {
                    // 대분류명-중분류명
                    return parentOpt.get().getCategoryName() + "-" + category.getCategoryName();
                } else {
                    // 부모를 찾을 수 없는 경우 중분류명만
                    return category.getCategoryName();
                }
            } else {
                // 대분류인 경우 대분류명만
                return category.getCategoryName();
            }
            
        } catch (Exception e) {
            log.warn("분류명 조회 중 오류 발생 - categoryCode: {}, error: {}", categoryCode, e.getMessage());
            return "분류조회실패";
        }
    }
    
    /**
     * 품목 다중 삭제 (Soft Delete - endDt 설정)
     */
    @Transactional
    public RespDto<ItemBatchResult> deleteItems(ItemDeleteReqDto reqDto) {
        
        log.info("품목 다중 삭제 시작 - 총 {}건", reqDto.getItemCodes().size());
        
        List<Integer> successCodes = new ArrayList<>();
        List<ItemBatchResult.ItemErrorDto> failData = new ArrayList<>();
        
        for (Integer itemCode : reqDto.getItemCodes()) {
            try {
                // 개별 품목 삭제 처리
                deleteSingleItem(itemCode);
                successCodes.add(itemCode);
                
                log.info("품목 삭제 성공 - itemCode: {}", itemCode);
                
            } catch (Exception e) {
                log.error("품목 삭제 실패 - itemCode: {}, 에러: {}", itemCode, e.getMessage());
                
                // 에러 시 품목명 조회 시도
                String itemName = getItemNameSafely(itemCode);
                
                ItemBatchResult.ItemErrorDto errorDto = ItemBatchResult.ItemErrorDto.builder()
                        .itemCode(itemCode)
                        .itemName(itemName)
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성 (삭제는 successData 대신 성공 코드만)
        ItemBatchResult result = ItemBatchResult.builder()
                .totalCount(reqDto.getItemCodes().size())
                .successCount(successCodes.size())
                .failCount(failData.size())
                .successData(successCodes.stream()
                        .map(code -> ItemRespDto.builder().itemCode(code).build())
                        .collect(Collectors.toList()))
                .failData(failData)
                .build();
        
        String message = String.format("품목 삭제 완료 - 성공: %d건, 실패: %d건", 
                successCodes.size(), failData.size());
        
        log.info("품목 다중 삭제 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getItemCodes().size(), successCodes.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 품목 삭제 처리 (Soft Delete - endDt 설정)
     */
    private void deleteSingleItem(Integer itemCode) {
        
        // 품목 존재 확인
        Item item = itemRepository.findById(itemCode)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 품목입니다: " + itemCode));

        // 이미 종료된 품목인지 확인
        if (item.getEndDt() != null && !item.getEndDt().trim().isEmpty()) {
            throw new RuntimeException("이미 종료된 품목입니다.");
        }

        // Soft Delete - 종료일자를 현재일로 설정
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        item.setEndDt(currentDate);
        
        itemRepository.save(item);
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
    
    /**
     * 품목 금액 계산 및 설정
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

        log.debug("품목 금액 계산 완료 - itemCode: {}, basePrice: {}, supplyPrice: {}, taxAmount: {}, totalAmount: {}",
                item.getItemCode(), item.getBasePrice(), item.getSupplyPrice(), item.getTaxAmount(), item.getTotalAmount());
    }
}