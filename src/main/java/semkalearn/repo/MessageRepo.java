package semkalearn.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import semkalearn.domain.Message;

public interface MessageRepo  extends JpaRepository<Message, Long>{
}
