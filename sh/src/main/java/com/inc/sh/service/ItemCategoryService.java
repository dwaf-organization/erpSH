package com.inc.sh.service;

import com.inc.sh.dto.itemCategory.reqDto.ItemCategoryDeleteReqDto;
import com.inc.sh.dto.itemCategory.reqDto.ItemCategoryReqDto;
import com.inc.sh.dto.itemCategory.reqDto.ItemCategorySaveReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.itemCategory.respDto.ItemCategoryTreeRespDto;
import com.inc.sh.dto.itemCategory.respDto.ItemCategoryTableRespDto;
import com.inc.sh.dto.itemCategory.respDto.ItemCategoryBatchResult;
import com.inc.sh.dto.itemCategory.respDto.ItemCategoryRespDto;
import com.inc.sh.entity.ItemCategory;
import com.inc.sh.repository.ItemCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ItemCategoryService {
    
    private final ItemCategoryRepository itemCategoryRepository;
    
    /**
     * 트리용 전체 품목분류 조회 (계층구조)
     * @param hqCode 본사코드
     * @return 트리 구조 품목분류 목록
     */
    @Transactional(readOnly = true)
    public RespDto<List<ItemCategoryTreeRespDto>> getCategoryTreeList(Integer hqCode) {
        try {
            log.info("트리용 품목분류 전체 조회 시작 - hqCode: {}", hqCode);
            
            List<ItemCategory> categories = itemCategoryRepository.findByHqCodeOrderByHierarchy(hqCode);
            List<ItemCategoryTreeRespDto> treeList = buildCategoryTree(categories);
            
            log.info("트리용 품목분류 전체 조회 완료 - hqCode: {}, 대분류 수: {}", hqCode, treeList.size());
            return RespDto.success("트리용 품목분류 조회 성공", treeList);
            
        } catch (Exception e) {
            log.error("트리용 품목분류 전체 조회 중 오류 발생 - hqCode: {}", hqCode, e);
            return RespDto.fail("트리용 품목분류 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 표용 전체 품목분류 조회 (평면 리스트)
     * @param hqCode 본사코드
     * @return 표 형태 품목분류 목록
     */
    @Transactional(readOnly = true)
    public RespDto<List<ItemCategoryTableRespDto>> getCategoryTableList(Integer hqCode) {
        try {
            log.info("표용 품목분류 전체 조회 시작 - hqCode: {}", hqCode);
            
            List<ItemCategory> categories = itemCategoryRepository.findByHqCodeOrderByHierarchy(hqCode);
            List<ItemCategoryTableRespDto> tableList = categories.stream()
                    .map(ItemCategoryTableRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("표용 품목분류 전체 조회 완료 - hqCode: {}, 조회 건수: {}", hqCode, tableList.size());
            return RespDto.success("표용 품목분류 조회 성공", tableList);
            
        } catch (Exception e) {
            log.error("표용 품목분류 전체 조회 중 오류 발생 - hqCode: {}", hqCode, e);
            return RespDto.fail("표용 품목분류 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 트리 클릭 시 본인 및 하위 항목 조회
     * @param categoryCode 클릭된 분류코드
     * @param hqCode 본사코드
     * @return 본인 + 하위 분류 목록
     */
    @Transactional(readOnly = true)
    public RespDto<List<ItemCategoryTableRespDto>> getCategoryWithChildren(Integer categoryCode) {
        try {
            
            List<ItemCategory> categories = itemCategoryRepository.findByParentAndChildrenWithHqCode(categoryCode);
            List<ItemCategoryTableRespDto> tableList = categories.stream()
                    .map(ItemCategoryTableRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            return RespDto.success("품목분류 하위 조회 성공", tableList);
            
        } catch (Exception e) {
            log.error("트리 클릭 조회 중 오류 발생 - categoryCode: {}", categoryCode, e);
            return RespDto.fail("품목분류 하위 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 품목분류 리스트를 트리 구조로 변환
     * @param categories 평면 리스트
     * @return 트리 구조 리스트 (대분류만 반환, 하위는 children에 포함)
     */
    private List<ItemCategoryTreeRespDto> buildCategoryTree(List<ItemCategory> categories) {
        // DTO 변환
        List<ItemCategoryTreeRespDto> allCategories = categories.stream()
                .map(ItemCategoryTreeRespDto::fromEntity)
                .collect(Collectors.toList());
        
        // parentsCategoryCode로 그룹화
        Map<Integer, List<ItemCategoryTreeRespDto>> categoryMap = allCategories.stream()
                .collect(Collectors.groupingBy(ItemCategoryTreeRespDto::getParentsCategoryCode));
        
        // 대분류 추출 (parentsCategoryCode = 0)
        List<ItemCategoryTreeRespDto> rootCategories = categoryMap.getOrDefault(0, List.of());
        
        // 각 대분류에 하위 분류 설정
        for (ItemCategoryTreeRespDto rootCategory : rootCategories) {
            setChildren(rootCategory, categoryMap);
        }
        
        return rootCategories;
    }
    
    /**
     * 재귀적으로 하위 분류 설정
     */
    private void setChildren(ItemCategoryTreeRespDto category, Map<Integer, List<ItemCategoryTreeRespDto>> categoryMap) {
        List<ItemCategoryTreeRespDto> children = categoryMap.getOrDefault(category.getCategoryCode(), List.of());
        category.setChildren(children);
        
        // 하위 분류에 대해서도 재귀 호출
        for (ItemCategoryTreeRespDto child : children) {
            setChildren(child, categoryMap);
        }
    }
    
    
    /**
     * 품목분류 다중 저장 (신규/수정)
     */
    @Transactional
    public RespDto<ItemCategoryBatchResult> saveItemCategories(ItemCategorySaveReqDto reqDto) {
        
        log.info("품목분류 다중 저장 시작 - 총 {}건", reqDto.getItemCategories().size());
        
        List<ItemCategoryRespDto> successData = new ArrayList<>();
        List<ItemCategoryBatchResult.ItemCategoryErrorDto> failData = new ArrayList<>();
        
        for (ItemCategorySaveReqDto.ItemCategoryItemDto item : reqDto.getItemCategories()) {
            try {
                // 개별 품목분류 저장 처리
                ItemCategoryRespDto savedItemCategory = saveSingleItemCategory(item);
                successData.add(savedItemCategory);
                
                log.info("품목분류 저장 성공 - categoryCode: {}, categoryName: {}", 
                        savedItemCategory.getCategoryCode(), savedItemCategory.getCategoryName());
                
            } catch (Exception e) {
                log.error("품목분류 저장 실패 - categoryName: {}, 에러: {}", item.getCategoryName(), e.getMessage());
                
                ItemCategoryBatchResult.ItemCategoryErrorDto errorDto = ItemCategoryBatchResult.ItemCategoryErrorDto.builder()
                        .categoryCode(item.getCategoryCode())
                        .categoryName(item.getCategoryName())
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성
        ItemCategoryBatchResult result = ItemCategoryBatchResult.builder()
                .totalCount(reqDto.getItemCategories().size())
                .successCount(successData.size())
                .failCount(failData.size())
                .successData(successData)
                .failData(failData)
                .build();
        
        String message = String.format("품목분류 저장 완료 - 성공: %d건, 실패: %d건", 
                successData.size(), failData.size());
        
        log.info("품목분류 다중 저장 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getItemCategories().size(), successData.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 품목분류 저장 처리
     */
    private ItemCategoryRespDto saveSingleItemCategory(ItemCategorySaveReqDto.ItemCategoryItemDto item) {
        
        ItemCategory itemCategory;
        
        if (item.getCategoryCode() == null) {
            // 신규 등록
            itemCategory = ItemCategory.builder()
                    .hqCode(item.getHqCode())
                    .parentsCategoryCode(item.getParentsCategoryCode())
                    .categoryName(item.getCategoryName())
                    .categoryLevel(item.getCategoryLevel())
                    .description(item.getDescription())
                    .build();
            
            itemCategory = itemCategoryRepository.save(itemCategory);
            
        } else {
            // 수정
            itemCategory = itemCategoryRepository.findById(item.getCategoryCode())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 품목분류입니다: " + item.getCategoryCode()));
            
            // 모든 필드 수정
            itemCategory.setParentsCategoryCode(item.getParentsCategoryCode());
            itemCategory.setCategoryName(item.getCategoryName());
            itemCategory.setCategoryLevel(item.getCategoryLevel());
            itemCategory.setDescription(item.getDescription());
            
            itemCategory = itemCategoryRepository.save(itemCategory);
        }
        
        return ItemCategoryRespDto.fromEntity(itemCategory);
    }

    /**
     * 품목분류 다중 삭제 (Hard Delete + 하위분류 체크)
     */
    @Transactional
    public RespDto<ItemCategoryBatchResult> deleteItemCategories(ItemCategoryDeleteReqDto reqDto) {
        
        log.info("품목분류 다중 삭제 시작 - 총 {}건", reqDto.getCategoryCodes().size());
        
        List<Integer> successCodes = new ArrayList<>();
        List<ItemCategoryBatchResult.ItemCategoryErrorDto> failData = new ArrayList<>();
        
        for (Integer categoryCode : reqDto.getCategoryCodes()) {
            try {
                // 개별 품목분류 삭제 처리
                deleteSingleItemCategory(categoryCode);
                successCodes.add(categoryCode);
                
                log.info("품목분류 삭제 성공 - categoryCode: {}", categoryCode);
                
            } catch (Exception e) {
                log.error("품목분류 삭제 실패 - categoryCode: {}, 에러: {}", categoryCode, e.getMessage());
                
                // 에러 시 품목분류명 조회 시도
                String categoryName = getCategoryNameSafely(categoryCode);
                
                ItemCategoryBatchResult.ItemCategoryErrorDto errorDto = ItemCategoryBatchResult.ItemCategoryErrorDto.builder()
                        .categoryCode(categoryCode)
                        .categoryName(categoryName)
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성 (삭제는 successData 대신 성공 코드만)
        ItemCategoryBatchResult result = ItemCategoryBatchResult.builder()
                .totalCount(reqDto.getCategoryCodes().size())
                .successCount(successCodes.size())
                .failCount(failData.size())
                .successData(successCodes.stream()
                        .map(code -> ItemCategoryRespDto.builder().categoryCode(code).build())
                        .collect(Collectors.toList()))
                .failData(failData)
                .build();
        
        String message = String.format("품목분류 삭제 완료 - 성공: %d건, 실패: %d건", 
                successCodes.size(), failData.size());
        
        log.info("품목분류 다중 삭제 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getCategoryCodes().size(), successCodes.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 품목분류 삭제 처리 (품목 체크 제거 버전)
     */
    private void deleteSingleItemCategory(Integer categoryCode) {
        
        // 품목분류 존재 확인
        ItemCategory itemCategory = itemCategoryRepository.findById(categoryCode)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 품목분류입니다: " + categoryCode));

        // 하위분류 존재 여부 확인
        Long childCount = itemCategoryRepository.countByParentsCategoryCode(categoryCode);
        if (childCount > 0) {
            throw new RuntimeException("하위분류가 " + childCount + "개 존재하여 삭제할 수 없습니다. 하위분류를 먼저 삭제해주세요.");
        }

        // 품목 체크는 일단 제거 (필요시 나중에 추가)
        /*
        Long itemCount = itemCategoryRepository.countItemsByCategoryCode(categoryCode);
        if (itemCount > 0) {
            throw new RuntimeException("해당 분류를 사용하는 품목이 " + itemCount + "개 존재하여 삭제할 수 없습니다.");
        }
        */

        // Hard Delete - 실제 레코드 삭제
        itemCategoryRepository.delete(itemCategory);
    }
    
    /**
     * 품목분류명 안전 조회 (에러 발생시 사용)
     */
    private String getCategoryNameSafely(Integer categoryCode) {
        try {
            return itemCategoryRepository.findById(categoryCode)
                    .map(ItemCategory::getCategoryName)
                    .orElse("알 수 없음");
        } catch (Exception e) {
            return "조회 실패";
        }
    }
}