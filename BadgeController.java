package com.example.kaisi_lagi.BadgeMaster;

import com.example.kaisi_lagi.CategoryMaster.CategoryMaster;
import com.example.kaisi_lagi.CategoryMaster.CategoryRepository;
import com.example.kaisi_lagi.PeopleMaster.PeopleMaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
public class BadgeController {

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // CREATE BADGE PAGE
    @GetMapping("/addbages")
    public String addBadge(Model model) {

        model.addAttribute("badge", new BadgeMaster());
        model.addAttribute("categories", categoryRepository.findAll());

        return "createbadge";
    }


    //  SAVE BADGE
    @PostMapping("/save")
    public String saveBadge(
            @ModelAttribute BadgeMaster badge,
            @RequestParam("categoryId") Long categoryId
    ) {
        CategoryMaster category =
                categoryRepository.findById(categoryId).orElseThrow();

        badge.setCategory(category);

        try {
            if (badge.getIconFile() != null && !badge.getIconFile().isEmpty()) {
                badge.setIcon(badge.getIconFile().getBytes());
            }
        } catch (Exception e) {
            throw new RuntimeException("Icon upload failed", e);
        }

        badgeRepository.save(badge);
        return "redirect:/list";
    }


    // ===================== SERVE ICON =====================
    @GetMapping("/icon/{badgeId}")
    public ResponseEntity<byte[]> getBadgeIcon(@PathVariable Long badgeId) {

        BadgeMaster badge = badgeRepository.findById(badgeId).orElse(null);

        if (badge == null || badge.getIcon() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                .body(badge.getIcon());
    }


    // ===================== LIST BADGES =====================
    @GetMapping("/list")
    public String listBadges(Model model) {
        model.addAttribute("activePage", "badgelist");
        List<BadgeMaster> badges = badgeRepository.findAll();
        model.addAttribute("badges", badges);

        // Debug: Print icon status
        System.out.println("=== BADGE LIST DEBUG ===");
        for (BadgeMaster badge : badges) {
            System.out.println("Badge: " + badge.getName() +
                    " | Icon size: " + (badge.getIcon() != null ? badge.getIcon().length + " bytes" : "NULL"));
        }

        return "badgelist";
    }

    //    ===========delete Badge============
    @PostMapping("/deletebadge/{badgeId}")
    public String deleteBadge(@PathVariable long badgeId) {
        badgeRepository.deleteById(badgeId);
        return "redirect:/list";
    }

    @GetMapping("/updatebadge/{badgeId}")
    public String getPageUpdate(@PathVariable long badgeId, Model model) {
        Optional<BadgeMaster> optionalBadge = badgeRepository.findById(badgeId);

        if (optionalBadge.isPresent()) {
            BadgeMaster badge = optionalBadge.get();

            // Add the badge to edit
            model.addAttribute("updateBadgeMaster", badge);

            // Fetch and add all categories for the dropdown
            List<CategoryMaster> categories = categoryRepository.findAll();
            model.addAttribute("categories", categories);

            System.out.println("=== EDIT PAGE DEBUG ===");
            System.out.println("Badge to edit: " + badge.getName());
            System.out.println("Current Category: " + (badge.getCategory() != null ? badge.getCategory().getName() : "NULL"));
            System.out.println("Current Icon size: " + (badge.getIcon() != null ? badge.getIcon().length + " bytes" : "NULL"));
            System.out.println("Categories found: " + categories.size());

            return "Editbadge";
        } else {
            // Handle case when badge not found
            return "redirect:/list";
        }
    }

    // Process the update - POST request
    @PostMapping("/updatebadge")
    public String updateById(@ModelAttribute("updateBadgeMaster") BadgeMaster badgeMaster,
                             @RequestParam(value = "categoryId", required = false) Long categoryId,
                             @RequestParam(value = "iconFile", required = false) MultipartFile iconFile,
                             Model model) {

        System.out.println("=== UPDATE BADGE DEBUG ===");
        System.out.println("Badge ID: " + badgeMaster.getBadgeId());
        System.out.println("Badge Name: " + badgeMaster.getName());
        System.out.println("Category ID from form: " + categoryId);
        System.out.println("Required Reviews: " + badgeMaster.getRequiredReviews());
        System.out.println("Icon file uploaded: " + (iconFile != null && !iconFile.isEmpty()));
        if (iconFile != null && !iconFile.isEmpty()) {
            System.out.println("Icon filename: " + iconFile.getOriginalFilename());
            System.out.println("Icon size: " + iconFile.getSize() + " bytes");
        }

        Optional<BadgeMaster> optionalBadge = badgeRepository.findById(badgeMaster.getBadgeId());

        if (!optionalBadge.isPresent()) {
            System.err.println("Badge not found with ID: " + badgeMaster.getBadgeId());
            return "redirect:/list";
        }

        BadgeMaster master = optionalBadge.get();

        System.out.println("Existing icon size: " + (master.getIcon() != null ? master.getIcon().length + " bytes" : "NULL"));

        // Update basic fields
        master.setName(badgeMaster.getName());
        master.setRequiredReviews(badgeMaster.getRequiredReviews());

        // CRITICAL: Update category - check if categoryId was received
        if (categoryId != null) {
            Optional<CategoryMaster> categoryOptional = categoryRepository.findById(categoryId);
            if (categoryOptional.isPresent()) {
                master.setCategory(categoryOptional.get());
                System.out.println("✓ Category set successfully: " + categoryOptional.get().getName());
            } else {
                System.err.println("✗ Category not found with ID: " + categoryId);
                // Return to edit page with error
                model.addAttribute("error", "Invalid category selected");
                model.addAttribute("updateBadgeMaster", master);
                model.addAttribute("categories", categoryRepository.findAll());
                return "Editbadge";
            }
        } else {
            System.err.println("⚠ WARNING: categoryId is NULL!");
            // If category was not provided, keep the existing one
            // But if there's no existing category, we have a problem
            if (master.getCategory() == null) {
                System.err.println("✗ ERROR: No category in form and no existing category!");
                model.addAttribute("error", "Please select a category");
                model.addAttribute("updateBadgeMaster", master);
                model.addAttribute("categories", categoryRepository.findAll());
                return "Editbadge";
            }
            System.out.println("Using existing category: " + master.getCategory().getName());
        }

        // Handle icon upload - FIXED VERSION
        try {
            if (iconFile != null && !iconFile.isEmpty()) {
                byte[] iconBytes = iconFile.getBytes();
                master.setIcon(iconBytes);  // ✓ CORRECT: Store the actual image bytes
                System.out.println("✓ Icon uploaded successfully: " + iconFile.getOriginalFilename() +
                        " (" + iconBytes.length + " bytes)");
            } else {
                System.out.println("⚠ No new icon uploaded, keeping existing icon");
                System.out.println("  Existing icon size: " + (master.getIcon() != null ? master.getIcon().length + " bytes" : "NULL"));
            }
        } catch (IOException e) {
            System.err.println("✗ Error uploading icon: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== SAVING BADGE ===");
        System.out.println("Name: " + master.getName());
        System.out.println("Category: " + (master.getCategory() != null ? master.getCategory().getName() : "NULL"));
        System.out.println("Icon size: " + (master.getIcon() != null ? master.getIcon().length + " bytes" : "NULL"));

        badgeRepository.save(master);
        System.out.println("✓ Badge saved successfully!");

        return "redirect:/list";
    }
}