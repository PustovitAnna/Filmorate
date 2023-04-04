package ru.yandex.practicum.filmorate.model;

import lombok.*;
import ru.yandex.practicum.filmorate.annotation.validLogin.ValidLogin;

import javax.validation.constraints.Email;
import javax.validation.constraints.Past;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class User {
    int id;
    @Email
    private String email;
    @ValidLogin
    private String login;
    private String name;
    @Past
    private LocalDate birthday;
    private Set<Integer> friendsId = new HashSet<>();//del
}
