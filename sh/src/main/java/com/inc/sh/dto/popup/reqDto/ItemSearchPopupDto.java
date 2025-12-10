package com.inc.sh.dto.popup.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemSearchPopupDto {
    
	private Integer hqCode;			//본사코드
    private String item;        // 품목코드 (부분일치)
    private Integer categoryCode;   // 분류코드 (완전일치)
    private Integer priceType;       // 단가유형 (2=납품단가, 1=납품싯가)
}