package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    public Collection<Film> findAll();

    public Film create(Film film);

    public Film put(Film film);

    public Film getFilmById(int id);

    public void addLike(int filmId, int userId);

    public void deleteLike(int filmId, int userId);

    public List<Film> getPopularFilms(int count);//Integer

    void deleteFilm(int filmId);

    List<Film> getFilmByDirectorByYear(int directorId, String sortBy);

    List<Film> getFilmByDirectorByLikes(int directorId, String sortBy);
}
