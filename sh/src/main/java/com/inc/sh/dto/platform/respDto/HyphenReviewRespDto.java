package com.inc.sh.dto.platform.respDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HyphenReviewRespDto {
    
    private Common common;
    private Data data;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Common {
        private String userTrNo;
        private String hyphenTrNo;
        private String errYn;
        private String errCd;
        private String errMsg;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private String errMsg;
        private String errYn;
        private List<StoreData> storeList;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreData {
        private String storeName;
        private String storeId;
        private String allStar;
        private String reviewCnt;
        private String commentCnt;
        
        // 요기요 전용 필드
        private String tasteStar;
        private String amountStar;
        private String deliveryStar;
        
        private List<ReviewData> reviewList;
        private String ownerReply;
        private String ownerReplyDt;
        private String ownerReplyTm;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewData {
        private String orderReviewId;
        private String reviewId;
        private String reviewDt;
        private String reviewTm;
        private String allStar;
        private String jumun;
        private String comment;
        
        // 쿠팡이츠 전용 필드
        private String abbrOrderId;
        private String orderType;
        
        // 요기요 전용 필드
        private String tasteStar;
        private String amountStar;
        private String deliveryStar;
        
        // 배민 전용 필드
        private DeliveryReview deliveryReview;
        private List<Object> menuReview;
        
        // 사장댓글
        private String ownerReplyId;
        private String ownerReply;
        private String ownerReplyDt;
        private String ownerReplyTm;
        
        // 리뷰 이미지
        private List<ReviewImage> reviewImgList;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryReview {
        private String review;
        private String reason;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewImage {
        private String reviewImg;
    }
}