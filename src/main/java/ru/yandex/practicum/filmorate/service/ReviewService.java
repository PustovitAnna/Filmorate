package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Slf4j
@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage) {
        this.reviewStorage = reviewStorage;
    }

    public List<Review> findAll() {
        return reviewStorage.getAllReviews();
    }

    public Review create(Review review) {
        return reviewStorage.create(review);
    }

    public Review put(Review review) {
        return reviewStorage.put(review);
    }

    public void del(int id) {
        reviewStorage.del(id);
    }

    public Review getReviewById(int id) {
        return reviewStorage.findById(id);
    }

    public List<Review> getAllReviewByFilmId(int filmId, int count) {
        return reviewStorage.getAllReviewByFilmId(filmId, count);
    }

    public void addLike(int reviewId, int userId) {
        int like = 1;
        reviewStorage.addLikeDislike(reviewId, userId, like);
        log.info("Добавлен лайк {}", reviewStorage.findById(reviewId));
    }

    public void delLike(int reviewId, int userId) {
        int dislike = -1;
        reviewStorage.delLikeDislike(reviewId, userId, dislike);
    }

    public void addDislike(int reviewId, int userId) {
        int like = -1;
        reviewStorage.addLikeDislike(reviewId, userId, like);
    }

    public void delDislike(int reviewId, int userId) {
        int dislike = 1;
        reviewStorage.delLikeDislike(reviewId, userId, dislike);
    }
}
