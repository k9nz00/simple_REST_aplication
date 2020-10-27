package semkalearn.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import semkalearn.domain.User;
import semkalearn.domain.UserSubscription;
import semkalearn.domain.UserSubscriptionId;

import java.util.List;

public interface UserSubscriptionRepo extends JpaRepository<UserSubscription, UserSubscriptionId> {
    List<UserSubscription> findBySubscriber(User user);
}