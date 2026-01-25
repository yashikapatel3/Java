package com.example.kaisi_lagi.UserMaster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<UserMaster> getAllUsers() {
        return userRepository.findAll();
    }

    public void blockUser(Long id) {
        UserMaster user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(true);
        userRepository.save(user);
    }

    public void unblockUser(Long id) {
        UserMaster user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(false);
        userRepository.save(user);
    }
}
