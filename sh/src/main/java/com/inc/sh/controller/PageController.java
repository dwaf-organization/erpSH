package com.inc.sh.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class PageController {
    
    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }
    
    /**
     * 거래명세표 엑셀 테스트 페이지 (Thymeleaf)
     * GET /excel-test → templates/excel-test.html
     */
    @GetMapping("/excel")
    public String excelTestPage() {
        return "excel";
    }
    
}