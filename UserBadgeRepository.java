package com.example.kaisi_lagi.UserBadgeMaster;

import com.example.kaisi_lagi.BadgeMaster.BadgeMaster;
import com.example.kaisi_lagi.UserMaster.UserMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBadgeRepository
        extends JpaRepository<UserBadgeMaster, Long> {

    List<UserBadgeMaster> findByUser(UserMaster user);

    boolean existsByUserAndBadge(UserMaster user, BadgeMaster badge);


}
