package com.coreplm.repository;

import com.coreplm.entity.ChangeOrder;
import com.coreplm.entity.ChangeOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChangeOrderRepository extends JpaRepository<ChangeOrder, Long> {

    Optional<ChangeOrder> findByChangeRequestId(Long changeRequestId);

    @Query("SELECT co FROM ChangeOrder co WHERE co.changeRequest.revision.id = :revisionId AND co.status = :status")
    Optional<ChangeOrder> findByRevisionIdAndStatus(@Param("revisionId") Long revisionId,
                                                       @Param("status") ChangeOrderStatus status);
}