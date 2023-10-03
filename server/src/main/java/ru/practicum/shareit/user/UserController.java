package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Create;
import ru.practicum.shareit.Update;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userServiceImpl;

    @GetMapping
    public List<UserDto> getAll() {
        log.info("Получен GET запрос getAll");
        return userServiceImpl.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable long id) {
        log.info("В метод getUserById передан userId {}", id);
        return userServiceImpl.getUserById(id);
    }

    @PostMapping()
    public UserDto create(@Validated(Create.class) @RequestBody UserDto userDto) {
        log.info("В метод create передан userDto.name {}, userDto.email {}", userDto.getName(), userDto.getEmail());
        return userServiceImpl.createUser(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable long id, @Validated(Update.class) @RequestBody UserDto userDto) {
        log.info("В метод updateUser передан userId {}, userDto.name {}, userDto.email {}",
                id, userDto.getName(), userDto.getEmail());
        return userServiceImpl.updateUser(userDto, id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable long id) {
        log.info("В метод deleteUser передан userId {}", id);
        userServiceImpl.deleteUser(id);
    }
}
