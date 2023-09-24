package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // поиск по заказчику
    List<Booking> findBookingsByBookerIdOrderByStartDesc(Long bookerId, Pageable page);

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStart(Long bookerId, LocalDateTime start,
                                                                         LocalDateTime end, Pageable page);

    List<Booking> findAllByBookerIdAndEndBeforeAndStatusOrderByStartDesc(Long userId, LocalDateTime dateTime,
                                                                         BookingStatus status, Pageable page);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(long userId, LocalDateTime dateTime, Pageable page);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(long userId, BookingStatus status, Pageable page);

    // поиск по хозяину вещи
    List<Booking> findAllByItemUserIdOrderByStartDesc(Long ownerId, Pageable page);

    List<Booking> findAllByItemUserIdAndStartBeforeAndEndAfterOrderByStart(Long ownerId, LocalDateTime start,
                                                                           LocalDateTime end, Pageable page);

    List<Booking> findAllByItemUserIdAndEndBeforeAndStatusOrderByStartDesc(Long ownerId, LocalDateTime end,
                                                                           BookingStatus status, Pageable page);

    List<Booking> findAllByItemUserIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime start,
                                                                   Pageable page);

    List<Booking> findAllByItemUserIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status, Pageable page);

    // Поиск last и next booking для item
    Optional<Booking> findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(Long itemId, LocalDateTime start,
                                                                               BookingStatus status);

    Optional<Booking> findFirstByItemIdAndStartAfterAndStatusOrderByStart(Long itemId, LocalDateTime start,
                                                                          BookingStatus status);

    List<Booking> findAllByItemUserIdAndItemIdInAndStartBeforeOrderByStartDesc(Long userId, List<Long> itemIds,
                                                                               LocalDateTime start);

    List<Booking> findAllByItemUserIdAndItemIdInAndStartAfterOrderByStart(Long itemId, List<Long> itemIds,
                                                                          LocalDateTime start);

    List<Booking> findAllByItemIdAndBookerIdAndStatusAndStartBeforeAndEndBefore(Long itemId, Long bookerId,
                                                                                BookingStatus status,
                                                                                LocalDateTime start,
                                                                                LocalDateTime end);
}
