package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDtoIn;
import ru.practicum.shareit.item.dto.CommentDtoOut;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoDated;

import java.util.List;

public interface ItemService {

    ItemDto createItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, ItemDto itemDto, long itemId);

    ItemDtoDated getItemById(long userId, long itemId);

    List<ItemDtoDated> getUserItems(long userId, int from, int size);

    List<ItemDto> search(String text, int from, int size);

    CommentDtoOut saveComment(long userId, long itemId, CommentDtoIn comment);

    void deleteItem(long id);
}
