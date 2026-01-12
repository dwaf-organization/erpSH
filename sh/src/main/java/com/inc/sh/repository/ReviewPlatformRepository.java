package com.inc.sh.repository;

import com.inc.sh.entity.ReviewPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewPlatformRepository extends JpaRepository<ReviewPlatform, Integer> {

    /**
     * 리뷰 중복 검사 (플랫폼 + orderReviewId 기준)
     */
    @Query(value = 
        "SELECT COUNT(*) " +
        "FROM review_platform " +
        "WHERE platform = :platform " +
        "AND JSON_EXTRACT(raw_data, '$.orderReviewId') = :orderReviewId",
        nativeQuery = true)
    int countByPlatformAndOrderReviewId(
        @Param("platform") String platform,
        @Param("orderReviewId") String orderReviewId
    );

    /**
     * 리뷰 저장 (Native Query)
     */
    @Modifying
    @Transactional
    @Query(value = 
        "INSERT INTO review_platform (" +
        "store_platform_code, customer_code, brand_code, platform, " +
        "review_date, review_time, rating, order_menu, content, " +
        "owner_reply_content, owner_reply_date, owner_reply_time, " +
        "has_images, raw_data, description" +
        ") VALUES (" +
        ":storePlatformCode, :customerCode, :brandCode, :platform, " +
        ":reviewDate, :reviewTime, :rating, :orderMenu, :content, " +
        ":ownerReplyContent, :ownerReplyDate, :ownerReplyTime, " +
        ":hasImages, :rawData, :description" +
        ")",
        nativeQuery = true)
    void insertReview(
        @Param("storePlatformCode") Integer storePlatformCode,
        @Param("customerCode") Integer customerCode,
        @Param("brandCode") Integer brandCode,
        @Param("platform") String platform,
        @Param("reviewDate") String reviewDate,
        @Param("reviewTime") String reviewTime,
        @Param("rating") Integer rating,
        @Param("orderMenu") String orderMenu,
        @Param("content") String content,
        @Param("ownerReplyContent") String ownerReplyContent,
        @Param("ownerReplyDate") String ownerReplyDate,
        @Param("ownerReplyTime") String ownerReplyTime,
        @Param("hasImages") String hasImages,
        @Param("rawData") String rawData,
        @Param("description") String description
    );

    /**
     * 마지막 INSERT된 review_platform_code 조회
     */
    @Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
    Integer getLastInsertId();

    /**
     * 리뷰 이미지 저장 (Native Query)
     */
    @Modifying
    @Transactional
    @Query(value = 
        "INSERT INTO review_image_platform (" +
        "review_platform_code, store_platform_code, customer_code, brand_code, " +
        "seq, image_url, description" +
        ") VALUES (" +
        ":reviewPlatformCode, :storePlatformCode, :customerCode, :brandCode, " +
        ":seq, :imageUrl, :description" +
        ")",
        nativeQuery = true)
    void insertReviewImage(
        @Param("reviewPlatformCode") Integer reviewPlatformCode,
        @Param("storePlatformCode") Integer storePlatformCode,
        @Param("customerCode") Integer customerCode,
        @Param("brandCode") Integer brandCode,
        @Param("seq") Integer seq,
        @Param("imageUrl") String imageUrl,
        @Param("description") String description
    );
}