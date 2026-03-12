package org.example.repo.coupon;

import org.example.model.dao.UserCoupons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCouponRepo extends JpaRepository<UserCoupons, Long> {
    @Query("SELECT uc FROM UserCoupons uc WHERE uc.user.id = :userId AND uc.isActive = TRUE")
    List<UserCoupons> findActiveByUserIdForUserCoupon(@Param("userId") Long userId);
}