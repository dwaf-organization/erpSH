package com.inc.sh.service;

import com.inc.sh.dto.deliveryPrice.reqDto.DeliveryPriceSearchDto;
import com.inc.sh.dto.deliveryPrice.reqDto.DeliveryPriceUpdateReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.deliveryPrice.respDto.DeliveryPriceRespDto;
import com.inc.sh.dto.deliveryPrice.respDto.DeliveryPriceUpdateRespDto;
import com.inc.sh.entity.Item;
import com.inc.sh.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            log.info("납품단가관리 품목 조회 시작 - itemCode: {}, categoryCode: {}, priceType: {}", 
                    searchDto.getItemCode(), searchDto.getCategoryCode(), searchDto.getPriceType());
            
            List<Item> items = itemRepository.findForDeliveryPriceManagement(
                    searchDto.getItemCode(),
                    searchDto.getCategoryCode(),
                    searchDto.getPriceType()
            );
            
            List<DeliveryPriceRespDto> responseList = items.stream()
                    .map(DeliveryPriceRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("납품단가관리 품목 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("납품단가관리 품목 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("납품단가관리 품목 조회 중 오류 발생", e);
            return RespDto.fail("납품단가관리 품목 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 품목 단가 수정
     * @param request 단가 수정 요청
     * @return 수정 결과
     */
    public RespDto<DeliveryPriceUpdateRespDto> updateItemPrice(DeliveryPriceUpdateReqDto request) {
        try {
            log.info("품목 단가 수정 시작 - itemCode: {}, newBasePrice: {}", 
                    request.getItemCode(), request.getBasePrice());
            
            Item item = itemRepository.findByItemCode(request.getItemCode());
            if (item == null) {
                log.warn("수정할 품목을 찾을 수 없습니다 - itemCode: {}", request.getItemCode());
                return RespDto.fail("수정할 품목을 찾을 수 없습니다.");
            }
            
            // 종료된 품목인지 확인
            if (item.getEndDt() != null) {
                log.warn("종료된 품목은 단가를 수정할 수 없습니다 - itemCode: {}, endDt: {}", 
                        request.getItemCode(), item.getEndDt());
                return RespDto.fail("종료된 품목은 단가를 수정할 수 없습니다.");
            }
            
            Integer originalBasePrice = item.getBasePrice();
            Integer newPreviousPrice = item.getPreviousPrice();
            String processMessage;
            String priceTypeDesc;
            
            if (item.getPriceType() == 1) {
                // 납품단가: 기본단가만 수정
                item.setBasePrice(request.getBasePrice());
                priceTypeDesc = "납품단가";
                processMessage = "기본단가만 수정했습니다.";
                
                log.info("납품단가 단가 수정 - itemCode: {}, 기존: {}, 신규: {}", 
                        request.getItemCode(), originalBasePrice, request.getBasePrice());
                
            } else if (item.getPriceType() == 2) {
                // 납품싯가: 현재 기본단가를 직전단가로, 입력값을 기본단가로
                newPreviousPrice = originalBasePrice;
                item.setPreviousPrice(newPreviousPrice);
                item.setBasePrice(request.getBasePrice());
                priceTypeDesc = "납품싯가";
                processMessage = "기존 기본단가를 직전단가로 이동하고 새로운 기본단가를 설정했습니다.";
                
                log.info("납품싯가 단가 수정 - itemCode: {}, 기존 기본단가->직전단가: {}, 새 기본단가: {}", 
                        request.getItemCode(), originalBasePrice, request.getBasePrice());
                
            } else {
                log.warn("유효하지 않은 단가유형입니다 - itemCode: {}, priceType: {}", 
                        request.getItemCode(), item.getPriceType());
                return RespDto.fail("유효하지 않은 단가유형입니다.");
            }
            
            itemRepository.save(item);
            
            // 응답 생성
            DeliveryPriceUpdateRespDto responseDto = DeliveryPriceUpdateRespDto.builder()
                    .itemCode(item.getItemCode())
                    .previousPrice(item.getPreviousPrice())
                    .basePrice(item.getBasePrice())
                    .priceTypeDesc(priceTypeDesc)
                    .message(processMessage)
                    .build();
            
            log.info("품목 단가 수정 완료 - itemCode: {}, itemName: {}, priceType: {}, basePrice: {}, previousPrice: {}", 
                    item.getItemCode(), item.getItemName(), priceTypeDesc, item.getBasePrice(), item.getPreviousPrice());
            
            return RespDto.success("품목 단가가 성공적으로 수정되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("품목 단가 수정 중 오류 발생 - itemCode: {}", request.getItemCode(), e);
            return RespDto.fail("품목 단가 수정 중 오류가 발생했습니다.");
        }
    }
}