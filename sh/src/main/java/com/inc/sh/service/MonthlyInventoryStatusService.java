package com.inc.sh.service;

import com.inc.sh.dto.monthlyInventoryStatus.reqDto.MonthlyInventoryStatusSearchDto;
import com.inc.sh.dto.monthlyInventoryStatus.respDto.MonthlyInventoryStatusRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.repository.MonthlyInventoryClosingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlyInventoryStatusService {
    
    private final MonthlyInventoryClosingRepository monthlyInventoryClosingRepository;
    
    /**
     * 월재고현황 조회
     * 마감년월 + 창고코드 + 분류코드 + 품목검색 조건으로 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<MonthlyInventoryStatusRespDto>> getMonthlyInventoryStatusList(MonthlyInventoryStatusSearchDto searchDto) {
        try {
            log.info("월재고현황 조회 시작 - hqCode: {}, 마감년월: {}, 창고코드: {}, 분류코드: {}, 품목검색: {}", 
                    searchDto.getHqCode(), searchDto.getClosingYm(), searchDto.getWarehouseCode(), 
                    searchDto.getCategoryCode(), searchDto.getItemSearch());
            
            List<Object[]> results = monthlyInventoryClosingRepository.findMonthlyInventoryStatusByConditionsWithHqCode(
                    searchDto.getClosingYm(),
                    searchDto.getWarehouseCode(),
                    searchDto.getCategoryCode(),
                    searchDto.getItemSearch(),
                    searchDto.getHqCode()
            );
            
            List<MonthlyInventoryStatusRespDto> responseList = results.stream()
                    .map(result -> MonthlyInventoryStatusRespDto.builder()
                            .warehouseCode((Integer) result[0])
                            .categoryName((String) result[1])
                            .itemCode((Integer) result[2])
                            .itemName((String) result[3])
                            .specification((String) result[4])
                            .unit((String) result[5])
                            .openingQuantity(((Number) result[6]).intValue())
                            .openingAmount(((Number) result[7]).intValue())
                            .inQuantity(((Number) result[8]).intValue())
                            .inAmount(((Number) result[9]).intValue())
                            .outQuantity(((Number) result[10]).intValue())
                            .outAmount(((Number) result[11]).intValue())
                            .calQuantity(((Number) result[12]).intValue())
                            .actualQuantity(((Number) result[13]).intValue())
                            .actualUnitPrice(((Number) result[14]).intValue())
                            .actualAmount(((Number) result[15]).intValue())
                            .diffQuantity(((Number) result[16]).intValue())
                            .diffAmount(((Number) result[17]).intValue())
                            .build())
                    .collect(Collectors.toList());
            
            log.info("월재고현황 조회 완료 - hqCode: {}, 조회 건수: {}", searchDto.getHqCode(), responseList.size());
            return RespDto.success("월재고현황 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("월재고현황 조회 중 오류 발생 - hqCode: {}", searchDto.getHqCode(), e);
            return RespDto.fail("월재고현황 조회 중 오류가 발생했습니다.");
        }
    }
}