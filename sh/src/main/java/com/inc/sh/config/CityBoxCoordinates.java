package com.inc.sh.config;

import java.util.List;
import java.util.Map;

/**
 * 광역시별 7km x 5km 박스 좌표 설정
 */
public class CityBoxCoordinates {

    /**
     * 광역시별 박스 좌표 맵
     */
    public static final Map<String, List<BoxCoordinate>> CITY_BOXES = Map.of(
        // 서울특별시 (42개 박스)
        "서울특별시", List.of(
            new BoxCoordinate(179122, 186122, 434907, 439907), new BoxCoordinate(179122, 186122, 439907, 444907),
            new BoxCoordinate(179122, 186122, 444907, 449907), new BoxCoordinate(179122, 186122, 449907, 454907),
            new BoxCoordinate(179122, 186122, 454907, 459907), new BoxCoordinate(179122, 186122, 459907, 464907),
            new BoxCoordinate(179122, 186122, 464907, 466911), new BoxCoordinate(186122, 193122, 434907, 439907),
            new BoxCoordinate(186122, 193122, 439907, 444907), new BoxCoordinate(186122, 193122, 444907, 449907),
            new BoxCoordinate(186122, 193122, 449907, 454907), new BoxCoordinate(186122, 193122, 454907, 459907),
            new BoxCoordinate(186122, 193122, 459907, 464907), new BoxCoordinate(186122, 193122, 464907, 466911),
            new BoxCoordinate(193122, 200122, 434907, 439907), new BoxCoordinate(193122, 200122, 439907, 444907),
            new BoxCoordinate(193122, 200122, 444907, 449907), new BoxCoordinate(193122, 200122, 449907, 454907),
            new BoxCoordinate(193122, 200122, 454907, 459907), new BoxCoordinate(193122, 200122, 459907, 464907),
            new BoxCoordinate(193122, 200122, 464907, 466911), new BoxCoordinate(200122, 207122, 434907, 439907),
            new BoxCoordinate(200122, 207122, 439907, 444907), new BoxCoordinate(200122, 207122, 444907, 449907),
            new BoxCoordinate(200122, 207122, 449907, 454907), new BoxCoordinate(200122, 207122, 454907, 459907),
            new BoxCoordinate(200122, 207122, 459907, 464907), new BoxCoordinate(200122, 207122, 464907, 466911),
            new BoxCoordinate(207122, 214122, 434907, 439907), new BoxCoordinate(207122, 214122, 439907, 444907),
            new BoxCoordinate(207122, 214122, 444907, 449907), new BoxCoordinate(207122, 214122, 449907, 454907),
            new BoxCoordinate(207122, 214122, 454907, 459907), new BoxCoordinate(207122, 214122, 459907, 464907),
            new BoxCoordinate(207122, 214122, 464907, 466911), new BoxCoordinate(214122, 215971, 434907, 439907),
            new BoxCoordinate(214122, 215971, 439907, 444907), new BoxCoordinate(214122, 215971, 444907, 449907),
            new BoxCoordinate(214122, 215971, 449907, 454907), new BoxCoordinate(214122, 215971, 454907, 459907),
            new BoxCoordinate(214122, 215971, 459907, 464907), new BoxCoordinate(214122, 215971, 464907, 466911)
        ),
        
        // 부산광역시 (54개 박스)
        "부산광역시", List.of(
            new BoxCoordinate(369764, 376764, 170895, 175895), new BoxCoordinate(369764, 376764, 175895, 180895),
            new BoxCoordinate(369764, 376764, 180895, 185895), new BoxCoordinate(369764, 376764, 185895, 190895),
            new BoxCoordinate(369764, 376764, 190895, 195895), new BoxCoordinate(369764, 376764, 195895, 200895),
            new BoxCoordinate(369764, 376764, 200895, 205895), new BoxCoordinate(369764, 376764, 205895, 210895),
            new BoxCoordinate(369764, 376764, 210895, 211641), new BoxCoordinate(376764, 383764, 170895, 175895),
            new BoxCoordinate(376764, 383764, 175895, 180895), new BoxCoordinate(376764, 383764, 180895, 185895),
            new BoxCoordinate(376764, 383764, 185895, 190895), new BoxCoordinate(376764, 383764, 190895, 195895),
            new BoxCoordinate(376764, 383764, 195895, 200895), new BoxCoordinate(376764, 383764, 200895, 205895),
            new BoxCoordinate(376764, 383764, 205895, 210895), new BoxCoordinate(376764, 383764, 210895, 211641),
            new BoxCoordinate(383764, 390764, 170895, 175895), new BoxCoordinate(383764, 390764, 175895, 180895),
            new BoxCoordinate(383764, 390764, 180895, 185895), new BoxCoordinate(383764, 390764, 185895, 190895),
            new BoxCoordinate(383764, 390764, 190895, 195895), new BoxCoordinate(383764, 390764, 195895, 200895),
            new BoxCoordinate(383764, 390764, 200895, 205895), new BoxCoordinate(383764, 390764, 205895, 210895),
            new BoxCoordinate(383764, 390764, 210895, 211641), new BoxCoordinate(390764, 397764, 170895, 175895),
            new BoxCoordinate(390764, 397764, 175895, 180895), new BoxCoordinate(390764, 397764, 180895, 185895),
            new BoxCoordinate(390764, 397764, 185895, 190895), new BoxCoordinate(390764, 397764, 190895, 195895),
            new BoxCoordinate(390764, 397764, 195895, 200895), new BoxCoordinate(390764, 397764, 200895, 205895),
            new BoxCoordinate(390764, 397764, 205895, 210895), new BoxCoordinate(390764, 397764, 210895, 211641),
            new BoxCoordinate(397764, 404764, 170895, 175895), new BoxCoordinate(397764, 404764, 175895, 180895),
            new BoxCoordinate(397764, 404764, 180895, 185895), new BoxCoordinate(397764, 404764, 185895, 190895),
            new BoxCoordinate(397764, 404764, 190895, 195895), new BoxCoordinate(397764, 404764, 195895, 200895),
            new BoxCoordinate(397764, 404764, 200895, 205895), new BoxCoordinate(397764, 404764, 205895, 210895),
            new BoxCoordinate(397764, 404764, 210895, 211641), new BoxCoordinate(404764, 407188, 170895, 175895),
            new BoxCoordinate(404764, 407188, 175895, 180895), new BoxCoordinate(404764, 407188, 180895, 185895),
            new BoxCoordinate(404764, 407188, 185895, 190895), new BoxCoordinate(404764, 407188, 190895, 195895),
            new BoxCoordinate(404764, 407188, 195895, 200895), new BoxCoordinate(404764, 407188, 200895, 205895),
            new BoxCoordinate(404764, 407188, 205895, 210895), new BoxCoordinate(404764, 407188, 210895, 211641)
        ),
        
        // 대구광역시 (24개 박스)
        "대구광역시", List.of(
            new BoxCoordinate(333012, 340012, 247973, 252973), new BoxCoordinate(333012, 340012, 252973, 257973),
            new BoxCoordinate(333012, 340012, 257973, 262973), new BoxCoordinate(333012, 340012, 262973, 267973),
            new BoxCoordinate(333012, 340012, 267973, 272973), new BoxCoordinate(333012, 340012, 272973, 273961),
            new BoxCoordinate(340012, 347012, 247973, 252973), new BoxCoordinate(340012, 347012, 252973, 257973),
            new BoxCoordinate(340012, 347012, 257973, 262973), new BoxCoordinate(340012, 347012, 262973, 267973),
            new BoxCoordinate(340012, 347012, 267973, 272973), new BoxCoordinate(340012, 347012, 272973, 273961),
            new BoxCoordinate(347012, 354012, 247973, 252973), new BoxCoordinate(347012, 354012, 252973, 257973),
            new BoxCoordinate(347012, 354012, 257973, 262973), new BoxCoordinate(347012, 354012, 262973, 267973),
            new BoxCoordinate(347012, 354012, 267973, 272973), new BoxCoordinate(347012, 354012, 272973, 273961),
            new BoxCoordinate(354012, 360600, 247973, 252973), new BoxCoordinate(354012, 360600, 252973, 257973),
            new BoxCoordinate(354012, 360600, 257973, 262973), new BoxCoordinate(354012, 360600, 262973, 267973),
            new BoxCoordinate(354012, 360600, 267973, 272973), new BoxCoordinate(354012, 360600, 272973, 273961)
        ),
        
        // 인천광역시 (63개 박스)
        "인천광역시", List.of(
            new BoxCoordinate(149436, 156436, 418020, 423020), new BoxCoordinate(149436, 156436, 423020, 428020),
            new BoxCoordinate(149436, 156436, 428020, 433020), new BoxCoordinate(149436, 156436, 433020, 438020),
            new BoxCoordinate(149436, 156436, 438020, 443020), new BoxCoordinate(149436, 156436, 443020, 448020),
            new BoxCoordinate(149436, 156436, 448020, 453020), new BoxCoordinate(149436, 156436, 453020, 458020),
            new BoxCoordinate(149436, 156436, 458020, 461153), new BoxCoordinate(156436, 163436, 418020, 423020),
            new BoxCoordinate(156436, 163436, 423020, 428020), new BoxCoordinate(156436, 163436, 428020, 433020),
            new BoxCoordinate(156436, 163436, 433020, 438020), new BoxCoordinate(156436, 163436, 438020, 443020),
            new BoxCoordinate(156436, 163436, 443020, 448020), new BoxCoordinate(156436, 163436, 448020, 453020),
            new BoxCoordinate(156436, 163436, 453020, 458020), new BoxCoordinate(156436, 163436, 458020, 461153),
            new BoxCoordinate(163436, 170436, 418020, 423020), new BoxCoordinate(163436, 170436, 423020, 428020),
            new BoxCoordinate(163436, 170436, 428020, 433020), new BoxCoordinate(163436, 170436, 433020, 438020),
            new BoxCoordinate(163436, 170436, 438020, 443020), new BoxCoordinate(163436, 170436, 443020, 448020),
            new BoxCoordinate(163436, 170436, 448020, 453020), new BoxCoordinate(163436, 170436, 453020, 458020),
            new BoxCoordinate(163436, 170436, 458020, 461153), new BoxCoordinate(170436, 177436, 418020, 423020),
            new BoxCoordinate(170436, 177436, 423020, 428020), new BoxCoordinate(170436, 177436, 428020, 433020),
            new BoxCoordinate(170436, 177436, 433020, 438020), new BoxCoordinate(170436, 177436, 438020, 443020),
            new BoxCoordinate(170436, 177436, 443020, 448020), new BoxCoordinate(170436, 177436, 448020, 453020),
            new BoxCoordinate(170436, 177436, 453020, 458020), new BoxCoordinate(170436, 177436, 458020, 461153),
            new BoxCoordinate(177436, 184436, 418020, 423020), new BoxCoordinate(177436, 184436, 423020, 428020),
            new BoxCoordinate(177436, 184436, 428020, 433020), new BoxCoordinate(177436, 184436, 433020, 438020),
            new BoxCoordinate(177436, 184436, 438020, 443020), new BoxCoordinate(177436, 184436, 443020, 448020),
            new BoxCoordinate(177436, 184436, 448020, 453020), new BoxCoordinate(177436, 184436, 453020, 458020),
            new BoxCoordinate(177436, 184436, 458020, 461153), new BoxCoordinate(184436, 191436, 418020, 423020),
            new BoxCoordinate(184436, 191436, 423020, 428020), new BoxCoordinate(184436, 191436, 428020, 433020),
            new BoxCoordinate(184436, 191436, 433020, 438020), new BoxCoordinate(184436, 191436, 438020, 443020),
            new BoxCoordinate(184436, 191436, 443020, 448020), new BoxCoordinate(184436, 191436, 448020, 453020),
            new BoxCoordinate(184436, 191436, 453020, 458020), new BoxCoordinate(184436, 191436, 458020, 461153),
            new BoxCoordinate(191436, 198235, 418020, 423020), new BoxCoordinate(191436, 198235, 423020, 428020),
            new BoxCoordinate(191436, 198235, 428020, 433020), new BoxCoordinate(191436, 198235, 433020, 438020),
            new BoxCoordinate(191436, 198235, 438020, 443020), new BoxCoordinate(191436, 198235, 443020, 448020),
            new BoxCoordinate(191436, 198235, 448020, 453020), new BoxCoordinate(191436, 198235, 453020, 458020),
            new BoxCoordinate(191436, 198235, 458020, 461153)
        ),
        
        // 광주광역시 (24개 박스) 
        "광주광역시", List.of(
            new BoxCoordinate(181760, 188760, 175988, 180988), new BoxCoordinate(181760, 188760, 180988, 185988),
            new BoxCoordinate(181760, 188760, 185988, 190988), new BoxCoordinate(181760, 188760, 190988, 194849),
            new BoxCoordinate(188760, 195760, 175988, 180988), new BoxCoordinate(188760, 195760, 180988, 185988),
            new BoxCoordinate(188760, 195760, 185988, 190988), new BoxCoordinate(188760, 195760, 190988, 194849),
            new BoxCoordinate(195760, 202760, 175988, 180988), new BoxCoordinate(195760, 202760, 180988, 185988),
            new BoxCoordinate(195760, 202760, 185988, 190988), new BoxCoordinate(195760, 202760, 190988, 194849),
            new BoxCoordinate(202760, 209760, 175988, 180988), new BoxCoordinate(202760, 209760, 180988, 185988),
            new BoxCoordinate(202760, 209760, 185988, 190988), new BoxCoordinate(202760, 209760, 190988, 194849),
            new BoxCoordinate(209760, 216760, 175988, 180988), new BoxCoordinate(209760, 216760, 180988, 185988),
            new BoxCoordinate(209760, 216760, 185988, 190988), new BoxCoordinate(209760, 216760, 190988, 194849),
            new BoxCoordinate(216760, 218202, 175988, 180988), new BoxCoordinate(216760, 218202, 180988, 185988),
            new BoxCoordinate(216760, 218202, 185988, 190988), new BoxCoordinate(216760, 218202, 190988, 194849)
        ),
        
        // 대전광역시 (15개 박스)
        "대전광역시", List.of(
            new BoxCoordinate(228764, 235764, 304722, 309722), new BoxCoordinate(228764, 235764, 309722, 314722),
            new BoxCoordinate(228764, 235764, 314722, 319722), new BoxCoordinate(228764, 235764, 319722, 324722),
            new BoxCoordinate(228764, 235764, 324722, 325875), new BoxCoordinate(235764, 242764, 304722, 309722),
            new BoxCoordinate(235764, 242764, 309722, 314722), new BoxCoordinate(235764, 242764, 314722, 319722),
            new BoxCoordinate(235764, 242764, 319722, 324722), new BoxCoordinate(235764, 242764, 324722, 325875),
            new BoxCoordinate(242764, 244836, 304722, 309722), new BoxCoordinate(242764, 244836, 309722, 314722),
            new BoxCoordinate(242764, 244836, 314722, 319722), new BoxCoordinate(242764, 244836, 319722, 324722),
            new BoxCoordinate(242764, 244836, 324722, 325875)
        ),
        
        // 울산광역시 (20개 박스)
        "울산광역시", List.of(
            new BoxCoordinate(395204, 402204, 219144, 224144), new BoxCoordinate(395204, 402204, 224144, 229144),
            new BoxCoordinate(395204, 402204, 229144, 234144), new BoxCoordinate(395204, 402204, 234144, 239144),
            new BoxCoordinate(395204, 402204, 239144, 241976), new BoxCoordinate(402204, 409204, 219144, 224144),
            new BoxCoordinate(402204, 409204, 224144, 229144), new BoxCoordinate(402204, 409204, 229144, 234144),
            new BoxCoordinate(402204, 409204, 234144, 239144), new BoxCoordinate(402204, 409204, 239144, 241976),
            new BoxCoordinate(409204, 416204, 219144, 224144), new BoxCoordinate(409204, 416204, 224144, 229144),
            new BoxCoordinate(409204, 416204, 229144, 234144), new BoxCoordinate(409204, 416204, 234144, 239144),
            new BoxCoordinate(409204, 416204, 239144, 241976), new BoxCoordinate(416204, 421895, 219144, 224144),
            new BoxCoordinate(416204, 421895, 224144, 229144), new BoxCoordinate(416204, 421895, 229144, 234144),
            new BoxCoordinate(416204, 421895, 234144, 239144), new BoxCoordinate(416204, 421895, 239144, 241976)
        )
    );

