package semkalearn.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import semkalearn.domain.Message;

public interface MessageRepo  extends JpaRepository<Message, Long>{

    @EntityGraph(attributePaths = {"comments"})
    Page<Message> findAll(Pageable pageable);
}
