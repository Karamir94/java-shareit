package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

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
        if (text.isBlank()) {
            return List.of();
        }
        List<Item> itemsList = itemRepository.search(text);
        return itemsList.stream()
                .map(ItemMapper::toItemDto)
                .collect(toList());
    }

    @Override
    public ItemDtoDated getItemById(long userId, long itemId) {
        checkId(itemId);
        checkUser(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмета с ID " + itemId + " не зарегистрировано"));
        List<CommentDtoOut> comments = commentRepository.findCommentsByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(toList());
        if (item.getUser().getId() != userId) {
            return ItemMapper.toItemDto(item, null, null, comments);
        }
        List<Booking> lastBookings = bookingRepository.findLastBookingByItemId(itemId, LocalDateTime.now(),
                Sort.by(DESC, "start"));
        BookingDtoForItem lastBooking = BookingMapper.toItemBookingDto(lastBookings.isEmpty()
                ? null : lastBookings.get(0));
        List<Booking> nextBookings = bookingRepository.findNextBookingByItemId(itemId, LocalDateTime.now(),
                Sort.by(ASC, "start"));
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

        List<Long> itemIds = new ArrayList<>();
        for (Item item : items) {
            itemIds.add(item.getId());
        }
        List<ItemDtoDated> datedItemList = new ArrayList<>();
        Map<Item, List<Booking>> lastBookingsMap = bookingRepository.findLastBookingsByUserIdByItemIn(userId,
                        LocalDateTime.now(), itemIds, Sort.by(DESC, "start"))
                .stream()
                .collect(groupingBy(Booking::getItem, toList()));
        Map<Item, List<Booking>> nextBookingsMap = bookingRepository.findNextBookingsByUserIdByItemIn(userId,
                        LocalDateTime.now(), itemIds, Sort.by(ASC, "start"))
                .stream()
                .collect(groupingBy(Booking::getItem, toList()));
        Map<Item, List<Comment>> comments = commentRepository.findByItemIn(itemIds,
                        Sort.by(DESC, "created"))
                .stream()
                .collect(groupingBy(Comment::getItem, toList()));

        for (Item item : items) {
            Booking lastBooking = null;
            Booking nextBooking = null;
            List<CommentDtoOut> commentsList = new ArrayList<>();

            if (lastBookingsMap != null && lastBookingsMap.get(item) != null && lastBookingsMap.get(item).get(0) != null) {
                lastBooking = lastBookingsMap.get(item).get(0);
            }
            if (nextBookingsMap != null && nextBookingsMap.get(item) != null && nextBookingsMap.get(item).get(0) != null) {
                nextBooking = nextBookingsMap.get(item).get(0);
            }
            if (comments != null && comments.get(item) != null) {
                for (Comment comment : comments.get(item)) {
                    commentsList.add(CommentMapper.toCommentDto(comment));
                }
            }
            datedItemList.add(ItemMapper.toItemDto(item, BookingMapper.toItemBookingDto(lastBooking),
                    BookingMapper.toItemBookingDto(nextBooking), commentsList));
        }
        return datedItemList;
    }

    @Transactional
    @Override
    public CommentDtoOut saveComment(long userId, long itemId, CommentDtoIn commentDto) {
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
