package ru.practicum.shareit.request.dto;

import lombok.Data;
import ru.practicum.shareit.Create;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ItemRequestDto {

    private int id;
    @NotBlank(groups = {Create.class})
    private String description;
    @NotNull(groups = {Create.class})
    private User requestor;
}
