package com.coreplm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "change_number_sequence")
public class ChangeNumberSequence {

    @Id
    private Long id;

    @Column(name = "sequence_type", nullable = false, unique = true, length = 10)
    private String sequenceType;

    @Column(name = "next_value", nullable = false)
    private Long nextValue;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSequenceType() { return sequenceType; }
    public void setSequenceType(String sequenceType) { this.sequenceType = sequenceType; }

    public Long getNextValue() { return nextValue; }
    public void setNextValue(Long nextValue) { this.nextValue = nextValue; }
}