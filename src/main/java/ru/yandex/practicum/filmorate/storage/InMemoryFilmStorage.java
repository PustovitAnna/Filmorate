package ru.yandex.practicum.filmorate.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage{
    private final Map<Integer, Film> films = new HashMap<>();
    int count = 0;
    static final LocalDate DATE = LocalDate.of(1895, 12, 28);

    public Collection<Film> findAll() {
        return films.values();
    }

    public Film create(Film film) {
        validate(film);
        validateCreate(film);
        film.setId(++count);
        log.info("Создание фильма: {}", film);
        films.put(film.getId(), film);
        return film;
    }

    public Film put(Film film) {
        validateUpdate(film.getId());
        validate(film);
        films.put(film.getId(), film);
        return film;
    }

    public Film getFilmById(int filmId) {
        validateUpdate(filmId);
        Film film = films.get(filmId);
        return film;

    }

    void validateCreate(Film film) {
        if(films.containsKey(film.getId())) {
            throw new ValidationException("Фильм с id - " +
                    film.getId() + " уже зарегистрирован.");
        }
    }

    void validateUpdate(int id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Такого фильма не существует!");
        }
    }

    public void validate(Film film) {
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(DATE)) {
            throw new ValidationException("Дата фильма не модет быть " + DATE);
        }
    }
}
