package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@RestController
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public Collection<User> findAll() {
        return userService.findAll();
    }

    @PostMapping("/users")
    public User create(@Valid @RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping("/users")
    public User put(@Valid @RequestBody User user) {
        return userService.put(user);
    }

    @GetMapping("/users/{id}")                        // юзер по id (0)
    public User getUserBiYd(@PathVariable int id) {
        return userService.getUserById(id);
    }

    @PutMapping("/users/{id}/friends/{friendId}")     // добавить в друзья по id (1)
    public void addFriend(@PathVariable int id, @PathVariable int friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/users/{id}/friends/{friendId}")   // удалить из друзей по id (2)
    public void deleteFriend(@PathVariable int id, @PathVariable int friendId) {
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("/users/{id}/friends")                 // получить список друзей (3)
    public List<User> getFriendList(@PathVariable int id) {
        return userService.getFriendList(id);
    }

    @GetMapping("/users/{id}/friends/common/{otherId}") // получить список общих друзей (4)
    public List<User> getListOfMutualFriends(@PathVariable int id, @PathVariable int otherId) {
        return userService.getListOfMutualFriends(id, otherId);
    }
}
