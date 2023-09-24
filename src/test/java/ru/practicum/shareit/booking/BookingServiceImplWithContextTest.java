package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = "classpath:data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class BookingServiceImplWithContextTest {

    private final EntityManager em;
    private final BookingService bookingService;
    private final ItemService itemService;
    private final UserService userService;

    private ItemDto itemDto;
    private Item item;
    private User user;
    private UserDto userDto;
    private UserDto userDto2;
    private BookingDtoIn bookingDtoIn;
    private Booking booking;

    @BeforeEach
    void beforeEach() {
        user = new User(1L, "Иван Иванович", "ii@mail.ru");
        userDto = new UserDto(1L, "Иван Иванович", "ii@mail.ru");
        userDto2 = new UserDto(2L, "Петр Петрович", "pp@mail.ru");
        itemDto = new ItemDto(1L, "Вещь 1", "Описание вещи 1", true, null);
        item = new Item(1L, "Вещь 1", "Описание вещи 1", true, null, null);
        bookingDtoIn = new BookingDtoIn(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusDays(1),
                itemDto.getId());
        booking = new Booking(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusDays(1),
                item, user, BookingStatus.APPROVED);
        userService.createUser(userDto);
        userService.createUser(userDto2);
        itemService.createItem(userDto.getId(), itemDto);
    }

    @Test
    void shouldFindAllUserBookings() {
        bookingService.saveBooking(userDto2.getId(), bookingDtoIn, BookingStatus.WAITING);

        long userId = 2L;
        int from = 0;
        int size = 5;
        BookingState bookingState = BookingState.ALL;

        List<BookingDtoOut> dbBookingList = bookingService.findUserBookings(userId, bookingState, from, size);

        TypedQuery<Booking> query = em.createQuery("select b " +
                "from Booking b " +
                "where b.booker.id = :id " +
                "order by b.start desc", Booking.class);
        List<Booking> itemsFromDb = query.setParameter("id", userId)
                .getResultList();

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(itemsFromDb.get(0).getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(itemsFromDb.get(0).getBooker().getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(itemsFromDb.get(0).getItem().getId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(itemsFromDb.get(0).getStatus()));

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(userDto2.getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(bookingDtoIn.getItemId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
    }

    @Test
    void shouldFindCurrentUserBookings() {
        bookingDtoIn.setStart(LocalDateTime.now().minusDays(1));
        bookingService.saveBooking(userDto2.getId(), bookingDtoIn, BookingStatus.WAITING);
        // старт в прошлом, окончание в будущем

        long userId = 2L;
        int from = 0;
        int size = 5;
        BookingState bookingState = BookingState.CURRENT;

        List<BookingDtoOut> dbBookingList = bookingService.findUserBookings(userId, bookingState, from, size);

        TypedQuery<Booking> query = em.createQuery("select b " +
                "from Booking as b " +
                "join b.booker as bo " +
                "where bo.id = :id and b.start < :time and b.end > :time " +
                "order by b.start desc ", Booking.class);
        List<Booking> itemsFromDb = query.setParameter("id", userId)
                .setParameter("time", LocalDateTime.now())
                .getResultList();

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(itemsFromDb.get(0).getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(itemsFromDb.get(0).getBooker().getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(itemsFromDb.get(0).getItem().getId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(itemsFromDb.get(0).getStatus()));

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(userDto2.getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(bookingDtoIn.getItemId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
    }

    @Test
    void shouldFindPastUserBookings() {
        bookingDtoIn.setStart(LocalDateTime.now().minusDays(1));
        bookingDtoIn.setEnd(LocalDateTime.now().minusHours(1));
        bookingService.saveBooking(userDto2.getId(), bookingDtoIn, BookingStatus.APPROVED);
        // старт  в прошлом, окончание в прошлом, статус Appoved

        long userId = 2L;
        int from = 0;
        int size = 5;
        BookingState bookingState = BookingState.PAST;

        List<BookingDtoOut> dbBookingList = bookingService.findUserBookings(userId, bookingState, from, size);

        TypedQuery<Booking> query = em.createQuery("select b " +
                "from Booking as b " +
                "join b.booker as bo " +
                "where bo.id = :id and b.end < :time and b.status = 'APPROVED' " +
                "order by b.start desc ", Booking.class);
        List<Booking> itemsFromDb = query.setParameter("id", userId)
                .setParameter("time", LocalDateTime.now())
                .getResultList();

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(itemsFromDb.get(0).getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(itemsFromDb.get(0).getBooker().getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(itemsFromDb.get(0).getItem().getId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(itemsFromDb.get(0).getStatus()));

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(userDto2.getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(bookingDtoIn.getItemId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(BookingStatus.APPROVED));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
    }

    @Test
    void shouldFindFutureUserBookings() {
        bookingDtoIn.setStart(LocalDateTime.now().plusHours(1));
        bookingDtoIn.setEnd(LocalDateTime.now().plusDays(1));
        bookingService.saveBooking(userDto2.getId(), bookingDtoIn, BookingStatus.APPROVED);
        // старт  в прошлом, окончание в прошлом, статус Appoved

        long userId = 2L;
        int from = 0;
        int size = 5;
        BookingState bookingState = BookingState.FUTURE;

        List<BookingDtoOut> dbBookingList = bookingService.findUserBookings(userId, bookingState, from, size);

        TypedQuery<Booking> query = em.createQuery("select b " +
                "from Booking as b " +
                "join b.booker as bo " +
                "where bo.id = :id and b.start > :time " +
                "order by b.start desc ", Booking.class);
        List<Booking> itemsFromDb = query.setParameter("id", userId)
                .setParameter("time", LocalDateTime.now())
                .getResultList();

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(itemsFromDb.get(0).getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(itemsFromDb.get(0).getBooker().getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(itemsFromDb.get(0).getItem().getId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(itemsFromDb.get(0).getStatus()));

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(userDto2.getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(bookingDtoIn.getItemId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(BookingStatus.APPROVED));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
    }

    @Test
    void shouldFindWaitingUserBookings() {
        bookingService.saveBooking(userDto2.getId(), bookingDtoIn, BookingStatus.WAITING);

        long userId = 2L;
        int from = 0;
        int size = 5;
        BookingState bookingState = BookingState.WAITING;

        List<BookingDtoOut> dbBookingList = bookingService.findUserBookings(userId, bookingState, from, size);

        TypedQuery<Booking> query = em.createQuery("select b " +
                "from Booking as b " +
                "join b.booker as bo " +
                "where bo.id = :id and b.status = 'WAITING' " +
                "order by b.start desc ", Booking.class);
        List<Booking> itemsFromDb = query.setParameter("id", userId)
                .getResultList();

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(itemsFromDb.get(0).getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(itemsFromDb.get(0).getBooker().getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(itemsFromDb.get(0).getItem().getId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(itemsFromDb.get(0).getStatus()));

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(userDto2.getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(bookingDtoIn.getItemId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
    }

    @Test
    void shouldFindRejectedUserBookings() {
        bookingService.saveBooking(userDto2.getId(), bookingDtoIn, BookingStatus.REJECTED);

        long userId = 2L;
        int from = 0;
        int size = 5;
        BookingState bookingState = BookingState.REJECTED;

        List<BookingDtoOut> dbBookingList = bookingService.findUserBookings(userId, bookingState, from, size);

        TypedQuery<Booking> query = em.createQuery("select b " +
                "from Booking as b " +
                "join b.booker as bo " +
                "where bo.id = :id and b.status = 'REJECTED' " +
                "order by b.start desc ", Booking.class);
        List<Booking> itemsFromDb = query.setParameter("id", userId)
                .getResultList();

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(itemsFromDb.get(0).getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(itemsFromDb.get(0).getBooker().getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(itemsFromDb.get(0).getItem().getId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(itemsFromDb.get(0).getStatus()));

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(userDto2.getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(bookingDtoIn.getItemId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(BookingStatus.REJECTED));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
    }

    @Test
    void shouldFindOwnerBookings() {
        bookingService.saveBooking(userDto2.getId(), bookingDtoIn, BookingStatus.WAITING);
        // старт  в будущем, окончание в будущем

        long userId = 1L;
        int from = 0;
        int size = 5;
        BookingState bookingState = BookingState.ALL;

        List<BookingDtoOut> dbBookingList = bookingService.findOwnerBookings(userId, bookingState, from, size);

        TypedQuery<Booking> query = em.createQuery("select b " +
                "from Booking as b " +
                "join b.item as i " +
                "join i.user as u " +
                "where u.id = :id " +
                "order by b.start desc", Booking.class);
        List<Booking> itemsFromDb = query.setParameter("id", userId)
                .getResultList();

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(itemsFromDb.get(0).getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(itemsFromDb.get(0).getBooker().getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(itemsFromDb.get(0).getItem().getId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(itemsFromDb.get(0).getStatus()));

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(userDto2.getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(bookingDtoIn.getItemId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
    }

    @Test
    void shouldFindCurrentOwnerBookings() {
        bookingDtoIn.setStart(LocalDateTime.now().minusDays(1));
        bookingService.saveBooking(userDto2.getId(), bookingDtoIn, BookingStatus.WAITING);
        // старт  в прошлом, окончание в будущем

        long userId = 1L;
        int from = 0;
        int size = 5;
        BookingState bookingState = BookingState.CURRENT;

        List<BookingDtoOut> dbBookingList = bookingService.findOwnerBookings(userId, bookingState, from, size);

        TypedQuery<Booking> query = em.createQuery("select b " +
                "from Booking as b " +
                "join b.item as i " +
                "join i.user as u " +
                "where u.id = :id and b.start < :time and b.end > :time " +
                "order by b.start desc", Booking.class);
        List<Booking> itemsFromDb = query.setParameter("id", userId)
                .setParameter("time", LocalDateTime.now())
                .getResultList();

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(itemsFromDb.get(0).getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(itemsFromDb.get(0).getBooker().getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(itemsFromDb.get(0).getItem().getId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(itemsFromDb.get(0).getStatus()));

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(userDto2.getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(bookingDtoIn.getItemId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
    }

    @Test
    void shouldFindPastOwnerBookings() {
        bookingDtoIn.setStart(LocalDateTime.now().minusDays(1));
        bookingDtoIn.setEnd(LocalDateTime.now().minusHours(1));
        bookingService.saveBooking(userDto2.getId(), bookingDtoIn, BookingStatus.APPROVED);
        // старт  в прошлом, окончание в прошлом, статус Appoved

        long userId = 1L;
        int from = 0;
        int size = 5;
        BookingState bookingState = BookingState.PAST;

        List<BookingDtoOut> dbBookingList = bookingService.findOwnerBookings(userId, bookingState, from, size);

        TypedQuery<Booking> query = em.createQuery("select b " +
                "from Booking as b " +
                "join b.item as i " +
                "join i.user as u " +
                "where u.id = :id and b.end < :time and b.status = 'APPROVED' " +
                "order by b.start desc ", Booking.class);
        List<Booking> itemsFromDb = query.setParameter("id", userId)
                .setParameter("time", LocalDateTime.now())
                .getResultList();

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(itemsFromDb.get(0).getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(itemsFromDb.get(0).getBooker().getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(itemsFromDb.get(0).getItem().getId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(itemsFromDb.get(0).getStatus()));

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(userDto2.getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(bookingDtoIn.getItemId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(BookingStatus.APPROVED));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
    }

    @Test
    void shouldFindFutureOwnerBookings() {
        bookingDtoIn.setStart(LocalDateTime.now().plusHours(1));
        bookingDtoIn.setEnd(LocalDateTime.now().plusDays(1));
        bookingService.saveBooking(userDto2.getId(), bookingDtoIn, BookingStatus.APPROVED);
        // старт  в прошлом, окончание в прошлом, статус Appoved

        long userId = 1L;
        int from = 0;
        int size = 5;
        BookingState bookingState = BookingState.FUTURE;

        List<BookingDtoOut> dbBookingList = bookingService.findOwnerBookings(userId, bookingState, from, size);

        TypedQuery<Booking> query = em.createQuery("select b " +
                "from Booking as b " +
                "join b.item as i " +
                "join i.user as u " +
                "where u.id = :id and b.start > :time " +
                "order by b.start desc ", Booking.class);
        List<Booking> itemsFromDb = query.setParameter("id", userId)
                .setParameter("time", LocalDateTime.now())
                .getResultList();

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(itemsFromDb.get(0).getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(itemsFromDb.get(0).getBooker().getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(itemsFromDb.get(0).getItem().getId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(itemsFromDb.get(0).getStatus()));

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(userDto2.getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(bookingDtoIn.getItemId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(BookingStatus.APPROVED));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
    }

    @Test
    void shouldFindWaitingOwnerBookings() {
        bookingService.saveBooking(userDto2.getId(), bookingDtoIn, BookingStatus.WAITING);

        long userId = 1L;
        int from = 0;
        int size = 5;
        BookingState bookingState = BookingState.WAITING;

        List<BookingDtoOut> dbBookingList = bookingService.findOwnerBookings(userId, bookingState, from, size);

        TypedQuery<Booking> query = em.createQuery("select b " +
                "from Booking as b " +
                "join b.item as i " +
                "join i.user as u " +
                "where u.id = :id and b.status = 'WAITING' " +
                "order by b.start desc ", Booking.class);
        List<Booking> itemsFromDb = query.setParameter("id", userId)
                .getResultList();

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(itemsFromDb.get(0).getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(itemsFromDb.get(0).getBooker().getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(itemsFromDb.get(0).getItem().getId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(itemsFromDb.get(0).getStatus()));

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(userDto2.getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(bookingDtoIn.getItemId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
    }

    @Test
    void shouldFindRejectedOwnerBookings() {
        bookingService.saveBooking(userDto2.getId(), bookingDtoIn, BookingStatus.REJECTED);

        long userId = 1L;
        int from = 0;
        int size = 5;
        BookingState bookingState = BookingState.REJECTED;

        List<BookingDtoOut> dbBookingList = bookingService.findOwnerBookings(userId, bookingState, from, size);

        TypedQuery<Booking> query = em.createQuery("select b " +
                "from Booking as b " +
                "join b.item as i " +
                "join i.user as u " +
                "where u.id = :id and b.status = 'REJECTED' " +
                "order by b.start desc ", Booking.class);
        List<Booking> itemsFromDb = query.setParameter("id", userId)
                .getResultList();

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(itemsFromDb.get(0).getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(itemsFromDb.get(0).getBooker().getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(itemsFromDb.get(0).getItem().getId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(itemsFromDb.get(0).getStatus()));

        assertThat(dbBookingList.size(), equalTo(1));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
        assertThat(dbBookingList.get(0).getEnd(), notNullValue());
        assertThat(dbBookingList.get(0).getStart(), notNullValue());
        assertThat(dbBookingList.get(0).getBooker().getId(), equalTo(userDto2.getId()));
        assertThat(dbBookingList.get(0).getItem().getId(), equalTo(bookingDtoIn.getItemId()));
        assertThat(dbBookingList.get(0).getStatus(), equalTo(BookingStatus.REJECTED));
        assertThat(dbBookingList.get(0).getId(), equalTo(bookingDtoIn.getId()));
    }

    @Test
    void shouldFindUserBookingsWithIdNotFound() {
        bookingService.saveBooking(userDto2.getId(), bookingDtoIn, BookingStatus.WAITING);

        long userId = 3L;
        int from = 0;
        int size = 5;
        BookingState bookingState = BookingState.ALL;
        try {
            bookingService.findUserBookings(userId, bookingState, from, size);
        } catch (NotFoundException thrown) {
            assertThat(thrown.getMessage(), equalTo("Пользователь с ID " + userId + " не зарегистрирован"));
        }
    }

    @Test
    void shouldFindOwnerBookingsWithIdNotFound() {
        bookingService.saveBooking(userDto2.getId(), bookingDtoIn, BookingStatus.WAITING);

        long userId = 3L;
        int from = 0;
        int size = 5;
        BookingState bookingState = BookingState.ALL;
        try {
            bookingService.findOwnerBookings(userId, bookingState, from, size);
        } catch (NotFoundException thrown) {
            assertThat(thrown.getMessage(), equalTo("Пользователь с ID " + userId + " не зарегистрирован"));
        }
    }
}