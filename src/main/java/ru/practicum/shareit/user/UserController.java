package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Create;
import ru.practicum.shareit.Update;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserDto> getAll() {
        log.info("Получен GET запрос");
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable int id) {
        log.info("Получен GET запрос");
        return userService.getUser(id);
    }

    @PostMapping()
    public UserDto create(@Validated(Create.class) @RequestBody UserDto userDto) {
        log.info("Получен POST запрос");
        return userService.create(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable int id, @Validated(Update.class) @RequestBody UserDto userDto) {
        log.info("Получен PUT запрос");
        return userService.update(id, userDto);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable int id) {
        log.info("Получен DELETE запрос");
        userService.deleteUser(id);
    }
}
