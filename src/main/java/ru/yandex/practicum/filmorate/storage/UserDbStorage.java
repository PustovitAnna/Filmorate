package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmStorage filmStorage;
    private final FilmMapper filmMapper;

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
            PreparedStatement psst = connection.prepareStatement(sql, new String[]{"user_id"});
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
        int count = jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(),
                user.getBirthday(), user.getId());
        if (count == 0) {
            log.info("Обновление пользователя: {}", user);
            throw new NotFoundException("Пользователь еще не создан!");
        }
        return findById(user.getId());
    }

    @Override
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        return jdbcTemplate.query(sql, userRowMapper, id)
                .stream()
                .findAny()
                .orElseThrow(() -> new NotFoundException("Пользватель с id " + id + "не найден"));
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
    public List<User> getFriends(int id) {
        findById(id);
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

    @Override
    public void deleteUser(int userId) {
        jdbcTemplate.update("DELETE FROM users WHERE user_id = ?", userId);
    }

    @Override
    public List<Film> getRecommendation(int id) {
        String filmsId = "SELECT film_id FROM popular_films " +
                "WHERE user_id = ?";
        List<Integer> usersFilms = new ArrayList<>();
        jdbcTemplate.query(filmsId, (rs, rowNum) -> mapFilmsId(rs, usersFilms), id);
        String recommendUserId = "SELECT user_id FROM popular_films " +
                "GROUP BY user_id,film_id " +
                "HAVING film_id IN (" + usersFilms.stream().map(String::valueOf).collect(Collectors.joining(",")) + ") AND user_id != ? " +
                "ORDER BY COUNT(film_id) desc " +
                "LIMIT 1";
        List<Integer> usersId = new ArrayList<>();
        jdbcTemplate.query(recommendUserId, (rs, rowNum) -> mapUsersId(rs, usersId), id);

        String recommendations = "SELECT f.*, mpa.name_rating " +
                "FROM films AS f " +
                "LEFT JOIN ratings AS mpa ON f.rating_id = mpa.rating_id " +
                "LEFT JOIN popular_films AS likes ON f.film_id = likes.film_id " +
                "WHERE likes.user_id IN (" + usersId.stream().map(String::valueOf).collect(Collectors.joining(",")) + ") " +
                "AND f.film_id NOT IN (" + usersFilms.stream().map(String::valueOf).collect(Collectors.joining(",")) + ") ";

        Map<Integer, Set<Director>> filmsDirectors = new HashMap<>();
        List<Film> films = jdbcTemplate.query(recommendations, filmMapper);
        if (films.isEmpty()) {
            return films;
        }
        String sql3 = "SELECT *,df.film_id " +
                "FROM DIRECTORS d " +
                "LEFT JOIN DIRECTORS_FILMS df ON d.director_id = df.director_id ";
        jdbcTemplate.query(sql3, (rs, rowNum) -> filmStorage.assignDirectors(rs, films, filmsDirectors));
        return films;
    }

    private List<Integer> mapFilmsId(ResultSet resultSet, List<Integer> usersFilms) throws SQLException {
        final Integer a = resultSet.getInt("film_id");
        usersFilms.add(a);
        return usersFilms;
    }

    private List<Integer> mapUsersId(ResultSet resultSet, List<Integer> usersId) throws SQLException {
        usersId.add(resultSet.getInt("user_id"));
        return usersId;
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
