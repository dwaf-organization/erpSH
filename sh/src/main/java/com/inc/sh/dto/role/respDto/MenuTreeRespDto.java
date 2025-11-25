package com.inc.sh.dto.role.respDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuTreeRespDto {
    
    private Integer menuCode;           // 메뉴코드
    private String menuName;            // 메뉴이름
    private Integer parentId;           // 부모메뉴코드
    private Integer menuLevel;          // 메뉴레벨
    private Integer menuOrder;          // 정렬순서
    private String menuPath;            // 라우팅경로
    private Boolean isChecked;          // 체크 여부 (해당 권한에 지정됐는지)
    private List<MenuTreeRespDto> children; // 하위 메뉴들
}