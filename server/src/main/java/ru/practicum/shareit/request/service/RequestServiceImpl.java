package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.RequestDtoIn;
import ru.practicum.shareit.request.dto.RequestDtoOut;
import ru.practicum.shareit.request.dto.RequestMapper;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public RequestDtoOut createItemRequest(long userId, RequestDtoIn requestDto) {
        User user = checkUser(userId);

        Request requestFromDto = RequestMapper.toItemRequest(requestDto, user);
        Request request = requestRepository.save(requestFromDto);
        return RequestMapper.toItemRequestDto(request, null);
    }

    @Override
    public List<RequestDtoOut> getUserItemRequests(long userId) {
        checkUser(userId);

        List<Request> requests = requestRepository.findAllByUserIdOrderByCreatedDesc(userId);

        List<Long> requestsIds = requests
                .stream()
                .map(Request::getId)
                .collect(Collectors.toList());
        Map<Long, List<ItemDto>> requestItemsMap = itemRepository.findAllByRequestIdInOrderById(requestsIds)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(groupingBy(ItemDto::getRequestId, toList()));

        return collectRequestDtoList(requestItemsMap, requests);
    }

    @Override
    public List<RequestDtoOut> getItemRequestsFromOtherUsers(long userId, int from, int size) {
        checkUser(userId);

        Pageable page = PageRequest.of(from, size);
        List<Request> requests = requestRepository.findAllByUserIdNot(userId, page);
        List<Long> requestsIds = requests.stream()
                .map(Request::getId)
                .collect(Collectors.toList());
        Map<Long, List<ItemDto>> requestItemsMap = itemRepository.findAllByRequestIdInOrderById(requestsIds)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(groupingBy(ItemDto::getRequestId, toList()));

        return collectRequestDtoList(requestItemsMap, requests);
    }

    @Override
    public RequestDtoOut getOneItemRequest(long userId, long requestId) {
        checkUser(userId);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запроса с ID: " + requestId + " нет в базе"));
        List<ItemDto> itemDtos = itemRepository.findAllByRequestIdInOrderById(List.of(requestId)).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        return RequestMapper.toItemRequestDto(request, itemDtos);
    }

    private List<RequestDtoOut> collectRequestDtoList(Map<Long, List<ItemDto>> requestItemsMap,
                                                      List<Request> requests) {
        List<RequestDtoOut> requestDtos = new ArrayList<>();

        for (Request request : requests) {
            requestDtos.add(RequestMapper.toItemRequestDto(request, requestItemsMap.get(request.getId())));
        }
        return requestDtos;
    }

    private User checkUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не зарегистрирован"));
    }
}
