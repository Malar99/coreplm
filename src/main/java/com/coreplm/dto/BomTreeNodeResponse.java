package com.coreplm.dto;

import java.math.BigDecimal;
import java.util.List;

public record BomTreeNodeResponse(
        Long revisionId,
        String itemNumber,
        String itemName,
        String revisionLabel,
        BigDecimal quantity,
        List<BomTreeNodeResponse> children
) {}