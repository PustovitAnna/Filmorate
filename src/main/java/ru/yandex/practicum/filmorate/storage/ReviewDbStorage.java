package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.util.EventType;
import ru.yandex.practicum.filmorate.util.Operation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;

    @Override
    public List<Review> getAllReviews() {
        String sql = "SELECT * FROM reviews";
        List<Review> reviews = jdbcTemplate.query(sql, this::rowReviewToMap);
        try {
            reviews.sort(Comparator.comparing(Review::getUseful).reversed());
            return reviews;
        } catch (DataAccessException e) {
            return null;
        }
    }

    @Override
    public Review create(Review review) {
        if (review.getUserId() < 0 || review.getFilmId() < 0) {
            throw new NotFoundException("Id юзера или фильма не найден.");
        }
        if (review.getUserId() == 0 || review.getFilmId() == 0) {
            throw new ValidationException("Невалидный id юзера или фильма.");
        }
        try {
            final String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) " +
                    "VALUES ( ?, ?, ?, ?, ?)";
            final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"review_id"});
                ps.setString(1, review.getContent());
                ps.setBoolean(2, review.getIsPositive());
                ps.setInt(3, review.getUserId());
                ps.setInt(4, review.getFilmId());
                ps.setInt(5, 0);
                return ps;
            }, keyHolder);
            review.setReviewId(keyHolder.getKey().intValue());
            userStorage.saveFeed(review.getUserId(), EventType.REVIEW, Operation.ADD, review.getReviewId());
            return review;
        } catch (ValidationException e) {
            throw new ValidationException("Review не прошел валидацию.");
        }
    }

    @Override
    public Review put(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? " +
                " WHERE review_id = ?";
        int count = jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(),
                review.getReviewId());
        if (count == 0) {
            throw new NotFoundException("" + review.getReviewId());
        }
        Integer userId = jdbcTemplate.query("SELECT u.user_id FROM users u " +
                        "LEFT JOIN reviews r ON u.user_id = r.user_id " +
                        "WHERE review_id = ?", (rs, rowNum) -> mapperInt(rs), review.getReviewId())
                .stream()
                .findAny()
                .orElseThrow(() -> new NotFoundException("Пользватель не найден"));
        userStorage.saveFeed(userId, EventType.REVIEW, Operation.UPDATE, review.getReviewId());
        return getReviewById(review.getReviewId());
    }

    @Override
    public void del(int reviewId) {
        Integer userId = jdbcTemplate.query("SELECT u.user_id FROM users u " +
                        "LEFT JOIN reviews r ON u.user_id = r.user_id " +
                        "WHERE review_id = ?", (rs, rowNum) -> mapperInt(rs), reviewId)
                .stream()
                .findAny()
                .orElseThrow(() -> new NotFoundException("Пользватель не найден"));
        jdbcTemplate.update("DELETE FROM reviews WHERE review_id = ?", reviewId);
        userStorage.saveFeed(userId, EventType.REVIEW, Operation.REMOVE, reviewId);
    }

    private Integer mapperInt(ResultSet rs) throws SQLException {
        Integer userId = rs.getInt("user_id");
        return userId;
    }

    @Override
    public Review getReviewById(int id) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        List<Review> reviews = jdbcTemplate.query(sql, this::rowReviewToMap, id);
        if (reviews.size() != 0) {
            log.info("Найден review с id {}", id);
            return reviews.get(0);
        } else {
            log.info("Review с id {} не найден", id);
            throw new NotFoundException(String.format("Review с id %d не найден.", id));
        }
    }

    @Override
    public List<Review> getAllReviewByFilmId(int filmId, int count) {
        String sql = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC";
        return jdbcTemplate.query(sql, (this::rowReviewToMap), filmId).stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public void addLikeDislike(int reviewId, int userId, int count) {
        String sql = "SELECT * FROM reviews_ratings WHERE user_id = ? AND review_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, userId, reviewId);
        if (rowSet.next()) {
            String sqlUpdate = "UPDATE reviews_ratings SET rate = ? WHERE user_id = ? AND review_id = ?";
            jdbcTemplate.update(sqlUpdate, count, userId, reviewId);
        } else {
            String sglRate = "INSERT INTO reviews_ratings (review_id, user_id, rate) VALUES ( ?, ?, ?)";
            jdbcTemplate.update(sglRate, reviewId, userId, count);
        }
        updateUseful(count, reviewId);

    }

    @Override
    public void delLikeDislike(int reviewId, int userId, int count) {
        String sglRate = "DELETE FROM reviews_ratings WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(sglRate, reviewId, userId);
        updateUseful(count, reviewId);
    }

    private void updateUseful(int count, int reviewId) {
        String sql = "UPDATE reviews SET useful = useful + ? WHERE review_id = ?";
        jdbcTemplate.update(sql, count, reviewId);
    }

    private Review rowReviewToMap(ResultSet rs, int rowNum) throws SQLException {
        Review review = Review.builder()
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .userId(rs.getInt("user_id"))
                .filmId(rs.getInt("film_id"))
                .build();
        review.setReviewId(rs.getInt("review_id"));
        review.setUseful(rs.getInt("useful"));
        return review;
    }
}
