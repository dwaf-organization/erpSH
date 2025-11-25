package com.inc.sh.dto.returnRegistration.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppReturnHistoryListRespDto {
    
    private Long totalCount;                              // 총 반품 갯수
    private Integer currentPage;                          // 현재 페이지
    private Integer totalPages;                           // 총 페이지 수
    private Boolean hasNext;                              // 다음 페이지 여부
    private List<AppReturnHistoryRespDto> returnList;     // 반품 목록
}