package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadParameterException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDtoIn;
import ru.practicum.shareit.item.dto.CommentDtoOut;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    private UserRepository userRepository;
    private ItemRepository itemRepository;
    private BookingRepository bookingRepository;
    private CommentRepository commentRepository;
    private RequestRepository requestRepository;
    private ItemServiceImpl service;

    private ItemDto itemDto;
    private Comment comment;
    private CommentDtoIn commentDto;
    private Booking booking;
    private User user;
    private Item item;

    @BeforeEach
    void beforeEach() {
        itemDto = new ItemDto(1L, "Вещь 1", "Описание вещи 1", true, null);
        commentDto = new CommentDtoIn(1L, "Коммент 1");
        user = new User(1L, "Иван Иванович", "ii@mail.ru");
        item = new Item(1L, "Вещь 1", "Описание вещи 1", true, user, null);
        booking = new Booking(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusDays(1), item,
                user, BookingStatus.APPROVED);
        comment = new Comment(1L, "Коммент 1", item, user, LocalDateTime.now());

        userRepository = mock(UserRepository.class);
        itemRepository = mock(ItemRepository.class);
        bookingRepository = mock(BookingRepository.class);
        commentRepository = mock(CommentRepository.class);
        requestRepository = mock(RequestRepository.class);
        service = new ItemServiceImpl(itemRepository, userRepository, bookingRepository,
                commentRepository, requestRepository);
    }

    @Test
    void shouldCreateItem() {
        long userId = 1L;
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user));
        when(itemRepository.save(any()))
                .thenReturn(item);

        ItemDto itemFromMethod = service.createItem(userId, itemDto);

        assertThat(itemFromMethod.getId(), equalTo(item.getId()));
        assertThat(itemFromMethod.getName(), equalTo(item.getName()));
        assertThat(itemFromMethod.getDescription(), equalTo(item.getDescription()));
        assertThat(itemFromMethod.getAvailable(), equalTo(item.getIsAvailable()));
        assertThat(itemFromMethod.getRequestId(), nullValue());

        verify(userRepository, times(1))
                .findById(anyLong());
        verify(requestRepository, never())
                .findById(anyLong());
        verify(itemRepository, times(1))
                .save(any());
    }

    @Test
    void shouldCreateItemWithWrongUserId() {
        long userId = 1L;
        when(userRepository.findById(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с таким ID не зарегистрировано"));
        try {
            service.createItem(userId, itemDto);
        } catch (NotFoundException thrown) {
            assertThat(thrown.getMessage(), equalTo("Пользователь с таким ID не зарегистрировано"));
        }

        verify(userRepository, times(1))
                .findById(anyLong());
        verify(requestRepository, never())
                .findById(anyLong());
        verify(itemRepository, never())
                .save(any());
    }

    @Test
    void shouldCreateItemWithWrongRequestId() {
        long requestId = 2L;
        itemDto.setRequestId(requestId);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user));
        when(requestRepository.findById(itemDto.getRequestId()))
                .thenThrow(new NotFoundException("Запроса с ID " + itemDto.getRequestId() + " нет в базе"));
        try {
            service.createItem(1L, itemDto);
        } catch (NotFoundException thrown) {
            assertThat(thrown.getMessage(), equalTo("Запроса с ID " + itemDto.getRequestId() + " нет в базе"));
        }

        verify(userRepository, times(1))
                .findById(anyLong());
        verify(requestRepository, times(1))
                .findById(anyLong());
        verify(itemRepository, never())
                .save(any());
    }

    @Test
    void shouldUpdateItem() {
        long userId = 1L;
        long itemId = 1L;
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));
        when(itemRepository.save(any()))
                .thenReturn(item);

        ItemDto itemFromMethod = service.updateItem(userId, itemDto, itemId);

        assertThat(itemFromMethod.getId(), equalTo(itemId));
        assertThat(itemFromMethod.getName(), equalTo(itemDto.getName()));
        assertThat(itemFromMethod.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(itemFromMethod.getAvailable(), equalTo(item.getIsAvailable()));
        assertThat(itemFromMethod.getRequestId(), nullValue());

        verify(userRepository, times(1))
                .findById(anyLong());
        verify(itemRepository, times(1))
                .findById(anyLong());
        verify(itemRepository, times(1))
                .save(any());
        verify(requestRepository, never())
                .findById(anyLong());
        verify(itemRepository, times(1))
                .save(any());
    }

    @Test
    void shouldUpdateItemNotByOwner() {
        User user2 = new User(2L, "Петр Петрович", "pp@mail.ru");
        item.setUser(user2);
        long userId = 1L;
        long itemId = 1L;

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));

        try {
            service.updateItem(userId, itemDto, itemId);
        } catch (NotFoundException thrown) {
            assertThat(thrown.getMessage(), equalTo("Пользователь с ID " + userId
                    + " не является владельцем вещи c ID " + itemId + ". Изменение запрещено"));
        }
        verify(userRepository, times(1))
                .findById(anyLong());
        verify(itemRepository, times(1))
                .findById(anyLong());
        verify(itemRepository, never())
                .save(any());
        verify(requestRepository, never())
                .findById(anyLong());
    }

    @Test
    void shouldSaveComment() {
        long userId = 1L;
        long itemId = 1L;
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));
        when(bookingRepository
                .findAllByItemIdAndBookerIdAndStatusAndStartBeforeAndEndBefore(anyLong(),
                        anyLong(), any(), any(), any()))
                .thenReturn(List.of(booking));
        when(commentRepository.save(any()))
                .thenReturn(comment);

        CommentDtoOut commentFromMethod = service.saveComment(userId, itemId, commentDto);

        assertThat(commentFromMethod.getId(), equalTo(commentDto.getId()));
        assertThat(commentFromMethod.getText(), equalTo(commentDto.getText()));
        assertThat(commentFromMethod.getCreated(), notNullValue());

        verify(userRepository, times(1))
                .findById(anyLong());
        verify(itemRepository, times(1))
                .findById(anyLong());
        verify(bookingRepository, times(1))
                .findAllByItemIdAndBookerIdAndStatusAndStartBeforeAndEndBefore(anyLong(),
                        anyLong(), any(), any(), any());
        verify(commentRepository, times(1))
                .save(any());
    }

    @Test
    void shouldCommentSaveFailUserHasNoPastBookings() {
        long userId = 1L;
        long itemId = 1L;
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));
        when(bookingRepository
                .findAllByItemIdAndBookerIdAndStatusAndStartBeforeAndEndBefore(anyLong(),
                        anyLong(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        try {
            service.saveComment(userId, itemId, commentDto);
        } catch (BadParameterException thrown) {
            assertThat(thrown.getMessage(), equalTo("Пользователь " + userId + " не арендовал вещь "
                    + itemId + ". Не имеет права писать отзыв"));
        }

        verify(userRepository, times(1))
                .findById(anyLong());
        verify(itemRepository, times(1))
                .findById(anyLong());
        verify(bookingRepository, times(1))
                .findAllByItemIdAndBookerIdAndStatusAndStartBeforeAndEndBefore(anyLong(),
                        anyLong(), any(), any(), any());
        verify(commentRepository, never())
                .save(any());
    }
}
