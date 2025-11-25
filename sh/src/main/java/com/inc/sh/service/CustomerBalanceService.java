package com.inc.sh.service;

import com.inc.sh.dto.customerBalance.reqDto.CustomerBalanceUpdateDto;
import com.inc.sh.dto.customerBalance.respDto.CustomerBalanceRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.entity.Customer;
import com.inc.sh.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerBalanceService {
    
    private final CustomerRepository customerRepository;
    
    /**
     * 브랜드별 후입금 거래처 미수잔액 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<CustomerBalanceRespDto>> getCustomerBalanceList(Integer brandCode) {
        try {
            log.info("거래처미수잔액 조회 시작 - 브랜드코드: {}", brandCode);
            
            List<Object[]> results = customerRepository.findCustomerBalanceByBrandCode(brandCode);
            
            List<CustomerBalanceRespDto> responseList = results.stream()
                    .map(result -> CustomerBalanceRespDto.builder()
                            .customerCode((Integer) result[0])
                            .customerName((String) result[1])
                            .ownerName((String) result[2])
                            .creditLimit(((Number) result[3]).intValue())
                            .balanceAmt(((Number) result[4]).intValue())
                            .build())
                    .collect(Collectors.toList());
            
            log.info("거래처미수잔액 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("거래처미수잔액 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("거래처미수잔액 조회 중 오류 발생", e);
            return RespDto.fail("거래처미수잔액 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 거래처 미수잔액 수정 (덮어쓰기)
     */
    @Transactional
    public RespDto<String> updateCustomerBalance(CustomerBalanceUpdateDto updateDto) {
        try {
            log.info("거래처미수잔액 수정 시작 - 거래처코드: {}, 기초미수금: {}", 
                    updateDto.getCustomerCode(), updateDto.getBalanceAmt());
            
            // 거래처 존재 여부 확인
            Optional<Customer> customerOpt = Optional.ofNullable(customerRepository.findByCustomerCode(updateDto.getCustomerCode()));
            
            if (customerOpt.isEmpty()) {
                return RespDto.fail("해당 거래처를 찾을 수 없습니다.");
            }
            
            Customer customer = customerOpt.get();
            
            // 후입금 거래처인지 확인
            if (customer.getDepositTypeCode() != 0) {
                return RespDto.fail("후입금 거래처만 미수잔액 수정이 가능합니다.");
            }
            
            // 미수잔액 덮어쓰기
            customer.setBalanceAmt(updateDto.getBalanceAmt());
            customerRepository.save(customer);
            
            log.info("거래처미수잔액 수정 완료 - 거래처코드: {}, 변경된 미수금: {}", 
                    updateDto.getCustomerCode(), updateDto.getBalanceAmt());
            
            return RespDto.success("거래처미수잔액이 수정되었습니다.", "수정 완료");
            
        } catch (Exception e) {
            log.error("거래처미수잔액 수정 중 오류 발생", e);
            return RespDto.fail("거래처미수잔액 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}