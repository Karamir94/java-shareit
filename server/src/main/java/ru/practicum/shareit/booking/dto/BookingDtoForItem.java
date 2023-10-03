package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BookingDtoForItem {

    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Long bookerId;
    private BookingStatus status;
}
