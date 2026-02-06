package com.example.kaisi_lagi.UserMaster;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserMaster,Long> {
    long countByStatus(boolean status);

//  @Query(value = """
//        SELECT DATE(review_date) AS day, COUNT(*) AS count
//        FROM review_master
//        WHERE review_date >= CURRENT_DATE - INTERVAL '7 days'
//        GROUP BY DATE(review_date)
//        ORDER BY day ASC
//   """, nativeQuery = true)
//    List<Object[]> getDailyReviews();


    @Query(value = """
        SELECT TO_CHAR(created_date, 'Mon') AS month, COUNT(*) AS count
        FROM user_master
        WHERE created_date >= CURRENT_DATE - INTERVAL '12 months'
        GROUP BY TO_CHAR(created_date, 'Mon'), EXTRACT(MONTH FROM created_date)
        ORDER BY EXTRACT(MONTH FROM created_date)
    """, nativeQuery = true)
    List<Object[]> getMonthlyNewUsers();


    UserMaster findByEmailAndPassword(String email,String password);
    UserMaster findByEmail(String email);
    UserMaster findByUsername(String username);
    boolean existsByUsernameIgnoreCase(String username);




}


