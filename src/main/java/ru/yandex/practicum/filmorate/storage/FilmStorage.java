package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FilmStorage {

    Collection<Film> findAll();

    Film create(Film film);

    Film put(Film film);

    Film findById(int id);

    void addLike(int filmId, int userId);

    void deleteLike(int filmId, int userId);

    List<Film> getPopularFilms(int count);

    void deleteFilm(int filmId);

    List<Film> searchFilms(String query, String by);

    List<Film> getFilmByDirectorByYear(int directorId, String sortBy);

    List<Film> getFilmByDirectorByLikes(int directorId, String sortBy);

    List<Film> assignDirectors(ResultSet rs, List<Film> films, Map<Integer, Set<Director>> filmsDirectors);

    List<Film> getRecommendation(int id);
}
