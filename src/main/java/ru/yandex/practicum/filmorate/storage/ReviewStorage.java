package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    List<Review> getAllReviews();

    Review create(Review review);

    Review put(Review review);

    void del(int id);

    Review getReviewById(int id);

    List<Review> getAllReviewByFilmId(int filmId, int count);

    void addLikeDislike(int reviewId, int userId, int count);

    void delLikeDislike(int reviewId, int userId, int count);


}
