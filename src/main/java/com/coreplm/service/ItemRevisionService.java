package com.coreplm.service;

import com.coreplm.dto.RevisionCreateRequest;
import com.coreplm.dto.RevisionResponse;
import com.coreplm.dto.RevisionStatusUpdateRequest;

import java.util.List;

public interface ItemRevisionService {

    RevisionResponse createRevision(Long itemId, RevisionCreateRequest request, String requestingUsername);

    RevisionResponse getRevisionById(Long id);

    List<RevisionResponse> getRevisionsForItem(Long itemId);

    RevisionResponse updateStatus(Long revisionId, RevisionStatusUpdateRequest request, String requestingUsername);
}