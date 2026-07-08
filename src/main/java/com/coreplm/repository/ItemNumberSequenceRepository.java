package com.coreplm.repository;

import com.coreplm.entity.ItemNumberSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ItemNumberSequenceRepository extends JpaRepository<ItemNumberSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ItemNumberSequence s WHERE s.id = 1")
    ItemNumberSequence findSequenceForUpdate();
}