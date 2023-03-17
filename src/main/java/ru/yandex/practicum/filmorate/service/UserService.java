package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.ValidationException;
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
        validate(user);
        if (user.getName() == null || user.getName().isBlank() || user.getName().isEmpty()) {
            if(user.getEmail().contains(" ") || user.getEmail().isEmpty() || user.getEmail() == null || user.getEmail().isBlank()){
                throw new ValidationException("Логин не может содержать пробелы!");
            } else {
                user.setName(user.getLogin());
            }
        }
        return userStorage.create(user);
    }

    public User put(User user) {
        validate(user);
        return userStorage.put(user);
    }

    public User getUserById(int userId) {
        return userStorage.getUserById(userId);
    }

    public void addFriend(int userId, int friendId) {
        validateId(userId, friendId);
        userStorage.addFriend(userId, friendId);
    }

    public  void  deleteFriend(int userId, int friendId) {
        validateId(userId, friendId);
        userStorage.deleteFriend(userId, friendId);
    }

    public List<User> getFriendList(int userId) {
       return userStorage.getFriends(userId);
    }

    public List<User> getListOfMutualFriends(int userId, int otherUserId) {
        validateId(userId, otherUserId);
        return userStorage.getListOfMutualFriends(userId, otherUserId);
    }


    void validateId(int userId, int otherUserId) {
        if(userId <=0 || otherUserId <= 0 || userId == otherUserId)
            throw new NotFoundException("Некорректный id пользователя!");
    }

    public void validate(User user) {
        String nameAndLogin = user.getLogin();
        if(nameAndLogin.contains(" ") || nameAndLogin.isEmpty() || nameAndLogin == null || nameAndLogin.isBlank()){
            throw new ValidationException("Логин не может содержать пробелы!");
        }
    }
}
