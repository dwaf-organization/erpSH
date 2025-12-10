package com.inc.sh.service;

import com.inc.sh.dto.customerBalance.reqDto.CustomerBalanceUpdateDto;
import com.inc.sh.dto.customerBalance.respDto.CustomerBalanceBatchResult;
import com.inc.sh.dto.customerBalance.respDto.CustomerBalanceRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.entity.Customer;
import com.inc.sh.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
     * 거래처미수잔액 다중 수정 (덮어쓰기)
     */
    @Transactional
    public RespDto<CustomerBalanceBatchResult> updateCustomerBalances(CustomerBalanceUpdateDto reqDto) {
        
        log.info("거래처미수잔액 다중 수정 시작 - 총 {}건", reqDto.getCustomers().size());
        
        List<CustomerBalanceRespDto> successData = new ArrayList<>();
        List<CustomerBalanceBatchResult.CustomerBalanceErrorDto> failData = new ArrayList<>();
        
        for (CustomerBalanceUpdateDto.CustomerBalanceItemDto customer : reqDto.getCustomers()) {
            try {
                // 개별 거래처 미수잔액 수정 처리
                CustomerBalanceRespDto updatedCustomer = updateSingleCustomerBalance(customer);
                successData.add(updatedCustomer);
                
                log.info("거래처미수잔액 수정 성공 - customerCode: {}, balanceAmt: {}", 
                        updatedCustomer.getCustomerCode(), updatedCustomer.getBalanceAmt());
                
            } catch (Exception e) {
                log.error("거래처미수잔액 수정 실패 - customerCode: {}, 에러: {}", customer.getCustomerCode(), e.getMessage());
                
                // 에러 시 거래처명 조회 시도
                String customerName = getCustomerNameSafely(customer.getCustomerCode());
                
                CustomerBalanceBatchResult.CustomerBalanceErrorDto errorDto = CustomerBalanceBatchResult.CustomerBalanceErrorDto.builder()
                        .customerCode(customer.getCustomerCode())
                        .customerName(customerName)
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성
        CustomerBalanceBatchResult result = CustomerBalanceBatchResult.builder()
                .totalCount(reqDto.getCustomers().size())
                .successCount(successData.size())
                .failCount(failData.size())
                .successData(successData)
                .failData(failData)
                .build();
        
        String message = String.format("거래처잔액 수정 완료 - 성공: %d건, 실패: %d건", 
                successData.size(), failData.size());
        
        log.info("거래처잔액 다중 수정 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getCustomers().size(), successData.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 거래처 미수잔액 수정 처리 (기존 검증 로직 포함)
     */
    private CustomerBalanceRespDto updateSingleCustomerBalance(CustomerBalanceUpdateDto.CustomerBalanceItemDto updateDto) {
        
        log.info("거래처잔액 수정 시작 - 거래처코드: {}, 기초미수금: {}",
                updateDto.getCustomerCode(), updateDto.getBalanceAmt());

        // 거래처 존재 여부 확인
        Optional<Customer> customerOpt = Optional.ofNullable(customerRepository.findByCustomerCode(updateDto.getCustomerCode()));

        if (customerOpt.isEmpty()) {
            throw new RuntimeException("해당 거래처를 찾을 수 없습니다: " + updateDto.getCustomerCode());
        }

        Customer customer = customerOpt.get();

        // 미수잔액 덮어쓰기
        customer.setBalanceAmt(updateDto.getBalanceAmt());
        customerRepository.save(customer);

        log.info("거래처미수잔액 수정 완료 - 거래처코드: {}, 변경된 미수금: {}",
                updateDto.getCustomerCode(), updateDto.getBalanceAmt());

        // 응답 DTO 생성
        return CustomerBalanceRespDto.builder()
                .customerCode(customer.getCustomerCode())
                .customerName(customer.getCustomerName())
                .ownerName(customer.getOwnerName())
                .creditLimit(customer.getCreditLimit())
                .balanceAmt(customer.getBalanceAmt())
                .build();
    }
    
    /**
     * 거래처명 안전 조회 (에러 발생시 사용)
     */
    private String getCustomerNameSafely(Integer customerCode) {
        try {
            Customer customer = customerRepository.findByCustomerCode(customerCode);
            return customer != null ? customer.getCustomerName() : "알 수 없음";
        } catch (Exception e) {
            return "조회 실패";
        }
    }
}