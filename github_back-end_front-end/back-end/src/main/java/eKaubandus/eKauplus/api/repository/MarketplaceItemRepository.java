package eKaubandus.eKauplus.api.repository;

import eKaubandus.eKauplus.api.entity.MarketplaceItem;
import eKaubandus.eKauplus.api.entity.MarketplaceItem.MarketplaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketplaceItemRepository extends JpaRepository<MarketplaceItem, Long> {

    List<MarketplaceItem> findByUserId(Long userId);

    List<MarketplaceItem> findByStatus(MarketplaceStatus status);

    List<MarketplaceItem> findByCategory(String category);

    List<MarketplaceItem> findByUserIdAndStatus(Long userId, MarketplaceStatus status);
}
