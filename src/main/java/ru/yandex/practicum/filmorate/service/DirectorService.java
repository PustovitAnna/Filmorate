package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director getDirectorById(int directorId) {
        return directorStorage.findById(directorId);
    }

    public Director update(Director director) {
        return directorStorage.update(director);
    }

    public void delete(int directorId) {
        directorStorage.delete(directorId);
    }

    public List<Director> getDirectors() {
        return directorStorage.getDirectors();
    }
}
