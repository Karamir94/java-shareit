package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserMapperTest {

    private User user;
    private UserDto userDto;

    @BeforeEach
    void beforeEach() {
        user = new User(1L, "Иван Иванович", "ii@mail.ru");
        userDto = new UserDto(1L, "Иван Иванович", "ii@mail.ru");
    }

    @Test
    void shouldConvertToUserDto() {
        UserDto userDto = UserMapper.toUserDto(user);

        assertEquals(1L, userDto.getId());
        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getEmail(), userDto.getEmail());
    }

    @Test
    void shouldConvertToUser() {
        User user = UserMapper.toUser(userDto);

        assertEquals(1L, userDto.getId());
        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getEmail(), userDto.getEmail());
    }

    @Test
    void shouldConvertToUserDtoToUserWithUser() {
        userDto.setEmail(null);

        User user2 = new User(1L, "Петр Петрович", "pp@mail.ru");

        User mappedUser = UserMapper.toUser(userDto, user2);

        assertEquals(1L, mappedUser.getId());
        assertEquals("Иван Иванович", mappedUser.getName());
        assertEquals("pp@mail.ru", mappedUser.getEmail());
    }
}
