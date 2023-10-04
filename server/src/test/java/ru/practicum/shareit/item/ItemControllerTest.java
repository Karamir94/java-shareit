package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDtoOut;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoDated;
import ru.practicum.shareit.item.service.ItemServiceImpl;

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

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
class ItemControllerTest {

    @MockBean
    private ItemServiceImpl itemService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private ItemDto itemDto;
    private BookingDtoForItem bookingLast;
    private BookingDtoForItem bookingNext;
    private CommentDtoOut commentDto;
    private ItemDtoDated itemDtoDated;

    @BeforeEach
    public void shouldItemCreate() {
        itemDto = new ItemDto(1L, "Вещь 1", "Описание вещи 1", true, null);
        bookingLast = new BookingDtoForItem(1L, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusHours(5), 1L, BookingStatus.APPROVED);
        bookingNext = new BookingDtoForItem(2L, LocalDateTime.now().plusHours(12),
                LocalDateTime.now().plusDays(1), 2L, BookingStatus.APPROVED);
        commentDto = new CommentDtoOut(1L, "Коммент 1", itemDto, "Иван Иванович", LocalDateTime.now());
        itemDtoDated = new ItemDtoDated(1L, "Вещь 1", "Описание вещи 1", true, bookingLast,
                bookingNext, List.of(commentDto));
    }

    @Test
    void shouldSaveItem() throws Exception {
        when(itemService.createItem(anyLong(), any()))
                .thenReturn(itemDto);
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId())));

        verify(itemService, times(1))
                .createItem(anyLong(), any());
    }

    @Test
    void shouldUpdateItem() throws Exception {
        when(itemService.updateItem(anyLong(), any(), anyLong()))
                .thenReturn(itemDto);
        mvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId())));

        verify(itemService, times(1))
                .updateItem(anyLong(), any(), anyLong());
    }

    @Test
    void shouldGetItemById() throws Exception {
        when(itemService.getItemById(anyLong(), anyLong()))
                .thenReturn(itemDtoDated);

        mvc.perform(get("/items/1")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemDtoDated)));

        verify(itemService, times(1))
                .getItemById(anyLong(), anyLong());
    }

    @Test
    void shouldGetUserItemsWithNoParams() throws Exception {
        List<ItemDtoDated> itemList = List.of(itemDtoDated);
        when(itemService.getUserItems(anyLong(), anyInt(), anyInt()))
                .thenReturn(itemList);
        mvc.perform(get("/items")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemList)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemDtoDated.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDtoDated.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDtoDated.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDtoDated.getAvailable())))
                .andExpect(jsonPath("$[0].lastBooking", notNullValue()))
                .andExpect(jsonPath("$[0].nextBooking", notNullValue()))
                .andExpect(jsonPath("$[0].comments.*", hasSize(1)));

        verify(itemService, times(1))
                .getUserItems(anyLong(), anyInt(), anyInt());
    }

    @Test
    void shouldGetUserItemsWithParams() throws Exception {
        int from = 0;
        int size = 2;
        List<ItemDtoDated> itemList = List.of(itemDtoDated);
        when(itemService.getUserItems(anyLong(), anyInt(), anyInt()))
                .thenReturn(itemList);
        mvc.perform(get("/items")
                        .header(USER_ID, 1L)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemList)));

        verify(itemService, times(1))
                .getUserItems(anyLong(), anyInt(), anyInt());
    }

    @Test
    void shouldSearch() throws Exception {
        int from = 0;
        int size = 2;
        String text = "вещ";
        List<ItemDto> itemList = List.of(itemDto);
        when(itemService.search(text, from, size))
                .thenReturn(itemList);
        mvc.perform(get("/items/search")
                        .header(USER_ID, 1L)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .param("text", text))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemList)));

        verify(itemService, times(1))
                .search(text, from, size);
    }

    @Test
    void shouldSaveComment() throws Exception {
        when(itemService.saveComment(anyLong(), anyLong(), any()))
                .thenReturn(commentDto);
        mvc.perform(post("/items/1/comment")
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(commentDto)));

        verify(itemService, times(1))
                .saveComment(anyLong(), anyLong(), any());
    }
}
