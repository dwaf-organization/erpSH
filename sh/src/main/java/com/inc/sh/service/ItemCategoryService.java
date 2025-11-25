package com.inc.sh.service;

import com.inc.sh.dto.itemCategory.reqDto.ItemCategoryReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.itemCategory.respDto.ItemCategoryTreeRespDto;
import com.inc.sh.dto.itemCategory.respDto.ItemCategoryTableRespDto;
import com.inc.sh.dto.itemCategory.respDto.ItemCategorySaveRespDto;
import com.inc.sh.dto.itemCategory.respDto.ItemCategoryDeleteRespDto;
import com.inc.sh.entity.ItemCategory;
import com.inc.sh.repository.ItemCategoryRepository;
import com.inc.sh.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ItemCategoryService {
    
    private final ItemCategoryRepository itemCategoryRepository;
    private final ItemRepository itemRepository;
    
    /**
     * 트리용 전체 품목분류 조회 (계층구조)
     * @return 트리 구조 품목분류 목록
     */
    @Transactional(readOnly = true)
    public RespDto<List<ItemCategoryTreeRespDto>> getCategoryTreeList() {
        try {
            log.info("트리용 품목분류 전체 조회 시작");
            
            List<ItemCategory> categories = itemCategoryRepository.findAllOrderByHierarchy();
            List<ItemCategoryTreeRespDto> treeList = buildCategoryTree(categories);
            
            log.info("트리용 품목분류 전체 조회 완료 - 대분류 수: {}", treeList.size());
            return RespDto.success("트리용 품목분류 조회 성공", treeList);
            
        } catch (Exception e) {
            log.error("트리용 품목분류 전체 조회 중 오류 발생", e);
            return RespDto.fail("트리용 품목분류 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 표용 전체 품목분류 조회 (평면 리스트)
     * @return 표 형태 품목분류 목록
     */
    @Transactional(readOnly = true)
    public RespDto<List<ItemCategoryTableRespDto>> getCategoryTableList() {
        try {
            log.info("표용 품목분류 전체 조회 시작");
            
            List<ItemCategory> categories = itemCategoryRepository.findAllOrderByHierarchy();
            List<ItemCategoryTableRespDto> tableList = categories.stream()
                    .map(ItemCategoryTableRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("표용 품목분류 전체 조회 완료 - 조회 건수: {}", tableList.size());
            return RespDto.success("표용 품목분류 조회 성공", tableList);
            
        } catch (Exception e) {
            log.error("표용 품목분류 전체 조회 중 오류 발생", e);
            return RespDto.fail("표용 품목분류 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 트리 클릭 시 본인 및 하위 항목 조회
     * @param categoryCode 클릭된 분류코드
     * @return 본인 + 하위 분류 목록
     */
    @Transactional(readOnly = true)
    public RespDto<List<ItemCategoryTableRespDto>> getCategoryWithChildren(Integer categoryCode) {
        try {
            log.info("트리 클릭 조회 시작 - categoryCode: {}", categoryCode);
            
            List<ItemCategory> categories = itemCategoryRepository.findByParentAndChildren(categoryCode);
            List<ItemCategoryTableRespDto> tableList = categories.stream()
                    .map(ItemCategoryTableRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("트리 클릭 조회 완료 - categoryCode: {}, 조회 건수: {}", categoryCode, tableList.size());
            return RespDto.success("품목분류 하위 조회 성공", tableList);
            
        } catch (Exception e) {
            log.error("트리 클릭 조회 중 오류 발생 - categoryCode: {}", categoryCode, e);
            return RespDto.fail("품목분류 하위 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 품목분류 저장 (신규/수정)
     * @param request 품목분류 정보
     * @return 저장된 품목분류 코드
     */
    public RespDto<ItemCategorySaveRespDto> saveCategory(ItemCategoryReqDto request) {
        try {
            ItemCategory savedCategory;
            String action;
            
            if (request.getCategoryCode() == null) {
                // 신규 등록
                log.info("품목분류 신규 등록 시작 - categoryName: {}, parentsCategoryCode: {}", 
                        request.getCategoryName(), request.getParentsCategoryCode());
                
                ItemCategory category = request.toEntity();
                savedCategory = itemCategoryRepository.save(category);
                action = "등록";
                
            } else {
                // 수정
                log.info("품목분류 수정 시작 - categoryCode: {}, categoryName: {}", 
                        request.getCategoryCode(), request.getCategoryName());
                
                ItemCategory existingCategory = itemCategoryRepository.findByCategoryCode(request.getCategoryCode());
                if (existingCategory == null) {
                    log.warn("수정할 품목분류를 찾을 수 없습니다 - categoryCode: {}", request.getCategoryCode());
                    return RespDto.fail("수정할 품목분류를 찾을 수 없습니다.");
                }
                
                request.updateEntity(existingCategory);
                savedCategory = itemCategoryRepository.save(existingCategory);
                action = "수정";
            }
            
            // 간소화된 응답 생성
            ItemCategorySaveRespDto responseDto = ItemCategorySaveRespDto.builder()
                    .categoryCode(savedCategory.getCategoryCode())
                    .build();
            
            log.info("품목분류 {} 완료 - categoryCode: {}, categoryName: {}", 
                    action, savedCategory.getCategoryCode(), savedCategory.getCategoryName());
            
            return RespDto.success("품목분류가 성공적으로 " + action + "되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("품목분류 저장 중 오류 발생 - categoryCode: {}", request.getCategoryCode(), e);
            return RespDto.fail("품목분류 저장 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 품목분류 삭제 (하드 삭제)
     * @param categoryCode 분류코드
     * @return 삭제 결과 (품목 연관 확인)
     */
    public RespDto<ItemCategoryDeleteRespDto> deleteCategory(Integer categoryCode) {
        try {
            log.info("품목분류 삭제 시작 - categoryCode: {}", categoryCode);
            
            ItemCategory category = itemCategoryRepository.findByCategoryCode(categoryCode);
            if (category == null) {
                log.warn("삭제할 품목분류를 찾을 수 없습니다 - categoryCode: {}", categoryCode);
                return RespDto.fail("삭제할 품목분류를 찾을 수 없습니다.");
            }
            
            // 품목 테이블 연관 확인
            List<Integer> relatedItemCodes = itemRepository.findItemCodesByCategoryCode(categoryCode);
            if (!relatedItemCodes.isEmpty()) {
                log.warn("연관된 품목이 있어 삭제할 수 없습니다 - categoryCode: {}, relatedItems: {}", 
                        categoryCode, relatedItemCodes);
                
                String itemCodesStr = relatedItemCodes.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                
                ItemCategoryDeleteRespDto responseDto = ItemCategoryDeleteRespDto.builder()
                        .categoryCode(categoryCode)
                        .relatedItemCodes(relatedItemCodes)
                        .message("해당 품목분류가 품목테이블에 존재합니다. " + itemCodesStr)
                        .build();
                
                return RespDto.fail("해당 품목분류가 품목테이블에 존재합니다. " + itemCodesStr);
            }
            
            // 하위 분류 확인
            List<ItemCategory> childCategories = itemCategoryRepository.findByParentsCategoryCodeOrderByCategoryLevel(categoryCode);
            if (!childCategories.isEmpty()) {
                log.warn("하위 분류가 있어 삭제할 수 없습니다 - categoryCode: {}, childCount: {}", 
                        categoryCode, childCategories.size());
                return RespDto.fail("하위 분류가 존재하여 삭제할 수 없습니다.");
            }
            
            // 하드 삭제 진행
            itemCategoryRepository.delete(category);
            
            // 삭제 성공 응답 생성
            ItemCategoryDeleteRespDto responseDto = ItemCategoryDeleteRespDto.builder()
                    .categoryCode(categoryCode)
                    .relatedItemCodes(null)
                    .message("품목분류가 성공적으로 삭제되었습니다.")
                    .build();
            
            log.info("품목분류 삭제 완료 - categoryCode: {}", categoryCode);
            
            return RespDto.success("품목분류가 성공적으로 삭제되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("품목분류 삭제 중 오류 발생 - categoryCode: {}", categoryCode, e);
            return RespDto.fail("품목분류 삭제 중 오류가 발생했습니다.");
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
}