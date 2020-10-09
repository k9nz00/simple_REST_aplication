package semkalearn.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import semkalearn.domain.User;

public interface UserDetailsRepo extends JpaRepository<User, String> {
}
