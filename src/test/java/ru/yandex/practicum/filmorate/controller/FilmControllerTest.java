package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    static InMemoryFilmStorage inMemoryFilmStorage= new InMemoryFilmStorage();

    @Test
    void validate() {
        final Film film = new Film();
        film.setName("Film Name");
        film.setDescription("Film Description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(90);
        inMemoryFilmStorage.validate(film);
    }
}