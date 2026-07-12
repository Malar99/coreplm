package com.coreplm.service;

import com.coreplm.dto.ChangeRequestCreateRequest;
import com.coreplm.dto.ChangeRequestResponse;
import com.coreplm.dto.ChangeRequestReviewRequest;
import com.coreplm.entity.*;
import com.coreplm.exception.InvalidStatusTransitionException;
import com.coreplm.exception.ResourceNotFoundException;
import com.coreplm.exception.UnauthorizedActionException;
import com.coreplm.repository.*;
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
public class ChangeRequestServiceImpl implements ChangeRequestService {

    private static final Logger log = LoggerFactory.getLogger(ChangeRequestServiceImpl.class);
    private static final String ECR_PREFIX = "ECR-";
    private static final int NUMBER_PADDING = 5;

    private static final Map<ChangeRequestStatus, Set<ChangeRequestStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(ChangeRequestStatus.class);
    static {
        ALLOWED_TRANSITIONS.put(ChangeRequestStatus.DRAFT, EnumSet.of(ChangeRequestStatus.SUBMITTED));
        ALLOWED_TRANSITIONS.put(ChangeRequestStatus.SUBMITTED, EnumSet.of(ChangeRequestStatus.APPROVED, ChangeRequestStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(ChangeRequestStatus.APPROVED, EnumSet.noneOf(ChangeRequestStatus.class));
        ALLOWED_TRANSITIONS.put(ChangeRequestStatus.REJECTED, EnumSet.noneOf(ChangeRequestStatus.class));
    }

    private final ChangeRequestRepository changeRequestRepository;
    private final ChangeNumberSequenceRepository sequenceRepository;
    private final ItemRepository itemRepository;
    private final ItemRevisionRepository revisionRepository;
    private final UserRepository userRepository;

    public ChangeRequestServiceImpl(ChangeRequestRepository changeRequestRepository,
                                     ChangeNumberSequenceRepository sequenceRepository,
                                     ItemRepository itemRepository,
                                     ItemRevisionRepository revisionRepository,
                                     UserRepository userRepository) {
        this.changeRequestRepository = changeRequestRepository;
        this.sequenceRepository = sequenceRepository;
        this.itemRepository = itemRepository;
        this.revisionRepository = revisionRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ChangeRequestResponse createChangeRequest(ChangeRequestCreateRequest request, String requestingUsername) {

        Item item = itemRepository.findById(request.itemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + request.itemId()));

        ItemRevision revision = null;
        if (request.revisionId() != null) {
            revision = revisionRepository.findById(request.revisionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Revision not found: " + request.revisionId()));
        }

        User submitter = userRepository.findByUsername(requestingUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        String ecrNumber = generateNextNumber("ECR", ECR_PREFIX);

        ChangeRequest cr = new ChangeRequest();
        cr.setEcrNumber(ecrNumber);
        cr.setItem(item);
        cr.setRevision(revision);
        cr.setReason(request.reason());
        cr.setStatus(ChangeRequestStatus.DRAFT);
        cr.setSubmittedBy(submitter);

        ChangeRequest saved = changeRequestRepository.save(cr);

        log.info("ECR created: number={}, itemId={}, submittedBy={}",
                ecrNumber, request.itemId(), requestingUsername);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ChangeRequestResponse submitChangeRequest(Long id, String requestingUsername) {

        ChangeRequest cr = changeRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Change request not found: " + id));

        if (!cr.getSubmittedBy().getUsername().equals(requestingUsername)) {
            throw new UnauthorizedActionException("Only the original submitter can submit this change request for review");
        }

        validateTransition(cr.getStatus(), ChangeRequestStatus.SUBMITTED);
        cr.setStatus(ChangeRequestStatus.SUBMITTED);

        ChangeRequest saved = changeRequestRepository.save(cr);

        log.info("ECR submitted for review: number={}", cr.getEcrNumber());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ChangeRequestResponse reviewChangeRequest(Long id, ChangeRequestReviewRequest request, String reviewingUsername) {

        ChangeRequest cr = changeRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Change request not found: " + id));

        // Separation of duties: reviewer cannot be the submitter
        if (cr.getSubmittedBy().getUsername().equals(reviewingUsername)) {
            throw new UnauthorizedActionException("You cannot approve or reject your own change request");
        }

        ChangeRequestStatus targetStatus = request.approved()
                ? ChangeRequestStatus.APPROVED
                : ChangeRequestStatus.REJECTED;

        validateTransition(cr.getStatus(), targetStatus);

        User reviewer = userRepository.findByUsername(reviewingUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        cr.setStatus(targetStatus);
        cr.setReviewedBy(reviewer);
        cr.setReviewedAt(LocalDateTime.now());
        cr.setReviewComments(request.comments());

        ChangeRequest saved = changeRequestRepository.save(cr);

        log.info("ECR reviewed: number={}, decision={}, reviewedBy={}",
                cr.getEcrNumber(), targetStatus, reviewingUsername);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ChangeRequestResponse getChangeRequestById(Long id) {
        ChangeRequest cr = changeRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Change request not found: " + id));
        return mapToResponse(cr);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChangeRequestResponse> getAllChangeRequests() {
        return changeRequestRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    private void validateTransition(ChangeRequestStatus current, ChangeRequestStatus requested) {
        Set<ChangeRequestStatus> allowed = ALLOWED_TRANSITIONS.get(current);
        if (allowed == null || !allowed.contains(requested)) {
            throw new InvalidStatusTransitionException(
                    "Cannot transition change request from " + current + " to " + requested);
        }
    }

    private String generateNextNumber(String sequenceType, String prefix) {
        ChangeNumberSequence sequence = sequenceRepository.findSequenceForUpdate(sequenceType);
        long current = sequence.getNextValue();
        sequence.setNextValue(current + 1);
        sequenceRepository.save(sequence);
        return prefix + String.format("%0" + NUMBER_PADDING + "d", current);
    }

    private ChangeRequestResponse mapToResponse(ChangeRequest cr) {
        return new ChangeRequestResponse(
                cr.getId(),
                cr.getEcrNumber(),
                cr.getItem().getId(),
                cr.getItem().getItemNumber(),
                cr.getRevision() != null ? cr.getRevision().getId() : null,
                cr.getRevision() != null ? cr.getRevision().getRevisionLabel() : null,
                cr.getReason(),
                cr.getStatus().name(),
                cr.getSubmittedBy().getUsername(),
                cr.getReviewedBy() != null ? cr.getReviewedBy().getUsername() : null,
                cr.getReviewedAt(),
                cr.getReviewComments(),
                cr.getCreatedAt()
        );
    }
}