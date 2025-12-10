package com.inc.sh.service;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.customer.reqDto.CustomerDeleteReqDto;
import com.inc.sh.dto.customer.reqDto.CustomerReqDto;
import com.inc.sh.dto.customer.reqDto.CustomerSaveReqDto;
import com.inc.sh.dto.customer.reqDto.CustomerSearchDto;
import com.inc.sh.dto.customer.respDto.CustomerBatchResult;
import com.inc.sh.dto.customer.respDto.CustomerDeleteRespDto;
import com.inc.sh.dto.customer.respDto.CustomerRespDto;
import com.inc.sh.entity.Customer;
import com.inc.sh.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    
    /**
     * 거래처 목록 조회 (브랜드명, 물류센터명 포함)
     */
    public RespDto<List<CustomerRespDto>> getCustomerList(
    		CustomerSearchDto searchDto) {
        try {
            log.info("거래처 목록 조회 시작 - brandCode: {}, customerName: {}, closeDtYn: {}, orderBlockYn: {}", 
            		searchDto.getBrandCode(), searchDto.getCustomerName(), searchDto.getCloseDtYn(), searchDto.getOrderBlockYn());
            
            // 조인 쿼리 실행 (Customer + BrandInfo + DistCenter 조인)
            List<Object[]> results = customerRepository.findBySearchConditionsWithJoin(
            		searchDto.getBrandCode(), searchDto.getCustomerName(), searchDto.getCloseDtYn(), searchDto.getOrderBlockYn());
            
            // Object[] 결과를 CustomerRespDto로 변환
            List<CustomerRespDto> responseList = new ArrayList<>();
            
            for (Object[] result : results) {
                try {
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
            
            log.info("거래처 목록 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("거래처 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("거래처 목록 조회 중 오류 발생", e);
            return RespDto.fail("거래처 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * Object를 String으로 안전하게 변환
     */
    private String convertToString(Object obj) {
        return obj != null ? obj.toString() : null;
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
     * 거래처 다중 저장 (신규/수정)
     */
    @Transactional
    public RespDto<CustomerBatchResult> saveCustomers(CustomerSaveReqDto reqDto) {
        
        log.info("거래처 다중 저장 시작 - 총 {}건", reqDto.getCustomers().size());
        
        List<CustomerRespDto> successData = new ArrayList<>();
        List<CustomerBatchResult.CustomerErrorDto> failData = new ArrayList<>();
        
        for (CustomerSaveReqDto.CustomerItemDto customer : reqDto.getCustomers()) {
            try {
                // 개별 거래처 저장 처리
                CustomerRespDto savedCustomer = saveSingleCustomer(customer);
                successData.add(savedCustomer);
                
                log.info("거래처 저장 성공 - customerCode: {}, customerName: {}", 
                        savedCustomer.getCustomerCode(), savedCustomer.getCustomerName());
                
            } catch (Exception e) {
                log.error("거래처 저장 실패 - customerName: {}, 에러: {}", customer.getCustomerName(), e.getMessage());
                
                CustomerBatchResult.CustomerErrorDto errorDto = CustomerBatchResult.CustomerErrorDto.builder()
                        .customerCode(customer.getCustomerCode())
                        .customerName(customer.getCustomerName())
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성
        CustomerBatchResult result = CustomerBatchResult.builder()
                .totalCount(reqDto.getCustomers().size())
                .successCount(successData.size())
                .failCount(failData.size())
                .successData(successData)
                .failData(failData)
                .build();
        
        String message = String.format("거래처 저장 완료 - 성공: %d건, 실패: %d건", 
                successData.size(), failData.size());
        
        log.info("거래처 다중 저장 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getCustomers().size(), successData.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 거래처 저장 처리 (DTO에서 Entity 생성)
     */
    private CustomerRespDto saveSingleCustomer(CustomerSaveReqDto.CustomerItemDto reqDto) {
        
        log.info("거래처 저장 시작 - customerCode: {}, customerName: {}", reqDto.getCustomerCode(), reqDto.getCustomerName());
        
        // 필수 필드 검증
        if (reqDto.getHqCode() == null) {
            throw new RuntimeException("본사코드는 필수입니다");
        }
        if (reqDto.getBrandCode() == null) {
            throw new RuntimeException("브랜드코드는 필수입니다");
        }
        if (reqDto.getCustomerName() == null || reqDto.getCustomerName().trim().isEmpty()) {
            throw new RuntimeException("거래처명은 필수입니다");
        }
        if (reqDto.getOwnerName() == null || reqDto.getOwnerName().trim().isEmpty()) {
            throw new RuntimeException("대표자는 필수입니다");
        }
        if (reqDto.getBizNum() == null || reqDto.getBizNum().trim().isEmpty()) {
            throw new RuntimeException("사업자번호는 필수입니다");
        }
        if (reqDto.getDistCenterCode() == null) {
            throw new RuntimeException("물류센터코드는 필수입니다");
        }
        
        boolean isUpdate = reqDto.getCustomerCode() != null;
        Customer customer;
        
        if (isUpdate) {
            // 수정
            customer = customerRepository.findByCustomerCode(reqDto.getCustomerCode());
            if (customer == null) {
                throw new RuntimeException("수정할 거래처를 찾을 수 없습니다: " + reqDto.getCustomerCode());
            }
            
            // DTO에서 기존 Entity 업데이트
            reqDto.updateEntity(customer);
            
        } else {
            // 신규 - 사업자번호 중복 체크
            List<Customer> existingCustomers = customerRepository.findByBizNum(reqDto.getBizNum());
            if (!existingCustomers.isEmpty()) {
                throw new RuntimeException("이미 등록된 사업자번호입니다: " + reqDto.getBizNum());
            }
            
            // DTO에서 새로운 Entity 생성
            customer = reqDto.toEntity();
        }
        
        customer = customerRepository.save(customer);
        
        String action = isUpdate ? "수정" : "등록";
        log.info("거래처 {} 완료 - customerCode: {}, customerName: {}", action, customer.getCustomerCode(), customer.getCustomerName());
        
        // 저장 후 조인 조회를 통해 brandName, distCenterName 포함된 응답 생성
        try {
            return getCustomerWithJoin(customer.getCustomerCode());
        } catch (Exception e) {
            log.warn("조인 조회 실패, 기본 변환 사용 - customerCode: {}, error: {}", customer.getCustomerCode(), e.getMessage());
            // 조인 실패 시 기본 Entity 변환 사용 (brandName, distCenterName은 null)
            return CustomerRespDto.fromEntity(customer);
        }
    }

    /**
     * 거래처 조회 (brandName, distCenterName 조인 포함)
     */
    private CustomerRespDto getCustomerWithJoin(Integer customerCode) {
        
        // 조인 조회 (List<Object[]> 형태로 반환)
        List<Object[]> results = customerRepository.findCustomerWithJoinByCustomerCode(customerCode);
        
        if (results == null || results.isEmpty()) {
            throw new RuntimeException("저장된 거래처를 조회할 수 없습니다: " + customerCode);
        }
        
        Object[] result = results.get(0); // 첫 번째 결과 사용
        
        log.debug("조인 조회 결과 길이: {}, 첫 번째 값: {}", result.length, result[0]);
        
        return CustomerRespDto.builder()
                .customerCode(safeIntegerCast(result[0]))
                .hqCode(safeIntegerCast(result[1]))
                .customerName(safeStringCast(result[2]))
                .ownerName(safeStringCast(result[3]))
                .bizNum(safeStringCast(result[4]))
                .zipCode(safeStringCast(result[5]))
                .addr(safeStringCast(result[6]))
                .bizType(safeStringCast(result[7]))
                .bizSector(safeStringCast(result[8]))
                .email(safeStringCast(result[9]))
                .telNum(safeStringCast(result[10]))
                .mobileNum(safeStringCast(result[11]))
                .faxNum(safeStringCast(result[12]))
                .taxInvoiceYn(safeStringCast(result[13]))
                .taxInvoiceName(safeStringCast(result[14]))
                .regDt(safeStringCast(result[15]))
                .closeDt(safeStringCast(result[16]))
                .printNote(safeStringCast(result[17]))
                .bankName(safeStringCast(result[18]))
                .accountHolder(safeStringCast(result[19]))
                .accountNum(safeStringCast(result[20]))
                .distCenterCode(safeIntegerCast(result[21]))
                .brandCode(safeIntegerCast(result[22]))
                .deliveryWeekday(safeStringCast(result[23]))
                .depositTypeCode(safeIntegerCast(result[24]))
                .virtualAccount(safeStringCast(result[25]))
                .virtualBankName(safeStringCast(result[26]))
                .balanceAmt(safeIntegerCast(result[27]))
                .hqMemo(safeStringCast(result[28]))
                .creditLimit(safeIntegerCast(result[29]))
                .collectionDay(safeIntegerCast(result[30]))
                .orderBlockYn(safeIntegerCast(result[31]))
                .orderBlockReason(safeStringCast(result[32]))
                .orderBlockDt(safeStringCast(result[33]))
                .description(safeStringCast(result[34]))
                // LocalDateTime은 조인 조회에서 제외 (기본값 설정)
                .createdAt(null)
                .updatedAt(null)
                // 조인 필드들 (37, 38번 인덱스)
                .brandName(safeStringCast(result[37]))
                .distCenterName(safeStringCast(result[38]))
                .build();
    }
    
    /**
     * 안전한 Integer 캐스팅
     */
    private Integer safeIntegerCast(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
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
    
    /**
     * 거래처 다중 삭제
     */
    @Transactional
    public RespDto<CustomerBatchResult> deleteCustomers(CustomerDeleteReqDto reqDto) {
        
        log.info("거래처 다중 삭제 시작 - 총 {}건", reqDto.getCustomerCodes().size());
        
        List<Integer> successCodes = new ArrayList<>();
        List<CustomerBatchResult.CustomerErrorDto> failData = new ArrayList<>();
        
        for (Integer customerCode : reqDto.getCustomerCodes()) {
            try {
                // 개별 거래처 삭제 처리
                deleteSingleCustomer(customerCode);
                successCodes.add(customerCode);
                
                log.info("거래처 삭제 성공 - customerCode: {}", customerCode);
                
            } catch (Exception e) {
                log.error("거래처 삭제 실패 - customerCode: {}, 에러: {}", customerCode, e.getMessage());
                
                // 에러 시 거래처명 조회 시도
                String customerName = getCustomerNameSafely(customerCode);
                
                CustomerBatchResult.CustomerErrorDto errorDto = CustomerBatchResult.CustomerErrorDto.builder()
                        .customerCode(customerCode)
                        .customerName(customerName)
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성 (삭제는 successData 대신 성공 코드만)
        CustomerBatchResult result = CustomerBatchResult.builder()
                .totalCount(reqDto.getCustomerCodes().size())
                .successCount(successCodes.size())
                .failCount(failData.size())
                .successData(successCodes.stream()
                        .map(code -> CustomerRespDto.builder().customerCode(code).build())
                        .collect(Collectors.toList()))
                .failData(failData)
                .build();
        
        String message = String.format("거래처 삭제 완료 - 성공: %d건, 실패: %d건", 
                successCodes.size(), failData.size());
        
        log.info("거래처 다중 삭제 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getCustomerCodes().size(), successCodes.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 거래처 삭제 처리 (폐기일자 설정)
     */
    private void deleteSingleCustomer(Integer customerCode) {
        
        Customer customer = customerRepository.findByCustomerCode(customerCode);
        if (customer == null) {
            throw new RuntimeException("존재하지 않는 거래처입니다: " + customerCode);
        }
        
        // 이미 폐기된 거래처인지 확인
        if (customer.getCloseDt() != null) {
            throw new RuntimeException("이미 폐기된 거래처입니다.");
        }
        
        // 폐기일자를 현재일로 설정
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        customer.setCloseDt(currentDate);
        
        customerRepository.save(customer);
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