package com.coreplm.service;

import com.coreplm.dto.ChangeOrderResponse;

public interface ChangeOrderService {

    ChangeOrderResponse generateFromApprovedEcr(Long changeRequestId, String requestingUsername);

    ChangeOrderResponse getByChangeRequestId(Long changeRequestId);

    void closeChangeOrder(Long changeOrderId);
}