package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
//    Set<Integer> findAllItemsIdByUserId(int id);

    @Query(" select i from Item i " +
            "where upper(i.name) like upper(concat('%', ?1, '%')) " +
            "or upper(i.description) like upper(concat('%', ?1, '%')) " +
            "and i.isAvailable = true ")
    List<Item> search(String text);

    List<Item> findAllItemsByUserIdOrderById(Long userId);

//    Optional<Item> findItemById(int id);
//
//    Item create(Item item, User user);
//
//    Item update(int id, int userId, Item item);
//
//    void deleteItemById(int id);
}
