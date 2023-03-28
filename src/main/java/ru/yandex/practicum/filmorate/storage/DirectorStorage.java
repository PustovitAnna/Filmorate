package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorStorage {
    Director create(Director director);

    Director getDirectorById(int directorId);

    Director update(Director director);

    void delete(int directorId);

    List<Director> getDirectors();
}
