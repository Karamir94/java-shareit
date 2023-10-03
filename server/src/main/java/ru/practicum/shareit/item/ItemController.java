package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Create;
import ru.practicum.shareit.Update;
import ru.practicum.shareit.item.dto.CommentDtoIn;
import ru.practicum.shareit.item.dto.CommentDtoOut;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoDated;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

import static ru.practicum.shareit.service.Header.USER_ID;

@Slf4j
@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public List<ItemDtoDated> getUserItems(@RequestHeader(USER_ID) long userId,
                                           @RequestParam(defaultValue = "0") @Min(0) int from,
                                           @RequestParam(defaultValue = "20") @Positive int size) {
        log.info("В метод getUserItems передан userId {}, индекс первого элемента {}, количество элементов на " +
                "странице {}", userId, from, size);
        return itemService.getUserItems(userId, from, size);
    }

    @GetMapping("/{id}")
    public ItemDtoDated getItem(@RequestHeader(USER_ID) long userId, @PathVariable long id) {
        log.info("В метод getItemById передан userId {}, itemId {}", userId, id);
        return itemService.getItemById(userId, id);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text,
                                @RequestParam(defaultValue = "0") @Min(0) int from,
                                @RequestParam(defaultValue = "20") @Positive int size) {
        log.info("В метод search передан text: '{}', индекс первого элемента {}, количество элементов на " +
                "странице {}", text, from, size);
        return itemService.search(text, from, size);
    }

    @PostMapping()
    public ItemDto create(@RequestHeader(USER_ID) long userId, @Validated(Create.class) @RequestBody ItemDto itemDto) {
        log.info("В метод create передан userId {}, itemDto.name: {}, itemDto.description: {}, itemDto.isAvailable {}",
                userId, itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable());
        return itemService.createItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDtoOut saveComment(@RequestHeader(USER_ID) long userId,
                                     @PathVariable long itemId,
                                     @RequestBody @Validated(Create.class) CommentDtoIn comment) {
        log.info("В метод saveComment передан userId {}, itemId {}, отзыв с длиной текста: {}",
                userId, itemId, comment.getText().length());
        return itemService.saveComment(userId, itemId, comment);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader(USER_ID) long userId,
                          @PathVariable long id,
                          @Validated(Update.class) @RequestBody ItemDto itemDto) {
        log.info("В метод updateItem передан userId {}, itemId {}, itemDto.name: {}, itemDto.description: {}, " +
                        "itemDto.isAvailable {}",
                userId, id, itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable());
        return itemService.updateItem(userId, itemDto, id);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable long id) {
        log.info("В метод deleteItem передан userId {}", id);
        itemService.deleteItem(id);
    }
}
