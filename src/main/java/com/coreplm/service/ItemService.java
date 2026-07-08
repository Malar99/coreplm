package com.coreplm.service;

import com.coreplm.dto.ItemCreateRequest;
import com.coreplm.dto.ItemResponse;
import com.coreplm.dto.ItemUpdateRequest;

import java.util.List;

public interface ItemService {

    ItemResponse createItem(ItemCreateRequest request, String requestingUsername);

    ItemResponse getItemById(Long id);

    List<ItemResponse> getAllItems(String itemType);

    ItemResponse updateItem(Long id, ItemUpdateRequest request);

    ItemResponse getItemByItemNumber(String itemNumber);
    
    void deactivateItem(Long id);
}