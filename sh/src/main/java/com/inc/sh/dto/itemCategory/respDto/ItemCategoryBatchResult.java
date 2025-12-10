package com.inc.sh.dto.itemCategory.respDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCategoryBatchResult {
    
    private int totalCount;                                 // 총 처리 건수
    private int successCount;                               // 성공 건수
    private int failCount;                                  // 실패 건수
    private List<ItemCategoryRespDto> successData;          // 성공한 품목분류 데이터들
    private List<ItemCategoryErrorDto> failData;            // 실패한 품목분류 데이터들
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemCategoryErrorDto {
        private Integer categoryCode;                       // 실패한 품목분류코드
        private String categoryName;                        // 실패한 품목분류명  
        private String errorMessage;                        // 실패 사유
    }
}