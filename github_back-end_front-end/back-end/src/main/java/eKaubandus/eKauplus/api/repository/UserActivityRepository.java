package eKaubandus.eKauplus.api.repository;

import eKaubandus.eKauplus.api.entity.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity,Long> {

    List<UserActivity> findByUserId(Long userId);

    List<UserActivity> findByUserIdAndActivityType(Long userId, UserActivity.ActivityType activityType);

    List<UserActivity> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime timestamp);

    List<UserActivity> findByUserIdAndActivityTypeAndCreatedAtAfter(
            Long userId, UserActivity.ActivityType activityType, LocalDateTime timestamp);
}
