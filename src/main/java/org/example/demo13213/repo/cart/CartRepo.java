package org.example.demo13213.repo.cart;

import org.example.demo13213.model.dao.Carts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepo extends JpaRepository<Carts, Long> {
    @Query("Select p From Carts p WHERE p.user.id=:userId")
    Optional<Carts> findByUserIdForCarts(Long userId);
}