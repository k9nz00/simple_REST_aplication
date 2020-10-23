package semkalearn.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import semkalearn.domain.Comment;

public interface CommentRepo extends JpaRepository<Comment, Long> {
}
