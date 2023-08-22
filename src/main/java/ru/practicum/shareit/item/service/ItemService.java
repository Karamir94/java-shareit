package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    List<ItemDto> findItem(String text);

    List<ItemDto> getAll(int userId);

    ItemDto getItem(int id);

    ItemDto create(ItemDto itemDto, int userId);

    ItemDto update(int id, int userId, ItemDto itemDto);

    void deleteItem(int id);
}
