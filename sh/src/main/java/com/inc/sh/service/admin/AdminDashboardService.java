package com.inc.sh.service.admin;

import com.inc.sh.dto.dashboard.respDto.AdminDashboardOverviewRespDto;
import com.inc.sh.dto.dashboard.respDto.AdminHeadquartersStatsRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.repository.HeadquarterRepository;
import com.inc.sh.repository.UserRepository;
import com.inc.sh.repository.BrandRepository;
import com.inc.sh.repository.CustomerRepository;
import com.inc.sh.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService {
    
    private final HeadquarterRepository headquarterRepository;      // 관리자
    private final UserRepository userRepository;                   // 관리자
    private final BrandRepository brandRepository;         // 관리자
    private final CustomerRepository customerRepository;           // 관리자
    private final ItemRepository itemRepository;                   // 관리자

    /**
     * 관리자 - 대시보드 현황 조회
     */
    @Transactional(readOnly = true)
    public RespDto<AdminDashboardOverviewRespDto> getDashboardOverview() {
        try {
            log.info("관리자 대시보드 현황 조회 시작");
            
            // 각 테이블 카운트 조회
            Long totalHeadquarters = headquarterRepository.count();
            Long totalUsers = userRepository.count();
            Long totalBrands = brandRepository.count();
            Long totalCustomers = customerRepository.count();
            Long totalItems = itemRepository.count();
            
            // 응답 DTO 생성
            AdminDashboardOverviewRespDto overviewData = AdminDashboardOverviewRespDto.builder()
                    .totalHeadquarters(totalHeadquarters)
                    .totalUsers(totalUsers)
                    .totalBrands(totalBrands)
                    .totalCustomers(totalCustomers)
                    .totalItems(totalItems)
                    .build();
            
            log.info("관리자 대시보드 현황 조회 완료 - 본사:{}개, 사용자:{}명, 브랜드:{}개, 거래처:{}개, 품목:{}개", 
                    totalHeadquarters, totalUsers, totalBrands, totalCustomers, totalItems);
            
            return RespDto.success("대시보드 현황 조회 성공", overviewData);
            
        } catch (Exception e) {
            log.error("관리자 대시보드 현황 조회 중 오류 발생", e);
            return RespDto.fail("대시보드 현황 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 관리자 - 본사별 현황 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<AdminHeadquartersStatsRespDto>> getHeadquartersStats() {
        try {
            log.info("관리자 본사별 현황 조회 시작");
            
            List<Object[]> results = headquarterRepository.findHeadquartersStatsForAdmin();
            
            List<AdminHeadquartersStatsRespDto> statsList = results.stream()
                    .map(result -> {
                        // Timestamp를 LocalDateTime으로 안전하게 변환
                        LocalDateTime createdAt = null;
                        if (result[7] != null) {
                            if (result[7] instanceof java.sql.Timestamp) {
                                createdAt = ((java.sql.Timestamp) result[7]).toLocalDateTime();  // ✅ 수정
                            } else if (result[7] instanceof LocalDateTime) {
                                createdAt = (LocalDateTime) result[7];
                            }
                        }
                        
                        return AdminHeadquartersStatsRespDto.builder()
                                .hqCode((Integer) result[0])
                                .hqName((String) result[1])
                                .businessNumber((String) result[2])
                                .customerCount(((Number) result[3]).longValue())
                                .itemCount(((Number) result[4]).longValue())
                                .brandCount(((Number) result[5]).longValue())
                                .userCount(((Number) result[6]).longValue())
                                .createdAt(createdAt)  // ✅ 안전하게 설정
                                .build();
                    })
                    .collect(Collectors.toList());
            
            log.info("관리자 본사별 현황 조회 완료 - 조회된 본사 수: {}", statsList.size());
            
            return RespDto.success("본사별 현황 조회 성공", statsList);
            
        } catch (Exception e) {
            log.error("관리자 본사별 현황 조회 중 오류 발생", e);
            return RespDto.fail("본사별 현황 조회 중 오류가 발생했습니다.");
        }
    }
}