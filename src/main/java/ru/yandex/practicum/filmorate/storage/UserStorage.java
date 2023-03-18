package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;
import java.util.Collection;
import java.util.List;


public interface UserStorage {
    public Collection<User> findAll();
    public User create(User user);
    public User put(User user);
    public User getUserById(int id);
    public void addFriend(int userId, int friendId);
    public void deleteFriend(int userId, int friendId);
    public List<User> getFriends(int id);
    public List<User> getListOfMutualFriends(int userId, int otherUserId);
}
