package com.inc.sh.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;

import jakarta.persistence.*;

/**
 * 공공데이터 분석 엔티티
 */
@Entity
@Table(name = "public_data_analysis",
       uniqueConstraints = @UniqueConstraint(columnNames = "adminDongCode"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicDataAnalysis {

    /**
     * 분석코드 (자동증가)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_code")
    private Long analysisCode;

    /**
     * 행정동코드
     */
    @Column(name = "admin_dong_code", nullable = false, unique = true)
    private Integer adminDongCode;

    /**
     * 행정동이름
     */
    @Column(name = "admin_dong_name", nullable = false, length = 100)
    private String adminDongName;

    // ===== 한식 관련 =====
    /**
     * 한식업소수
     */
    @Column(name = "korean_restaurant_count")
    @Builder.Default
    private Integer koreanRestaurantCount = 0;

    /**
     * 한식매출
     */
    @Column(name = "korean_sales")
    @Builder.Default
    private Long koreanSales = 0L;

    // ===== 중식 관련 =====
    /**
     * 중식업소수
     */
    @Column(name = "chinese_restaurant_count")
    @Builder.Default
    private Integer chineseRestaurantCount = 0;

    /**
     * 중식매출
     */
    @Column(name = "chinese_sales")
    @Builder.Default
    private Long chineseSales = 0L;

    // ===== 일식 관련 =====
    /**
     * 일식업소수
     */
    @Column(name = "japanese_restaurant_count")
    @Builder.Default
    private Integer japaneseRestaurantCount = 0;

    /**
     * 일식매출
     */
    @Column(name = "japanese_sales")
    @Builder.Default
    private Long japaneseSales = 0L;

    // ===== 서양식 관련 =====
    /**
     * 서양식업소수
     */
    @Column(name = "western_restaurant_count")
    @Builder.Default
    private Integer westernRestaurantCount = 0;

    /**
     * 서양식매출
     */
    @Column(name = "western_sales")
    @Builder.Default
    private Long westernSales = 0L;

    // ===== 동남아식 관련 =====
    /**
     * 동남아식업소수
     */
    @Column(name = "southeast_asian_restaurant_count")
    @Builder.Default
    private Integer southeastAsianRestaurantCount = 0;

    /**
     * 동남아식매출
     */
    @Column(name = "southeast_asian_sales")
    @Builder.Default
    private Long southeastAsianSales = 0L;

    // ===== 인구 관련 =====
    /**
     * 유동인구
     */
    @Column(name = "floating_population")
    @Builder.Default
    private Integer floatingPopulation = 0;

    /**
     * 주거인구
     */
    @Column(name = "residential_population")
    @Builder.Default
    private Integer residentialPopulation = 0;

    /**
     * 직장인구
     */
    @Column(name = "working_population")
    @Builder.Default
    private Integer workingPopulation = 0;

    /**
     * 세대수
     */
    @Column(name = "household_count")
    @Builder.Default
    private Integer householdCount = 0;

    // ===== 경제 관련 =====
    /**
     * 소득
     */
    @Column(name = "income")
    @Builder.Default
    private Long income = 0L;

    /**
     * 소비
     */
    @Column(name = "consumption")
    @Builder.Default
    private Long consumption = 0L;

    // ===== 메타데이터 =====
    /**
     * 생성일
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정일
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== 비즈니스 메서드 =====

    /**
     * 업종별 매출 데이터 업데이트
     */
    public void updateSalesData(String upjongCd, Integer restaurantCount, Long sales) {
        switch (upjongCd) {
            case "I20101": // 한식
                this.koreanRestaurantCount = restaurantCount != null ? restaurantCount : this.koreanRestaurantCount;
                this.koreanSales = sales != null ? sales : this.koreanSales;
                break;
            case "I20201": // 중식
                this.chineseRestaurantCount = restaurantCount != null ? restaurantCount : this.chineseRestaurantCount;
                this.chineseSales = sales != null ? sales : this.chineseSales;
                break;
            case "I20301": // 일식
                this.japaneseRestaurantCount = restaurantCount != null ? restaurantCount : this.japaneseRestaurantCount;
                this.japaneseSales = sales != null ? sales : this.japaneseSales;
                break;
            case "I20402": // 서양식
                this.westernRestaurantCount = restaurantCount != null ? restaurantCount : this.westernRestaurantCount;
                this.westernSales = sales != null ? sales : this.westernSales;
                break;
            case "I20501": // 동남아식
                this.southeastAsianRestaurantCount = restaurantCount != null ? restaurantCount : this.southeastAsianRestaurantCount;
                this.southeastAsianSales = sales != null ? sales : this.southeastAsianSales;
                break;
        }
    }

    /**
     * 업소수 업데이트 (업종별)
     */
    public void updateBusinessCount(String upjongCd, Integer count) {
        switch (upjongCd) {
            case "I201": // 한식
                this.koreanRestaurantCount = count != null ? count : this.koreanRestaurantCount;
                break;
            case "I202": // 중식
                this.chineseRestaurantCount = count != null ? count : this.chineseRestaurantCount;
                break;
            case "I203": // 일식
                this.japaneseRestaurantCount = count != null ? count : this.japaneseRestaurantCount;
                break;
            case "I204": // 서양식
                this.westernRestaurantCount = count != null ? count : this.westernRestaurantCount;
                break;
            case "I205": // 동남아식
                this.southeastAsianRestaurantCount = count != null ? count : this.southeastAsianRestaurantCount;
                break;
        }
    }

    /**
     * 소득/소비 데이터 업데이트
     */
    public void updateIncomeAndConsumption(Long income, Long consumption) {
        this.income = income != null ? income : this.income;
        this.consumption = consumption != null ? consumption : this.consumption;
    }

    /**
     * 인구 데이터 업데이트
     */
    public void updatePopulationData(Integer floatingPopulation, 
                                   Integer residentialPopulation,
                                   Integer workingPopulation, 
                                   Integer householdCount) {
        this.floatingPopulation = floatingPopulation != null ? floatingPopulation : this.floatingPopulation;
        this.residentialPopulation = residentialPopulation != null ? residentialPopulation : this.residentialPopulation;
        this.workingPopulation = workingPopulation != null ? workingPopulation : this.workingPopulation;
        this.householdCount = householdCount != null ? householdCount : this.householdCount;
    }

    /**
     * 매출 정보 업데이트 (전체)
     */
    public void updateSalesData(Integer koreanCount, Long koreanSales,
                               Integer chineseCount, Long chineseSales,
                               Integer japaneseCount, Long japaneseSales,
                               Integer westernCount, Long westernSales,
                               Integer southeastAsianCount, Long southeastAsianSales) {
        this.koreanRestaurantCount = koreanCount != null ? koreanCount : this.koreanRestaurantCount;
        this.koreanSales = koreanSales != null ? koreanSales : this.koreanSales;
        this.chineseRestaurantCount = chineseCount != null ? chineseCount : this.chineseRestaurantCount;
        this.chineseSales = chineseSales != null ? chineseSales : this.chineseSales;
        this.japaneseRestaurantCount = japaneseCount != null ? japaneseCount : this.japaneseRestaurantCount;
        this.japaneseSales = japaneseSales != null ? japaneseSales : this.japaneseSales;
        this.westernRestaurantCount = westernCount != null ? westernCount : this.westernRestaurantCount;
        this.westernSales = westernSales != null ? westernSales : this.westernSales;
        this.southeastAsianRestaurantCount = southeastAsianCount != null ? southeastAsianCount : this.southeastAsianRestaurantCount;
        this.southeastAsianSales = southeastAsianSales != null ? southeastAsianSales : this.southeastAsianSales;
    }

    /**
     * 전체 매출 계산
     */
    public Long getTotalSales() {
        return (koreanSales != null ? koreanSales : 0L) +
               (chineseSales != null ? chineseSales : 0L) +
               (japaneseSales != null ? japaneseSales : 0L) +
               (westernSales != null ? westernSales : 0L) +
               (southeastAsianSales != null ? southeastAsianSales : 0L);
    }

    /**
     * 전체 업소수 계산
     */
    public Integer getTotalRestaurantCount() {
        return (koreanRestaurantCount != null ? koreanRestaurantCount : 0) +
               (chineseRestaurantCount != null ? chineseRestaurantCount : 0) +
               (japaneseRestaurantCount != null ? japaneseRestaurantCount : 0) +
               (westernRestaurantCount != null ? westernRestaurantCount : 0) +
               (southeastAsianRestaurantCount != null ? southeastAsianRestaurantCount : 0);
    }

    /**
     * 전체 인구수 계산
     */
    public Integer getTotalPopulation() {
        return (floatingPopulation != null ? floatingPopulation : 0) +
               (residentialPopulation != null ? residentialPopulation : 0) +
               (workingPopulation != null ? workingPopulation : 0);
    }
}