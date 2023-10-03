package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.Create;
import ru.practicum.shareit.Update;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ItemDto {

    private Long id;

    @NotBlank(groups = {Create.class})
    @Size(max = 50, groups = {Create.class, Update.class})
    private String name;

    @NotBlank(groups = {Create.class})
    @Size(max = 500, groups = {Create.class, Update.class})
    private String description;

    @NotNull(groups = {Create.class})
    private Boolean available;

    private Long requestId;
}
