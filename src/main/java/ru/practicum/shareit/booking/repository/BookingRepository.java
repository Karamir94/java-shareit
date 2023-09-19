package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBooker_IdOrderByStartDesc(Long bookerId);

    @Query("select b " +
            "from Booking b " +
            "join b.booker as bo  " +
            "where bo.id = ?1 and b.start < ?2 and b.end > ?2 " +
            "order by b.start ")
    List<Booking> findAllCurrentByBookerId(Long bookerId, LocalDateTime dateTime);

    @Query("select b " +
            "from Booking b " +
            "join b.booker as bo  " +
            "where bo.id = ?1 and b.end < ?2 and b.status = 'APPROVED' ")
    List<Booking> findAllPastByBookerId(Long userId, LocalDateTime dateTime, Sort sort);

    @Query("select b " +
            "from Booking b " +
            "join b.booker as bo " +
            "where bo.id = ?1 and b.start > ?2 ")
    List<Booking> findAllFutureByBookerId(Long userId, LocalDateTime dateTime, Sort sort);

    @Query("select b " +
            "from Booking b " +
            "join b.booker as bo " +
            "where bo.id = ?1 and b.status = 'WAITING' ")
    List<Booking> findAllWaitingByBookerId(Long userId, Sort sort);

    @Query("select b " +
            "from Booking b " +
            "join b.booker as bo " +
            "where bo.id = ?1 and b.status = 'REJECTED' ")
    List<Booking> findAllRejectedByBookerId(Long userId, Sort sort);


    @Query("select b " +
            "from Booking b " +
            "join b.item as i " +
            "join i.user as u " +
            "where u.id = ?1 ")
    List<Booking> findByOwnerIdOrderByStartDesc(Long ownerId, Sort sort);

    @Query("select b " +
            "from Booking b " +
            "join b.item as i " +
            "join i.user as u " +
            "where u.id = ?1 and b.start < ?2 and b.end > ?2 " +
            "order by b.start")
    List<Booking> findAllCurrentByOwnerId(Long ownerId, LocalDateTime dateTime);

    @Query("select b " +
            "from Booking b " +
            "join b.item as i " +
            "join i.user as u " +
            "where u.id = ?1 and b.end < ?2 and b.status = 'APPROVED' ")
    List<Booking> findAllPastByOwnerId(Long ownerId, LocalDateTime dateTime, Sort sort);

    @Query("select b " +
            "from Booking b " +
            "join b.item as i " +
            "join i.user as u " +
            "where u.id = ?1 and b.start > ?2 ")
    List<Booking> findAllFutureByOwnerId(Long ownerId, LocalDateTime dateTime, Sort sort);


    @Query("select b " +
            "from Booking b " +
            "join b.item as i " +
            "join i.user as u " +
            "where u.id = ?1 and b.status = 'WAITING' ")
    List<Booking> findAllWaitingByOwnerId(Long ownerId, Sort sort);

    @Query("select b " +
            "from Booking b " +
            "join b.item as i " +
            "join i.user as u " +
            "where u.id = ?1 and b.status = 'REJECTED' ")
    List<Booking> findAllRejectedByOwnerId(Long ownerId, Sort sort);

    @Query("select b " +
            "from Booking b " +
            "join b.item as i " +
            "where i.id = ?1 and b.start <= ?2 and b.status = 'APPROVED'")
    List<Booking> findLastBookingByItemId(Long itemId, LocalDateTime dateTime, Sort sort);

    @Query("select b " +
            "from Booking b " +
            "join b.item as i " +
            "where i.id = ?1 and b.start > ?2 and b.status = 'APPROVED'")
    List<Booking> findNextBookingByItemId(Long itemId, LocalDateTime dateTime, Sort sort);

    @Query("select b " +
            "from Booking b " +
            "join b.item as i join i.user as u " +
            "where u.id = ?1 and b.start <= ?2  and i.id in (?3) and b.status = 'APPROVED'")
    List<Booking> findLastBookingsByUserIdByItemIn(Long userId, LocalDateTime dateTime, List<Long> ids, Sort sort);

    @Query("select b " +
            "from Booking b " +
            "join b.item as i join i.user as u " +
            "where u.id = ?1 and b.start > ?2 and i.id in (?3) and b.status = 'APPROVED'")
    List<Booking> findNextBookingsByUserIdByItemIn(Long userId, LocalDateTime dateTime, List<Long> ids, Sort sort);

    @Query("select b " +
            "from Booking b " +
            "join b.item as i " +
            "join b.booker as bo " +
            "where bo.id = ?1 and i.id = ?2 and b.status = 'APPROVED' and b.start < ?3 and b.end < ?3")
    List<Booking> findAllByBookerIdAndItemId(Long userId, Long itemId, LocalDateTime dateTime);
}
