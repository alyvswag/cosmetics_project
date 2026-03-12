package org.example.repo.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.example.model.dao.ProductInventory;

import java.util.Optional;

@Repository
public interface ProductInventoryRepo extends JpaRepository<ProductInventory, Long> {
    @Query("Select p From ProductInventory p WHERE p.productId=:id")
    Optional<ProductInventory> findByIdForProductQuantity(@Param("id") Long id);
}