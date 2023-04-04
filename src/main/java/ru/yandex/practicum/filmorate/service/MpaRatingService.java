package ru.yandex.practicum.filmorate.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRatings;
import ru.yandex.practicum.filmorate.storage.MpaRatingsDbStorage;

import java.util.List;

@Service
public class MpaRatingService {
    private final MpaRatingsDbStorage mpaRatingsDbStorage;

    @Autowired
    public MpaRatingService(MpaRatingsDbStorage mpaRatingsDbStorage) {
        this.mpaRatingsDbStorage = mpaRatingsDbStorage;
    }

    public List<MpaRatings> getRatings() {
        System.out.println("IN SERVICE ALL");
        return mpaRatingsDbStorage.getRatings();
    }

    public MpaRatings getRatingById(int ratingId) {
        System.out.println("in SERVICE ID");
        return mpaRatingsDbStorage.findById(ratingId);
    }
}
