package com.coreplm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "bom_lines")
public class BomLine extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_revision_id", nullable = false, updatable = false)
    private ItemRevision parentRevision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_revision_id", nullable = false, updatable = false)
    private ItemRevision childRevision;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by", nullable = false, updatable = false)
    private User addedBy;

    public BomLine() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ItemRevision getParentRevision() { return parentRevision; }
    public void setParentRevision(ItemRevision parentRevision) { this.parentRevision = parentRevision; }

    public ItemRevision getChildRevision() { return childRevision; }
    public void setChildRevision(ItemRevision childRevision) { this.childRevision = childRevision; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public User getAddedBy() { return addedBy; }
    public void setAddedBy(User addedBy) { this.addedBy = addedBy; }
}