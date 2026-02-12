package com.example.kaisi_lagi;

import com.example.kaisi_lagi.CategoryMaster.CategoryRepository;
import com.example.kaisi_lagi.MovieMaster.MovieMaster;
import com.example.kaisi_lagi.MovieMaster.MovieRepository;
import com.example.kaisi_lagi.ReviewMaster.ReviewRepository;
import com.example.kaisi_lagi.UserMaster.EmailService;
import com.example.kaisi_lagi.UserMaster.UserMaster;
import com.example.kaisi_lagi.UserMaster.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import com.example.kaisi_lagi.UserMaster.UserDisplayDTO;

@Controller
public class AdminDashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EmailService emailService;

    @GetMapping("/block-users")
    public String blockUsers(Model model, HttpSession session) {

        UserMaster admin = (UserMaster) session.getAttribute("loggedUser");
        model.addAttribute("activePage", "blockUser");

        if (admin == null || admin.getRole() != UserMaster.Role.ADMIN) {
            return "redirect:/login";
        }

        // Get all users and filter out the admin
        List<UserMaster> allUsers = userRepository.findAll();

        // Convert to DTO with Base64 encoded photos
        List<UserDisplayDTO> displayUsers = allUsers.stream()
                .filter(user -> !user.getId().equals(admin.getId()))
                .map(user -> {
                    String photoBase64 = null;
                    if (user.getProfile_pic() != null) {
                        photoBase64 = Base64.getEncoder().encodeToString(user.getProfile_pic());
                    }
                    return new UserDisplayDTO(
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            user.isStatus(),
                            photoBase64
                    );
                })
                .collect(Collectors.toList());

        model.addAttribute("users", displayUsers);

        return "blockUser";
    }

    @PostMapping("/block-user/{id}")
    public String blockUser(@PathVariable Long id, RedirectAttributes ra) {
        try {
            UserMaster user = userRepository.findById(id).orElseThrow();
            user.setStatus(false);
            userRepository.save(user);

            // Send Block Notification Email
            emailService.sendBlockNotification(user.getEmail(), user.getUsername());

            ra.addFlashAttribute("toastMsg", "User blocked successfully! Email notification sent.");
            ra.addFlashAttribute("toastType", "success");
        } catch (Exception e) {
            ra.addFlashAttribute("toastMsg", "Failed to block user!");
            ra.addFlashAttribute("toastType", "danger");
        }
        return "redirect:/block-users";  // ← Changed from /blockUser
    }

    @PostMapping("/unblock-user/{id}")
    public String unblockUser(@PathVariable Long id, RedirectAttributes ra) {
        try {
            UserMaster user = userRepository.findById(id).orElseThrow();
            user.setStatus(true);
            userRepository.save(user);

            // Send Unblock Notification Email
            emailService.sendUnblockNotification(user.getEmail(), user.getUsername());

            ra.addFlashAttribute("toastMsg", "User unblocked successfully! Email notification sent.");
            ra.addFlashAttribute("toastType", "success");
        } catch (Exception e) {
            ra.addFlashAttribute("toastMsg", "Failed to unblock user!");
            ra.addFlashAttribute("toastType", "danger");
        }
        return "redirect:/block-users";  // ← Changed from /blockUser
    }

    // show user list
    @GetMapping("/users")
    public String users(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        model.addAttribute("activePage", "makeAdmin");
        UserMaster admin = (UserMaster) session.getAttribute("loggedUser");

        if (admin == null || admin.getRole() != UserMaster.Role.ADMIN) {
            return "redirect:/login";
        }

        model.addAttribute("users", userRepository.findAll());
        return "newAdmin";
    }

    @PostMapping("/make-admin/{id}")
    public String makeAdmin(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {

        UserMaster admin = (UserMaster) session.getAttribute("loggedUser");

        if (admin == null || admin.getRole() != UserMaster.Role.ADMIN) {
            return "redirect:/login";
        }

        UserMaster user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(UserMaster.Role.ADMIN);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("toastMsg", "User promoted to Admin successfully");
        redirectAttributes.addFlashAttribute("toastType", "success");

        return "redirect:/users";
    }
    @PostMapping("/remove-admin/{id}")
    public String removeAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            UserMaster user = userRepository.findById(id).orElseThrow();
            user.setRole(UserMaster.Role.USER);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("toastMsg", "User role changed to USER successfully!");
            redirectAttributes.addFlashAttribute("toastType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("toastMsg", "Failed to remove admin role!");
            redirectAttributes.addFlashAttribute("toastType", "danger");
        }
        return "redirect:/users";
    }

    @GetMapping("/dashboard")
    public String dashboard(
            Model model,
            HttpSession session,
            @RequestParam(value = "movieName", required = false) String movieName) {

        UserMaster user = (UserMaster) session.getAttribute("loggedUser");
        if (user == null) {
            return "redirect:/login";
        }

        // Profile photo and username are now added globally via GlobalControllerAdvice

        // Optional: Fetch a movie by name
        MovieMaster movie = null;
        if (movieName != null && !movieName.isBlank()) {
            movie = movieRepository.findByMovieNameContainingIgnoreCase(movieName.trim())
                    .stream()
                    .findFirst()
                    .orElse(null);
        }
        model.addAttribute("movie", movie);

        try {
            // Users stats
            model.addAttribute("totalUsers", userRepository.count());
            model.addAttribute("activeUsers", userRepository.countByStatus(true));
            model.addAttribute("inactiveUsers", userRepository.countByStatus(false));

            // Movies/Reviews stats
            model.addAttribute("totalMovies", movieRepository.count());
            model.addAttribute("totalReviews", reviewRepository.count());

            // Top rated items
            List<Object[]> topMovies = reviewRepository.getTopMovie();
            List<Object[]> topWeb = reviewRepository.getTopWebseries();
            List<Object[]> topTv = reviewRepository.getTopTvShow();

            model.addAttribute("topMovie", convertTopItem(topMovies.isEmpty() ? null : topMovies.get(0), "Movie"));
            model.addAttribute("topWeb", convertTopItem(topWeb.isEmpty() ? null : topWeb.get(0), "Web Series"));
            model.addAttribute("topSerial", convertTopItem(topTv.isEmpty() ? null : topTv.get(0), "TV Show"));

            // Category counts
            long movieCount = movieRepository.countByCategory_NameIgnoreCase("MOVIE");
            long webseriesCount = movieRepository.countByCategory_NameIgnoreCase("WEB SERIES");
            long tvShowCount = movieRepository.countByCategory_NameIgnoreCase("TV SHOW");

            model.addAttribute("movieCount", movieCount);
            model.addAttribute("webseriesCount", webseriesCount);
            model.addAttribute("serialCount", tvShowCount);
            model.addAttribute("totalEntries", movieCount + webseriesCount + tvShowCount);

            // Charts data
            populateChartData(model, reviewRepository.getDailyReviews(), "dailyLabels", "dailyCounts");
            populateChartData(model, userRepository.getMonthlyNewUsers(), "monthlyLabels", "monthlyCounts");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "adminDashBoard";
    }

    // Helper: Convert Object[] row to TopItem
    private TopItem convertTopItem(Object[] row, String type) {
        if (row != null && row.length >= 2) {
            String name = row[0] != null ? row[0].toString() : "N/A";
            String rating;
            try {
                rating = row[1] != null ? String.format("%.1f", Double.parseDouble(row[1].toString())) : "0.0";
            } catch (NumberFormatException e) {
                rating = "0.0";
            }
            return new TopItem(name, rating, type);
        }
        return new TopItem("N/A", "0.0", type);
    }

    // Helper: Populate chart data
    private void populateChartData(Model model, List<Object[]> rows, String labelAttr, String countAttr) {
        List<String> labels = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        if (rows != null) {
            for (Object[] row : rows) {
                labels.add(row[0] != null ? row[0].toString() : "N/A");
                try {
                    counts.add(row[1] != null ? Integer.parseInt(row[1].toString()) : 0);
                } catch (NumberFormatException e) {
                    counts.add(0);
                }
            }
        }

        model.addAttribute(labelAttr, labels);
        model.addAttribute(countAttr, counts);
    }

    // TopItem class for Thymeleaf
    public static class TopItem {
        private final String name;
        private final String rating;
        private final String type;

        public TopItem(String name, String rating, String type) {
            this.name = name;
            this.rating = rating;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getRating() {
            return rating;
        }

        public String getType() {
            return type;
        }
    }
}