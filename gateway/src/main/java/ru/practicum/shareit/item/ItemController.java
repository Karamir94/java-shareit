package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDtoIn;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.service.Create;
import ru.practicum.shareit.service.Update;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

import static ru.practicum.shareit.service.Header.USER_ID;

@Slf4j
@RequiredArgsConstructor
@Controller
@Validated
@RequestMapping("/items")
public class ItemController {

    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> getUserItems(@RequestHeader(USER_ID) long userId,
                                               @RequestParam(defaultValue = "0") @Min(0) int from,
                                               @RequestParam(defaultValue = "20") @Positive int size) {
        log.info("В метод getUserItems передан userId {}, индекс первого элемента {}, количество элементов на " +
                "странице {}", userId, from, size);
        return itemClient.getUserItems(userId, from, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItem(@RequestHeader(USER_ID) long userId, @PathVariable long id) {
        log.info("В метод getItemById передан userId {}, itemId {}", userId, id);
        return itemClient.getItemById(userId, id);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader(USER_ID) long userId,
                                         @RequestParam String text,
                                         @RequestParam(defaultValue = "0") @Min(0) int from,
                                         @RequestParam(defaultValue = "20") @Positive int size) {
        log.info("В метод search передан userId {}, text: '{}', индекс первого элемента {}, количество элементов на " +
                "странице {}", userId, text, from, size);
        if (text.isBlank()) {
            return new ResponseEntity<>(List.of(), HttpStatus.OK);
        }
        return itemClient.search(userId, text, from, size);
    }

    @PostMapping()
    public ResponseEntity<Object> create(@RequestHeader(USER_ID) long userId,
                                         @Validated(Create.class)
                                         @RequestBody ItemDto itemDto) {
        log.info("В метод create передан userId {}, itemDto.name: {}, itemDto.description: {}, itemDto.isAvailable {}",
                userId, itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable());
        return itemClient.createItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> saveComment(@RequestHeader(USER_ID) long userId,
                                              @PathVariable long itemId,
                                              @RequestBody @Validated(Create.class) CommentDtoIn comment) {
        log.info("В метод saveComment передан userId {}, itemId {}, отзыв с длиной текста: {}",
                userId, itemId, comment.getText().length());
        return itemClient.saveComment(userId, itemId, comment);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@RequestHeader(USER_ID) long userId,
                                         @PathVariable long id,
                                         @Validated(Update.class) @RequestBody ItemDto itemDto) {
        log.info("В метод updateItem передан userId {}, itemId {}, itemDto.name: {}, itemDto.description: {}, " +
                        "itemDto.isAvailable {}",
                userId, id, itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable());
        return itemClient.updateItem(userId, itemDto, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteItem(@PathVariable long id) {
        log.info("В метод deleteItem передан id {}", id);
        return itemClient.deleteItem(id);
    }
}
