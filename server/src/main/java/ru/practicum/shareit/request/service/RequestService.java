package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.RequestDtoIn;
import ru.practicum.shareit.request.dto.RequestDtoOut;

import java.util.List;

public interface RequestService {

    RequestDtoOut createItemRequest(long userId, RequestDtoIn requestDto);

    List<RequestDtoOut> getUserItemRequests(long userId);

    List<RequestDtoOut> getItemRequestsFromOtherUsers(long userId, int from, int size);

    RequestDtoOut getOneItemRequest(long userId, long requestId);
}
