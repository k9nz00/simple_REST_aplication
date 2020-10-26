package semkalearn.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import semkalearn.domain.Comment;
import semkalearn.domain.User;
import semkalearn.domain.Views;
import semkalearn.dto.EventType;
import semkalearn.dto.ObjectType;
import semkalearn.repo.CommentRepo;
import semkalearn.util.WsSender;

import java.util.function.BiConsumer;

@Service
public class CommentService {

    private final CommentRepo commentRepo;
    private final BiConsumer<EventType, Comment> wsSender;

    @Autowired
    public CommentService(CommentRepo commentRepo, WsSender wsSender) {
        this.commentRepo = commentRepo;
        this.wsSender = wsSender.getSender(ObjectType.COMMENT, Views.FullComment.class);
    }

    public Comment create(Comment comment, User user) {
        comment.setAuthor(user);
        Comment commentFromDb = this.commentRepo.save(comment);

        this.wsSender.accept(EventType.CREATE, commentFromDb);
        return commentFromDb;
    }
}
