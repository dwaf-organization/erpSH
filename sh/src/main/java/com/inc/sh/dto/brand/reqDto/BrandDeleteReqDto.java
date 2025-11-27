package com.inc.sh.dto.brand.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandDeleteReqDto {
    
    private List<Integer> brandCodes;  // 삭제할 브랜드코드 리스트
}