    /**
     * 업종 코드 (매출 API용)
     */
    public static final String[] UPJONG_CODES_SALES = {
        "I20101", // 한식
        "I20201", // 중식  
        "I20301", // 일식
        "I20402", // 서양식
        "I20501"  // 동남아식
    };

    /**
     * 업종 코드 (업소수 API용)
     */
    public static final String[] UPJONG_CODES_BUSINESS = {
        "I201", // 한식
        "I202", // 중식
        "I203", // 일식  
        "I204", // 서양식
        "I205"  // 동남아식
    };

    /**
     * 매출 배수 (업종별)
     */
    public static final Map<String, Double> SALES_MULTIPLIER = Map.of(
        "I20101", 3.0,   // 한식 x3
        "I20201", 2.0,   // 중식 x2
        "I20301", 3.5,   // 일식 x3.5
        "I20402", 3.5,   // 서양식 x3.5
        "I20501", 1.5    // 동남아식 x1.5
    );

    /**
     * 지역코드 배열 (업소수/인구 API용)
     */
    public static final String[] AREA_CODES = {
        // 서울특별시 (25개)
        "11010", "11020", "11030", "11040", "11050", "11060", "11070", "11080", "11090", "11100",
        "11110", "11120", "11130", "11140", "11150", "11160", "11170", "11180", "11190", "11200",
        "11210", "11220", "11230", "11240", "11250",
        // 부산광역시 (16개)
        "26110", "26120", "26130", "26140", "26150", "26160", "26170", "26180", "26350", "26380",
        "26410", "26440", "26470", "26500", "26530", "26710",
        // 대구광역시 (8개)
        "27110", "27140", "27170", "27200", "27230", "27260", "27290", "27710",
        // 인천광역시 (10개)
        "28110", "28140", "28177", "28185", "28200", "28237", "28245", "28260", "28710", "28720",
        // 광주광역시 (5개)  
        "29110", "29140", "29155", "29170", "29200",
        // 대전광역시 (5개)
        "30110", "30140", "30170", "30200", "30230",
        // 울산광역시 (5개)
        "31110", "31140", "31170", "31200", "31710",
        // 세종특별자치시 (1개)
        "36110"
    };

