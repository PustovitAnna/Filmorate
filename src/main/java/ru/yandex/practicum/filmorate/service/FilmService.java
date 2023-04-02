package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.util.EventType;
import ru.yandex.practicum.filmorate.util.Operation;

import java.time.LocalDate;
import java.util.*;

@Service
public class FilmService {
    static final LocalDate DATE = LocalDate.of(1895, 12, 28);
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        validate(film);
        return filmStorage.create(film);
    }

    public Film put(Film film) {
        validate(film);
        return filmStorage.put(film);
    }

    public Film getFilmById(int filmId) {
        return filmStorage.getFilmById(filmId);
    }

    public void addLike(int filmId, int userId) {
        validateId(filmId, userId);
        filmStorage.addLike(filmId, userId);
        userStorage.saveFeed(userId, EventType.LIKE, Operation.ADD, filmId);
    }

    public void deleteLike(int filmId, int userId) {
        validateId(filmId, userId);
        filmStorage.deleteLike(filmId, userId);
        userStorage.saveFeed(userId, EventType.LIKE, Operation.REMOVE, filmId);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getPopularFilms(count);
    }

    void validateId(int filmId, int userId) {
        if (filmId <= 0 || userId <= 0)
            throw new NotFoundException("Некорректный id фильма или пользователя!");
    }

    public void validate(Film film) {
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(DATE)) {
            throw new ValidationException("Дата фильма не модет быть " + DATE);
        }
    }

    public void deleteFilm(int filmId) {
        filmStorage.deleteFilm(filmId);
    }

    public List<Film> searchFilms(String query, String by) {
        return filmStorage.searchFilms(query, by);
    }

    public List<Film> getFilmByDirector(int directorId, String sortBy) {
        if (sortBy.equals("year")) {
            return filmStorage.getFilmByDirectorByYear(directorId, sortBy);
        }
        return filmStorage.getFilmByDirectorByLikes(directorId, sortBy);
    }
}
