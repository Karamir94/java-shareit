package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.service.Create;
import ru.practicum.shareit.service.Update;
import ru.practicum.shareit.user.dto.UserDto;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("Получен GET запрос getAllUsers");
        return userClient.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUser(@PathVariable long id) {
        log.info("В метод getUserById передан userId {}", id);
        return userClient.getUserById(id);
    }

    @PostMapping()
    public ResponseEntity<Object> create(@Validated(Create.class) @RequestBody UserDto userDto) {
        log.info("В метод create передан userDto.name {}, userDto.email {}", userDto.getName(), userDto.getEmail());
        return userClient.createUser(userDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable long id, @Validated(Update.class) @RequestBody UserDto userDto) {
        log.info("В метод updateUser передан userId {}, userDto.name {}, userDto.email {}",
                id, userDto.getName(), userDto.getEmail());
        return userClient.updateUser(userDto, id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable long id) {
        log.info("В метод deleteUser передан userId {}", id);
        userClient.deleteUser(id);
    }
}
