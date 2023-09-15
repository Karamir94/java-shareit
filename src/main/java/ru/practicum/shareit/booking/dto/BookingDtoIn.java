package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.Create;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.service.StartBeforeEnd;
import ru.practicum.shareit.service.StartEndChecker;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@StartBeforeEnd
public class BookingDtoIn implements StartEndChecker {

    private Long id;

    @NotNull(groups = {Create.class})
    @FutureOrPresent
    private LocalDateTime start;

    @NotNull(groups = {Create.class})
    @FutureOrPresent
    private LocalDateTime end;

    @NotNull(groups = {Create.class})
    private Long itemId;

    private BookingStatus status = BookingStatus.WAITING;
}
