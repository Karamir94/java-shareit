package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;

    @GetMapping
    public List<UserDto> getAll() {
        log.info("Получен GET запрос getAll");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable long id) {
        log.info("В метод getUserById передан userId {}", id);
        return userService.getUserById(id);
    }

    @PostMapping()
    public UserDto createUser(@RequestBody UserDto userDto) {
        log.info("В метод createUser передан userDto.name {}, userDto.email {}", userDto.getName(), userDto.getEmail());
        return userService.createUser(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@RequestBody UserDto userDto, @PathVariable long userId) {
        log.info("В метод updateUser передан userId {}, userDto.name {}, userDto.email {}",
                userId, userDto.getName(), userDto.getEmail());
        return userService.updateUser(userDto, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable long id) {
        log.info("В метод deleteUser передан userId {}", id);
        userService.deleteUser(id);
    }
}
