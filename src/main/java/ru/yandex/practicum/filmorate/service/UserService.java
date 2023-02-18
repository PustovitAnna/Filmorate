package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage){
      this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User put(User user) {
        return userStorage.put(user);
    }

    public User getUserById(int userId) {
        return userStorage.getUserById(userId);
    }

    public void addFriend(int userId, int friendId) {
        validateId(userId, friendId);
        final User user = userStorage.getUserById(userId);
        final User friend = userStorage.getUserById(friendId);
        user.getFriendsId().add(friendId);
        friend.getFriendsId().add(userId);
    }

    public  void  deleteFriend(int userId, int friendId) {
        validateId(userId, friendId);
        final User user = userStorage.getUserById(userId);
        final User friend = userStorage.getUserById(friendId);
        user.getFriendsId().remove(friendId);
        friend.getFriendsId().remove(userId);
    }

    public List<User> getFriendList(int userId) {
        final User user = userStorage.getUserById(userId);
        Set<Integer> friendList = user.getFriendsId();
        List<User> usersFriends = new ArrayList<>();

        for (Integer id : friendList) {
            usersFriends.add(userStorage.getUserById(id));
        }
        return usersFriends;
    }

    public List<User> getListOfMutualFriends(int userId, int otherUserId) {
        validateId(userId, otherUserId);
        final User user = userStorage.getUserById(userId);
        final User otherUser = userStorage.getUserById(otherUserId);
        Set<Integer> friendList1 = user.getFriendsId();
        Set<Integer> friendList2 = otherUser.getFriendsId();
        List<User> resultList = new ArrayList<>();

        for (Integer id : friendList1) {
            if (friendList2.contains(id)) {
                resultList.add(userStorage.getUserById(id));
            }
        }
        return resultList;
    }

    void validateId(int userId, int otherUserId) {
        if(userId <=0 || otherUserId <= 0 || userId == otherUserId)
            throw new NotFoundException("Некорректный id пользователя!");
    }
}
