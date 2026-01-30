package com.example.kaisi_lagi.BadgeMaster;

import com.example.kaisi_lagi.CategoryMaster.CategoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgeRepository extends JpaRepository<BadgeMaster, Long> {
    List<BadgeMaster> findByCategoryOrderByRequiredReviewsAsc(CategoryMaster category);

}
