package com.coreplm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "change_requests")
public class ChangeRequest extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ecr_number", nullable = false, unique = true, length = 20, updatable = false)
    private String ecrNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false, updatable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revision_id", updatable = false)
    private ItemRevision revision;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChangeRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false, updatable = false)
    private User submittedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_comments", length = 1000)
    private String reviewComments;

    public ChangeRequest() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEcrNumber() { return ecrNumber; }
    public void setEcrNumber(String ecrNumber) { this.ecrNumber = ecrNumber; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public ItemRevision getRevision() { return revision; }
    public void setRevision(ItemRevision revision) { this.revision = revision; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public ChangeRequestStatus getStatus() { return status; }
    public void setStatus(ChangeRequestStatus status) { this.status = status; }

    public User getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(User submittedBy) { this.submittedBy = submittedBy; }

    public User getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(User reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getReviewComments() { return reviewComments; }
    public void setReviewComments(String reviewComments) { this.reviewComments = reviewComments; }
}