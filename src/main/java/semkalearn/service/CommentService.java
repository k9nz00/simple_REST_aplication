package semkalearn.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import semkalearn.domain.Comment;
import semkalearn.domain.User;
import semkalearn.repo.CommentRepo;

@Service
public class CommentService {

    private final CommentRepo commentRepo;

    @Autowired
    public CommentService(CommentRepo commentRepo) {
        this.commentRepo = commentRepo;
    }

    public Comment create(Comment comment, User user) {
        comment.setAuthor(user);
        this.commentRepo.save(comment);

        return comment;
    }
}
