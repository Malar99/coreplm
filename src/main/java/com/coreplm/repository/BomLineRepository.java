package com.coreplm.repository;

import com.coreplm.entity.BomLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BomLineRepository extends JpaRepository<BomLine, Long> {

    List<BomLine> findByParentRevisionId(Long parentRevisionId);

    boolean existsByParentRevisionIdAndChildRevisionId(Long parentRevisionId, Long childRevisionId);
}