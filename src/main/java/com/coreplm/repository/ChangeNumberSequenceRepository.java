package com.coreplm.repository;

import com.coreplm.entity.ChangeNumberSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChangeNumberSequenceRepository extends JpaRepository<ChangeNumberSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ChangeNumberSequence s WHERE s.sequenceType = :type")
    ChangeNumberSequence findSequenceForUpdate(@Param("type") String type);
}