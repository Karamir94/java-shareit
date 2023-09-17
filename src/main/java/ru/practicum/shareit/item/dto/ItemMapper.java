package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getIsAvailable()
        );
    }

    public static ItemDtoDated toItemDto(Item item,
                                         BookingDtoForItem lastBooking,
                                         BookingDtoForItem nextBooking,
                                         List<CommentDtoOut> comments) {
        return new ItemDtoDated(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getIsAvailable(),
                lastBooking,
                nextBooking,
                comments
        );
    }

    public static Item toItem(ItemDto itemDto, User user) {
        return new Item(
                itemDto.getId(),
                itemDto.getName() != null ? itemDto.getName() : null,
                itemDto.getDescription() != null ? itemDto.getDescription() : null,
                itemDto.getAvailable() != null ? itemDto.getAvailable() : null,
                user
        );
    }

    public static Item toItem(ItemDto itemDto, Item item) {
        return new Item(
                itemDto.getId(),
                itemDto.getName() != null ? itemDto.getName() : item.getName(),
                itemDto.getDescription() != null ? itemDto.getDescription() : item.getDescription(),
                itemDto.getAvailable() != null ? itemDto.getAvailable() : item.getIsAvailable(),
                item.getUser()
        );
    }
}
