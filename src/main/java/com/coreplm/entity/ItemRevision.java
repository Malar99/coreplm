package com.coreplm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "item_revisions")
public class ItemRevision extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false, updatable = false)
    private Item item;

    @Column(name = "revision_label", nullable = false, length = 10, updatable = false)
    private String revisionLabel;

    @Column(name = "change_description", length = 500)
    private String changeDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RevisionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "released_by")
    private User releasedBy;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    public ItemRevision() {
    }

    // Manual getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public String getRevisionLabel() { return revisionLabel; }
    public void setRevisionLabel(String revisionLabel) { this.revisionLabel = revisionLabel; }

    public String getChangeDescription() { return changeDescription; }
    public void setChangeDescription(String changeDescription) { this.changeDescription = changeDescription; }

    public RevisionStatus getStatus() { return status; }
    public void setStatus(RevisionStatus status) { this.status = status; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public User getReleasedBy() { return releasedBy; }
    public void setReleasedBy(User releasedBy) { this.releasedBy = releasedBy; }

    public LocalDateTime getReleasedAt() { return releasedAt; }
    public void setReleasedAt(LocalDateTime releasedAt) { this.releasedAt = releasedAt; }
}