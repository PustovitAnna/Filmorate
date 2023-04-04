package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/directors")
public class DirectorController {

    private final DirectorService directorService;

    @PostMapping
    public Director create(@Valid @RequestBody Director director) {
        Director directorRequest = directorService.create(director);
        log.debug("Добавлен режиссер: {} ", directorRequest);
        return directorRequest;
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@Valid @PathVariable int id) {
        Director director = directorService.getDirectorById(id);
        log.debug("Получен режиссер: {} ", director);
        return director;
    }

    @PutMapping
    public Director update(@RequestBody Director director) {
        Director directorRequest = directorService.update(director);
        log.debug("Обновлен режиссер: {} ", directorRequest);
        return directorRequest;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        directorService.delete(id);
        log.debug("Режиссер с идентификатором: " + id + " удален.");
    }

    @GetMapping
    public List<Director> getDirectors() {
        List<Director> directors = directorService.getDirectors();
        log.debug("Получен список режиссеров: {} ", directors);
        return directors;
    }
}
