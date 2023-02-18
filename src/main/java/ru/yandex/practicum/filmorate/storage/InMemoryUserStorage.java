package ru.yandex.practicum.filmorate.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage{
    private final Map<Integer, User> users = new HashMap<>();
    int count = 0;

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        validateCreate(user);
        if (user.getName() == null || user.getName().isBlank() || user.getName().isEmpty()) {
            validate(user);
            user.setName(user.getLogin());
        }
        user.setId(++count);
        log.info("Создание пользователя: {}", user);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User put(User user) {
        validateUpdate(user.getId());
        if (user.getName() == null) {
            validate(user);
            user.setName(user.getLogin());
        }
        log.info("Обновление пользователя: {}", user);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getUserById(int userId) {
        validateUpdate(userId);
        User user = users.get(userId);
        return user;
    }

    public void validateCreate(User user) {
        if(users.containsKey(user.getId())) {
            throw new ValidationException("Пользователь с id - " +
                    user.getId() + " уже зарегистрирован.");
        }
    }

    public void validateUpdate(int id) {//ValidationException  <- было до
        if (!users.containsKey(id)) {
            throw new NotFoundException("Такого пользователя не существует!");
        }
    }

    public void validate(User user) {
        String nameAndLogin = user.getLogin();
        if(nameAndLogin.contains(" ") || nameAndLogin.isEmpty() || nameAndLogin == null || nameAndLogin.isBlank()){
            throw new ValidationException("Логин не может содержать пробелы!");
        }
    }
}
