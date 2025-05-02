package eKaubandus.eKauplus.api.repository;

import eKaubandus.eKauplus.api.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

    List<Product> findByCategory(String category);

    List<Product> findByBrand(String brand);

    @Query("SELECT p FROM Product p WHERE " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:brand IS NULL OR p.brand = :brand) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Product> findWithFilters(String category, String brand,
                                  BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> findByIsNewTrue();

   public abstract List<Product> findTop10ByOrderBySoldCountDesc();
}
