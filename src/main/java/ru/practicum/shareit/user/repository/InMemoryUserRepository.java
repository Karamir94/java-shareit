package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Component
public class InMemoryUserRepository implements UserRepository {

    private final Map<Integer, User> users = new HashMap<>();
    private int id = 0;

    @Override
    public Optional<User> getUser(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(User user) {
        user.setId(generateId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        User userUpd = users.get(user.getId());

        if (user.getName() != null && (!user.getName().isBlank())) {
            userUpd.setName(user.getName());
        }
        if (user.getEmail() != null && (!user.getEmail().isBlank())) {
            userUpd.setEmail(user.getEmail());
        }
        return userUpd;
    }

    @Override
    public void deleteUser(int id) {
        users.remove(id);
    }

    private int generateId() {
        return ++id;
    }
}
