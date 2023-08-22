package ru.practicum.shareit.item.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Component
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Integer, Item> items = new HashMap<>(); // id, item
    private final Map<Integer, Set<Integer>> usersItems = new HashMap<>(); // userId, itemIds
    private int id = 0;

    @Override
    public Set<Integer> getUsersItems(int id) {
        Set<Integer> itemsForUser = usersItems.getOrDefault(id, new HashSet<>());
        return itemsForUser;
    }

    @Override
    public List<Item> findItem(String text) {
        List<Item> itemsList = new ArrayList<>();
        for (Item item : items.values()) {
            if ((StringUtils.containsIgnoreCase(item.getName(), text) ||
                    StringUtils.containsIgnoreCase(item.getDescription(), text)) &&
                    item.getAvailable()) {
                itemsList.add(item);
            }
        }
        return itemsList;
    }

    @Override
    public List<Item> getAll(int userId) {
        Set<Integer> itemKeys = usersItems.get(userId);
        List<Item> itemsList = new ArrayList<>();
        for (Integer key : itemKeys) {
            itemsList.add(items.get(key));
        }
        return itemsList;
    }

    @Override
    public Optional<Item> getItem(int id) {
        return Optional.of(items.get(id));
    }

    @Override
    public Item create(Item item, User user) {
        item.setId(generateId());
        item.setOwner(user);
        items.put(item.getId(), item);

        final Set<Integer> itemKeys = usersItems.computeIfAbsent(user.getId(), k -> new HashSet<>());

        itemKeys.add(item.getId());
        return item;
    }

    @Override
    public Item update(int id, int userId, Item item) {
        Item itemUpd = items.get(id);

        if (item.getName() != null && (!item.getName().isBlank())) {
            itemUpd.setName(item.getName());
        }
        if (item.getDescription() != null && (!item.getDescription().isBlank())) {
            itemUpd.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            itemUpd.setAvailable(item.getAvailable());
        }
        return itemUpd;
    }

    @Override
    public void deleteItem(int id) {
        items.remove(id);
    }

    private int generateId() {
        return ++id;
    }
}
