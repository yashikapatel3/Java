package com.example.kaisi_lagi.UserBadgeMaster;

import com.example.kaisi_lagi.BadgeMaster.BadgeMaster;
import com.example.kaisi_lagi.BadgeMaster.BadgeRepository;
import com.example.kaisi_lagi.CategoryMaster.CategoryMaster;
import com.example.kaisi_lagi.ReviewMaster.ReviewRepository;
import com.example.kaisi_lagi.UserMaster.UserMaster;
import com.example.kaisi_lagi.UserMaster.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserBadgeService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void checkAndAssignBadges(UserMaster user, CategoryMaster category) {

        // ===== STEP 1: VALIDATE INPUTS =====
        if (user == null || user.getId() == null) {
            System.err.println("❌ ERROR: User is NULL or has no ID");
            return;
        }

        if (category == null || category.getId() == null) {
            System.err.println("❌ ERROR: Category is NULL or has no ID");
            return;
        }

        System.out.println("========================================");
        System.out.println("🎖️ BADGE CHECK STARTED");
        System.out.println("User ID: " + user.getId());
        System.out.println("User Name: " + user.getUsername());
        System.out.println("Category ID: " + category.getId());
        System.out.println("Category Name: " + category.getName());
        System.out.println("========================================");

        // ===== STEP 2: FETCH MANAGED USER =====
        UserMaster managedUser = userRepository.findById(user.getId()).orElse(null);
        if (managedUser == null) {
            System.err.println("❌ ERROR: User not found in database");
            return;
        }
        System.out.println("✅ Managed User fetched: " + managedUser.getUsername());

        // ===== STEP 3: COUNT REVIEWS =====
        long reviewCount = reviewRepository.countByUserAndCategory(managedUser, category);
        System.out.println("✅ Review Count for this category: " + reviewCount);

        // ===== STEP 4: FETCH BADGES =====
        List<BadgeMaster> badges = badgeRepository.findByCategoryOrderByRequiredReviewsAsc(category);
        System.out.println("✅ Available Badges for this Category: " + badges.size());

        if (badges.isEmpty()) {
            System.out.println("ℹ️ No badges configured for this category");
            return;
        }

        // ===== STEP 5: CHECK EACH BADGE =====
        for (BadgeMaster badge : badges) {

            System.out.println("----------------------------------------");

            // Validate badge
            if (badge == null) {
                System.err.println("❌ ERROR: Badge is NULL in loop - SKIPPING");
                continue;
            }

            if (badge.getBadgeId() == null) {
                System.err.println("❌ ERROR: Badge ID is NULL");
                System.err.println("Badge Name: " + badge.getName());
                System.err.println("SKIPPING this badge");
                continue;
            }

            System.out.println("📛 Checking Badge:");
            System.out.println("   ID: " + badge.getBadgeId());
            System.out.println("   Name: " + badge.getName());
            System.out.println("   Required Reviews: " + badge.getRequiredReviews());
            System.out.println("   User Reviews: " + reviewCount);

            // Check if user qualifies
            if (reviewCount >= badge.getRequiredReviews()) {

                System.out.println("   ✅ User QUALIFIES for this badge!");

                // Check if already assigned
                boolean exists = userBadgeRepository.existsByUserAndBadge(managedUser, badge);

                if (exists) {
                    System.out.println("   ℹ️ Badge ALREADY ASSIGNED to user");
                } else {
                    System.out.println("   🎖️ ASSIGNING NEW BADGE...");

                    try {
                        UserBadgeMaster ub = new UserBadgeMaster();
                        ub.setUser(managedUser);
                        ub.setBadge(badge);
                        ub.setEarnedAt(LocalDateTime.now());

                        // Final validation
                        if (ub.getUser() == null || ub.getUser().getId() == null) {
                            System.err.println("   ❌ ERROR: UserBadge User is NULL");
                            continue;
                        }
                        if (ub.getBadge() == null || ub.getBadge().getBadgeId() == null) {
                            System.err.println("   ❌ ERROR: UserBadge Badge is NULL");
                            continue;
                        }

                        userBadgeRepository.save(ub);
                        System.out.println("   ✅✅✅ BADGE SAVED SUCCESSFULLY! ✅✅✅");

                    } catch (Exception e) {
                        System.err.println("   ❌ ERROR saving badge:");
                        System.err.println("   Message: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("   ❌ User does NOT qualify yet");
                System.out.println("   Needs " + (badge.getRequiredReviews() - reviewCount) + " more reviews");
            }
        }

        System.out.println("========================================");
        System.out.println("🎖️ BADGE CHECK COMPLETED");
        System.out.println("========================================");
    }
}