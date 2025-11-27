package com.inc.sh.repository;

import com.inc.sh.entity.Headquarter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HeadquarterRepository extends JpaRepository<Headquarter, Integer> {
    
    /**
     * 본사코드로 본사 정보 조회
     */
    Headquarter findByHqCode(Integer hqCode);
    
    /**
     * 본사코드 존재 여부 확인
     */
    boolean existsByHqCode(Integer hqCode);
    
    /**
     * 사업자번호 중복 확인
     */
    boolean existsByBizNum(String bizNum);
    
    /**
     * 사업자번호로 본사 조회 (Optional)
     */
    java.util.Optional<Headquarter> findByBizNum(String bizNum);
    
    /**
     * 본사접속코드 중복 확인
     */
    boolean existsByHqAccessCode(String hqAccessCode);
    
    /**
     * 본사접속코드로 본사 조회
     */
    Headquarter findByHqAccessCode(@Param("hqAccessCode") String hqAccessCode);
}