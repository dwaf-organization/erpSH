package com.inc.sh.dto.deliveryHoliday.reqDto;

import com.inc.sh.entity.DeliveryHoliday;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasicHolidayReqDto {
    
    @NotBlank(message = "날짜는 필수입니다")
    private String holidayDt;       // 날짜 (YYYY-MM-DD)
    
    @NotBlank(message = "요일은 필수입니다")
    private String weekday;         // 요일
    
    @NotNull(message = "브랜드코드는 필수입니다")
    private Integer brandCode;      // 브랜드코드
    
    @NotNull(message = "본사코드는 필수입니다")
    private Integer hqCode;         // 본사코드
    
    @NotBlank(message = "휴일명은 필수입니다")
    private String holidayName;     // 휴일명
    
    /**
     * DTO to Entity 변환 (기본휴일)
     */
    public DeliveryHoliday toEntity() {
        return DeliveryHoliday.builder()
                .brandCode(this.brandCode)
                .hqCode(this.hqCode)
                .holidayDt(this.holidayDt)
                .weekday(this.weekday)
                .holidayName(this.holidayName)
                .description("기본휴일")
                .build();
    }
}