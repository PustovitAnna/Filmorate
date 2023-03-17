package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MpaRatings;
import ru.yandex.practicum.filmorate.service.MpaRatingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@Slf4j
public class MpaRatingsController {
    MpaRatingService mpaRatingService;
    @Autowired
    public MpaRatingsController(MpaRatingService mpaRatingService){
        this.mpaRatingService = mpaRatingService;
    }
    @GetMapping("/mpa")
    public List<MpaRatings> getAllMpa() {
        log.info("Получение списка всех рейтингов");
        return mpaRatingService.getRatings();
    }

    @GetMapping("/mpa/{id}")
    public MpaRatings getMpaById(@PathVariable int id) {
        log.info("Получение рейтинга по id", id);
        return mpaRatingService.getRatingById(id);
    }
}
