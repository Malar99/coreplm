package com.coreplm.service;

import com.coreplm.dto.ItemCreateRequest;
import com.coreplm.dto.ItemResponse;
import com.coreplm.dto.ItemUpdateRequest;
import com.coreplm.entity.Item;
import com.coreplm.entity.ItemNumberSequence;
import com.coreplm.entity.User;
import com.coreplm.exception.ResourceNotFoundException;
import com.coreplm.repository.ItemNumberSequenceRepository;
import com.coreplm.repository.ItemRepository;
import com.coreplm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemServiceImpl.class);
    private static final String ITEM_NUMBER_PREFIX = "ITM-";
    private static final int ITEM_NUMBER_PADDING = 5;

    private final ItemRepository itemRepository;
    private final ItemNumberSequenceRepository sequenceRepository;
    private final UserRepository userRepository;

    public ItemServiceImpl(ItemRepository itemRepository,
                            ItemNumberSequenceRepository sequenceRepository,
                            UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.sequenceRepository = sequenceRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ItemResponse createItem(ItemCreateRequest request, String requestingUsername) {

        User creator = userRepository.findByUsername(requestingUsername)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found: " + requestingUsername));

        String itemNumber = generateNextItemNumber();

        Item item = new Item();
        item.setItemNumber(itemNumber);
        item.setName(request.name());
        item.setDescription(request.description());
        item.setItemType(request.itemType());
        item.setActive(true);
        item.setCreatedBy(creator);

        Item saved = itemRepository.save(item);

        log.info("Item created: id={}, itemNumber={}, createdBy={}",
                saved.getId(), saved.getItemNumber(), requestingUsername);

        return mapToResponse(saved);
    }

    private String generateNextItemNumber() {
        ItemNumberSequence sequence = sequenceRepository.findSequenceForUpdate();
        long current = sequence.getNextValue();
        sequence.setNextValue(current + 1);
        sequenceRepository.save(sequence);

        return ITEM_NUMBER_PREFIX + String.format("%0" + ITEM_NUMBER_PADDING + "d", current);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + id));
        return mapToResponse(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems(String itemType) {
        List<Item> items = (itemType == null || itemType.isBlank())
                ? itemRepository.findAll()
                : itemRepository.findByItemType(itemType);

        return items.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public ItemResponse updateItem(Long id, ItemUpdateRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + id));

        if (request.name() != null && !request.name().isBlank()) {
            item.setName(request.name());
        }
        if (request.description() != null) {
            item.setDescription(request.description());
        }

        Item saved = itemRepository.save(item);

        log.info("Item updated: id={}, itemNumber={}", saved.getId(), saved.getItemNumber());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deactivateItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + id));

        item.setActive(false);
        itemRepository.save(item);

        log.info("Item deactivated: id={}, itemNumber={}", item.getId(), item.getItemNumber());
    }
    @Override
    @Transactional(readOnly = true)
    public ItemResponse getItemByItemNumber(String itemNumber) {
        Item item = itemRepository.findByItemNumber(itemNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemNumber));
        return mapToResponse(item);
    }

    private ItemResponse mapToResponse(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getItemNumber(),
                item.getName(),
                item.getDescription(),
                item.getItemType(),
                item.isActive(),
                item.getCreatedBy().getUsername(),
                item.getCreatedAt()
        );
    }
}