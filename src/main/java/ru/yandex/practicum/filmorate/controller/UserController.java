package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public Collection<User> findAll() {
        log.info("Поиск всех пользователей");
        return userService.findAll();
    }

    @PostMapping("/users")
    public User create(@Valid @RequestBody User user) {
        log.info("Создание пользователя: {}", user);
        return userService.create(user);
    }

    @PutMapping("/users")
    public User put(@Valid @RequestBody User user) {
        log.info("Обновление пользователя: {}", user);
        return userService.put(user);
    }

    @GetMapping("/users/{id}")
    public User getUserBiYd(@PathVariable int id) {
        log.info("Получение пользователя по id: {}", id);
        return userService.getUserById(id);
    }

    @PutMapping("/users/{id}/friends/{friendId}")
    public void addFriend(@PathVariable int id, @PathVariable int friendId) {
        log.info("Добавление пользователя в друзья: {}", id);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/users/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable int id, @PathVariable int friendId) {
        log.info("Удаление пользователя из друзей: {}, {}", friendId, id);
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("/users/{id}/friends")
    public List<User> getFriendList(@PathVariable int id) {
        log.info("Получение списка друзей: {}", id);
        return userService.getFriendList(id);
    }

    @GetMapping("/users/{id}/friends/common/{otherId}")
    public List<User> getListOfMutualFriends(@PathVariable int id, @PathVariable int otherId) {
        log.info("Получение списка общих друзей : {}, {}", id, otherId);
        return userService.getListOfMutualFriends(id, otherId);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        log.info("Пользователь с идентификатором: " + id + " удален.");
    }

    @GetMapping("/users/{id}/recommendations")
    public List<Film> getRecommendation(@PathVariable int id) {
        return userService.getRecommendation(id);
    }

    @GetMapping("/users/{id}/feed")
    public List<Feed> getFeed(@PathVariable Integer id) {
        List<Feed> feed = userService.getFeed(id);
        log.info("Получена лента событий пользователя с идентификатором: {} ", id);
        return feed;
    }
}
