package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, userRowMapper);
    }
    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement psst = connection.prepareStatement(sql,  new String[] { "user_id" });
            psst.setString(1, user.getEmail());
            psst.setString(2, user.getLogin());
            psst.setString(3, user.getName());
            psst.setDate(4, Date.valueOf(user.getBirthday()));
            return psst;
        }, keyHolder);
        user.setId(keyHolder.getKey().intValue());
        log.info("Создание пользователя: {}", user);
        return user;
    }

    @Override
    public User put(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        int count = jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName()
                , user.getBirthday(), user.getId());
        if (count == 0) {
            log.info("Обновление пользователя: {}", user);
            throw new NotFoundException("Пользователь еще не создан!");
        }
        return getUserById(user.getId());
    }

    @Override
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        return jdbcTemplate.query(sql, userRowMapper, id)
                .stream().
                findAny().
                orElseThrow(() -> new NotFoundException("Пользватель с id " + id + "не найден"));
    }

    @Override
    public void addFriend(int userId, int friendId) {
        String sql = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        String sql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(int id){
        String sql = "SELECT * FROM users WHERE user_id IN (SELECT friend_id FROM friendship WHERE user_id = ?)";
        return jdbcTemplate.query(sql, userRowMapper, id);
    }

    @Override
    public List<User> getListOfMutualFriends(int userId, int otherUserId) {
        String sql = "SELECT u.* \n" +
                "FROM friendship AS f\n" +
                "INNER JOIN users AS u ON f.friend_id = u.user_id \n" +
                "WHERE f.user_id = ?\n" +
                "AND f.friend_id in (SELECT fr.friend_id\n" +
                "                  FROM friendship AS fr\n" +
                "                  WHERE fr.user_id = ?)";
        return jdbcTemplate.query(sql, userRowMapper, userId, otherUserId);
    }

    private final RowMapper<User> userRowMapper = (resultSet, rowNum) -> {
        User user = new User();
        user.setId(resultSet.getInt("user_id"));
        user.setEmail(resultSet.getString("email"));
        user.setLogin(resultSet.getString("login"));
        user.setName(resultSet.getString("name"));
        user.setBirthday(resultSet.getDate("birthday").toLocalDate());
        return user;
    };
}
