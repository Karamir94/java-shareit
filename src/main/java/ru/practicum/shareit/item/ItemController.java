package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Create;
import ru.practicum.shareit.Update;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoDated;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static ru.practicum.shareit.item.model.Header.USER_ID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public List<ItemDtoDated> getUserItems(@RequestHeader(USER_ID) long userId) {
        log.info("Получен GET запрос");
        return itemService.getUserItems(userId);
    }

    @GetMapping("/{id}")
    public ItemDtoDated getItem(@RequestHeader(USER_ID) long userId, @PathVariable long id) {
        log.info("Получен GET запрос");
        return itemService.getItemById(userId, id);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        log.info("Получен GET запрос на поиск вещи");
        return itemService.search(text);
    }

    @PostMapping()
    public ItemDto create(@RequestHeader(USER_ID) long userId, @Validated(Create.class) @RequestBody ItemDto itemDto) {
        log.info("Получен POST запрос");
        return itemService.createItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto saveComment(@RequestHeader(USER_ID) long userId,
                                  @PathVariable long itemId,
                                  @RequestBody @Validated(Create.class) CommentDto comment) {
        log.info("Получен POST запрос на создание комментария");
        return itemService.saveComment(userId, itemId, comment);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader(USER_ID) long userId,
                          @PathVariable long id,
                          @Validated(Update.class) @RequestBody ItemDto itemDto) {
        log.info("Получен PUT запрос");
        return itemService.updateItem(userId, itemDto, id);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable long id) {
        log.info("Получен DELETE запрос");
        itemService.deleteItem(id);
    }
}
