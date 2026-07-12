package com.coreplm.repository;

import com.coreplm.entity.ChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, Long> {
    List<ChangeRequest> findByItemId(Long itemId);
}