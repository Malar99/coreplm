package com.coreplm.service;

import com.coreplm.dto.BomLineCreateRequest;
import com.coreplm.dto.BomLineResponse;
import com.coreplm.dto.BomTreeNodeResponse;

import java.util.List;

public interface BomService {

    BomLineResponse addBomLine(Long parentRevisionId, BomLineCreateRequest request, String requestingUsername);

    List<BomLineResponse> getDirectChildren(Long parentRevisionId);

    BomTreeNodeResponse getFullBomTree(Long rootRevisionId);

    void removeBomLine(Long bomLineId);
}