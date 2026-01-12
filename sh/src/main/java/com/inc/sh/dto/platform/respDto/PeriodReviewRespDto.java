package com.inc.sh.dto.platform.respDto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodReviewRespDto {
    
    /**
     * 리뷰 상세 목록
     */
    private List<ReviewDetail> reviewDetails;
    
    /**
     * 총 리뷰 개수
     */
    private Integer totalCount;
    
    /**
     * 리뷰 상세 내부 클래스
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewDetail {
        
        /**
         * 매장명 (거래처명)
         */
        private String storeName;
        
        /**
         * 작성일자
         */
        private LocalDate reviewDate;
        
        /**
         * 별점
         */
        private Integer rating;
        
        /**
         * 주문내용
         */
        private String orderMenu;
        
        /**
         * 리뷰내용
         */
        private String content;
        
        /**
         * 리뷰이미지 URL 리스트
         */
        private List<String> reviewImages;
        
        /**
         * 사장님댓글 (없으면 null)
         */
        private String ownerReplyContent;
        
        /**
         * 앱이름 (플랫폼)
         */
        private String platform;
        
        /**
         * 리뷰 고유코드 (내부용)
         */
        private Integer reviewPlatformCode;
    }
}