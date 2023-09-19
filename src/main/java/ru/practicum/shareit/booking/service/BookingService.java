package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {

    BookingDtoOut saveBooking(long userId, BookingDtoIn bookingDto);

    BookingDtoOut bookingApprove(long userId, long bookingId, boolean approved);

    BookingDtoOut findBookingById(long userId, long bookingId);

    List<BookingDtoOut> findUserBookings(long userId, BookingState state);

    List<BookingDtoOut> findOwnerBookings(long userId, BookingState state);
}
