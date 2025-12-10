package com.inc.sh.service;

import com.inc.sh.dto.picking.reqDto.PickingListSearchDto;
import com.inc.sh.dto.picking.respDto.PickingListRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PickingListService {
    
    private final OrderRepository orderRepository;
    
    /**
     * 품목별 PickingList 조회
     * 배송요청 상태의 주문에서 품목별 출고량 집계
     */
    @Transactional(readOnly = true)
    public RespDto<List<PickingListRespDto>> getPickingList(PickingListSearchDto searchDto) {
        try {
            log.info("품목별 PickingList 조회 시작 - hqCode: {}, 납기일자: {}~{}, 품목코드: {}, 물류센터코드: {}, 브랜드코드: {}", 
                    searchDto.getHqCode(), searchDto.getDeliveryRequestDtStart(), searchDto.getDeliveryRequestDtEnd(), 
                    searchDto.getItemCode(), searchDto.getDistCenterCode(), searchDto.getBrandCode());
            
            List<Object[]> results = orderRepository.findPickingListByConditionsWithHqCode(
                    searchDto.getDeliveryRequestDtStart(),
                    searchDto.getDeliveryRequestDtEnd(),
                    searchDto.getItemCode(),
                    searchDto.getDistCenterCode(),
                    searchDto.getBrandCode(),
                    searchDto.getHqCode()
            );
            
            List<PickingListRespDto> responseList = results.stream()
                    .map(result -> PickingListRespDto.builder()
                            .deliveryRequestDt((String) result[0])       // 납기일자 추가
                            .distCenterCode((Integer) result[1])         // 물류센터코드 추가  
                            .distCenterName((String) result[2])          // 물류센터명
                            .itemCode((Integer) result[3])               // 품목코드
                            .itemName((String) result[4])                // 품명
                            .specification((String) result[5])           // 규격
                            .unit((String) result[6])                    // 단위
                            .totalQty(((Number) result[7]).intValue())   // 출고량
                            .build())
                    .collect(Collectors.toList());
            
            log.info("품목별 PickingList 조회 완료 - hqCode: {}, 조회 건수: {}", searchDto.getHqCode(), responseList.size());
            return RespDto.success("품목별 PickingList 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("품목별 PickingList 조회 중 오류 발생 - hqCode: {}", searchDto.getHqCode(), e);
            return RespDto.fail("품목별 PickingList 조회 중 오류가 발생했습니다.");
        }
    }
}