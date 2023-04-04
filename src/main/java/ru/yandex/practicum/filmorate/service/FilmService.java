package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.util.EventType;
import ru.yandex.practicum.filmorate.util.Operation;

import java.util.*;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final FeedStorage feedStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, FeedStorage feedStorage) {
        this.filmStorage = filmStorage;
        this.feedStorage = feedStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film put(Film film) {
        return filmStorage.put(film);
    }

    public Film getFilmById(int filmId) {
        return filmStorage.findById(filmId);
    }

    public void addLike(int filmId, int userId) {
        validateId(filmId, userId);
        filmStorage.addLike(filmId, userId);
        feedStorage.saveFeed(userId, EventType.LIKE, Operation.ADD, filmId);
    }

    public void deleteLike(int filmId, int userId) {
        validateId(filmId, userId);
        filmStorage.deleteLike(filmId, userId);
        feedStorage.saveFeed(userId, EventType.LIKE, Operation.REMOVE, filmId);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getPopularFilms(count);
    }

    void validateId(int filmId, int userId) {
        if (filmId <= 0 || userId <= 0)
            throw new NotFoundException("Некорректный id фильма или пользователя!");
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
