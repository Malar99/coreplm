package com.coreplm.repository;

import com.coreplm.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByItemType(String itemType);
    Optional<Item> findByItemNumber(String itemNumber);
}