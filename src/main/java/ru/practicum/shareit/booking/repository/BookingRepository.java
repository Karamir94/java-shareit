package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
                                                                                  BookingStatus status, LocalDateTime start,
                                                                                  LocalDateTime end);

//    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);
//
//    @Query("select b " +
//            "from Booking b " +
//            "join b.booker as bo  " +
//            "where bo.id = ?1 and b.start < ?2 and b.end > ?2 " +
//            "order by b.start ")
//    List<Booking> findAllCurrentByBookerId(Long bookerId, LocalDateTime dateTime);
//
//    @Query("select b " +
//            "from Booking b " +
//            "join b.booker as bo  " +
//            "where bo.id = ?1 and b.end < ?2 and b.status = 'APPROVED' ")
//    List<Booking> findAllPastByBookerId(Long userId, LocalDateTime dateTime, Sort sort);
//
//    @Query("select b " +
//            "from Booking b " +
//            "join b.booker as bo " +
//            "where bo.id = ?1 and b.start > ?2 ")
//    List<Booking> findAllFutureByBookerId(Long userId, LocalDateTime dateTime, Sort sort);
//
//    @Query("select b " +
//            "from Booking b " +
//            "join b.booker as bo " +
//            "where bo.id = ?1 and b.status = 'WAITING' ")
//    List<Booking> findAllWaitingByBookerId(Long userId, Sort sort);
//
//    @Query("select b " +
//            "from Booking b " +
//            "join b.booker as bo " +
//            "where bo.id = ?1 and b.status = 'REJECTED' ")
//    List<Booking> findAllRejectedByBookerId(Long userId, Sort sort);
//
//
//    @Query("select b " +
//            "from Booking b " +
//            "join b.item as i " +
//            "join i.user as u " +
//            "where u.id = ?1 ")
//    List<Booking> findByOwnerIdOrderByStartDesc(Long ownerId, Sort sort);
//
//    @Query("select b " +
//            "from Booking b " +
//            "join b.item as i " +
//            "join i.user as u " +
//            "where u.id = ?1 and b.start < ?2 and b.end > ?2 " +
//            "order by b.start")
//    List<Booking> findAllCurrentByOwnerId(Long ownerId, LocalDateTime dateTime);
//
//    @Query("select b " +
//            "from Booking b " +
//            "join b.item as i " +
//            "join i.user as u " +
//            "where u.id = ?1 and b.end < ?2 and b.status = 'APPROVED' ")
//    List<Booking> findAllPastByOwnerId(Long ownerId, LocalDateTime dateTime, Sort sort);
//
//    @Query("select b " +
//            "from Booking b " +
//            "join b.item as i " +
//            "join i.user as u " +
//            "where u.id = ?1 and b.start > ?2 ")
//    List<Booking> findAllFutureByOwnerId(Long ownerId, LocalDateTime dateTime, Sort sort);
//
//
//    @Query("select b " +
//            "from Booking b " +
//            "join b.item as i " +
//            "join i.user as u " +
//            "where u.id = ?1 and b.status = 'WAITING' ")
//    List<Booking> findAllWaitingByOwnerId(Long ownerId, Sort sort);
//
//    @Query("select b " +
//            "from Booking b " +
//            "join b.item as i " +
//            "join i.user as u " +
//            "where u.id = ?1 and b.status = 'REJECTED' ")
//    List<Booking> findAllRejectedByOwnerId(Long ownerId, Sort sort);
//
//    @Query("select b " +
//            "from Booking b " +
//            "join b.item as i " +
//            "where i.id = ?1 and b.start <= ?2 and b.status = 'APPROVED'")
//    List<Booking> findLastBookingByItemId(Long itemId, LocalDateTime dateTime, Sort sort);
//
//    @Query("select b " +
//            "from Booking b " +
//            "join b.item as i " +
//            "where i.id = ?1 and b.start > ?2 and b.status = 'APPROVED'")
//    List<Booking> findNextBookingByItemId(Long itemId, LocalDateTime dateTime, Sort sort);
//
//    @Query("select b " +
//            "from Booking b " +
//            "join b.item as i join i.user as u " +
//            "where u.id = ?1 and b.start <= ?2  and i.id in (?3) and b.status = 'APPROVED'")
//    List<Booking> findLastBookingsByUserIdByItemIn(Long userId, LocalDateTime dateTime, List<Long> ids, Sort sort);
//
//    @Query("select b " +
//            "from Booking b " +
//            "join b.item as i join i.user as u " +
//            "where u.id = ?1 and b.start > ?2 and i.id in (?3) and b.status = 'APPROVED'")
//    List<Booking> findNextBookingsByUserIdByItemIn(Long userId, LocalDateTime dateTime, List<Long> ids, Sort sort);
//
//    @Query("select b " +
//            "from Booking b " +
//            "join b.item as i " +
//            "join b.booker as bo " +
//            "where bo.id = ?1 and i.id = ?2 and b.status = 'APPROVED' and b.start < ?3 and b.end < ?3")
//    List<Booking> findAllByBookerIdAndItemId(Long userId, Long itemId, LocalDateTime dateTime);
}
