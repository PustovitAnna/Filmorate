package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    final UserController userController = new UserController();

    @Test
    void validate() {
        final User user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("userLoginAndName");
        user.setBirthday(LocalDate.of(2000, 10,10));
        if (user.getName() == null) {
            userController.validate(user);
            user.setName(user.getLogin());
        }
    }
}