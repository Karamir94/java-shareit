package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
@Sql(scripts = "classpath:data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RequestRepository requestRepository;

    private User user;
    private Request request;
    private Item item;
    private Item item2;

    @BeforeEach
    void beforeEach() {
        user = userRepository.save(new User(1L, "Иван Иванович", "ii@mail.ru"));
        request = requestRepository.save(new Request(1L, "Request 1", user, LocalDateTime.now()));
        item = itemRepository.save(new Item(1L, "Вещь 1", "Описание вещи 1",
                true, user, request));
        item2 = itemRepository.save(new Item(2L, "Вещь 2", "Описание вещи 2",
                true, user, null));
    }

    @Test
    void shouldFindByNameOrDescription() {
        String text = "ИсаНи";
        List<Item> items = itemRepository.findByNameOrDescription(text, Pageable.unpaged());

        assertThat(items.size(), equalTo(2));
        assertThat(items.get(0).getId(), equalTo(item.getId()));
    }

    @Test
    void shouldFindByNothingNameOrDescription() {
        String text = "кирпичный";
        List<Item> itemsList = itemRepository.findByNameOrDescription(text, Pageable.unpaged());

        assertThat(itemsList.size(), equalTo(0));
    }
}
