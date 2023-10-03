package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime start; // дата и время начала бронирования;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime end;

    @ManyToOne(fetch = FetchType.EAGER)
    private Item item;

    @ManyToOne(fetch = FetchType.EAGER)
    private User booker; // пользователь, который осуществляет бронирование;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;   /* статус бронирования. Может принимать одно из следующих значений:
    WAITING — новое бронирование, ожидает одобрения,
    APPROVED — бронирование подтверждено владельцем,
    REJECTED — бронирование отклонено владельцем,
    CANCELED — бронирование отменено создателем. */
}