    /**
     * 박스 좌표 클래스
     */
    public static class BoxCoordinate {
        public final int minXAxis;
        public final int maxXAxis; 
        public final int minYAxis;
        public final int maxYAxis;

        public BoxCoordinate(int minXAxis, int maxXAxis, int minYAxis, int maxYAxis) {
            this.minXAxis = minXAxis;
            this.maxXAxis = maxXAxis;
            this.minYAxis = minYAxis;
            this.maxYAxis = maxYAxis;
        }

        /**
         * URL 파라미터 형식으로 변환
         */
        public String toUrlParams() {
            return String.format("minXAxis=%d&maxXAxis=%d&minYAxis=%d&maxYAxis=%d", 
                               minXAxis, maxXAxis, minYAxis, maxYAxis);
        }

        /**
         * 크기 계산 (km²)
         */
        public double getAreaKm2() {
            double widthKm = (maxXAxis - minXAxis) / 1000.0;
            double heightKm = (maxYAxis - minYAxis) / 1000.0;
            return widthKm * heightKm;
        }

        @Override
        public String toString() {
            return String.format("BoxCoordinate{minX=%d, maxX=%d, minY=%d, maxY=%d}", 
                               minXAxis, maxXAxis, minYAxis, maxYAxis);
        }
    }

    /**
     * 특정 도시의 박스 좌표 조회
     */
    public static List<BoxCoordinate> getBoxes(String cityName) {
        return CITY_BOXES.getOrDefault(cityName, List.of());
    }

    /**
     * 전체 박스 개수 조회
     */
    public static int getTotalBoxCount() {
        return CITY_BOXES.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * 전체 API 호출 예상 횟수 계산
     */
    public static class ApiCallEstimate {
        public final int salesApiCalls;      // 매출 API (박스 × 5개 업종)
        public final int incomeApiCalls;     // 소득/소비 API (박스 × 2개)
        public final int businessApiCalls;   // 업소수/인구 API (지역 × 업종 + 지역 × 2개)
        public final int totalApiCalls;

        public ApiCallEstimate() {
            int totalBoxes = getTotalBoxCount();
            int totalAreas = AREA_CODES.length;
            
            this.salesApiCalls = totalBoxes * UPJONG_CODES_SALES.length;
            this.incomeApiCalls = totalBoxes * 2; // 소득 + 소비
            this.businessApiCalls = totalAreas * UPJONG_CODES_BUSINESS.length + totalAreas * 2; // 업소수 + 세대수 + 직장인구
            this.totalApiCalls = salesApiCalls + incomeApiCalls + businessApiCalls;
        }
    }

    public static ApiCallEstimate getApiCallEstimate() {
        return new ApiCallEstimate();
    }
}