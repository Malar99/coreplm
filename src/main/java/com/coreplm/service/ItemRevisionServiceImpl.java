package com.coreplm.service;

import com.coreplm.dto.RevisionCreateRequest;
import com.coreplm.dto.RevisionResponse;
import com.coreplm.dto.RevisionStatusUpdateRequest;
import com.coreplm.entity.ChangeOrder;
import com.coreplm.entity.ChangeOrderStatus;
import com.coreplm.entity.Item;
import com.coreplm.entity.ItemRevision;
import com.coreplm.entity.RevisionStatus;
import com.coreplm.entity.User;
import com.coreplm.exception.InvalidStatusTransitionException;
import com.coreplm.exception.ResourceNotFoundException;
import com.coreplm.repository.ChangeOrderRepository;
import com.coreplm.repository.ItemRepository;
import com.coreplm.repository.ItemRevisionRepository;
import com.coreplm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ItemRevisionServiceImpl implements ItemRevisionService {

    private static final Logger log = LoggerFactory.getLogger(ItemRevisionServiceImpl.class);

    private static final Map<RevisionStatus, Set<RevisionStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(RevisionStatus.class);
    static {
        ALLOWED_TRANSITIONS.put(RevisionStatus.IN_WORK, EnumSet.of(RevisionStatus.IN_REVIEW));
        ALLOWED_TRANSITIONS.put(RevisionStatus.IN_REVIEW, EnumSet.of(RevisionStatus.RELEASED, RevisionStatus.IN_WORK));
        ALLOWED_TRANSITIONS.put(RevisionStatus.RELEASED, EnumSet.of(RevisionStatus.OBSOLETE));
        ALLOWED_TRANSITIONS.put(RevisionStatus.OBSOLETE, EnumSet.noneOf(RevisionStatus.class));
    }

    private final ItemRevisionRepository revisionRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ChangeOrderRepository changeOrderRepository;

    public ItemRevisionServiceImpl(ItemRevisionRepository revisionRepository,
                                    ItemRepository itemRepository,
                                    UserRepository userRepository,
                                    ChangeOrderRepository changeOrderRepository) {
        this.revisionRepository = revisionRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.changeOrderRepository = changeOrderRepository;
    }

    @Override
    @Transactional
    public RevisionResponse createRevision(Long itemId, RevisionCreateRequest request, String requestingUsername) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));

        User creator = userRepository.findByUsername(requestingUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        String nextLabel = generateNextRevisionLabel(itemId);

        ItemRevision revision = new ItemRevision();
        revision.setItem(item);
        revision.setRevisionLabel(nextLabel);
        revision.setChangeDescription(request.changeDescription());
        revision.setStatus(RevisionStatus.IN_WORK);
        revision.setCreatedBy(creator);

        ItemRevision saved = revisionRepository.save(revision);

        log.info("Revision created: itemId={}, label={}, createdBy={}",
                itemId, nextLabel, requestingUsername);

        return mapToResponse(saved);
    }

    private String generateNextRevisionLabel(Long itemId) {
        long existingCount = revisionRepository.countByItemId(itemId);

        if (existingCount >= 26) {
            throw new IllegalStateException(
                    "Revision label rollover beyond 'Z' not yet supported for item " + itemId);
        }

        char nextLetter = (char) ('A' + existingCount);
        return String.valueOf(nextLetter);
    }

    @Override
    @Transactional(readOnly = true)
    public RevisionResponse getRevisionById(Long id) {
        ItemRevision revision = revisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Revision not found: " + id));
        return mapToResponse(revision);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevisionResponse> getRevisionsForItem(Long itemId) {
        return revisionRepository.findByItemIdOrderByRevisionLabelAsc(itemId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public RevisionResponse updateStatus(Long revisionId, RevisionStatusUpdateRequest request, String requestingUsername) {

        ItemRevision revision = revisionRepository.findById(revisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Revision not found: " + revisionId));

        RevisionStatus currentStatus = revision.getStatus();
        RevisionStatus requestedStatus = request.status();

        validateTransition(currentStatus, requestedStatus);

        User actor = userRepository.findByUsername(requestingUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        if (requestedStatus == RevisionStatus.RELEASED) {
            ChangeOrder eco = changeOrderRepository.findByRevisionIdAndStatus(revisionId, ChangeOrderStatus.OPEN)
                    .orElseThrow(() -> new InvalidStatusTransitionException(
                            "Cannot release revision without an approved, open Change Order authorizing it"));

            eco.setStatus(ChangeOrderStatus.CLOSED);
            eco.setClosedAt(LocalDateTime.now());
            eco.setClosedBy(actor);
            changeOrderRepository.save(eco);

            log.info("ECO closed via revision release: eco={}, revisionId={}", eco.getEcoNumber(), revisionId);

            revision.setReleasedBy(actor);
            revision.setReleasedAt(LocalDateTime.now());
        }

        revision.setStatus(requestedStatus);

        ItemRevision saved = revisionRepository.save(revision);

        log.info("Revision status changed: id={}, {} -> {}, by={}",
                revisionId, currentStatus, requestedStatus, requestingUsername);

        return mapToResponse(saved);
    }

    private void validateTransition(RevisionStatus current, RevisionStatus requested) {
        Set<RevisionStatus> allowedNext = ALLOWED_TRANSITIONS.get(current);

        if (allowedNext == null || !allowedNext.contains(requested)) {
            throw new InvalidStatusTransitionException(
                    "Cannot transition revision from " + current + " to " + requested);
        }
    }

    private RevisionResponse mapToResponse(ItemRevision revision) {
        return new RevisionResponse(
                revision.getId(),
                revision.getItem().getId(),
                revision.getItem().getItemNumber(),
                revision.getRevisionLabel(),
                revision.getChangeDescription(),
                revision.getStatus().name(),
                revision.getCreatedBy().getUsername(),
                revision.getReleasedBy() != null ? revision.getReleasedBy().getUsername() : null,
                revision.getReleasedAt(),
                revision.getCreatedAt()
        );
    }
}