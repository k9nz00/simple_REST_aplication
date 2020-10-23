package semkalearn.repo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import semkalearn.domain.Message;

import java.util.List;

public interface MessageRepo  extends JpaRepository<Message, Long>{

    @EntityGraph(attributePaths = {"comments"})
    public List<Message> findAll();
}
