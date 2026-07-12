package com.coreplm.service;

import com.coreplm.dto.ChangeOrderResponse;
import com.coreplm.entity.*;
import com.coreplm.exception.InvalidStatusTransitionException;
import com.coreplm.exception.ResourceNotFoundException;
import com.coreplm.repository.ChangeNumberSequenceRepository;
import com.coreplm.repository.ChangeOrderRepository;
import com.coreplm.repository.ChangeRequestRepository;
import com.coreplm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ChangeOrderServiceImpl implements ChangeOrderService {

    private static final Logger log = LoggerFactory.getLogger(ChangeOrderServiceImpl.class);
    private static final String ECO_PREFIX = "ECO-";
    private static final int NUMBER_PADDING = 5;

    private final ChangeOrderRepository changeOrderRepository;
    private final ChangeRequestRepository changeRequestRepository;
    private final ChangeNumberSequenceRepository sequenceRepository;
    private final UserRepository userRepository;

    public ChangeOrderServiceImpl(ChangeOrderRepository changeOrderRepository,
                                   ChangeRequestRepository changeRequestRepository,
                                   ChangeNumberSequenceRepository sequenceRepository,
                                   UserRepository userRepository) {
        this.changeOrderRepository = changeOrderRepository;
        this.changeRequestRepository = changeRequestRepository;
        this.sequenceRepository = sequenceRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ChangeOrderResponse generateFromApprovedEcr(Long changeRequestId, String requestingUsername) {

        ChangeRequest cr = changeRequestRepository.findById(changeRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Change request not found: " + changeRequestId));

        if (cr.getStatus() != ChangeRequestStatus.APPROVED) {
            throw new InvalidStatusTransitionException(
                    "Cannot generate an ECO from a change request that is not APPROVED (current status: " + cr.getStatus() + ")");
        }

        if (changeOrderRepository.findByChangeRequestId(changeRequestId).isPresent()) {
            throw new IllegalArgumentException("An ECO already exists for change request: " + cr.getEcrNumber());
        }

        User creator = userRepository.findByUsername(requestingUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        String ecoNumber = generateNextNumber("ECO", ECO_PREFIX);

        ChangeOrder eco = new ChangeOrder();
        eco.setEcoNumber(ecoNumber);
        eco.setChangeRequest(cr);
        eco.setStatus(ChangeOrderStatus.OPEN);
        eco.setCreatedBy(creator);

        ChangeOrder saved = changeOrderRepository.save(eco);

        log.info("ECO generated: number={}, fromEcr={}, createdBy={}",
                ecoNumber, cr.getEcrNumber(), requestingUsername);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ChangeOrderResponse getByChangeRequestId(Long changeRequestId) {
        ChangeOrder eco = changeOrderRepository.findByChangeRequestId(changeRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("No ECO found for change request: " + changeRequestId));
        return mapToResponse(eco);
    }

    @Override
    @Transactional
    public void closeChangeOrder(Long changeOrderId) {
        ChangeOrder eco = changeOrderRepository.findById(changeOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Change order not found: " + changeOrderId));

        eco.setStatus(ChangeOrderStatus.CLOSED);
        eco.setClosedAt(LocalDateTime.now());

        changeOrderRepository.save(eco);

        log.info("ECO closed: number={}", eco.getEcoNumber());
    }

    private String generateNextNumber(String sequenceType, String prefix) {
        ChangeNumberSequence sequence = sequenceRepository.findSequenceForUpdate(sequenceType);
        long current = sequence.getNextValue();
        sequence.setNextValue(current + 1);
        sequenceRepository.save(sequence);
        return prefix + String.format("%0" + NUMBER_PADDING + "d", current);
    }

    private ChangeOrderResponse mapToResponse(ChangeOrder eco) {
        return new ChangeOrderResponse(
                eco.getId(),
                eco.getEcoNumber(),
                eco.getChangeRequest().getId(),
                eco.getChangeRequest().getEcrNumber(),
                eco.getStatus().name(),
                eco.getCreatedBy().getUsername(),
                eco.getClosedBy() != null ? eco.getClosedBy().getUsername() : null,
                eco.getClosedAt(),
                eco.getCreatedAt()
        );
    }
}