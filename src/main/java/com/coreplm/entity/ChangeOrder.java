package com.coreplm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "change_orders")
public class ChangeOrder extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "eco_number", nullable = false, unique = true, length = 20, updatable = false)
    private String ecoNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "change_request_id", nullable = false, unique = true, updatable = false)
    private ChangeRequest changeRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChangeOrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by")
    private User closedBy;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    public ChangeOrder() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEcoNumber() { return ecoNumber; }
    public void setEcoNumber(String ecoNumber) { this.ecoNumber = ecoNumber; }

    public ChangeRequest getChangeRequest() { return changeRequest; }
    public void setChangeRequest(ChangeRequest changeRequest) { this.changeRequest = changeRequest; }

    public ChangeOrderStatus getStatus() { return status; }
    public void setStatus(ChangeOrderStatus status) { this.status = status; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public User getClosedBy() { return closedBy; }
    public void setClosedBy(User closedBy) { this.closedBy = closedBy; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
}