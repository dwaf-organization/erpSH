package com.inc.sh.service;

import com.inc.sh.dto.virtualAccount.reqDto.VirtualAccountReqDto;
import com.inc.sh.dto.virtualAccount.reqDto.VirtualAccountSearchDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.virtualAccount.respDto.VirtualAccountRespDto;
import com.inc.sh.dto.virtualAccount.respDto.VirtualAccountSaveRespDto;
import com.inc.sh.dto.virtualAccount.respDto.VirtualAccountDeleteRespDto;
import com.inc.sh.entity.VirtualAccount;
import com.inc.sh.repository.VirtualAccountRepository;
import com.inc.sh.repository.CustomerRepository;
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
public class VirtualAccountService {
    
    private final VirtualAccountRepository virtualAccountRepository;
    private final CustomerRepository customerRepository;
    
    /**
     * 가상계좌 조회 (검색 조건)
     * @param searchDto 검색 조건
     * @return 조회된 가상계좌 목록
     */
    @Transactional(readOnly = true)
    public RespDto<List<VirtualAccountRespDto>> getVirtualAccountList(VirtualAccountSearchDto searchDto) {
        try {
            log.info("가상계좌 목록 조회 시작 - linkedCustomerCode: {}, virtualAccountStatus: {}, closeDtYn: {}", 
                    searchDto.getLinkedCustomerCode(), searchDto.getVirtualAccountStatus(), searchDto.getCloseDtYn());
            
            List<VirtualAccount> virtualAccounts = virtualAccountRepository.findBySearchConditions(
                    searchDto.getLinkedCustomerCode(),
                    searchDto.getVirtualAccountStatus(),
                    searchDto.getCloseDtYn()
            );
            
            List<VirtualAccountRespDto> responseList = virtualAccounts.stream()
                    .map(VirtualAccountRespDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("가상계좌 목록 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("가상계좌 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("가상계좌 목록 조회 중 오류 발생", e);
            return RespDto.fail("가상계좌 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 가상계좌 상세 조회
     * @param virtualAccountCode 가상계좌코드
     * @return 가상계좌 상세 정보
     */
    @Transactional(readOnly = true)
    public RespDto<VirtualAccountRespDto> getVirtualAccount(Integer virtualAccountCode) {
        try {
            log.info("가상계좌 상세 조회 시작 - virtualAccountCode: {}", virtualAccountCode);
            
            VirtualAccount virtualAccount = virtualAccountRepository.findByVirtualAccountCode(virtualAccountCode);
            if (virtualAccount == null) {
                log.warn("가상계좌를 찾을 수 없습니다 - virtualAccountCode: {}", virtualAccountCode);
                return RespDto.fail("가상계좌를 찾을 수 없습니다.");
            }
            
            VirtualAccountRespDto responseDto = VirtualAccountRespDto.fromEntity(virtualAccount);
            
            log.info("가상계좌 상세 조회 완료 - virtualAccountCode: {}, virtualAccountNum: {}", 
                    virtualAccountCode, virtualAccount.getVirtualAccountNum());
            return RespDto.success("가상계좌 조회 성공", responseDto);
            
        } catch (Exception e) {
            log.error("가상계좌 상세 조회 중 오류 발생 - virtualAccountCode: {}", virtualAccountCode, e);
            return RespDto.fail("가상계좌 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 가상계좌 저장 (신규/수정)
     * @param request 가상계좌 정보
     * @return 저장된 가상계좌 코드
     */
    public RespDto<VirtualAccountSaveRespDto> saveVirtualAccount(VirtualAccountReqDto request) {
        try {
            // 1. 거래처 중복 체크 (linkedCustomerCode가 null이 아닌 경우에만)
            if (request.getLinkedCustomerCode() != null) {
                VirtualAccount duplicateCheck = virtualAccountRepository.findFirstByLinkedCustomerCode(request.getLinkedCustomerCode());
                
                if (duplicateCheck != null) {
                    // 수정인 경우 본인 제외, 신규인 경우 바로 fail
                    if (request.getVirtualAccountCode() == null || 
                        !duplicateCheck.getVirtualAccountCode().equals(request.getVirtualAccountCode())) {
                        log.warn("해당 거래처는 이미 다른 가상계좌와 연결되어 있습니다 - customerCode: {}", request.getLinkedCustomerCode());
                        return RespDto.fail("해당 거래처는 가상계좌와 연결되어 있습니다.");
                    }
                }
            }
            
            // 2. 미사용 상태 처리 (무조건 null 처리 먼저)
            Integer originalCustomerCode = request.getLinkedCustomerCode(); // 원본 거래처코드 백업
            
            if ("미사용".equals(request.getVirtualAccountStatus())) {
                // 미사용인 경우 무조건 linkedCustomerCode를 null로 설정
                request.setLinkedCustomerCode(null);
                
                // 원본에 거래처코드가 있었다면 fail 처리
                if (originalCustomerCode != null) {
                    log.warn("미사용 상태에서는 거래처를 연결할 수 없습니다 - customerCode: {}", originalCustomerCode);
                    return RespDto.fail("상태값을 사용으로 변경해야 거래처가 저장됩니다.");
                }
            }
            
            VirtualAccount savedVirtualAccount;
            String action;
            Integer previousCustomerCode = null; // 이전 거래처코드 (미사용 처리 시 Customer 테이블 정리용)
            
            if (request.getVirtualAccountCode() == null) {
                // 신규 등록
                log.info("가상계좌 신규 등록 시작 - virtualAccountNum: {}", request.getVirtualAccountNum());
                
                // 가상계좌번호 중복 확인
                if (virtualAccountRepository.existsByVirtualAccountNum(request.getVirtualAccountNum())) {
                    log.warn("이미 존재하는 가상계좌번호입니다 - virtualAccountNum: {}", request.getVirtualAccountNum());
                    return RespDto.fail("이미 존재하는 가상계좌번호입니다.");
                }
                
                VirtualAccount virtualAccount = request.toEntity();
                savedVirtualAccount = virtualAccountRepository.save(virtualAccount);
                action = "등록";
                
            } else {
                // 수정
                log.info("가상계좌 수정 시작 - virtualAccountCode: {}, virtualAccountNum: {}", 
                        request.getVirtualAccountCode(), request.getVirtualAccountNum());
                
                VirtualAccount existingVirtualAccount = virtualAccountRepository.findByVirtualAccountCode(request.getVirtualAccountCode());
                if (existingVirtualAccount == null) {
                    log.warn("수정할 가상계좌를 찾을 수 없습니다 - virtualAccountCode: {}", request.getVirtualAccountCode());
                    return RespDto.fail("수정할 가상계좌를 찾을 수 없습니다.");
                }
                
                // 이전 거래처코드 저장 (미사용 처리 시 Customer 테이블 정리용)
                previousCustomerCode = existingVirtualAccount.getLinkedCustomerCode();
                
                // 가상계좌번호 중복 확인 (자신 제외)
                VirtualAccount duplicateCheck = virtualAccountRepository.findByVirtualAccountNum(request.getVirtualAccountNum());
                if (duplicateCheck != null && !duplicateCheck.getVirtualAccountCode().equals(request.getVirtualAccountCode())) {
                    log.warn("이미 존재하는 가상계좌번호입니다 - virtualAccountNum: {}", request.getVirtualAccountNum());
                    return RespDto.fail("이미 존재하는 가상계좌번호입니다.");
                }
                
                request.updateEntity(existingVirtualAccount);
                savedVirtualAccount = virtualAccountRepository.save(existingVirtualAccount);
                action = "수정";
            }
            
            // 3. Customer 테이블 처리
            if ("미사용".equals(request.getVirtualAccountStatus())) {
                // 미사용인 경우: 이전에 연결된 거래처가 있다면 null 처리
                if (previousCustomerCode != null) {
                    customerRepository.updateVirtualAccountInfo(previousCustomerCode, null, null, null);
                    log.info("거래처 가상계좌 정보 null 처리 - customerCode: {}", previousCustomerCode);
                }
            } else {
                // 사용인 경우: 거래처 테이블 업데이트 (linkedCustomerCode가 null이 아닌 경우)
                updateCustomerVirtualAccount(savedVirtualAccount);
            }
            
            // 4. 간소화된 응답 생성 (거래처코드 포함)
            VirtualAccountSaveRespDto responseDto = VirtualAccountSaveRespDto.builder()
                    .virtualAccountCode(savedVirtualAccount.getVirtualAccountCode())
                    .linkedCustomerCode(savedVirtualAccount.getLinkedCustomerCode()) // 거래처코드 추가
                    .build();
            
            log.info("가상계좌 {} 완료 - virtualAccountCode: {}, virtualAccountNum: {}, linkedCustomerCode: {}", 
                    action, savedVirtualAccount.getVirtualAccountCode(), savedVirtualAccount.getVirtualAccountNum(),
                    savedVirtualAccount.getLinkedCustomerCode());
            
            return RespDto.success("가상계좌가 성공적으로 " + action + "되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("가상계좌 저장 중 오류 발생 - virtualAccountCode: {}", request.getVirtualAccountCode(), e);
            return RespDto.fail("가상계좌 저장 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 거래처 테이블의 가상계좌 정보 업데이트
     * @param virtualAccount 가상계좌 정보
     */
    private void updateCustomerVirtualAccount(VirtualAccount virtualAccount) {
        try {
            if (virtualAccount.getLinkedCustomerCode() != null) {
                log.info("거래처 가상계좌 정보 업데이트 시작 - customerCode: {}, virtualAccountNum: {}", 
                        virtualAccount.getLinkedCustomerCode(), virtualAccount.getVirtualAccountNum());
                
                customerRepository.updateVirtualAccountInfo(
                        virtualAccount.getLinkedCustomerCode(),
                        virtualAccount.getVirtualAccountCode(),
                        virtualAccount.getVirtualAccountNum(),
                        virtualAccount.getBankName()
                );
                
                log.info("거래처 가상계좌 정보 업데이트 완료 - customerCode: {}", virtualAccount.getLinkedCustomerCode());
            }
        } catch (Exception e) {
            log.error("거래처 가상계좌 정보 업데이트 중 오류 발생 - customerCode: {}", 
                    virtualAccount.getLinkedCustomerCode(), e);
            // 가상계좌 저장은 성공했으므로 예외를 다시 던지지 않음
        }
    }
    
    /**
     * 가상계좌 삭제 (하드 삭제)
     * @param virtualAccountCode 가상계좌코드
     * @return 삭제 결과
     */
    public RespDto<VirtualAccountDeleteRespDto> deleteVirtualAccount(Integer virtualAccountCode) {
        try {
            log.info("가상계좌 삭제 시작 - virtualAccountCode: {}", virtualAccountCode);
            
            VirtualAccount virtualAccount = virtualAccountRepository.findByVirtualAccountCode(virtualAccountCode);
            if (virtualAccount == null) {
                log.warn("삭제할 가상계좌를 찾을 수 없습니다 - virtualAccountCode: {}", virtualAccountCode);
                return RespDto.fail("삭제할 가상계좌를 찾을 수 없습니다.");
            }
            
            // 연결된 거래처 확인
            Integer linkedCustomerCode = virtualAccount.getLinkedCustomerCode();
            
            if (linkedCustomerCode != null) {
                // 연결된 거래처가 있으면 삭제 중단
                log.warn("연결된 거래처가 있어 삭제할 수 없습니다 - virtualAccountCode: {}, linkedCustomerCode: {}", 
                        virtualAccountCode, linkedCustomerCode);
                
                VirtualAccountDeleteRespDto responseDto = VirtualAccountDeleteRespDto.builder()
                        .virtualAccountCode(virtualAccountCode)
                        .linkedCustomerCode(linkedCustomerCode)
                        .message("거래처코드 " + linkedCustomerCode + "와 연결되어 있어 삭제할 수 없습니다.")
                        .build();
                
                return RespDto.fail("연결된 거래처가 있어 삭제할 수 없습니다. (거래처코드: " + linkedCustomerCode + ")");
            }
            
            // 연결된 거래처가 없으면 하드 삭제 진행
            virtualAccountRepository.delete(virtualAccount);
            
            // 삭제 성공 응답 생성
            VirtualAccountDeleteRespDto responseDto = VirtualAccountDeleteRespDto.builder()
                    .virtualAccountCode(virtualAccountCode)
                    .linkedCustomerCode(null)
                    .message("가상계좌가 성공적으로 삭제되었습니다.")
                    .build();
            
            log.info("가상계좌 삭제 완료 - virtualAccountCode: {}", virtualAccountCode);
            
            return RespDto.success("가상계좌가 성공적으로 삭제되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("가상계좌 삭제 중 오류 발생 - virtualAccountCode: {}", virtualAccountCode, e);
            return RespDto.fail("가상계좌 삭제 중 오류가 발생했습니다.");
        }
    }
}