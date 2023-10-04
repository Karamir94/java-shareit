package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDtoOut;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoDated;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ItemMapperTest {

    private User user;
    private Request request;
    private Item item;
    private ItemDto itemDto;
    private CommentDtoOut comment;
    private BookingDtoForItem bookingLastDto;
    private BookingDtoForItem bookingNextDto;


    @BeforeEach
    void beforeEach() {
        user = new User(1L, "Иван Иванович", "ii@mail.ru");
        request = new Request(1L, "Request 1", user, LocalDateTime.now());
        item = new Item(1L, "Вещь 1", "Описание вещи 1", true, user, request);
        bookingLastDto = new BookingDtoForItem(1L, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusHours(5), 1L, BookingStatus.APPROVED);
        bookingNextDto = new BookingDtoForItem(2L, LocalDateTime.now().plusHours(12),
                LocalDateTime.now().plusDays(1), 1L, BookingStatus.APPROVED);
        itemDto = new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getIsAvailable(), request.getId());
        comment = new CommentDtoOut(1L, "Коммент 1", itemDto, user.getName(), LocalDateTime.now());
    }

    @Test
    void shouldConvertToItemDto() {
        ItemDto itemDto = ItemMapper.toItemDto(item);

        assertEquals(item.getId(), itemDto.getId());
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getIsAvailable(), itemDto.getAvailable());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertEquals(item.getRequest().getId(), itemDto.getRequestId());
    }

    @Test
    void shouldConvertToItemDtoWithNextAndLastBooking() {
        ItemDtoDated itemFromMapper = ItemMapper.toItemDto(item, bookingLastDto, bookingNextDto, List.of(comment));

        assertEquals(item.getId(), itemFromMapper.getId());
        assertEquals(item.getName(), itemFromMapper.getName());
        assertEquals(item.getDescription(), itemFromMapper.getDescription());
        assertEquals(item.getIsAvailable(), itemFromMapper.getAvailable());
        assertEquals(bookingLastDto.getId(), itemFromMapper.getLastBooking().getId());
        assertEquals(bookingLastDto.getBookerId(), itemFromMapper.getLastBooking().getBookerId());
        assertEquals(bookingLastDto.getStatus(), itemFromMapper.getLastBooking().getStatus());
        assertNotNull(itemFromMapper.getLastBooking().getStart());

        assertEquals(bookingNextDto.getId(), itemFromMapper.getNextBooking().getId());
        assertEquals(bookingNextDto.getBookerId(), itemFromMapper.getNextBooking().getBookerId());
        assertEquals(bookingNextDto.getStatus(), itemFromMapper.getNextBooking().getStatus());
        assertNotNull(itemFromMapper.getNextBooking().getStart());

        assertEquals(1, itemFromMapper.getComments().size());
    }

    @Test
    void shouldConvertToItemWithUser() {
        Item mapperItem = ItemMapper.toItem(itemDto, user, request);

        assertEquals(itemDto.getId(), mapperItem.getId());
        assertEquals(itemDto.getName(), mapperItem.getName());
        assertEquals(itemDto.getDescription(), mapperItem.getDescription());
        assertEquals(itemDto.getAvailable(), mapperItem.getIsAvailable());
        assertEquals(user.getId(), mapperItem.getUser().getId());
        assertEquals(user.getId(), mapperItem.getUser().getId());
        assertEquals(user.getEmail(), mapperItem.getUser().getEmail());
        assertEquals(user.getName(), mapperItem.getUser().getName());
        assertEquals(request.getId(), mapperItem.getRequest().getId());
        assertNotNull(request.getCreated());
        assertEquals(request.getDescription(), mapperItem.getRequest().getDescription());
    }

    @Test
    void shouldConvertToItem() {
        Item mapperItem = ItemMapper.toItem(itemDto, item, request);

        assertEquals(itemDto.getId(), mapperItem.getId());
        assertEquals(itemDto.getName(), mapperItem.getName());
        assertEquals(item.getDescription(), mapperItem.getDescription());
        assertEquals(itemDto.getAvailable(), mapperItem.getIsAvailable());
        assertEquals(request.getId(), mapperItem.getRequest().getId());
        assertEquals(request.getDescription(), mapperItem.getRequest().getDescription());
    }
}
