package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Film {
    int id;
    @NotBlank
    private String name;
    @Size(min = 1, max = 200)
    private String description;
    @NotNull
    private LocalDate releaseDate;
    @Positive
    private int duration;
    int rate;
    private Set<Integer> usersLike = new HashSet<>();

    public void addLike(int userId) {
        usersLike.add(userId);
        rate = usersLike.size();
    }

    public void deleteLike(int userId) {
        usersLike.remove(userId);
        rate = usersLike.size();
    }
}
