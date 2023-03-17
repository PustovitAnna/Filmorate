package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<Genre> getGenres() {
        final String sql = "SELECT * FROM genre ORDER BY genre_id ASC";
        return jdbcTemplate.query(sql, genreRowMapper);
    }

    public Genre getGenreById(int genreId) {
        final String sql = "SELECT * FROM genre WHERE genre_id = ?";
        return jdbcTemplate.query(sql, genreRowMapper, genreId)
                .stream()
                .findAny()
                .orElseThrow(() -> new NotFoundException("temp" + genreId));

    }

    public Set<Genre> getGenreByIdFilm(int id) {
        String sql = "SELECT * FROM genre WHERE genre_id IN (SELECT genre_id FROM film_genre WHERE film_id=?) " +
                "ORDER BY genre_id ASC";
        return new HashSet<>(jdbcTemplate.query(sql, genreRowMapper, id));
    }

    private final RowMapper<Genre> genreRowMapper = (resultSet, rowNum) -> {
        Genre genre = new Genre();
        genre.setId(resultSet.getInt("genre_id"));//
        genre.setName(resultSet.getString("name_genre"));
        return genre;
    };
}
