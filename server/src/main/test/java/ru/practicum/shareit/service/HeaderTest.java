package ru.practicum.shareit.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.practicum.shareit.service.Header.USER_ID;

class HeaderTest {

    String userId = "X-Sharer-User-Id";

    @Test
    void shouldGetUserId() {
        assertEquals(USER_ID, userId);
    }
}