package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.Create;

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
    @Size(max = 50)
    private String name;

    @NotBlank(groups = {Create.class})
    @Size(max = 500)
    private String description;

    @NotNull(groups = {Create.class})
    private Boolean available;
}
