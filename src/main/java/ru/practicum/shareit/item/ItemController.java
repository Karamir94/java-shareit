package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Create;
import ru.practicum.shareit.Update;
import ru.practicum.shareit.item.dto.ItemDto;
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
    public List<ItemDto> getAllUsersItems(@RequestHeader(USER_ID) int userId) {
        log.info("Получен GET запрос");
        return itemService.getAll(userId);
    }

    @GetMapping("/{id}")
    public ItemDto getItem(@PathVariable int id) {
        log.info("Получен GET запрос");
        return itemService.getItem(id);
    }

    @GetMapping("/search")
    public List<ItemDto> findItem(@RequestParam String text) {
        log.info("Получен GET запрос на поиск вещи");
        return itemService.findItem(text);
    }

    @PostMapping()
    public ItemDto create(@RequestHeader(USER_ID) int userId, @Validated(Create.class) @RequestBody ItemDto itemDto) {
        log.info("Получен POST запрос");
        return itemService.create(itemDto, userId);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader(USER_ID) int userId,
                          @PathVariable int id,
                          @Validated(Update.class) @RequestBody ItemDto itemDto) {
        log.info("Получен PUT запрос");
        return itemService.update(id, userId, itemDto);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable int id) {
        log.info("Получен DELETE запрос");
        itemService.deleteItem(id);
    }
}
