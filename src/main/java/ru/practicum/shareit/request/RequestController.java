package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;

import java.util.List;

import static ru.practicum.shareit.item.model.Header.USER_ID;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class RequestController {

    private final RequestService requestService;

    @PostMapping
    public RequestDto saveItemRequest(@RequestHeader(USER_ID) long userId, @RequestBody @Valid RequestDto requestDto) {
        log.info("В метод saveItemRequest передан userId {}, itemRequestDto.description: {}",
                userId, requestDto.getDescription());

        return requestService.createItemRequest(userId, requestDto);
    }

    @GetMapping
    public List<RequestDto> getItemRequests(@RequestHeader(USER_ID) long userId) { // список СВОИХ запросов
        log.info("В метод getItemRequests передан userId {}", userId);

        return requestService.getUserItemRequests(userId);
    }

    @GetMapping("/all")
    public List<RequestDto> getItemRequestsFromOtherUsers(@RequestHeader(USER_ID) long userId,
                                                          @RequestParam(defaultValue = "0") @Min(0) int from,
                                                          @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("В метод getItemRequestsFromOtherUsers передан userId {}, индекс первого элемента {}, " +
                "количество элементов на странице {}", userId, from, size);

        return requestService.getItemRequestsFromOtherUsers(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public RequestDto getOneItemRequest(@RequestHeader(USER_ID) long userId, @PathVariable long requestId) {
        log.info("В метод getOneItemRequest передан userId: {}, requestId: {}", userId, requestId);

        return requestService.getOneItemRequest(userId, requestId);
    }

}
