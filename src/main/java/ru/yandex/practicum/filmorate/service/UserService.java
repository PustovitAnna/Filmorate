package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.util.EventType;
import ru.yandex.practicum.filmorate.util.Operation;

import java.util.*;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final FeedStorage feedStorage;

    @Autowired
    public UserService(UserStorage userStorage, FeedStorage feedStorage) {
        this.userStorage = userStorage;
        this.feedStorage = feedStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank() || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User put(User user) {
        return userStorage.put(user);
    }

    public User getUserById(int userId) {
        return userStorage.findById(userId);
    }

    public void addFriend(int userId, int friendId) {
        validateId(userId, friendId);
        userStorage.addFriend(userId, friendId);
        feedStorage.saveFeed(userId, EventType.FRIEND, Operation.ADD, friendId);
    }

    public void deleteFriend(int userId, int friendId) {
        validateId(userId, friendId);
        userStorage.deleteFriend(userId, friendId);
        feedStorage.saveFeed(userId, EventType.FRIEND, Operation.REMOVE, friendId);
    }

    public List<User> getFriendList(int userId) {
        return userStorage.getFriends(userId);
    }

    public List<User> getListOfMutualFriends(int userId, int otherUserId) {
        validateId(userId, otherUserId);
        return userStorage.getListOfMutualFriends(userId, otherUserId);
    }

    void validateId(int userId, int otherUserId) {
        if (userId <= 0 || otherUserId <= 0 || userId == otherUserId)
            throw new NotFoundException("Некорректный id пользователя!");
    }

    public void deleteUser(int userId) {
        userStorage.deleteUser(userId);
    }

    public List<Film> getRecommendation(int id) {
        return userStorage.getRecommendation(id);
    }

    public List<Feed> getFeed(Integer id) {
        User user = userStorage.findById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id: " + id + " не найден.");
        }
        return feedStorage.getFeed(id);
    }
}

