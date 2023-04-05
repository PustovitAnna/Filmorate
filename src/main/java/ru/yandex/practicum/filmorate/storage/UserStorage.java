package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface UserStorage {

    Collection<User> findAll();

    User create(User user);

    User put(User user);

    User findById(int id);

    void addFriend(int userId, int friendId);

    void deleteFriend(int userId, int friendId);

    List<User> getFriends(int id);

    List<User> getListOfMutualFriends(int userId, int otherUserId);

    void deleteUser(int userId);
}
