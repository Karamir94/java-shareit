package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.RequestDtoIn;
import ru.practicum.shareit.request.dto.RequestDtoOut;
import ru.practicum.shareit.request.dto.RequestMapper;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RequestMapperTest {

    private User user;
    private Request request;
    private RequestDtoIn requestDto;
    private ItemDto itemDto;

    @BeforeEach
    void beforeEach() {
        user = new User(1L, "Иван Иванович", "ii@mail.ru");
        requestDto = new RequestDtoIn(1L, "Описание запроса 1");
        itemDto = new ItemDto(1L, "Вещь 1", "Описание вещи 1", true, 1L);
        request = new Request(1L, "Описание вещи 1", user, LocalDateTime.now());
    }

    @Test
    void shouldConvertToItemRequest() {
        Request mapperRequest = RequestMapper.toItemRequest(requestDto, user);

        assertEquals(requestDto.getId(), mapperRequest.getId());
        assertEquals(requestDto.getDescription(), mapperRequest.getDescription());
        assertNotNull(mapperRequest.getCreated());
        assertEquals(user.getId(), mapperRequest.getUser().getId());
        assertEquals(user.getName(), mapperRequest.getUser().getName());
        assertEquals(user.getEmail(), mapperRequest.getUser().getEmail());
    }

    @Test
    void shouldConvertToItemRequestDto() {
        RequestDtoOut mapperRequestDto = RequestMapper.toItemRequestDto(request, List.of(itemDto));

        assertEquals(request.getId(), mapperRequestDto.getId());
        assertEquals(request.getDescription(), mapperRequestDto.getDescription());
        assertNotNull(request.getCreated());
        assertEquals(1, mapperRequestDto.getItems().size());
        assertEquals(itemDto.getId(), mapperRequestDto.getItems().get(0).getId());
        assertEquals(itemDto.getName(), mapperRequestDto.getItems().get(0).getName());
        assertEquals(itemDto.getDescription(), mapperRequestDto.getItems().get(0).getDescription());
        assertEquals(itemDto.getAvailable(), mapperRequestDto.getItems().get(0).getAvailable());
        assertEquals(itemDto.getRequestId(), mapperRequestDto.getId());

    }
}
