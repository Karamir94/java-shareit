package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserDto> getAll() {
        List<User> users = userRepository.getAll();
        List<UserDto> usersDto = new ArrayList<>();
        for (User user : users) {
            usersDto.add(userMapper.convert(user));
        }
        return usersDto;
    }

    public UserDto getUser(int id) {
        checkId(id);
        User user = userRepository.getUser(id)
                .orElseThrow(() -> new NotFoundException(String.format("User № %d not found", id)));
        return userMapper.convert(user);
    }

    public UserDto create(UserDto userDto) {
        if (!checkEmail(userDto)) {
            throw new AlreadyExistException("Email уже зарегистрирован");
        }
        User user = userRepository.create(userMapper.convert(userDto));
        return userMapper.convert(user);
    }

    private boolean checkEmail(UserDto userDto) {
        String email = userDto.getEmail();
        List<User> users = userRepository.getAll().stream()
                .filter((user) -> user.getEmail().equals(email))
                .collect(Collectors.toList());
        return users.isEmpty();
    }

    public UserDto update(int id, UserDto userDto) {
        checkId(id);
        UserDto userDtoUpd = getUser(id);

        if (userDto.getEmail() != null) {
            if (!(userDtoUpd.getEmail().equals(userDto.getEmail()))) {
                if (!checkEmail(userDto)) {
                    throw new AlreadyExistException("Email уже зарегистрирован");
                }
            }
        }

        User user = userMapper.convert(userDto);

        user.setId(id);
        return userMapper.convert(userRepository.update(user));
    }

    public void deleteUser(int id) {
        checkId(id);
        userRepository.deleteUser(id);
    }

    private void checkId(long userId) {
        if (userId <= 0) {
            throw new NotFoundException("id must be positive");
        }
    }
}
