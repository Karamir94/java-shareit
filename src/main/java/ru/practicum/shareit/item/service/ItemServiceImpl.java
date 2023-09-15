package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadParameterException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public void deleteItem(long id) {
        checkId(id);
        itemRepository.deleteById(id);
    }

    @Transactional
    @Override
    public ItemDto createItem(long userId, ItemDto itemDto) {
        User user = checkUser(userId);
        Item itemFromDto = ItemMapper.toItem(itemDto, user);
        Item item = itemRepository.save(itemFromDto);
        return ItemMapper.toItemDto(item);
    }

    @Transactional
    @Override
    public ItemDto updateItem(long userId, ItemDto itemDto, long itemId) {
        checkId(itemId);
        checkUser(userId);
        Item itemFromRep = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмета с ID " + itemId + " не зарегистрировано"));
        if (itemFromRep.getUser().getId() != userId) {
            throw new NotFoundException("Пользователь с ID " + userId + " не является владельцем вещи c ID "
                    + itemId + ". Изменение запрещено");
        }
        Item item = ItemMapper.toItem(itemDto, itemFromRep);
        item.setId(itemId);

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        List<Item> itemsList = itemRepository.search(text);
        return itemsList.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDtoDated getItemById(long userId, long itemId) {
        checkId(itemId);
        checkUser(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмета с ID " + itemId + " не зарегистрировано"));
        List<CommentDto> comments = commentRepository.findCommentsByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        if (item.getUser().getId() != userId) {
            return ItemMapper.toItemDto(item, null, null, comments);
        }
        List<Booking> lastBookings = bookingRepository.findLastBookingByItemId(itemId, LocalDateTime.now());
        BookingDtoForItem lastBooking = BookingMapper.toItemBookingDto(lastBookings.isEmpty()
                ? null : lastBookings.get(0));
        List<Booking> nextBookings = bookingRepository.findNextBookingByItemId(itemId, LocalDateTime.now());
        BookingDtoForItem nextBooking = BookingMapper.toItemBookingDto(nextBookings.isEmpty()
                ? null : nextBookings.get(0));

        return ItemMapper.toItemDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemDtoDated> getUserItems(long userId) {
        checkUser(userId);
        List<Item> items = itemRepository.findAllItemsByUserIdOrderById(userId);
        if (items.isEmpty()) {
            throw new NotFoundException("Пользователь " + userId + " не является хозяином ни одной вещи");
        }

        List<ItemDtoDated> datedItemList = new ArrayList<>();
        List<Booking> lastBookingsList = bookingRepository.findLastBookingsByUserId(userId, LocalDateTime.now());
        List<Booking> nextBookingsList = bookingRepository.findNextBookingsByUserId(userId, LocalDateTime.now());
        List<Comment> comments = commentRepository.findCommentsForItemsByOwnerId(userId);

        for (Item item : items) {
            long id = item.getId();
            Booking lastBooking = null;
            Booking nextBooking = null;
            List<CommentDto> commentList = new ArrayList<>();
            for (Booking booking : lastBookingsList) {
                if (booking.getItem().getId() == id) {
                    lastBooking = booking;
                    break;
                }
            }
            for (Booking booking : nextBookingsList) {
                if (booking.getItem().getId() == id) {
                    nextBooking = booking;
                    break;
                }
            }
            for (Comment comment : comments) {
                if (comment.getItem().getId() == id) {
                    commentList.add(CommentMapper.toCommentDto(comment));
                }
            }
            datedItemList.add(ItemMapper.toItemDto(item, BookingMapper.toItemBookingDto(lastBooking),
                    BookingMapper.toItemBookingDto(nextBooking), commentList));
        }
        return datedItemList;
    }

    @Transactional
    @Override
    public CommentDto saveComment(long userId, long itemId, CommentDto commentDto) {
        checkId(itemId);
        User user = checkUser(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмета с ID " + itemId + " не зарегистрировано"));
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndItemId(userId, itemId, LocalDateTime.now());
        if (bookings.isEmpty()) {
            throw new BadParameterException("Пользователь " + userId + " не арендовал вещь "
                    + itemId + ". Не имеет права писать отзыв");
        }
        Comment comment = CommentMapper.toComment(commentDto, item, user);

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private User checkUser(long userId) {
        checkId(userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не зарегистрирован"));
    }

    private void checkId(long userId) {
        if (userId <= 0) {
            throw new BadParameterException("id must be positive");
        }
    }
}
