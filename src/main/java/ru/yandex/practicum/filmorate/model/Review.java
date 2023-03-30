package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@ToString
@Builder
public class Review {
    int reviewId;
    @NotBlank
    private final String content;
    @NotNull
    private final Boolean isPositive;
    @NotNull
    private final int userId;
    @NotNull
    private final int filmId;
    private int useful;
}
