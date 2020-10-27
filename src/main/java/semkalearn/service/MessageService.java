package semkalearn.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import semkalearn.domain.Message;
import semkalearn.domain.User;
import semkalearn.domain.UserSubscription;
import semkalearn.domain.Views;
import semkalearn.dto.EventType;
import semkalearn.dto.MessagePageDto;
import semkalearn.dto.MetaDto;
import semkalearn.dto.ObjectType;
import semkalearn.repo.MessageRepo;
import semkalearn.repo.UserSubscriptionRepo;
import semkalearn.util.WsSender;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private static String URL_PATTERN = "https?:\\/\\/?[\\w\\d\\._\\-%\\/\\?=&#]+";
    private static String IMAGE_PATTERN = "\\.(jpeg|jpg|gif|png)$";

    private static Pattern URL_REGEX = Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE);
    private static Pattern IMG_REGEX = Pattern.compile(IMAGE_PATTERN, Pattern.CASE_INSENSITIVE);

    private final MessageRepo messageRepo;
    private final UserSubscriptionRepo userSubscriptionRepo;
    private final BiConsumer<EventType, Message> wsSender;

    @Autowired
    public MessageService(
            MessageRepo messageRepo,
            UserSubscriptionRepo userSubscriptionRepo,
            WsSender wsSender
    ) {
        this.messageRepo = messageRepo;
        this.userSubscriptionRepo = userSubscriptionRepo;
        this.wsSender = wsSender.getSender(ObjectType.MESSAGE, Views.Id.class);
    }

    private void fillMeta(Message message) throws IOException {
        String text = message.getText();
        Matcher matcher = URL_REGEX.matcher(text);

        if (matcher.find()) {
            String url = text.substring(matcher.start(), matcher.end());
            message.setLink(url);

            matcher = IMG_REGEX.matcher(url);
            if (matcher.find()) {
                message.setLinkCover(url);

                //обработка не ютубовских страниц
            } else if (!url.contains("youtu")) {
                MetaDto meta = this.getMeta(url);

                message.setLinkCover(meta.getCover());
                message.setLinkTitle(meta.getTitle());
                message.setLinkDescription(meta.getDescription());
            }
        }
    }

    private MetaDto getMeta(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();

        Elements title = doc.select("meta[name$=title],meta[property$=title]");
        Elements description = doc.select("meta[name$=description],meta[property$=description]");
        Elements cover = doc.select("meta[name$=image],meta[property$=image]");

        return new MetaDto(
                this.getContent(title.first()),
                this.getContent(description.first()),
                this.getContent(cover.first())
        );
    }

    private String getContent(Element element) {
        return element == null ? "" : element.attr("content");
    }

    public void delete(Message message) {
        this.messageRepo.delete(message);
        this.wsSender.accept(EventType.REMOVE, message);
    }

    public Message update(Message messageFromDb, Message message) throws IOException {
        BeanUtils.copyProperties(message, messageFromDb, "id");
        fillMeta(messageFromDb);

        Message updatedMessage = this.messageRepo.save(messageFromDb);
        this.wsSender.accept(EventType.UPDATE, updatedMessage);
        return updatedMessage;
    }

    public Message create(Message message, User user) throws IOException {
        message.setCreationDate(LocalDateTime.now());
        fillMeta(message);
        message.setAuthor(user);
        Message updatedMessage = this.messageRepo.save(message);
        this.wsSender.accept(EventType.CREATE, updatedMessage);
        return updatedMessage;
    }

    public MessagePageDto findForUser(Pageable pageable, User user) {
        List<User> channels = userSubscriptionRepo.findBySubscriber(user)
                .stream()
                .filter(UserSubscription::isActive)
                .map(UserSubscription::getChannel)
                .collect(Collectors.toList());

        channels.add(user);

        Page<Message> page = messageRepo.findByAuthorIn(channels, pageable);

        return new MessagePageDto(
                page.getContent(),
                pageable.getPageNumber(),
                page.getTotalPages()
        );
    }
}
