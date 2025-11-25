package com.inc.sh.service;

import com.inc.sh.dto.item.reqDto.ItemReqDto;
import com.inc.sh.dto.item.reqDto.ItemSearchDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.item.respDto.ItemRespDto;
import com.inc.sh.dto.item.respDto.ItemSaveRespDto;
import com.inc.sh.dto.item.respDto.ItemDeleteRespDto;
import com.inc.sh.entity.Item;
import com.inc.sh.repository.ItemRepository;
import com.inc.sh.util.ItemPriceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ItemService {
    
    private final ItemRepository itemRepository;
    
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
     * 품목 저장 (신규/수정)
     * @param request 품목 정보
     * @return 저장된 품목 코드
     */
    public RespDto<ItemSaveRespDto> saveItem(ItemReqDto request) {
        try {
            Item savedItem;
            String action;
            
            if (request.getItemCode() == null) {
                // 신규 등록
                log.info("품목 신규 등록 시작 - itemName: {}, categoryCode: {}", 
                        request.getItemName(), request.getCategoryCode());
                
                Item item = request.toEntity();
                
                // 금액 계산 및 설정
                calculateAndSetPrices(item);
                
                savedItem = itemRepository.save(item);
                action = "등록";
                
            } else {
                // 수정
                log.info("품목 수정 시작 - itemCode: {}, itemName: {}", 
                        request.getItemCode(), request.getItemName());
                
                Item existingItem = itemRepository.findByItemCode(request.getItemCode());
                if (existingItem == null) {
                    log.warn("수정할 품목을 찾을 수 없습니다 - itemCode: {}", request.getItemCode());
                    return RespDto.fail("수정할 품목을 찾을 수 없습니다.");
                }
                
                request.updateEntity(existingItem);
                
                // 금액 계산 및 설정
                calculateAndSetPrices(existingItem);
                
                savedItem = itemRepository.save(existingItem);
                action = "수정";
            }
            
            // 간소화된 응답 생성
            ItemSaveRespDto responseDto = ItemSaveRespDto.builder()
                    .itemCode(savedItem.getItemCode())
                    .build();
            
            log.info("품목 {} 완료 - itemCode: {}, itemName: {}", 
                    action, savedItem.getItemCode(), savedItem.getItemName());
            
            return RespDto.success("품목이 성공적으로 " + action + "되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("품목 저장 중 오류 발생 - itemCode: {}", request.getItemCode(), e);
            return RespDto.fail("품목 저장 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 품목 삭제 (소프트 삭제 - 종료일자 업데이트)
     * @param itemCode 품목코드
     * @return 삭제 결과
     */
    public RespDto<ItemDeleteRespDto> deleteItem(Integer itemCode) {
        try {
            log.info("품목 삭제 시작 - itemCode: {}", itemCode);
            
            Item item = itemRepository.findByItemCode(itemCode);
            if (item == null) {
                log.warn("삭제할 품목을 찾을 수 없습니다 - itemCode: {}", itemCode);
                return RespDto.fail("삭제할 품목을 찾을 수 없습니다.");
            }
            
            // 이미 종료된 품목인지 확인
            if (item.getEndDt() != null) {
                log.warn("이미 종료된 품목입니다 - itemCode: {}, endDt: {}", itemCode, item.getEndDt());
                return RespDto.fail("이미 종료된 품목입니다.");
            }
            
            // 종료일자를 현재일로 설정 (소프트 삭제)
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            item.setEndDt(currentDate);
            
            itemRepository.save(item);
            
            // 응답 생성
            ItemDeleteRespDto responseDto = ItemDeleteRespDto.builder()
                    .itemCode(itemCode)
                    .endDt(currentDate)
                    .build();
            
            log.info("품목 삭제 완료 - itemCode: {}, itemName: {}, endDt: {}", 
                    itemCode, item.getItemName(), currentDate);
            
            return RespDto.success("품목이 성공적으로 삭제되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("품목 삭제 중 오류 발생 - itemCode: {}", itemCode, e);
            return RespDto.fail("품목 삭제 중 오류가 발생했습니다.");
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