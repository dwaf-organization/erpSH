package com.inc.sh.dto.warehouseTransfer.reqDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseTransferProcessDto {
    
    private List<TransferItemDto> items; // 이송품목 리스트 (모든 이송 정보 포함)
}