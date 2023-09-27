package ru.practicum.shareit.service;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDtoIn;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StartBeforeEndValidatorTest {

    @Test
    void isValid() {

        StartBeforeEndValidator validator = new StartBeforeEndValidator();
        BookingDtoIn bookingDtoIn = new BookingDtoIn(1L, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(5), 1L);

        boolean result = validator.isValid(bookingDtoIn, null);

        assertTrue(result);
    }

    @Test
    void isNotValidStartAfterEnd() {

        StartBeforeEndValidator validator = new StartBeforeEndValidator();
        BookingDtoIn bookingDtoIn = new BookingDtoIn(1L, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusHours(5), 1L);

        boolean result = validator.isValid(bookingDtoIn, null);

        assertFalse(result);
    }

    @Test
    void isNotValidStartIsEqualEnd() {

        StartBeforeEndValidator validator = new StartBeforeEndValidator();
        BookingDtoIn bookingDtoIn = new BookingDtoIn(1L,
                LocalDateTime.of(2023, 9, 21, 23, 20),
                LocalDateTime.of(2023, 9, 21, 23, 20),
                1L);

        boolean result = validator.isValid(bookingDtoIn, null);

        assertFalse(result);
    }
}
