package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

    @Override
    public List<ItemDto> findItem(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        } else {
            List<Item> items = itemRepository.findItem(text);
            List<ItemDto> itemsDto = new ArrayList<>();

            for (Item item : items) {
                itemsDto.add(itemMapper.convert(item));
            }
            return itemsDto;
        }
    }

    @Override
    public List<ItemDto> getAll(int userId) {
        List<Item> items = itemRepository.getAllUsersItems(userId);
        List<ItemDto> itemsDto = new ArrayList<>();
        for (Item item : items) {
            itemsDto.add(itemMapper.convert(item));
        }
        return itemsDto;
    }

    @Override
    public ItemDto getItem(int id) {
        checkId(id);
        Item item = itemRepository.getItem(id)
                .orElseThrow(() -> new NotFoundException(String.format("Item № %d not found", id)));
        return itemMapper.convert(item);
    }

    @Override
    public ItemDto create(ItemDto itemDto, int userId) {
        checkId(userId);
        User user = userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User № %d not found", userId)));
        Item item = itemRepository.create(itemMapper.convert(itemDto), user);
        return itemMapper.convert(item);
    }

    @Override
    public ItemDto update(int id, int userId, ItemDto itemDto) {
        checkId(id);
        checkId(userId);
        User user = userRepository.getUser(userId) // Проверка существования пользователя
                .orElseThrow(() -> new NotFoundException(String.format("User № %d not found", userId)));
        if (itemRepository.getUsersItemsId(userId).contains(id)) { // проверка принадлежности предмета к пользователю
            Item item = itemRepository.update(id, userId, itemMapper.convert(itemDto));
            return itemMapper.convert(item);
        } else {
            throw new NotFoundException("Этот предмет не принадлежит данному пользователю");
        }
    }

    @Override
    public void deleteItem(int id) {
        checkId(id);
        itemRepository.deleteItem(id);
    }

    private void checkId(long userId) {
        if (userId <= 0) {
            throw new NotFoundException("id must be positive");
        }
    }
}
