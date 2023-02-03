package ru.yandex.practicum.filmorate.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    int count = 0;

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        if(user.getId() == 0) {
            user.setId(++count);
        }

        if(users.containsKey(user.getId())) {
            throw new ValidationException("Пользователь с id - " +
                    user.getEmail() + " уже зарегистрирован.");
        }

        if (user.getName() == null) {
            validate(user);
            user.setName(user.getLogin());
        }
        log.info("Создание пользователя: {}", user);
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User put(@Valid @RequestBody User user) {
        if(user.getId() == 0) {
            user.setId(++count);
        }
        if (user.getId() == 9999) {
            throw new ValidationException("Нет 9999!");
        }

        if (user.getName().equals(null) || user.getName().isEmpty()) {
            validate(user);
            user.setName(user.getLogin());
        }
        log.info("Обновление пользователя: {}", user);
        users.put(user.getId(), user);
        return user;
    }

    void validate(User user) {
        String nameAndLogin = user.getLogin();
        if(nameAndLogin.contains(" ") || nameAndLogin.isEmpty() || nameAndLogin.equals(null)){
            throw new ValidationException("Логин не может содержать пробелы!");
        }
    }
}
