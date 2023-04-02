package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.util.EventType;
import ru.yandex.practicum.filmorate.util.Operation;

import java.util.Collection;
import java.util.List;


public interface UserStorage {
    Collection<User> findAll();

    User create(User user);

    User put(User user);

    User getUserById(int id);

    void addFriend(int userId, int friendId);

    void deleteFriend(int userId, int friendId);

    List<User> getFriends(int id);

    List<User> getListOfMutualFriends(int userId, int otherUserId);

    void deleteUser(int userId);

    List<Film> getRecommendation(int id);

    List<Feed> getFeed(Integer id);

    void saveFeed(Integer userId, EventType ev, Operation op, Integer entityId);
}
