package com.inc.sh.service;

import com.inc.sh.dto.popup.reqDto.ItemSearchPopupDto;
import com.inc.sh.dto.popup.reqDto.CustomerItemSearchDto;
import com.inc.sh.dto.popup.reqDto.CustomerSearchPopupDto;
import com.inc.sh.dto.popup.reqDto.VirtualAccountSearchPopupDto;
import com.inc.sh.dto.popup.respDto.CustomerItemRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.item.respDto.ItemRespDto;
import com.inc.sh.dto.customer.respDto.CustomerRespDto;
import com.inc.sh.dto.virtualAccount.respDto.VirtualAccountRespDto;
import com.inc.sh.entity.Item;
import com.inc.sh.repository.ItemRepository;
import com.inc.sh.repository.CustomerRepository;
import com.inc.sh.repository.VirtualAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PopupService {
    
    private final ItemRepository itemRepository;
    private final CustomerRepository customerRepository;
    private final VirtualAccountRepository virtualAccountRepository;
    
    /**
     * 품목 팝업 검색
     * @param searchDto 검색 조건
     * @return 조회된 품목 목록
     */
    public RespDto<List<ItemRespDto>> searchItems(ItemSearchPopupDto searchDto) {
        try {
            log.info("품목 팝업 검색 시작 - hqCode: {}, item: {}, categoryCode: {}, priceType: {}", 
            		searchDto.getHqCode(), searchDto.getItem(), searchDto.getCategoryCode(), searchDto.getPriceType());
            
            List<Item> items = itemRepository.findByPopupSearchConditions(
            		searchDto.getHqCode(),
                    searchDto.getItem(),
                    searchDto.getCategoryCode(),
                    searchDto.getPriceType()
            );
            
            List<ItemRespDto> responseList = items.stream()
                    .map(ItemRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("품목 팝업 검색 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("품목 검색 성공", responseList);
            
        } catch (Exception e) {
            log.error("품목 팝업 검색 중 오류 발생", e);
            return RespDto.fail("품목 검색 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 거래처 팝업 검색 (브랜드명, 물류센터명 포함)
     * @param searchDto 검색 조건
     * @return 조회된 거래처 목록 (브랜드명, 물류센터명 포함)
     */
    public RespDto<List<CustomerRespDto>> searchCustomers(CustomerSearchPopupDto searchDto) {
        try {
            log.info("거래처 팝업 검색 시작 - hqCode: {}, customerSearch: {}, brandCode: {}", 
                    searchDto.getHqCode(), searchDto.getCustomerSearch(), searchDto.getBrandCode());
            
            // hqCode 필수 체크
            if (searchDto.getHqCode() == null) {
                return RespDto.fail("본사코드는 필수 파라미터입니다.");
            }
            
            // customerSearch 빈값 처리
            String customerSearchParam = null;
            if (searchDto.getCustomerSearch() != null && !searchDto.getCustomerSearch().trim().isEmpty()) {
                customerSearchParam = searchDto.getCustomerSearch().trim();
            }
            
            // 브랜드코드 처리: '전체', '0', null, 빈값이면 null, 숫자면 Integer로 변환
            Integer brandCodeParam = null;
            if (searchDto.getBrandCode() != null && 
                !searchDto.getBrandCode().trim().isEmpty() &&
                !"전체".equals(searchDto.getBrandCode().trim()) && 
                !"0".equals(searchDto.getBrandCode().trim())) {
                try {
                    brandCodeParam = Integer.valueOf(searchDto.getBrandCode());
                } catch (NumberFormatException e) {
                    log.warn("브랜드코드 형식 오류: {}", searchDto.getBrandCode());
                }
            }
            
            // 조인 쿼리 실행 (Customer + BrandInfo + DistCenter 조인)
            List<Object[]> results = customerRepository.findByPopupSearchConditionsWithJoin(
                    searchDto.getHqCode(),
                    customerSearchParam,
                    brandCodeParam
            );
            
            // Object[] 결과를 CustomerRespDto로 변환
            List<CustomerRespDto> responseList = new ArrayList<>();
            
            for (Object[] result : results) {
                try {
                    // Customer 데이터 매핑 - 데이터 타입별 안전한 변환 처리
                    CustomerRespDto customerDto = CustomerRespDto.builder()
                            .customerCode(convertToInteger(result[0]))
                            .hqCode(convertToInteger(result[1]))
                            .customerName(convertToString(result[2]))
                            .ownerName(convertToString(result[3]))
                            .bizNum(convertToString(result[4]))
                            .zipCode(convertToString(result[5]))
                            .addr(convertToString(result[6]))
                            .bizType(convertToString(result[7]))
                            .bizSector(convertToString(result[8]))
                            .email(convertToString(result[9]))
                            .telNum(convertToString(result[10]))
                            .mobileNum(convertToString(result[11]))
                            .faxNum(convertToString(result[12]))
                            .taxInvoiceYn(convertToString(result[13]))
                            .taxInvoiceName(convertToString(result[14]))
                            .regDt(convertToString(result[15]))
                            .closeDt(convertToString(result[16]))
                            .printNote(convertToString(result[17]))
                            .bankName(convertToString(result[18]))
                            .accountHolder(convertToString(result[19]))
                            .accountNum(convertToString(result[20]))
                            .distCenterCode(convertToInteger(result[21]))
                            .brandCode(convertToInteger(result[22]))
                            .deliveryWeekday(convertToString(result[23]))
                            .depositTypeCode(convertToInteger(result[24]))
                            .virtualAccount(convertToString(result[25]))
                            .virtualBankName(convertToString(result[26]))
                            .balanceAmt(convertToInteger(result[27]))
                            .hqMemo(convertToString(result[28]))
                            .creditLimit(convertToInteger(result[29]))
                            .collectionDay(convertToInteger(result[30]))
                            .orderBlockYn(convertToInteger(result[31]))
                            .orderBlockReason(convertToString(result[32]))
                            .orderBlockDt(convertToString(result[33]))
                            .description(convertToString(result[34]))
                            .createdAt(convertToLocalDateTime(result[35]))
                            .updatedAt(convertToLocalDateTime(result[36]))
                            // 조인으로 가져온 데이터
                            .brandName(convertToString(result[37]))      // brand_name
                            .distCenterName(convertToString(result[38])) // dist_center_name
                            .build();
                    
                    responseList.add(customerDto);
                    
                } catch (Exception e) {
                    log.error("거래처 데이터 변환 중 오류 발생 - customerCode: {}, error: {}", result[0], e.getMessage());
                }
            }
            
            log.info("거래처 팝업 검색 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("거래처 검색 성공", responseList);
            
        } catch (Exception e) {
            log.error("거래처 팝업 검색 중 오류 발생", e);
            return RespDto.fail("거래처 검색 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * Object를 String으로 안전하게 변환
     */
    private String convertToString(Object obj) {
        if (obj == null) return null;
        return obj.toString();
    }
    
    /**
     * Object를 Integer로 안전하게 변환
     */
    private Integer convertToInteger(Object obj) {
        if (obj == null) return null;
        
        if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof BigInteger) {
            return ((BigInteger) obj).intValue();
        } else if (obj instanceof Long) {
            return ((Long) obj).intValue();
        } else if (obj instanceof String) {
            try {
                return Integer.valueOf((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Object를 LocalDateTime으로 안전하게 변환
     */
    private LocalDateTime convertToLocalDateTime(Object obj) {
        if (obj == null) return null;
        
        if (obj instanceof LocalDateTime) {
            return (LocalDateTime) obj;
        } else if (obj instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) obj).toLocalDateTime();
        } else if (obj instanceof java.util.Date) {
            return new java.sql.Timestamp(((java.util.Date) obj).getTime()).toLocalDateTime();
        }
        return null;
    }
    
    /**
     * 가상계좌 팝업 검색 (거래처명 포함)
     * @param searchDto 검색 조건
     * @return 조회된 가상계좌 목록 (거래처명 포함)
     */
    public RespDto<List<VirtualAccountRespDto>> searchVirtualAccounts(VirtualAccountSearchPopupDto searchDto) {
        try {
            log.info("가상계좌 팝업 검색 시작 - hqCode: {}, virtualAccountNum: {}, virtualAccountStatus: {}", 
                    searchDto.getHqCode(), searchDto.getVirtualAccountNum(), searchDto.getVirtualAccountStatus());
            
            // hqCode 필수 체크
            if (searchDto.getHqCode() == null) {
                return RespDto.fail("본사코드는 필수 파라미터입니다.");
            }
            
            // 검색 조건 빈값 처리
            String virtualAccountNumParam = (searchDto.getVirtualAccountNum() != null && !searchDto.getVirtualAccountNum().trim().isEmpty()) 
                    ? searchDto.getVirtualAccountNum().trim() : null;
            String virtualAccountStatusParam = (searchDto.getVirtualAccountStatus() != null && !searchDto.getVirtualAccountStatus().trim().isEmpty()) 
                    ? searchDto.getVirtualAccountStatus().trim() : null;
            
            // 조인 쿼리 실행 (VirtualAccount + Customer 조인)
            List<Object[]> results = virtualAccountRepository.findByPopupSearchConditionsWithJoin(
                    searchDto.getHqCode(),
                    virtualAccountNumParam,
                    virtualAccountStatusParam
            );
            
            // Object[] 결과를 VirtualAccountRespDto로 변환
            List<VirtualAccountRespDto> responseList = new ArrayList<>();
            
            for (Object[] result : results) {
                try {
                    VirtualAccountRespDto virtualAccountDto = VirtualAccountRespDto.fromObjectArrayWithJoin(result);
                    responseList.add(virtualAccountDto);
                    
                } catch (Exception e) {
                    log.error("가상계좌 데이터 변환 중 오류 발생 - virtualAccountCode: {}, error: {}", result[0], e.getMessage());
                }
            }
            
            log.info("가상계좌 팝업 검색 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("가상계좌 검색 성공", responseList);
            
        } catch (Exception e) {
            log.error("가상계좌 팝업 검색 중 오류 발생", e);
            return RespDto.fail("가상계좌 검색 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 거래처품목조회 팝업
     */
    @Transactional(readOnly = true)
    public RespDto<List<CustomerItemRespDto>> searchCustomerItems(CustomerItemSearchDto searchDto) {
        try {
            log.info("거래처품목 팝업 조회 시작 - hqCode: {}, customerCode: {}, item: {}", 
                    searchDto.getHqCode(), searchDto.getCustomerCode(), searchDto.getItem());
            
            // 필수 파라미터 검증
            if (searchDto.getHqCode() == null) {
                return RespDto.fail("본사코드는 필수입니다.");
            }
            if (searchDto.getCustomerCode() == null) {
                return RespDto.fail("거래처코드는 필수입니다.");
            }
            
            // 복합 조인 조회 (기존 CustomerRepository 사용)
            List<Object[]> results = customerRepository.findCustomerItemsWithPrice(
                    searchDto.getHqCode(),
                    searchDto.getCustomerCode(),
                    searchDto.getItem(),
                    searchDto.getWarehouseCode(),
                    searchDto.getCategoryCode(),
                    searchDto.getPriceType()
            );
            
            // Object[] → DTO 변환
            List<CustomerItemRespDto> responseList = results.stream()
                    .map(this::convertToCustomerItemRespDto)
                    .collect(Collectors.toList());
            
            log.info("거래처품목 팝업 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("거래처품목 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("거래처품목 팝업 조회 중 오류 발생", e);
            return RespDto.fail("거래처품목 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * Object[] → CustomerItemRespDto 변환
     */
    private CustomerItemRespDto convertToCustomerItemRespDto(Object[] result) {
        return CustomerItemRespDto.builder()
                .itemCode(safeIntegerCast(result[0]))
                .itemName(safeStringCast(result[1]))
                .specification(safeStringCast(result[2]))
                .purchaseUnit(safeStringCast(result[3]))
                .vatType(safeStringCast(result[4]))
                .vatDetail(safeStringCast(result[5]))
                .origin(safeStringCast(result[6]))
                .categoryCode(safeIntegerCast(result[7]))
                .categoryName(safeStringCast(result[8]))
                .warehouseCode(safeIntegerCast(result[9]))
                .warehouseName(safeStringCast(result[10]))
                .currentQuantity(safeIntegerCast(result[11]))
                .safeQuantity(safeIntegerCast(result[12]))
                // 가격 정보 (거래처별 or 기본가격)
                .basePrice(safeIntegerCast(result[13]))
                .supplyPrice(safeIntegerCast(result[14]))
                .taxAmount(safeIntegerCast(result[15]))
                .taxableAmount(safeIntegerCast(result[16]))
                .dutyFreeAmount(safeIntegerCast(result[17]))
                .totalAmount(safeIntegerCast(result[18]))
                .orderAvailableYn(safeIntegerCast(result[19]))
                .minOrderQty(safeIntegerCast(result[20]))
                .maxOrderQty(safeIntegerCast(result[21]))
                .deadlineDay(safeIntegerCast(result[22]))
                .deadlineTime(safeStringCast(result[23]))
                .build();
    }
    
    /**
     * 안전한 Integer 캐스팅
     */
    private Integer safeIntegerCast(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try {
            return Integer.valueOf(obj.toString());
        } catch (NumberFormatException e) {
            log.warn("Integer 캐스팅 실패: {}", obj);
            return null;
        }
    }
    
    /**
     * 안전한 String 캐스팅
     */
    private String safeStringCast(Object obj) {
        return obj != null ? obj.toString() : null;
    }
}