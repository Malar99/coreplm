package com.coreplm.service;

import com.coreplm.dto.BomLineCreateRequest;
import com.coreplm.dto.BomLineResponse;
import com.coreplm.dto.BomTreeNodeResponse;
import com.coreplm.entity.BomLine;
import com.coreplm.entity.ItemRevision;
import com.coreplm.entity.User;
import com.coreplm.exception.CircularReferenceException;
import com.coreplm.exception.ResourceNotFoundException;
import com.coreplm.repository.BomLineRepository;
import com.coreplm.repository.ItemRevisionRepository;
import com.coreplm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BomServiceImpl implements BomService {

    private static final Logger log = LoggerFactory.getLogger(BomServiceImpl.class);

    private final BomLineRepository bomLineRepository;
    private final ItemRevisionRepository revisionRepository;
    private final UserRepository userRepository;

    public BomServiceImpl(BomLineRepository bomLineRepository,
                           ItemRevisionRepository revisionRepository,
                           UserRepository userRepository) {
        this.bomLineRepository = bomLineRepository;
        this.revisionRepository = revisionRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public BomLineResponse addBomLine(Long parentRevisionId, BomLineCreateRequest request, String requestingUsername) {

        if (parentRevisionId.equals(request.childRevisionId())) {
            throw new CircularReferenceException(
                    "A revision cannot directly contain itself as a component");
        }

        ItemRevision parent = revisionRepository.findById(parentRevisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent revision not found: " + parentRevisionId));

        ItemRevision child = revisionRepository.findById(request.childRevisionId())
                .orElseThrow(() -> new ResourceNotFoundException("Child revision not found: " + request.childRevisionId()));

        if (bomLineRepository.existsByParentRevisionIdAndChildRevisionId(parentRevisionId, request.childRevisionId())) {
            throw new IllegalArgumentException("This component already exists directly under this parent");
        }

        // Deep cycle check: does the proposed child's own tree already contain the proposed parent?
        if (wouldCreateCycle(parentRevisionId, request.childRevisionId())) {
            throw new CircularReferenceException(
                    "Adding this component would create a circular reference in the BOM structure");
        }

        User adder = userRepository.findByUsername(requestingUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        BomLine line = new BomLine();
        line.setParentRevision(parent);
        line.setChildRevision(child);
        line.setQuantity(request.quantity());
        line.setAddedBy(adder);

        BomLine saved = bomLineRepository.save(line);

        log.info("BOM line added: parentRevisionId={}, childRevisionId={}, quantity={}, addedBy={}",
                parentRevisionId, request.childRevisionId(), request.quantity(), requestingUsername);

        return mapToResponse(saved);
    }

    /**
     * Determines whether making 'proposedParentId' the ultimate parent of 'proposedChildId'
     * would create a cycle, by checking if proposedParentId already appears anywhere
     * in proposedChildId's existing subtree (at any depth).
     */
    private boolean wouldCreateCycle(Long proposedParentId, Long proposedChildId) {
        Set<Long> visited = new HashSet<>();
        return containsRevisionInSubtree(proposedChildId, proposedParentId, visited);
    }

    private boolean containsRevisionInSubtree(Long currentRevisionId, Long targetRevisionId, Set<Long> visited) {

        if (currentRevisionId.equals(targetRevisionId)) {
            return true;
        }

        // Guard against infinite loops if bad data already exists (defensive, shouldn't normally trigger)
        if (!visited.add(currentRevisionId)) {
            return false;
        }

        List<BomLine> children = bomLineRepository.findByParentRevisionId(currentRevisionId);

        for (BomLine line : children) {
            if (containsRevisionInSubtree(line.getChildRevision().getId(), targetRevisionId, visited)) {
                return true;
            }
        }

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BomLineResponse> getDirectChildren(Long parentRevisionId) {
        return bomLineRepository.findByParentRevisionId(parentRevisionId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BomTreeNodeResponse getFullBomTree(Long rootRevisionId) {
        ItemRevision root = revisionRepository.findById(rootRevisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Revision not found: " + rootRevisionId));

        return buildTreeNode(root, null, new HashSet<>());
    }

    private BomTreeNodeResponse buildTreeNode(ItemRevision revision, java.math.BigDecimal quantity, Set<Long> ancestryPath) {

        // Defensive guard: should be unreachable given write-time cycle prevention,
        // but protects tree traversal from ever infinite-looping if bad data somehow exists.
        if (!ancestryPath.add(revision.getId())) {
            throw new IllegalStateException(
                    "Cycle detected while traversing BOM tree at revision " + revision.getId() +
                    " — this indicates a data integrity issue, since write-time checks should prevent this");
        }

        List<BomLine> childLines = bomLineRepository.findByParentRevisionId(revision.getId());

        List<BomTreeNodeResponse> children = childLines.stream()
                .map(line -> buildTreeNode(line.getChildRevision(), line.getQuantity(), new HashSet<>(ancestryPath)))
                .toList();

        return new BomTreeNodeResponse(
                revision.getId(),
                revision.getItem().getItemNumber(),
                revision.getItem().getName(),
                revision.getRevisionLabel(),
                quantity,
                children
        );
    }

    @Override
    @Transactional
    public void removeBomLine(Long bomLineId) {
        BomLine line = bomLineRepository.findById(bomLineId)
                .orElseThrow(() -> new ResourceNotFoundException("BOM line not found: " + bomLineId));

        bomLineRepository.delete(line);

        log.info("BOM line removed: id={}, parentRevisionId={}, childRevisionId={}",
                bomLineId, line.getParentRevision().getId(), line.getChildRevision().getId());
    }

    private BomLineResponse mapToResponse(BomLine line) {
        return new BomLineResponse(
                line.getId(),
                line.getParentRevision().getId(),
                line.getChildRevision().getId(),
                line.getChildRevision().getItem().getItemNumber(),
                line.getChildRevision().getRevisionLabel(),
                line.getQuantity(),
                line.getAddedBy().getUsername(),
                line.getCreatedAt()
        );
    }
}