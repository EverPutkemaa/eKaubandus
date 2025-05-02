package eKaubandus.eKauplus.api.repository;


import eKaubandus.eKauplus.api.entity.CartItem;
import eKaubandus.eKauplus.api.entity.CartItem.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Long> {

    List<CartItem> findByUserId(Long userId);

    Optional<CartItem> findByUserIdAndItemTypeAndItemId(Long userId, CartItem.ItemType itemType, Long itemId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.user.id = :userId")
    void deleteAllByUserId(Long userId);
}
