package com.inc.sh.service;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.customer.reqDto.CustomerReqDto;
import com.inc.sh.dto.customer.reqDto.CustomerSearchDto;
import com.inc.sh.dto.customer.respDto.CustomerDeleteRespDto;
import com.inc.sh.dto.customer.respDto.CustomerRespDto;
import com.inc.sh.entity.Customer;
import com.inc.sh.repository.CustomerRepository;
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
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    
    /**
     * 거래처 조회 (검색 조건)
     * @param searchDto 검색 조건
     * @return 조회된 거래처 목록
     */
    @Transactional(readOnly = true)
    public RespDto<List<CustomerRespDto>> getCustomerList(CustomerSearchDto searchDto) {
        try {
            log.info("거래처 목록 조회 시작 - brandCode: {}, customerName: {}, closeDtYn: {}, orderBlockYn: {}", 
                    searchDto.getBrandCode(), searchDto.getCustomerName(), 
                    searchDto.getCloseDtYn(), searchDto.getOrderBlockYn());
            
            List<Customer> customers = customerRepository.findBySearchConditions(
                    searchDto.getBrandCode(),
                    searchDto.getCustomerName(),
                    searchDto.getCloseDtYn(),
                    searchDto.getOrderBlockYn()
            );
            
            List<CustomerRespDto> responseList = customers.stream()
                    .map(CustomerRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("거래처 목록 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("거래처 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("거래처 목록 조회 중 오류 발생", e);
            return RespDto.fail("거래처 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 거래처 상세 조회
     * @param customerCode 거래처코드
     * @return 거래처 상세 정보
     */
    @Transactional(readOnly = true)
    public RespDto<CustomerRespDto> getCustomer(Integer customerCode) {
        try {
            log.info("거래처 상세 조회 시작 - customerCode: {}", customerCode);
            
            Customer customer = customerRepository.findByCustomerCode(customerCode);
            if (customer == null) {
                log.warn("거래처를 찾을 수 없습니다 - customerCode: {}", customerCode);
                return RespDto.fail("거래처를 찾을 수 없습니다.");
            }
            
            CustomerRespDto responseDto = CustomerRespDto.fromEntity(customer);
            
            log.info("거래처 상세 조회 완료 - customerCode: {}, customerName: {}", 
                    customerCode, customer.getCustomerName());
            return RespDto.success("거래처 조회 성공", responseDto);
            
        } catch (Exception e) {
            log.error("거래처 상세 조회 중 오류 발생 - customerCode: {}", customerCode, e);
            return RespDto.fail("거래처 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 거래처 저장 (신규/수정)
     * @param request 거래처 정보
     * @return 저장된 거래처 정보
     */
    public RespDto<CustomerRespDto> saveCustomer(CustomerReqDto request) {
        try {
            Customer savedCustomer;
            String action;
            
            if (request.getCustomerCode() == null) {
                // 신규 등록
                log.info("거래처 신규 등록 시작 - customerName: {}", request.getCustomerName());
                
                Customer customer = request.toEntity();
                savedCustomer = customerRepository.save(customer);
                action = "등록";
                
            } else {
                // 수정
                log.info("거래처 수정 시작 - customerCode: {}, customerName: {}", 
                        request.getCustomerCode(), request.getCustomerName());
                
                Customer existingCustomer = customerRepository.findByCustomerCode(request.getCustomerCode());
                if (existingCustomer == null) {
                    log.warn("수정할 거래처를 찾을 수 없습니다 - customerCode: {}", request.getCustomerCode());
                    return RespDto.fail("수정할 거래처를 찾을 수 없습니다.");
                }
                
                request.updateEntity(existingCustomer);
                savedCustomer = customerRepository.save(existingCustomer);
                action = "수정";
            }
            
//            CustomerRespDto responseDto = CustomerRespDto.fromEntity(savedCustomer);
            
            log.info("거래처 {} 완료 - customerCode: {}, customerName: {}", 
                    action, savedCustomer.getCustomerCode(), savedCustomer.getCustomerName());
            
            return RespDto.success("거래처가 성공적으로 " + action + "되었습니다.", null);
            
        } catch (Exception e) {
            log.error("거래처 저장 중 오류 발생 - customerCode: {}", request.getCustomerCode(), e);
            return RespDto.fail("거래처 저장 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 거래처 삭제 (폐기일자 입력)
     * @param customerCode 거래처코드
     * @return 삭제 결과
     */
    public RespDto<CustomerDeleteRespDto> deleteCustomer(Integer customerCode) {
        try {
            log.info("거래처 삭제 시작 - customerCode: {}", customerCode);
            
            Customer customer = customerRepository.findByCustomerCode(customerCode);
            if (customer == null) {
                log.warn("삭제할 거래처를 찾을 수 없습니다 - customerCode: {}", customerCode);
                return RespDto.fail("삭제할 거래처를 찾을 수 없습니다.");
            }
            
            // 폐기일자를 현재일로 설정
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            customer.setCloseDt(currentDate);
            
            customerRepository.save(customer);
            
            // 필요한 값만 포함한 응답 DTO 생성
            CustomerDeleteRespDto responseDto = CustomerDeleteRespDto.builder()
                    .customerCode(customerCode)
                    .closeDt(currentDate)
                    .build();
            
            log.info("거래처 삭제 완료 - customerCode: {}, customerName: {}, closeDt: {}", 
                    customerCode, customer.getCustomerName(), currentDate);
            
            return RespDto.success("거래처가 성공적으로 삭제되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("거래처 삭제 중 오류 발생 - customerCode: {}", customerCode, e);
            return RespDto.fail("거래처 삭제 중 오류가 발생했습니다.");
        }
    }
}