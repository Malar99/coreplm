package com.coreplm.service;

import com.coreplm.dto.ChangeRequestCreateRequest;
import com.coreplm.dto.ChangeRequestResponse;
import com.coreplm.dto.ChangeRequestReviewRequest;

import java.util.List;

public interface ChangeRequestService {

    ChangeRequestResponse createChangeRequest(ChangeRequestCreateRequest request, String requestingUsername);

    ChangeRequestResponse submitChangeRequest(Long id, String requestingUsername);

    ChangeRequestResponse reviewChangeRequest(Long id, ChangeRequestReviewRequest request, String reviewingUsername);

    ChangeRequestResponse getChangeRequestById(Long id);

    List<ChangeRequestResponse> getAllChangeRequests();
}