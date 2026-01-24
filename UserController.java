package com.example.kaisi_lagi.UserMaster;

import com.example.kaisi_lagi.UserBadgeMaster.UserBadgeMaster;
import com.example.kaisi_lagi.UserBadgeMaster.UserBadgeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Controller
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    /* ---------------- BASIC PAGES ---------------- */

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }



    @GetMapping("/editProfile")
    public String editProfile(Model model, HttpSession session) {
        UserMaster user = (UserMaster) session.getAttribute("loggedUser");
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        if (user.getProfile_pic() != null) {
            model.addAttribute("photo",
                    Base64.getEncoder().encodeToString(user.getProfile_pic()));
        }
        return "editProfile";
    }
    @GetMapping("/check-username")
    @ResponseBody
    public boolean checkUsername(@RequestParam String username) {
        return userRepository.existsByUsernameIgnoreCase(username);
    }


    @PostMapping("/editProfile")
    public String editProfile(
            @ModelAttribute("user") UserMaster formUser,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String oldPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            @RequestParam(required = false)  LocalDate dob,
            HttpSession session,
            Model model) throws IOException {

        UserMaster sessionUser = (UserMaster) session.getAttribute("loggedUser");
        if (sessionUser == null)
            return "redirect:/login";

        UserMaster user = userRepository.findById(sessionUser.getId()).orElse(null);
        if (user == null)
            return "redirect:/login";

        // username validation
        UserMaster existing = userRepository.findByUsername(formUser.getUsername());
        if (existing != null && !existing.getId().equals(user.getId())) {
            model.addAttribute("error", "Username already taken");
            model.addAttribute("user", user);
            return "editProfile";
        }
        user.setUsername(formUser.getUsername());

        // profile picture
        if (file != null && !file.isEmpty()) {
            user
.setProfile_pic(file.getBytes());
        }

        // password change
        if (oldPassword != null && !oldPassword.isEmpty()) {
            if (!passwordEncoder.matches(oldPassword, user
.getPassword())) {
                model.addAttribute("error", "Old password incorrect");
                model.addAttribute("user", user
);
                return "editProfile";
            }
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("error", "Passwords do not match");
                model.addAttribute("user", user
);
                return "editProfile";
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        // DOB
        if (dob != null) {
            user.setDob(dob);
        }
        userRepository.save(user);
        session.setAttribute("loggedUser", user);
        return "redirect:/profile";
    }


    /* ---------------- LOGIN ---------------- */

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        UserMaster user = userRepository.findByEmail(email);

        if (user == null) {
            model.addAttribute("error", "User not found");
            return "login";
        }

        // CORRECT PASSWORD CHECK
        if (!passwordEncoder.matches(password, user.getPassword())) {
            model.addAttribute("error", "Invalid password");
            return "login";
        }
        System.out.println("👉Email : "+user.getEmail());
        System.out.println("✔password : "+ user.getPassword());

        session.setAttribute("loggedUser", user);
        return "redirect:/home";
    }


    /* ---------------- REGISTRATION + OTP ---------------- */

    @PostMapping("/register")
    public String register(@RequestParam String email,
                           @RequestParam String password,
                           HttpSession session,
                           Model model) throws IOException {

        if (userRepository.findByEmail(email) != null) {
            model.addAttribute("error", "Email already registered");
            return "login";
        }

        // Validate password presence
        if (password == null || password.isBlank()) {
            model.addAttribute("error", "Password is required");
            return "login";
        }

        UserMaster user = new UserMaster();
        user.setEmail(email.toLowerCase());
        user.setPassword(passwordEncoder.encode(password));
        user.setCreatedDate(LocalDate.now());
        user.setRole(UserMaster.Role.USER);
        user.setStatus(true);

        System.out.println("Password before encode: " + password);

        byte[] img = new ClassPathResource("static/images/default.png")
                .getInputStream().readAllBytes();
        user.setProfile_pic(img);

        session.setAttribute("pendingUser", user);

        // Generate OTP
        int otp = 100000 + new Random().nextInt(900000);
        session.setAttribute("otp", otp);
        session.setAttribute("otpExpiry", System.currentTimeMillis() + 2 * 60 * 1000);

        System.out.println("🚀 Registration OTP : " + otp);

        emailService.sendOtpEmail(user.getEmail(), String.valueOf(otp),"register");

        return "verifyOTP";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String otp,
                            HttpSession session,
                            Model model) {

        Integer storedOtp = (Integer) session.getAttribute("otp");
        Long expiry = (Long) session.getAttribute("otpExpiry");
        UserMaster user = (UserMaster) session.getAttribute("pendingUser");

        //  OTP not found
        if (storedOtp == null || expiry == null) {
            model.addAttribute("error", "OTP not found. Please request again.");
            return "login";
        }

        //  OTP expired
        if (System.currentTimeMillis() > expiry) {
            session.removeAttribute("otp");
            session.removeAttribute("otpExpiry");
            session.removeAttribute("pendingUser");

            model.addAttribute("error", "OTP expired. Please request a new one.");
            return "verifyOtp";
        }

        //  Validate numeric OTP safely
        int enteredOtp;
        try {
            enteredOtp = Integer.parseInt(otp);
        } catch (NumberFormatException e) {
            model.addAttribute("error", "OTP must be numeric.");
            return "verifyOtp";
        }

        //  OTP mismatch
        if (!storedOtp.equals(enteredOtp)) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            model.addAttribute("error", "Invalid OTP. Please try again.");
            return "verifyOtp";
        }

        // OTP matched — save user
        if (user != null) {
            userRepository.save(user);
        }

        //  Clean session
        session.removeAttribute("otp");
        session.removeAttribute("otpExpiry");
        session.removeAttribute("pendingUser");

        //  Redirect to success page
        return "redirect:/verifiedOtp";
    }

    @GetMapping("/verifiedOtp")
    public String verifiedOtpPage() {
        return "verifiedOtp";
  }

    /* ---------------- DELETE ACCOUNT ---------------- */

    @PostMapping("/deleteAccount")
    @ResponseBody
    public ResponseEntity<?> deleteAccount(HttpSession session) {
        UserMaster user = (UserMaster) session.getAttribute("loggedUser");
        if (user == null)
            return ResponseEntity.status(401).body(Map.of("success", false));

        user.setStatus(false);
        userRepository.save(user);
        session.invalidate();

        return ResponseEntity.ok(Map.of("success", true));
    }
    // API endpoint to check if email is already registered
    @GetMapping("/api/check-email")
    @ResponseBody
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.ok(new java.util.HashMap<String, Object>() {
                {
                    put("available", false);
                    put("message", "Email cannot be empty");
                }
            });
        }

        UserMaster existingUser = userRepository.findByEmail(email.trim());
        boolean available = existingUser == null;
        String message = available ? "Email is available" : "Email is already registered";

        return ResponseEntity.ok(new java.util.HashMap<String, Object>() {
            {
                put("available", available);
                put("message", message);
            }
        });
    }

    // FORGOT PASSWORD
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgotPassword";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email,
                                 HttpSession session,
                                 Model model) {

        UserMaster user = userRepository.findByEmail(email.toLowerCase().trim());

        if (user == null) {
            model.addAttribute("error", "Email not found!");
            return "forgotPassword";
        }

        // Generate reset OTP
        int resetOtp = 100000 + new Random().nextInt(900000);
        session.setAttribute("resetOtp", resetOtp);
        session.setAttribute("resetOtpExpiry", System.currentTimeMillis() + 5 * 60 * 1000);
        session.setAttribute("resetUser", user);
        session.setAttribute("resetEmail", email.toLowerCase());

        // Send OTP email
        emailService.sendOtpEmail(
                user.getEmail(),
                String.valueOf(resetOtp),
                "reset"
        );


        model.addAttribute("message", "OTP sent to your email!");
        model.addAttribute("email", email);
        System.out.println("Reset OTP is " + resetOtp);
        return "resetPassword";
    }

    @PostMapping("/verify-reset-otp")
    @ResponseBody
    public ResponseEntity<?> verifyResetOtp(@RequestParam String otp,
                                            HttpSession session) {

        Integer storedResetOtp = (Integer) session.getAttribute("resetOtp");
        Long resetOtpExpiry = (Long) session.getAttribute("resetOtpExpiry");

        // Check if OTP exists
        if (storedResetOtp == null || resetOtpExpiry == null) {
            return ResponseEntity.status(400).body(Map.of("success", false, "message", "OTP not sent or expired."));
        }

        // Check expiry
        if (System.currentTimeMillis() > resetOtpExpiry) {
            session.removeAttribute("resetOtp");
            session.removeAttribute("resetOtpExpiry");
            return ResponseEntity.status(400).body(Map.of("success", false, "message", "OTP expired. Please request again."));
        }

        // Convert and compare OTP
        try {
            int enteredOtp = Integer.parseInt(otp);
            if (enteredOtp == storedResetOtp) {
                session.setAttribute("otpVerified", true);
                return ResponseEntity.ok(Map.of("success", true, "message", "OTP verified successfully!"));
            } else {
                return ResponseEntity.status(400).body(Map.of("success", false, "message", "Invalid OTP."));
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.status(400).body(Map.of("success", false, "message", "Invalid OTP format."));
        }
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                HttpSession session,
                                Model model) {

        Boolean otpVerified = (Boolean) session.getAttribute("otpVerified");
        UserMaster resetUser = (UserMaster) session.getAttribute("resetUser");

        if (otpVerified == null || !otpVerified || resetUser == null) {
            model.addAttribute("error", "Please verify OTP first!");
            return "resetPassword";
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            model.addAttribute("error", "Password cannot be empty!");
            return "resetPassword";
        }

        if (newPassword.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters!");
            return "resetPassword";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match!");
            return "resetPassword";
        }

        // Update password in database
        resetUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(resetUser);

        // Clear reset session attributes
        session.removeAttribute("resetOtp");
        session.removeAttribute("resetOtpExpiry");
        session.removeAttribute("resetUser");
        session.removeAttribute("resetEmail");
        session.removeAttribute("otpVerified");

        model.addAttribute("successMessage", "Password reset successfully! Please login with your new password.");
        return "login";
    }

    @PostMapping("/resend-reset-otp")
    @ResponseBody
    public ResponseEntity<?> resendResetOtp(HttpSession session) {
        UserMaster resetUser = (UserMaster) session.getAttribute("resetUser");

        if (resetUser == null) {
            return ResponseEntity.status(400).body(Map.of("success", false, "message", "No password reset in progress."));
        }

        try {
            // Generate new OTP
            int resetOtp = 100000 + new Random().nextInt(900000);
            session.setAttribute("resetOtp", resetOtp);
            session.setAttribute("resetOtpExpiry", System.currentTimeMillis() + 5 * 60 * 1000);

            // Send email
            String subject = "Password Reset OTP (Resend)";
            String body = "Your OTP for password reset is: " + resetOtp + "\nValid for 5 minutes.";
            emailService.sendEmail(resetUser.getEmail(), subject, body);

            return ResponseEntity.ok(Map.of("success", true, "message", "OTP resent to your email."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error resending OTP."));
        }
    }
}
