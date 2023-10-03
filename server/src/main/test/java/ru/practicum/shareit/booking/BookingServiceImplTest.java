package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.BadParameterException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    private UserRepository userRepository;
    private BookingServiceImpl bookingService;
    private BookingRepository bookingRepository;
    private ItemRepository itemRepository;

    private Item item;
    private User user;
    private User user2;
    private Booking booking;
    private BookingDtoIn bookingDtoIn;


    @BeforeEach
    void beforeEach() {
        user = new User(1L, "Иван Иванович", "ii@mail.ru");
        user2 = new User(2L, "Петр Петрович", "pp@mail.ru");
        item = new Item(1L, "Вещь 1", "Описание вещи 1", true, user, null);
        booking = new Booking(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusDays(1), item,
                user2, BookingStatus.APPROVED);
        bookingDtoIn = new BookingDtoIn(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusDays(1),
                item.getId());

        userRepository = mock(UserRepository.class);
        itemRepository = mock(ItemRepository.class);
        bookingRepository = mock(BookingRepository.class);

        bookingService = new BookingServiceImpl(bookingRepository, itemRepository, userRepository);
    }

    @Test
    void shouldSaveBooking() {
        long userId = 2L;
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user2));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));
        when(bookingRepository.save(any()))
                .thenReturn(booking);

        BookingDtoOut bookingDtoOut = bookingService.saveBooking(userId, bookingDtoIn, BookingStatus.WAITING);

        assertThat(bookingDtoOut.getId(), equalTo(bookingDtoIn.getId()));
        assertThat(bookingDtoOut.getBooker().getId(), equalTo(userId));
        assertThat(bookingDtoOut.getItem().getId(), equalTo(bookingDtoIn.getItemId()));
        assertThat(bookingDtoOut.getStart(), notNullValue());
        assertThat(bookingDtoOut.getEnd(), notNullValue());

        verify(userRepository, times(1))
                .findById(anyLong());
        verify(itemRepository, times(1))
                .findById(anyLong());
        verify(bookingRepository, times(1))
                .save(any());
    }

    @Test
    void shouldSaveBookingWithWrongUserId() {
        long userId = 3L;
        when(userRepository.findById(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с таким ID не зарегистрировано"));
        try {
            bookingService.saveBooking(userId, bookingDtoIn, BookingStatus.WAITING);
        } catch (NotFoundException thrown) {
            assertThat(thrown.getMessage(), equalTo("Пользователь с таким ID не зарегистрировано"));
        }

        verify(userRepository, times(1))
                .findById(anyLong());
        verify(itemRepository, never())
                .findById(anyLong());
        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void shouldSaveBookingWithWrongItemId() {
        long userId = 2L;
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user2));
        when(itemRepository.findById(anyLong()))
                .thenThrow(new NotFoundException("Предмета с ID " + bookingDtoIn.getItemId()
                        + " не зарегистрировано"));
        try {
            bookingService.saveBooking(userId, bookingDtoIn, BookingStatus.WAITING);
        } catch (NotFoundException thrown) {
            assertThat(thrown.getMessage(), equalTo("Предмета с ID " + bookingDtoIn.getItemId()
                    + " не зарегистрировано"));
        }

        verify(userRepository, times(1))
                .findById(anyLong());
        verify(itemRepository, times(1))
                .findById(anyLong());
        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void shouldSaveBookingWithUnavailableItem() {
        long userId = 2L;
        item.setIsAvailable(false);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user2));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));
        try {
            bookingService.saveBooking(userId, bookingDtoIn, BookingStatus.WAITING);
        } catch (BadParameterException thrown) {
            assertThat(thrown.getMessage(), equalTo("вещь недоступна для аренды"));
        }

        verify(userRepository, times(1))
                .findById(anyLong());
        verify(itemRepository, times(1))
                .findById(anyLong());
        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void shouldSaveBookingFromItemOwner() {
        long userId = 1L;
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));
        try {
            bookingService.saveBooking(userId, bookingDtoIn, BookingStatus.WAITING);
        } catch (NotFoundException thrown) {
            assertThat(thrown.getMessage(), equalTo("ошибка: запрос аренды отправлен от владельца вещи"));
        }

        verify(userRepository, times(1))
                .findById(anyLong());
        verify(itemRepository, times(1))
                .findById(anyLong());
        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void shouldBookingApprove() {
        long userId = 1L;
        long bookingId = 1L;
        boolean approved = true;
        booking.setStatus(BookingStatus.WAITING);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booking));

        BookingDtoOut bookingDtoOut = bookingService.bookingApprove(userId, bookingId, approved);

        assertThat(bookingDtoOut.getId(), equalTo(bookingId));
        assertThat(bookingDtoOut.getStatus(), equalTo(BookingStatus.APPROVED));
        assertThat(bookingDtoOut.getItem().getId(), equalTo(booking.getItem().getId()));
        assertThat(bookingDtoOut.getStart(), notNullValue());
        assertThat(bookingDtoOut.getEnd(), notNullValue());

        verify(userRepository, times(1))
                .findById(anyLong());
        verify(bookingRepository, times(1))
                .findById(anyLong());
    }

    @Test
    void shouldBookingApproveWithWrongUserId() {
        long userId = 3L;
        long bookingId = 1L;
        boolean approved = true;
        when(userRepository.findById(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с таким ID не зарегистрировано"));
        try {
            bookingService.bookingApprove(userId, bookingId, approved);
        } catch (NotFoundException thrown) {
            assertThat(thrown.getMessage(), equalTo("Пользователь с таким ID не зарегистрировано"));
        }

        verify(userRepository, times(1))
                .findById(anyLong());
        verify(bookingRepository, never())
                .findById(anyLong());
        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void shouldBookingApproveNotFromItemOwner() {
        long userId = 2L;
        long bookingId = 1L;
        boolean approved = true;
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booking));

        try {
            bookingService.bookingApprove(userId, bookingId, approved);
        } catch (NotFoundException thrown) {
            assertThat(thrown.getMessage(), equalTo("Пользователь ID " + userId
                    + " не является владельцем вещи с ID " + booking.getItem().getId()
                    + " и не может менять одобрить/отклонить запрос на аренду этой вещи"));
        }

        verify(userRepository, times(1))
                .findById(anyLong());
        verify(bookingRepository, times(1))
                .findById(anyLong());
        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void shouldBookingApproveWhenBookingStatusIsNotWaiting() {
        long userId = 1L;
        long bookingId = 1L;
        boolean approved = true;
        booking.setStatus(BookingStatus.REJECTED);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booking));

        try {
            bookingService.bookingApprove(userId, bookingId, approved);
        } catch (BadParameterException thrown) {
            assertThat(thrown.getMessage(), equalTo("У запроса на аренду с ID " + bookingId
                    + " нельзя поменять статус. Текущий статус: " + booking.getStatus()));
        }

        verify(userRepository, times(1))
                .findById(anyLong());
        verify(bookingRepository, times(1))
                .findById(anyLong());
        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void shouldFindBookingById() {
        long userId = 1L;
        long bookingId = 1L;

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booking));

        BookingDtoOut bookingDtoOut = bookingService.findBookingById(userId, bookingId);

        assertThat(bookingDtoOut.getId(), equalTo(bookingId));
        assertThat(bookingDtoOut.getStatus(), equalTo(BookingStatus.APPROVED));
        assertThat(bookingDtoOut.getItem().getId(), equalTo(booking.getItem().getId()));
        assertThat(bookingDtoOut.getStart(), notNullValue());
        assertThat(bookingDtoOut.getEnd(), notNullValue());

        verify(bookingRepository, times(1))
                .findById(anyLong());
    }

    @Test
    void shouldFindBookingByIdWhenUserNotOwnerNotBooker() {
        long userId = 3L;
        long bookingId = 1L;

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booking));

        try {
            bookingService.findBookingById(userId, bookingId);
        } catch (NotFoundException thrown) {
            assertThat(thrown.getMessage(), equalTo("Пользователь " + userId
                    + " не создавал бронь с ID " + bookingId +
                    " и не является владельцем вещи " + booking.getItem().getId()));
        }

        verify(bookingRepository, times(1))
                .findById(anyLong());
    }
}
