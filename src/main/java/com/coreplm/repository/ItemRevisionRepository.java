package com.coreplm.repository;

import com.coreplm.entity.ItemRevision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRevisionRepository extends JpaRepository<ItemRevision, Long> {

    long countByItemId(Long itemId);

    List<ItemRevision> findByItemIdOrderByRevisionLabelAsc(Long itemId);
}