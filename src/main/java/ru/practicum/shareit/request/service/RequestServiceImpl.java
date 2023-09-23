package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.BadParameterException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.item.repository.ItemRepository;

import ru.practicum.shareit.request.dto.RequestMapper;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public RequestDto createItemRequest(long userId, RequestDto requestDto) {
        User user = checkUser(userId);

        if (requestDto.getDescription() == null) {
            throw new BadParameterException("отсутствует описание");
        }
        Request requestFromDto = RequestMapper.toItemRequest(requestDto, user);
        Request request = requestRepository.save(requestFromDto);
        return RequestMapper.toItemRequestDto(request, null);
    }

    @Override
    public List<RequestDto> getUserItemRequests(long userId) {
        checkUser(userId);

        List<Request> requests = requestRepository.findAllByUserIdOrderByCreatedDesc(userId);
        List<Long> itemRequestsIds = requests.stream()
                .map(Request::getId)
                .collect(Collectors.toList());
        List<Item> items = itemRepository.findAllByRequestIdInOrderById(itemRequestsIds);
        return collectRequestDtoList(requests, items);
    }

    @Override
    public List<RequestDto> getItemRequestsFromOtherUsers(long userId, int from, int size) {
        checkUser(userId);

        Pageable page = PageRequest.of(from, size);
        List<Request> requests = requestRepository.findAllByUserIdNot(userId, page);
        List<Long> requestsIds = requests.stream()
                .map(Request::getId)
                .collect(Collectors.toList());
        List<Item> items = itemRepository.findAllByRequestUserIdNotAndRequestIdInOrderById(userId, requestsIds);
        return collectRequestDtoList(requests, items);
    }

    @Override
    public RequestDto getOneItemRequest(long userId, long requestId) {
        checkUser(userId);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запроса с ID: " + requestId + " нет в базе"));
        List<ItemDto> itemDtos = itemRepository.findAllByRequestIdInOrderById(List.of(requestId)).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        return RequestMapper.toItemRequestDto(request, itemDtos);
    }

    private List<RequestDto> collectRequestDtoList(List<Request> requests, List<Item> items) {
        List<RequestDto> requestDtos = new ArrayList<>();
        for (Request request : requests) {
            List<ItemDto> requestItems = new ArrayList<>();
            for (Item item : items) {
                if (item.getRequest().getId() == request.getId()) {
                    requestItems.add(ItemMapper.toItemDto(item));
                }
            }
            requestDtos.add(RequestMapper.toItemRequestDto(request, requestItems));
        }
        return requestDtos;
    }

    private User checkUser(long userId) {
        checkId(userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не зарегистрирован"));
    }

    private void checkId(long userId) {
        if (userId <= 0) {
            throw new BadParameterException("id must be positive");
        }
    }
}
