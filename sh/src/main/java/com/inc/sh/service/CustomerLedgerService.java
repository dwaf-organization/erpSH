package com.inc.sh.service;

import com.inc.sh.dto.customerLedger.reqDto.CustomerLedgerSummarySearchDto;
import com.inc.sh.dto.customerLedger.respDto.CustomerLedgerDailyRespDto;
import com.inc.sh.dto.customerLedger.respDto.CustomerLedgerDetailRespDto;
import com.inc.sh.dto.customerLedger.respDto.CustomerLedgerSummaryRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.repository.OrderRepository;
import com.inc.sh.repository.ReturnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerLedgerService {
    
    private final OrderRepository orderRepository;
    private final ReturnRepository returnRepository;
    
    /**
     * 거래처별원장 집계 조회
     * 주문상태에 따라 주문/배송/반품 데이터를 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<CustomerLedgerSummaryRespDto>> getCustomerLedgerSummary(CustomerLedgerSummarySearchDto searchDto) {
        try {
            log.info("거래처별원장 집계 조회 시작 - 조건: {}", searchDto);
            
            List<CustomerLedgerSummaryRespDto> resultList = new ArrayList<>();
            
            // 주문상태에 따른 데이터 조회
            if ("전체".equals(searchDto.getOrderStatus()) || searchDto.getOrderStatus() == null) {
                // 전체 조회: 주문 + 배송 + 반품
                resultList.addAll(getOrderSummary(searchDto));
                resultList.addAll(getDeliverySummary(searchDto));
                resultList.addAll(getReturnSummary(searchDto));
                
            } else if ("주문".equals(searchDto.getOrderStatus())) {
                // 주문만 조회
                resultList.addAll(getOrderSummary(searchDto));
                
            } else if ("배송".equals(searchDto.getOrderStatus())) {
                // 배송만 조회
                resultList.addAll(getDeliverySummary(searchDto));
                
            } else if ("반품".equals(searchDto.getOrderStatus())) {
                // 반품만 조회
                resultList.addAll(getReturnSummary(searchDto));
            }
            
            log.info("거래처별원장 집계 조회 완료 - 조회 건수: {}", resultList.size());
            return RespDto.success("거래처별원장 집계 조회 성공", resultList);
            
        } catch (Exception e) {
            log.error("거래처별원장 집계 조회 중 오류 발생", e);
            return RespDto.fail("거래처별원장 집계 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 주문 집계 조회 (배송요청만)
     */
    private List<CustomerLedgerSummaryRespDto> getOrderSummary(CustomerLedgerSummarySearchDto searchDto) {
        try {
            List<Object[]> orderResults = orderRepository.findCustomerLedgerOrderSummary(
                    searchDto.getDeliveryRequestDtStart(),
                    searchDto.getDeliveryRequestDtEnd(),
                    searchDto.getItemCode(),
                    searchDto.getBrandCode(),
                    searchDto.getCustomerCode()
            );
            
            List<CustomerLedgerSummaryRespDto> orderList = new ArrayList<>();
            for (Object[] result : orderResults) {
                CustomerLedgerSummaryRespDto dto = CustomerLedgerSummaryRespDto.builder()
                        .customerCode((Integer) result[0])
                        .customerName((String) result[1])
                        .brandName((String) result[2])
                        .telNum((String) result[3])
                        .orderType((String) result[4])
                        .totalQty(((Number) result[5]).intValue())
                        .taxFreeAmt(((Number) result[6]).intValue())
                        .taxableAmt(((Number) result[7]).intValue())
                        .supplyAmt(((Number) result[8]).intValue())
                        .vatAmt(((Number) result[9]).intValue())
                        .totalAmt(((Number) result[10]).intValue())
                        .deliveryQty(((Number) result[11]).intValue())
                        .deliverySupplyAmt(((Number) result[12]).intValue())
                        .deliveryVatAmt(((Number) result[13]).intValue())
                        .deliveryTotalAmt(((Number) result[14]).intValue())
                        .build();
                orderList.add(dto);
            }
            
            log.info("주문 집계 조회 완료 - 건수: {}", orderList.size());
            return orderList;
            
        } catch (Exception e) {
            log.error("주문 집계 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 배송 집계 조회 (배송중 + 배송완료)
     */
    private List<CustomerLedgerSummaryRespDto> getDeliverySummary(CustomerLedgerSummarySearchDto searchDto) {
        try {
            List<Object[]> deliveryResults = orderRepository.findCustomerLedgerDeliverySummary(
                    searchDto.getDeliveryRequestDtStart(),
                    searchDto.getDeliveryRequestDtEnd(),
                    searchDto.getItemCode(),
                    searchDto.getBrandCode(),
                    searchDto.getCustomerCode()
            );
            
            List<CustomerLedgerSummaryRespDto> deliveryList = new ArrayList<>();
            for (Object[] result : deliveryResults) {
                CustomerLedgerSummaryRespDto dto = CustomerLedgerSummaryRespDto.builder()
                        .customerCode((Integer) result[0])
                        .customerName((String) result[1])
                        .brandName((String) result[2])
                        .telNum((String) result[3])
                        .orderType((String) result[4])
                        .totalQty(((Number) result[5]).intValue())
                        .taxFreeAmt(((Number) result[6]).intValue())
                        .taxableAmt(((Number) result[7]).intValue())
                        .supplyAmt(((Number) result[8]).intValue())
                        .vatAmt(((Number) result[9]).intValue())
                        .totalAmt(((Number) result[10]).intValue())
                        .deliveryQty(((Number) result[11]).intValue())
                        .deliverySupplyAmt(((Number) result[12]).intValue())
                        .deliveryVatAmt(((Number) result[13]).intValue())
                        .deliveryTotalAmt(((Number) result[14]).intValue())
                        .build();
                deliveryList.add(dto);
            }
            
            log.info("배송 집계 조회 완료 - 건수: {}", deliveryList.size());
            return deliveryList;
            
        } catch (Exception e) {
            log.error("배송 집계 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 반품 집계 조회
     */
    private List<CustomerLedgerSummaryRespDto> getReturnSummary(CustomerLedgerSummarySearchDto searchDto) {
        try {
            List<Object[]> returnResults = returnRepository.findCustomerLedgerReturnSummary(
                    searchDto.getDeliveryRequestDtStart(),
                    searchDto.getDeliveryRequestDtEnd(),
                    searchDto.getItemCode(),
                    searchDto.getBrandCode(),
                    searchDto.getCustomerCode()
            );
            
            List<CustomerLedgerSummaryRespDto> returnList = new ArrayList<>();
            for (Object[] result : returnResults) {
                CustomerLedgerSummaryRespDto dto = CustomerLedgerSummaryRespDto.builder()
                        .customerCode((Integer) result[0])
                        .customerName((String) result[1])
                        .brandName((String) result[2])
                        .telNum((String) result[3])
                        .orderType((String) result[4])
                        .totalQty(((Number) result[5]).intValue())
                        .taxFreeAmt(((Number) result[6]).intValue())
                        .taxableAmt(((Number) result[7]).intValue())
                        .supplyAmt(((Number) result[8]).intValue())
                        .vatAmt(((Number) result[9]).intValue())
                        .totalAmt(((Number) result[10]).intValue())
                        .deliveryQty(((Number) result[11]).intValue())
                        .deliverySupplyAmt(((Number) result[12]).intValue())
                        .deliveryVatAmt(((Number) result[13]).intValue())
                        .deliveryTotalAmt(((Number) result[14]).intValue())
                        .build();
                returnList.add(dto);
            }
            
            log.info("반품 집계 조회 완료 - 건수: {}", returnList.size());
            return returnList;
            
        } catch (Exception e) {
            log.error("반품 집계 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 거래처별원장 세부 조회
     * 거래처별 + 품목별로 그룹화하여 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<CustomerLedgerDetailRespDto>> getCustomerLedgerDetail(CustomerLedgerSummarySearchDto searchDto) {
        try {
            log.info("거래처별원장 세부 조회 시작 - 조건: {}", searchDto);
            
            List<CustomerLedgerDetailRespDto> resultList = new ArrayList<>();
            
            // 주문상태에 따른 데이터 조회
            if ("전체".equals(searchDto.getOrderStatus()) || searchDto.getOrderStatus() == null) {
                // 전체 조회: 주문 + 배송 + 반품
                resultList.addAll(getOrderDetail(searchDto));
                resultList.addAll(getDeliveryDetail(searchDto));
                resultList.addAll(getReturnDetail(searchDto));
                
            } else if ("주문".equals(searchDto.getOrderStatus())) {
                // 주문만 조회
                resultList.addAll(getOrderDetail(searchDto));
                
            } else if ("배송".equals(searchDto.getOrderStatus())) {
                // 배송만 조회
                resultList.addAll(getDeliveryDetail(searchDto));
                
            } else if ("반품".equals(searchDto.getOrderStatus())) {
                // 반품만 조회
                resultList.addAll(getReturnDetail(searchDto));
            }
            
            log.info("거래처별원장 세부 조회 완료 - 조회 건수: {}", resultList.size());
            return RespDto.success("거래처별원장 세부 조회 성공", resultList);
            
        } catch (Exception e) {
            log.error("거래처별원장 세부 조회 중 오류 발생", e);
            return RespDto.fail("거래처별원장 세부 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 주문 세부 조회 (거래처별 + 품목별)
     */
    private List<CustomerLedgerDetailRespDto> getOrderDetail(CustomerLedgerSummarySearchDto searchDto) {
        try {
            List<Object[]> orderResults = orderRepository.findCustomerLedgerOrderDetail(
                    searchDto.getDeliveryRequestDtStart(),
                    searchDto.getDeliveryRequestDtEnd(),
                    searchDto.getItemCode(),
                    searchDto.getBrandCode(),
                    searchDto.getCustomerCode()
            );
            
            List<CustomerLedgerDetailRespDto> orderList = new ArrayList<>();
            for (Object[] result : orderResults) {
                CustomerLedgerDetailRespDto dto = CustomerLedgerDetailRespDto.builder()
                        .customerCode((Integer) result[0])
                        .customerName((String) result[1])
                        .brandName((String) result[2])
                        .telNum((String) result[3])
                        .itemCode((Integer) result[4])
                        .itemName((String) result[5])
                        .specification((String) result[6])
                        .unit((String) result[7])
                        .orderType((String) result[8])
                        .totalQty(((Number) result[9]).intValue())
                        .supplyAmt(((Number) result[10]).intValue())
                        .vatAmt(((Number) result[11]).intValue())
                        .totalAmt(((Number) result[12]).intValue())
                        .deliveryQty(((Number) result[13]).intValue())
                        .deliverySupplyAmt(((Number) result[14]).intValue())
                        .deliveryVatAmt(((Number) result[15]).intValue())
                        .deliveryTotalAmt(((Number) result[16]).intValue())
                        .build();
                orderList.add(dto);
            }
            
            log.info("주문 세부 조회 완료 - 건수: {}", orderList.size());
            return orderList;
            
        } catch (Exception e) {
            log.error("주문 세부 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 배송 세부 조회 (거래처별 + 품목별)
     */
    private List<CustomerLedgerDetailRespDto> getDeliveryDetail(CustomerLedgerSummarySearchDto searchDto) {
        try {
            List<Object[]> deliveryResults = orderRepository.findCustomerLedgerDeliveryDetail(
                    searchDto.getDeliveryRequestDtStart(),
                    searchDto.getDeliveryRequestDtEnd(),
                    searchDto.getItemCode(),
                    searchDto.getBrandCode(),
                    searchDto.getCustomerCode()
            );
            
            List<CustomerLedgerDetailRespDto> deliveryList = new ArrayList<>();
            for (Object[] result : deliveryResults) {
                CustomerLedgerDetailRespDto dto = CustomerLedgerDetailRespDto.builder()
                        .customerCode((Integer) result[0])
                        .customerName((String) result[1])
                        .brandName((String) result[2])
                        .telNum((String) result[3])
                        .itemCode((Integer) result[4])
                        .itemName((String) result[5])
                        .specification((String) result[6])
                        .unit((String) result[7])
                        .orderType((String) result[8])
                        .totalQty(((Number) result[9]).intValue())
                        .supplyAmt(((Number) result[10]).intValue())
                        .vatAmt(((Number) result[11]).intValue())
                        .totalAmt(((Number) result[12]).intValue())
                        .deliveryQty(((Number) result[13]).intValue())
                        .deliverySupplyAmt(((Number) result[14]).intValue())
                        .deliveryVatAmt(((Number) result[15]).intValue())
                        .deliveryTotalAmt(((Number) result[16]).intValue())
                        .build();
                deliveryList.add(dto);
            }
            
            log.info("배송 세부 조회 완료 - 건수: {}", deliveryList.size());
            return deliveryList;
            
        } catch (Exception e) {
            log.error("배송 세부 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 반품 세부 조회 (거래처별 + 품목별)
     */
    private List<CustomerLedgerDetailRespDto> getReturnDetail(CustomerLedgerSummarySearchDto searchDto) {
        try {
            List<Object[]> returnResults = returnRepository.findCustomerLedgerReturnDetail(
                    searchDto.getDeliveryRequestDtStart(),
                    searchDto.getDeliveryRequestDtEnd(),
                    searchDto.getItemCode(),
                    searchDto.getBrandCode(),
                    searchDto.getCustomerCode()
            );
            
            List<CustomerLedgerDetailRespDto> returnList = new ArrayList<>();
            for (Object[] result : returnResults) {
                CustomerLedgerDetailRespDto dto = CustomerLedgerDetailRespDto.builder()
                        .customerCode((Integer) result[0])
                        .customerName((String) result[1])
                        .brandName((String) result[2])
                        .telNum((String) result[3])
                        .itemCode((Integer) result[4])
                        .itemName((String) result[5])
                        .specification((String) result[6])
                        .unit((String) result[7])
                        .orderType((String) result[8])
                        .totalQty(((Number) result[9]).intValue())
                        .supplyAmt(((Number) result[10]).intValue())
                        .vatAmt(((Number) result[11]).intValue())
                        .totalAmt(((Number) result[12]).intValue())
                        .deliveryQty(((Number) result[13]).intValue())
                        .deliverySupplyAmt(((Number) result[14]).intValue())
                        .deliveryVatAmt(((Number) result[15]).intValue())
                        .deliveryTotalAmt(((Number) result[16]).intValue())
                        .build();
                returnList.add(dto);
            }
            
            log.info("반품 세부 조회 완료 - 건수: {}", returnList.size());
            return returnList;
            
        } catch (Exception e) {
            log.error("반품 세부 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 거래처별원장 일자별 조회
     * 일자별 + 거래처별 + 품목별로 그룹화하여 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<CustomerLedgerDailyRespDto>> getCustomerLedgerDaily(CustomerLedgerSummarySearchDto searchDto) {
        try {
            log.info("거래처별원장 일자별 조회 시작 - 조건: {}", searchDto);
            
            List<CustomerLedgerDailyRespDto> resultList = new ArrayList<>();
            
            // 주문상태에 따른 데이터 조회
            if ("전체".equals(searchDto.getOrderStatus()) || searchDto.getOrderStatus() == null) {
                // 전체 조회: 주문 + 배송 + 반품
                resultList.addAll(getOrderDaily(searchDto));
                resultList.addAll(getDeliveryDaily(searchDto));
                resultList.addAll(getReturnDaily(searchDto));
                
            } else if ("주문".equals(searchDto.getOrderStatus())) {
                // 주문만 조회
                resultList.addAll(getOrderDaily(searchDto));
                
            } else if ("배송".equals(searchDto.getOrderStatus())) {
                // 배송만 조회
                resultList.addAll(getDeliveryDaily(searchDto));
                
            } else if ("반품".equals(searchDto.getOrderStatus())) {
                // 반품만 조회
                resultList.addAll(getReturnDaily(searchDto));
            }
            
            log.info("거래처별원장 일자별 조회 완료 - 조회 건수: {}", resultList.size());
            return RespDto.success("거래처별원장 일자별 조회 성공", resultList);
            
        } catch (Exception e) {
            log.error("거래처별원장 일자별 조회 중 오류 발생", e);
            return RespDto.fail("거래처별원장 일자별 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 주문 일자별 조회 (일자별 + 거래처별 + 품목별)
     */
    private List<CustomerLedgerDailyRespDto> getOrderDaily(CustomerLedgerSummarySearchDto searchDto) {
        try {
            List<Object[]> orderResults = orderRepository.findCustomerLedgerOrderDaily(
                    searchDto.getDeliveryRequestDtStart(),
                    searchDto.getDeliveryRequestDtEnd(),
                    searchDto.getItemCode(),
                    searchDto.getBrandCode(),
                    searchDto.getCustomerCode()
            );
            
            List<CustomerLedgerDailyRespDto> orderList = new ArrayList<>();
            for (Object[] result : orderResults) {
                CustomerLedgerDailyRespDto dto = CustomerLedgerDailyRespDto.builder()
                        .orderDate((String) result[0])
                        .customerCode((Integer) result[1])
                        .customerName((String) result[2])
                        .brandName((String) result[3])
                        .telNum((String) result[4])
                        .itemCode((Integer) result[5])
                        .itemName((String) result[6])
                        .specification((String) result[7])
                        .unit((String) result[8])
                        .orderType((String) result[9])
                        .totalQty(((Number) result[10]).intValue())
                        .supplyAmt(((Number) result[11]).intValue())
                        .vatAmt(((Number) result[12]).intValue())
                        .totalAmt(((Number) result[13]).intValue())
                        .deliveryQty(((Number) result[14]).intValue())
                        .deliverySupplyAmt(((Number) result[15]).intValue())
                        .deliveryVatAmt(((Number) result[16]).intValue())
                        .deliveryTotalAmt(((Number) result[17]).intValue())
                        .build();
                orderList.add(dto);
            }
            
            log.info("주문 일자별 조회 완료 - 건수: {}", orderList.size());
            return orderList;
            
        } catch (Exception e) {
            log.error("주문 일자별 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 배송 일자별 조회 (일자별 + 거래처별 + 품목별)
     */
    private List<CustomerLedgerDailyRespDto> getDeliveryDaily(CustomerLedgerSummarySearchDto searchDto) {
        try {
            List<Object[]> deliveryResults = orderRepository.findCustomerLedgerDeliveryDaily(
                    searchDto.getDeliveryRequestDtStart(),
                    searchDto.getDeliveryRequestDtEnd(),
                    searchDto.getItemCode(),
                    searchDto.getBrandCode(),
                    searchDto.getCustomerCode()
            );
            
            List<CustomerLedgerDailyRespDto> deliveryList = new ArrayList<>();
            for (Object[] result : deliveryResults) {
                CustomerLedgerDailyRespDto dto = CustomerLedgerDailyRespDto.builder()
                        .orderDate((String) result[0])
                        .customerCode((Integer) result[1])
                        .customerName((String) result[2])
                        .brandName((String) result[3])
                        .telNum((String) result[4])
                        .itemCode((Integer) result[5])
                        .itemName((String) result[6])
                        .specification((String) result[7])
                        .unit((String) result[8])
                        .orderType((String) result[9])
                        .totalQty(((Number) result[10]).intValue())
                        .supplyAmt(((Number) result[11]).intValue())
                        .vatAmt(((Number) result[12]).intValue())
                        .totalAmt(((Number) result[13]).intValue())
                        .deliveryQty(((Number) result[14]).intValue())
                        .deliverySupplyAmt(((Number) result[15]).intValue())
                        .deliveryVatAmt(((Number) result[16]).intValue())
                        .deliveryTotalAmt(((Number) result[17]).intValue())
                        .build();
                deliveryList.add(dto);
            }
            
            log.info("배송 일자별 조회 완료 - 건수: {}", deliveryList.size());
            return deliveryList;
            
        } catch (Exception e) {
            log.error("배송 일자별 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 반품 일자별 조회 (일자별 + 거래처별 + 품목별)
     */
    private List<CustomerLedgerDailyRespDto> getReturnDaily(CustomerLedgerSummarySearchDto searchDto) {
        try {
            List<Object[]> returnResults = returnRepository.findCustomerLedgerReturnDaily(
                    searchDto.getDeliveryRequestDtStart(),
                    searchDto.getDeliveryRequestDtEnd(),
                    searchDto.getItemCode(),
                    searchDto.getBrandCode(),
                    searchDto.getCustomerCode()
            );
            
            List<CustomerLedgerDailyRespDto> returnList = new ArrayList<>();
            for (Object[] result : returnResults) {
                CustomerLedgerDailyRespDto dto = CustomerLedgerDailyRespDto.builder()
                        .orderDate((String) result[0])
                        .customerCode((Integer) result[1])
                        .customerName((String) result[2])
                        .brandName((String) result[3])
                        .telNum((String) result[4])
                        .itemCode((Integer) result[5])
                        .itemName((String) result[6])
                        .specification((String) result[7])
                        .unit((String) result[8])
                        .orderType((String) result[9])
                        .totalQty(((Number) result[10]).intValue())
                        .supplyAmt(((Number) result[11]).intValue())
                        .vatAmt(((Number) result[12]).intValue())
                        .totalAmt(((Number) result[13]).intValue())
                        .deliveryQty(((Number) result[14]).intValue())
                        .deliverySupplyAmt(((Number) result[15]).intValue())
                        .deliveryVatAmt(((Number) result[16]).intValue())
                        .deliveryTotalAmt(((Number) result[17]).intValue())
                        .build();
                returnList.add(dto);
            }
            
            log.info("반품 일자별 조회 완료 - 건수: {}", returnList.size());
            return returnList;
            
        } catch (Exception e) {
            log.error("반품 일자별 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }
}