package org.example.model.dao;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "user_coupons", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_coupons_user_coupon", columnNames = {"user_id", "coupon_id"})
})
public class UserCoupons {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_coupons_user"))
    Users user;

    @ManyToOne
    @JoinColumn(name = "coupon_id", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_coupons_coupon"))
    Coupons coupon;

    @Column(name = "activated_at", nullable = false)
    OffsetDateTime activatedAt;

    @Column(name = "is_active", nullable = false)
    Boolean isActive = true;

    @Column(name = "used_at")
    Timestamp usedAt;
}