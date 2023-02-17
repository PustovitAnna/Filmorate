package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
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
        return filmStorage.getFilmById(filmId);
    }

    public void addLike(int filmId, int userId) {
        validateId(filmId, userId);
        final Film film = filmStorage.getFilmById(filmId);
        if (filmStorage.getFilmById(filmId).getUsersLike().contains(userId)) {
            throw new ValidationException("Этот пользователь уже ставил оценку!");
        }
        film.addLike(userId);
    }

    public void deleteLike(int filmId, int userId) {
        validateId(filmId, userId);
        final Film film = filmStorage.getFilmById(filmId);
        film.deleteLike(userId);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt(Film::getRate))
                .limit(count)
                .collect(Collectors.toList());
    }

    void validateId(int filmId, int userId){
        if(filmId <=0 || userId <= 0)
            throw new NotFoundException("Некорректный id фильма или пользователя!");
    }
    // public static  final Comparator<Film> FILM_COMPARATOR = Comparator.comparingInt(Film::getRate);
}
