package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.CommentDtoIn;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoDated;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = "classpath:data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ItemServiceImplWithContextTest {

    private final EntityManager em;

    private final ItemService itemService;

    private final UserService userService;

    private final BookingService bookingService;

    private ItemDto itemDto;
    private BookingDtoIn bookingLastDtoIn;
    private BookingDtoIn bookingNextDtoIn;
    private UserDto userDto;
    private CommentDtoIn comment;
    private Item item;
    private User user;

    @BeforeEach
    void beforeEach() {
        itemDto = new ItemDto(1L, "Вещь 1", "Описание вещи 1", true, null);
        bookingLastDtoIn = new BookingDtoIn(1L, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusHours(5), 1L);
        bookingNextDtoIn = new BookingDtoIn(2L, LocalDateTime.now().plusHours(12),
                LocalDateTime.now().plusDays(1), 1L);
        userDto = new UserDto(1L, "Иван Иванович", "ii@mail.ru");
        user = new User(1L, "Иван Иванович", "ii@mail.ru");
        item = new Item(1L, "Вещь 1", "Описание вещи 1", true, user, null);
        comment = new CommentDtoIn(1L, "Коммент 1");

        userService.createUser(userDto);
    }

    @Test
    void shouldSearch() {
        itemService.createItem(userDto.getId(), itemDto);
        int from = 0;
        int size = 5;
        String text = "ещ";
        List<ItemDto> itemsList = itemService.search(text, from, size);
        TypedQuery<Item> query = em.createQuery("select i from Item i " +
                "where upper(i.name) like upper(concat('%', :text, '%')) " +
                "or upper(i.description) like upper(concat('%', :text, '%')) " +
                "and i.isAvailable = true ", Item.class);
        List<Item> itemsFromDb = query.setParameter("text", text)
                .getResultList();

        assertThat(itemsList.size(), equalTo(itemsFromDb.size()));
        assertThat(itemsList.get(0).getId(), equalTo(itemsFromDb.get(0).getId()));
        assertThat(itemsList.get(0).getName(), equalTo(itemsFromDb.get(0).getName()));
        assertThat(itemsList.get(0).getDescription(), equalTo(itemsFromDb.get(0).getDescription()));
        assertThat(itemsList.get(0).getAvailable(), equalTo(itemsFromDb.get(0).getIsAvailable()));
    }

    @Test
    void shouldSearchForNoItems() {
        itemService.createItem(userDto.getId(), itemDto);
        int from = 0;
        int size = 5;
        String text = "вещьстакойстрокойненайти";
        List<ItemDto> itemsList = itemService.search(text, from, size);

        assertThat(itemsList, empty());
    }

    @Test
    void shouldGetItemByIdWhenUserIsOwnerOfItem() {
        long userId = 1L;
        long itemId = 1L;
        UserDto user2 = new UserDto(2L, "Петр Петрович", "pp@mail.ru");
        userService.createUser(user2);
        itemService.createItem(userDto.getId(), itemDto);
        bookingService.saveBooking(2L, bookingLastDtoIn, BookingStatus.APPROVED);
        bookingService.saveBooking(2L, bookingNextDtoIn, BookingStatus.APPROVED);

        ItemDtoDated methodItem = itemService.getItemById(userId, itemId);

        TypedQuery<Item> query = em.createQuery("select i from Item i where i.id = :id", Item.class);
        Item dbItem = query.setParameter("id", userId)
                .getSingleResult();

        assertThat(methodItem.getId(), equalTo(dbItem.getId()));
        assertThat(methodItem.getName(), equalTo(dbItem.getName()));
        assertThat(methodItem.getDescription(), equalTo(dbItem.getDescription()));
        assertThat(methodItem.getAvailable(), equalTo(dbItem.getIsAvailable()));

        assertThat(methodItem.getId(), equalTo(itemId));
        assertThat(methodItem.getName(), equalTo(itemDto.getName()));
        assertThat(methodItem.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(methodItem.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(methodItem.getComments(), empty());

        assertThat(methodItem.getLastBooking().getId(), equalTo(bookingLastDtoIn.getId()));
        assertThat(methodItem.getLastBooking().getEnd(), notNullValue());
        assertThat(methodItem.getLastBooking().getStart(), notNullValue());
        assertThat(methodItem.getLastBooking().getStatus(), equalTo(BookingStatus.APPROVED));
        assertThat(methodItem.getLastBooking().getBookerId(), equalTo(user2.getId()));

        assertThat(methodItem.getNextBooking().getId(), equalTo(bookingNextDtoIn.getId()));
        assertThat(methodItem.getNextBooking().getEnd(), notNullValue());
        assertThat(methodItem.getNextBooking().getStart(), notNullValue());
        assertThat(methodItem.getNextBooking().getStatus(), equalTo(BookingStatus.APPROVED));
        assertThat(methodItem.getNextBooking().getBookerId(), equalTo(user2.getId()));

    }

    @Test
    void shouldGetItemByIdWhenUserIsNotOwnerOfItem() {
        long userId = 2L;
        long itemId = 1L;
        UserDto user2 = new UserDto(2L, "Петр Петрович", "pp@mail.ru");
        userService.createUser(user2);
        itemService.createItem(userDto.getId(), itemDto);
        bookingService.saveBooking(user2.getId(), bookingLastDtoIn, BookingStatus.WAITING);
        bookingService.saveBooking(user2.getId(), bookingNextDtoIn, BookingStatus.WAITING);

        ItemDtoDated methodItem = itemService.getItemById(userId, itemId);

        TypedQuery<Item> query = em.createQuery("select i from Item i where i.id = :id", Item.class);
        Item dbItem = query.setParameter("id", itemId)
                .getSingleResult();

        assertThat(methodItem.getId(), equalTo(dbItem.getId()));
        assertThat(methodItem.getName(), equalTo(dbItem.getName()));
        assertThat(methodItem.getDescription(), equalTo(dbItem.getDescription()));
        assertThat(methodItem.getAvailable(), equalTo(dbItem.getIsAvailable()));

        assertThat(methodItem.getId(), equalTo(itemId));
        assertThat(methodItem.getName(), equalTo(itemDto.getName()));
        assertThat(methodItem.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(methodItem.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(methodItem.getComments(), empty());

        assertThat(methodItem.getLastBooking(), nullValue());

        assertThat(methodItem.getNextBooking(), nullValue());
    }

    @Test
    void shouldGetUserItems() {
        long userId = 1L;
        int from = 0;
        int size = 5;
        UserDto user2 = new UserDto(2L, "Петр Петрович", "pp@mail.ru");
        userService.createUser(user2);

        itemService.createItem(userDto.getId(), itemDto);
        bookingService.saveBooking(user2.getId(), bookingLastDtoIn, BookingStatus.APPROVED);
        bookingService.saveBooking(user2.getId(), bookingNextDtoIn, BookingStatus.APPROVED);
        itemService.saveComment(user2.getId(), itemDto.getId(), comment);

        List<ItemDtoDated> itemsList = itemService.getUserItems(userId, from, size);

        TypedQuery<Item> query = em.createQuery("select i " +
                "from Item i " +
                "where i.user.id = :id", Item.class);
        List<Item> items = query.setParameter("id", userId)
                .getResultList();

        TypedQuery<Comment> query1 = em.createQuery("select c " +
                "from Comment c " +
                "where c.item.user.id = :id " +
                "order by c.created desc ", Comment.class);
        List<Comment> comments = query1.setParameter("id", userId)
                .getResultList();

        TypedQuery<Booking> query2 = em.createQuery("select b " +
                "from Booking b " +
                "where b.item.user.id = :userId and b.item.id in :itemId and b.start < :time " +
                "order by b.start desc ", Booking.class);
        List<Booking> lastBookings = query2.setParameter("userId", userId)
                .setParameter("itemId", List.of(item.getId()))
                .setParameter("time", LocalDateTime.now())
                .getResultList();

        TypedQuery<Booking> query3 = em.createQuery("select b " +
                "from Booking b " +
                "where b.item.user.id = :userId and b.item.id in :itemId and b.start > :time " +
                "order by b.start ", Booking.class);
        List<Booking> nextBookings = query3.setParameter("userId", userId)
                .setParameter("itemId", List.of(item.getId()))
                .setParameter("time", LocalDateTime.now())
                .getResultList();

        assertThat(itemsList.size(), equalTo(1));
        assertThat(itemsList.size(), equalTo(items.size()));
        assertThat(itemsList.get(0).getId(), equalTo(items.get(0).getId()));
        assertThat(itemsList.get(0).getDescription(), equalTo(items.get(0).getDescription()));
        assertThat(itemsList.get(0).getName(), equalTo(items.get(0).getName()));
        assertThat(itemsList.get(0).getAvailable(), equalTo(items.get(0).getIsAvailable()));

        assertThat(itemsList.get(0).getComments().size(), equalTo(comments.size()));
        assertThat(itemsList.get(0).getComments().get(0).getId(), equalTo(comments.get(0).getId()));
        assertThat(itemsList.get(0).getComments().get(0).getText(), equalTo(comments.get(0).getText()));
        assertThat(itemsList.get(0).getComments().get(0).getCreated(), notNullValue());
        assertThat(itemsList.get(0).getComments().get(0).getItem().getId(), equalTo(comments.get(0).getItem().getId()));

        assertThat(itemsList.get(0).getLastBooking().getId(), equalTo(lastBookings.get(0).getId()));
        assertThat(itemsList.get(0).getLastBooking().getStatus(), equalTo(lastBookings.get(0).getStatus()));
        assertThat(itemsList.get(0).getLastBooking().getBookerId(), equalTo(lastBookings.get(0).getBooker().getId()));
        assertThat(itemsList.get(0).getLastBooking().getEnd(), notNullValue());
        assertThat(itemsList.get(0).getLastBooking().getStart(), notNullValue());
        assertThat(lastBookings.get(0).getItem().getId(), equalTo(item.getId()));

        assertThat(itemsList.get(0).getNextBooking().getId(), equalTo(nextBookings.get(0).getId()));
        assertThat(itemsList.get(0).getNextBooking().getStatus(), equalTo(nextBookings.get(0).getStatus()));
        assertThat(itemsList.get(0).getNextBooking().getBookerId(), equalTo(nextBookings.get(0).getBooker().getId()));
        assertThat(itemsList.get(0).getNextBooking().getEnd(), notNullValue());
        assertThat(itemsList.get(0).getNextBooking().getStart(), notNullValue());
        assertThat(nextBookings.get(0).getItem().getId(), equalTo(item.getId()));

        assertThat(itemsList.get(0).getId(), equalTo(itemDto.getId()));
        assertThat(itemsList.get(0).getName(), equalTo(itemDto.getName()));
        assertThat(itemsList.get(0).getDescription(), equalTo(itemDto.getDescription()));
        assertThat(itemsList.get(0).getAvailable(), equalTo(itemDto.getAvailable()));

        assertThat(itemsList.get(0).getLastBooking().getId(), equalTo(bookingLastDtoIn.getId()));
        assertThat(itemsList.get(0).getLastBooking().getEnd(), notNullValue());
        assertThat(itemsList.get(0).getLastBooking().getStart(), notNullValue());
        assertThat(itemsList.get(0).getLastBooking().getStatus(), equalTo(BookingStatus.APPROVED));
        assertThat(itemsList.get(0).getLastBooking().getBookerId(), equalTo(user2.getId()));

        assertThat(itemsList.get(0).getNextBooking().getId(), equalTo(bookingNextDtoIn.getId()));
        assertThat(itemsList.get(0).getNextBooking().getEnd(), notNullValue());
        assertThat(itemsList.get(0).getNextBooking().getStart(), notNullValue());
        assertThat(itemsList.get(0).getNextBooking().getStatus(), equalTo(BookingStatus.APPROVED));
        assertThat(itemsList.get(0).getNextBooking().getBookerId(), equalTo(user2.getId()));
    }
}
