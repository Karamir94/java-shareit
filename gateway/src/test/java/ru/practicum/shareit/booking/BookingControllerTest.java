package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.service.Header.USER_ID;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc
class BookingControllerTest {

    @MockBean
    private BookingClient bookingClient;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private ItemDto itemDto;
    private BookingDtoIn bookingDtoIn;
    private UserDto userDto;
    private ResponseEntity<Object> response;

    @BeforeEach
    public void shouldItemCreate() {
        bookingDtoIn = new BookingDtoIn(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusDays(5), 1L);
        response = new ResponseEntity<>(bookingDtoIn, HttpStatus.OK);
    }

    @Test
    void shouldSaveBooking() throws Exception {
        when(bookingClient.saveBooking(anyLong(), any()))
                .thenReturn(response);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDtoIn.getId()), Long.class))
                .andExpect(jsonPath("$.start", notNullValue()))
                .andExpect(jsonPath("$.end", notNullValue()))
                .andExpect(jsonPath("$.itemId", notNullValue()));

        verify(bookingClient, times(1))
                .saveBooking(anyLong(), any());
    }

    @Test
    void shouldSaveBookingWithEndBeforeStart() throws Exception {
        bookingDtoIn.setStart(bookingDtoIn.getEnd().plusHours(2));

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .saveBooking(anyLong(), any());
    }

    @Test
    void saveBookingWithStartIsNull() throws Exception {
        bookingDtoIn.setStart(null);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .saveBooking(anyLong(), any());
    }

    @Test
    void shouldSaveBookingWithStartIsPast() throws Exception {
        bookingDtoIn.setStart(LocalDateTime.now().minusDays(1));

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .saveBooking(anyLong(), any());
    }

    @Test
    void shouldSaveBookingWithEndIsNull() throws Exception {
        bookingDtoIn.setEnd(null);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .saveBooking(anyLong(), any());
    }

    @Test
    void shouldSaveBookingWithNullItemId() throws Exception {
        bookingDtoIn.setItemId(null);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .saveBooking(anyLong(), any());
    }

    @Test
    void shouldBookingApprove() throws Exception {
        long userId = 1L;
        long bookingId = 1L;
        boolean approved = true;

        when(bookingClient.bookingApprove(userId, bookingId, approved))
                .thenReturn(response);

        mvc.perform(patch("/bookings/1")
                        .header(USER_ID, 1)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDtoIn.getId()), Long.class))
                .andExpect(jsonPath("$.start", notNullValue()))
                .andExpect(jsonPath("$.end", notNullValue()))
                .andExpect(jsonPath("$.itemId", notNullValue()));

        verify(bookingClient, times(1))
                .bookingApprove(userId, bookingId, approved);
    }

    @Test
    void shouldFindBookingById() throws Exception {
        long userId = 1L;
        long bookingId = 1L;

        when(bookingClient.findBookingById(userId, bookingId))
                .thenReturn(response);

        mvc.perform(get("/bookings/1")
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDtoIn.getId()), Long.class))
                .andExpect(jsonPath("$.start", notNullValue()))
                .andExpect(jsonPath("$.end", notNullValue()))
                .andExpect(jsonPath("$.itemId", notNullValue()));

        verify(bookingClient, times(1))
                .findBookingById(userId, bookingId);
    }


    @Test
    void shouldFindUserBookings() throws Exception {
        long userId = 1L;
        String state = "FUTURE";
        BookingState enumState = BookingState.FUTURE;
        int from = 0;
        int size = 5;
        List<BookingDtoIn> bookingList = List.of(bookingDtoIn);
        ResponseEntity<Object> responseWithList = new ResponseEntity<>(bookingList, HttpStatus.OK);

        when(bookingClient.findUserBookings(userId, enumState, from, size))
                .thenReturn(responseWithList);

        mvc.perform(get("/bookings")
                        .header(USER_ID, 1)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingList)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDtoIn.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", notNullValue()))
                .andExpect(jsonPath("$[0].end", notNullValue()));

        verify(bookingClient, times(1))
                .findUserBookings(userId, enumState, from, size);
    }

    @Test
    void shouldFindUserBookingsWithWrongFromParam() throws Exception {
        long userId = 1L;
        String state = "FUTURE";
        BookingState enumState = BookingState.FUTURE;
        int from = -1;
        int size = 5;

        mvc.perform(get("/bookings")
                        .header(USER_ID, 1)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .findUserBookings(userId, enumState, from, size);
    }

    @Test
    void shouldFindUserBookingsWithNullSizeParam() throws Exception {
        long userId = 1L;
        String state = "FUTURE";
        BookingState enumState = BookingState.FUTURE;
        int from = 0;
        int size = 0;

        mvc.perform(get("/bookings")
                        .header(USER_ID, 1)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .findUserBookings(userId, enumState, from, size);
    }

    @Test
    void shouldFindUserBookingsWithNegativeSizeParam() throws Exception {
        long userId = 1L;
        String state = "FUTURE";
        BookingState enumState = BookingState.FUTURE;
        int from = 0;
        int size = -5;

        mvc.perform(get("/bookings")
                        .header(USER_ID, 1)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .findUserBookings(userId, enumState, from, size);
    }

    @Test
    void shouldFindOwnerBookings() throws Exception {
        long userId = 1L;
        String state = "FUTURE";
        BookingState enumState = BookingState.FUTURE;
        int from = 0;
        int size = 5;
        List<BookingDtoIn> bookingList = List.of(bookingDtoIn);
        ResponseEntity<Object> responseWithList = new ResponseEntity<>(bookingList, HttpStatus.OK);

        when(bookingClient.findOwnerBookings(userId, enumState, from, size))
                .thenReturn(responseWithList);

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID, 1)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(bookingList)))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDtoIn.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", notNullValue()))
                .andExpect(jsonPath("$[0].end", notNullValue()));

        verify(bookingClient, times(1))
                .findOwnerBookings(userId, enumState, from, size);
    }

    @Test
    void shouldFindOwnersBookingsWithWrongFromParam() throws Exception {
        long userId = 1L;
        String state = "FUTURE";
        BookingState enumState = BookingState.FUTURE;
        int from = -1;
        int size = 5;

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID, 1)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .findOwnerBookings(userId, enumState, from, size);
    }

    @Test
    void shouldFindOwnersBookingsWithNullSizeParam() throws Exception {
        long userId = 1L;
        String state = "FUTURE";
        BookingState enumState = BookingState.FUTURE;
        int from = 0;
        int size = 0;

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID, 1)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .findOwnerBookings(userId, enumState, from, size);
    }

    @Test
    void shouldFindOwnersBookingsWithNegativeSizeParam() throws Exception {
        long userId = 1L;
        String state = "FUTURE";
        BookingState enumState = BookingState.FUTURE;
        int from = 0;
        int size = -5;

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID, 1)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never())
                .findOwnerBookings(userId, enumState, from, size);
    }
}
