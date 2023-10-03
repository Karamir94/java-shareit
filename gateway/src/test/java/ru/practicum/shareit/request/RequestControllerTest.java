package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.RequestDtoIn;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.service.Header.USER_ID;

@WebMvcTest(RequestController.class)
@AutoConfigureMockMvc
class RequestControllerTest {

    @MockBean
    private RequestClient requestClient;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private RequestDtoIn requestDto;
    private ResponseEntity<Object> response;
    private ResponseEntity<Object> responseWithList;

    @BeforeEach
    public void itemCreate() {
        requestDto = new RequestDtoIn(1L, "Описание запроса");
        response = new ResponseEntity<>(requestDto, HttpStatus.OK);
        responseWithList = new ResponseEntity<>(List.of(requestDto), HttpStatus.OK);
    }

    @Test
    void shouldSaveItemRequest() throws Exception {
        when(requestClient.createItemRequest(anyLong(), any()))
                .thenReturn(response);
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())));

        verify(requestClient, times(1))
                .createItemRequest(anyLong(), any());
    }

    @Test
    void shouldSaveItemRequestWithBlankDescription() throws Exception {
        requestDto.setDescription("");

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isBadRequest());

        verify(requestClient, never())
                .createItemRequest(anyLong(), any());
    }

    @Test
    void shouldSaveItemRequestWithNullDescription() throws Exception {
        requestDto.setDescription(null);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isBadRequest());

        verify(requestClient, never())
                .createItemRequest(anyLong(), any());
    }

    @Test
    void shouldGetItemRequests() throws Exception {

        when(requestClient.getUserItemRequests(anyLong()))
                .thenReturn(responseWithList);

        mvc.perform(get("/requests")
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestDto.getDescription())));

        verify(requestClient, times(1))
                .getUserItemRequests(anyLong());
    }

    @Test
    void shouldGetItemRequestsFromOtherUsers() throws Exception {
        int from = 0;
        int size = 5;

        when(requestClient.getItemRequestsFromOtherUsers(anyLong(), anyInt(), anyInt()))
                .thenReturn(responseWithList);

        mvc.perform(get("/requests/all")
                        .header(USER_ID, 1)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(requestDto))))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestDto.getDescription())));

        verify(requestClient, times(1))
                .getItemRequestsFromOtherUsers(anyLong(), anyInt(), anyInt());
    }

    @Test
    void shouldGetItemRequestsFromOtherUsersWithNegativeFrom() throws Exception {
        int from = -1;
        int size = 5;

        mvc.perform(get("/requests/all")
                        .header(USER_ID, 1)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest());

        verify(requestClient, never())
                .getItemRequestsFromOtherUsers(anyLong(), anyInt(), anyInt());
    }

    @Test
    void shouldGetItemRequestsFromOtherUsersWithNegativeSize() throws Exception {
        int from = 0;
        int size = -5;

        mvc.perform(get("/requests/all")
                        .header(USER_ID, 1)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest());

        verify(requestClient, never())
                .getItemRequestsFromOtherUsers(anyLong(), anyInt(), anyInt());
    }

    @Test
    void shouldGetItemRequestsFromOtherUsersWithNullSize() throws Exception {
        int from = 0;
        int size = 0;

        mvc.perform(get("/requests/all")
                        .header(USER_ID, 1)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest());

        verify(requestClient, never())
                .getItemRequestsFromOtherUsers(anyLong(), anyInt(), anyInt());
    }

    @Test
    void shouldGetOneItemRequest() throws Exception {

        when(requestClient.getOneItemRequest(anyLong(), anyLong()))
                .thenReturn(response);

        mvc.perform(get("/requests/1")
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())));

        verify(requestClient, times(1))
                .getOneItemRequest(anyLong(), anyLong());
    }
}
