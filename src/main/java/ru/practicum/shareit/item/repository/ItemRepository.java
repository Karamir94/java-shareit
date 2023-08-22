package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ItemRepository {
    Set<Integer> getUsersItemsId(int id);

    List<Item> findItem(String text);

    List<Item> getAllUsersItems(int userId);

    Optional<Item> getItem(int id);

    Item create(Item item, User user);

    Item update(int id, int userId, Item item);

    void deleteItem(int id);
}
