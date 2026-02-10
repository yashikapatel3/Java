package com.example.kaisi_lagi.BadgeMaster;

import com.example.kaisi_lagi.CategoryMaster.CategoryMaster;
import jakarta.persistence.*;
import org.springframework.web.multipart.MultipartFile;

@Entity
@Table(name = "badge_master")
public class BadgeMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="badge_id", nullable = false)
    private Long badgeId;

    @Column(name = "badge_name", nullable = false)
    private String name;

    @Column(nullable = false)
    private int requiredReviews;  // e.g. 5, 10, 20

    @Column(length = 100000) // no @Lob
    private byte[] icon;

    @Transient
    private MultipartFile iconFile;           // image filename

    @ManyToOne(optional = false)
    @JoinColumn(name = "cate_id", nullable = false)
    private CategoryMaster category;


    public Long getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(Long badgeId) {
        this.badgeId = badgeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRequiredReviews() {
        return requiredReviews;
    }

    public void setRequiredReviews(int requiredReviews) {
        this.requiredReviews = requiredReviews;
    }

    public byte[] getIcon() {
        return icon;
    }

    public void setIcon(byte[] icon) {
        this.icon = icon;
    }

    public CategoryMaster getCategory() {
        return category;
    }

    public void setCategory(CategoryMaster category) {
        this.category = category;
    }

    public MultipartFile getIconFile() {
        return iconFile;
    }

    public void setIconFile(MultipartFile iconFile) {
        this.iconFile = iconFile;
    }
}
