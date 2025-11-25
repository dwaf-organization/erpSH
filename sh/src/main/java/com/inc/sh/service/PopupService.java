package com.inc.sh.service;

import com.inc.sh.dto.popup.reqDto.ItemSearchPopupDto;
import com.inc.sh.dto.popup.reqDto.CustomerSearchPopupDto;
import com.inc.sh.dto.popup.reqDto.VirtualAccountSearchPopupDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.item.respDto.ItemRespDto;
import com.inc.sh.dto.customer.respDto.CustomerRespDto;
import com.inc.sh.dto.virtualAccount.respDto.VirtualAccountRespDto;
import com.inc.sh.entity.Item;
import com.inc.sh.entity.Customer;
import com.inc.sh.entity.VirtualAccount;
import com.inc.sh.repository.ItemRepository;
import com.inc.sh.repository.CustomerRepository;
import com.inc.sh.repository.VirtualAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            log.info("품목 팝업 검색 시작 - itemCode: {}, itemName: {}, categoryCode: {}, priceType: {}", 
                    searchDto.getItemCode(), searchDto.getItemName(), searchDto.getCategoryCode(), searchDto.getPriceType());
            
            List<Item> items = itemRepository.findByPopupSearchConditions(
                    searchDto.getItemCode(),
                    searchDto.getItemName(),
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
     * 거래처 팝업 검색
     * @param searchDto 검색 조건
     * @return 조회된 거래처 목록
     */
    public RespDto<List<CustomerRespDto>> searchCustomers(CustomerSearchPopupDto searchDto) {
        try {
            log.info("거래처 팝업 검색 시작 - customerCode: {}, customerName: {}, brandCode: {}", 
                    searchDto.getCustomerCode(), searchDto.getCustomerName(), searchDto.getBrandCode());
            
            List<Customer> customers = customerRepository.findByPopupSearchConditions(
                    searchDto.getCustomerCode(),
                    searchDto.getCustomerName(),
                    searchDto.getBrandCode()
            );
            
            List<CustomerRespDto> responseList = customers.stream()
                    .map(CustomerRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("거래처 팝업 검색 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("거래처 검색 성공", responseList);
            
        } catch (Exception e) {
            log.error("거래처 팝업 검색 중 오류 발생", e);
            return RespDto.fail("거래처 검색 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 가상계좌 팝업 검색
     * @param searchDto 검색 조건
     * @return 조회된 가상계좌 목록
     */
    public RespDto<List<VirtualAccountRespDto>> searchVirtualAccounts(VirtualAccountSearchPopupDto searchDto) {
        try {
            log.info("가상계좌 팝업 검색 시작 - virtualAccountCode: {}, virtualAccount: {}, accountStatus: {}", 
                    searchDto.getVirtualAccountCode(), searchDto.getVirtualAccount(), searchDto.getAccountStatus());
            
            List<VirtualAccount> virtualAccounts = virtualAccountRepository.findByPopupSearchConditions(
                    searchDto.getVirtualAccountCode(),
                    searchDto.getVirtualAccount(),
                    searchDto.getAccountStatus()
            );
            
            List<VirtualAccountRespDto> responseList = virtualAccounts.stream()
                    .map(VirtualAccountRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("가상계좌 팝업 검색 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("가상계좌 검색 성공", responseList);
            
        } catch (Exception e) {
            log.error("가상계좌 팝업 검색 중 오류 발생", e);
            return RespDto.fail("가상계좌 검색 중 오류가 발생했습니다.");
        }
    }
}