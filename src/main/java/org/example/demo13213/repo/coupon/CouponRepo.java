package org.example.demo13213.repo.coupon;

import org.example.demo13213.model.dao.Coupons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepo extends JpaRepository<Coupons, Long> {
    @Query("SELECT c FROM Coupons c WHERE c.name = :name AND c.isActive = :isActive")
    Optional<Coupons> findCoupons(@Param("name") String name,
                                  @Param("isActive") Boolean isActive);
}