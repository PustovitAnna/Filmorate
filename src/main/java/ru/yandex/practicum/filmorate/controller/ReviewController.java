package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("{id}")
    public Review getById(@Valid @PathVariable("id") int reviewId) {
        return reviewService.getReviewById(reviewId);
    }

    @GetMapping
    public List<Review> getAllReviewByFilmId(
            @RequestParam(value = "filmId", defaultValue = "-1", required = false) Integer filmId,
            @RequestParam(value = "count", defaultValue = "10", required = false) Integer count) {
        if (filmId == -1) {
            return reviewService.findAll();
        } else {
            return reviewService.getAllReviewByFilmId(filmId, count);
        }
    }

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        log.info("Добавлен отзыв {}.", review);
        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review review) {
        log.info("Отзыв {} обновлен", review);
        return reviewService.put(review);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@Valid @PathVariable("id") int id, @Valid @PathVariable("userId") int userId) {
        reviewService.addLike(id, userId);
        log.info("Добавление лайка для отзыва {}, user id {}", id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@Valid @PathVariable("id") int id, @Valid @PathVariable("userId") int userId) {
        log.info("Добавление дислайка для отзыва {}, user id {}", id, userId);
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}")
    public void delete(@Valid @PathVariable("id") int id) {
        log.info("Удаление отзыва {}", id);
        reviewService.delete(id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@Valid @PathVariable("id") int id, @Valid @PathVariable("userId") int userId) {
        log.info("Удаление лайка для отзыва {}", id);
        reviewService.deleteLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@Valid @PathVariable("id") int id, @Valid @PathVariable("userId") int userId) {
        log.info("Удаление дислайка для отзыва {}", id);
        reviewService.deleteDislike(id, userId);
    }
}
