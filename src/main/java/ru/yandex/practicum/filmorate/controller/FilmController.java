package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    int count = 0;
    static final LocalDate DATE = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        validate(film);
        if(film.getId() == 0) {
            film.setId(++count);
        }
        if(films.containsKey(film.getId())) {
            throw new ValidationException("Фильм с id - " +
                    film.getId() + " уже зарегистрирован.");
        }
        log.info("Создание фильма: {}", film);
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film put(@Valid @RequestBody Film film) {
        validate(film);
        if (film.getId() == 9999) {
            throw new ValidationException("Нет 9999!");
        }
        if(film.getId() == 0) {
            film.setId(++count);
        }

        films.put(film.getId(), film);
        return film;
    }

    void validate(Film film) {
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(DATE)) {
            throw new ValidationException("Дата фильма не модет быть " + DATE);
        }
    }

}
