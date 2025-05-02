package eKaubandus.eKauplus.api.repository;

import eKaubandus.eKauplus.api.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service,Long> {

    List<Service> findByCategory(String category);

    List<Service> findByIsPopularTrue();

    List<Service> findByIsAvailableTrue();
}
