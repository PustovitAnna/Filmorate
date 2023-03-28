package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import lombok.RequiredArgsConstructor;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmMapper filmMapper;

    @Override
    public List<Film> findAll() {
        final String sql = "SELECT f.*, mpa.name_rating\n" +
                "FROM films AS f, ratings AS mpa\n" +
                "WHERE f.rating_id = mpa.rating_id\n" +
                "ORDER BY f.film_id ASC";
        return jdbcTemplate.query(sql, filmMapper);
    }

    @Override
    public Film create(Film film) {
        final String sql = "INSERT INTO films (name, description, release_date, duration, rating_id, rate)" +
                " VALUES (?, ?, ?, ?, ?, ?)";
        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement psst = connection.prepareStatement(sql, new String[]{"film_id"});
            psst.setString(1, film.getName());
            psst.setString(2, film.getDescription());
            psst.setDate(3, Date.valueOf(film.getReleaseDate()));
            psst.setInt(4, film.getDuration());
            psst.setInt(5, film.getMpa().getId());
            psst.setInt(6, film.getRate());
            return psst;
        }, keyHolder);
        film.setId(keyHolder.getKey().intValue());
        updateGenreByFilm(film);
        return getFilmById(film.getId());
    }

    @Override
    public Film put(Film film) {
        final String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?," +
                "rating_id = ? WHERE film_id = ?";
        final int count = jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());
        updateGenreByFilm(film);
        if (count == 0) {
            throw new NotFoundException("" + film.getId());
        }
        updateGenreByFilm(film);
        return getFilmById(film.getId());
    }

    @Override
    public Film getFilmById(int id) {
        final String sql = "SELECT f.*, mpa.name_rating\n" +
                "FROM films AS f, ratings AS mpa\n" +
                "WHERE f.rating_id = mpa.rating_id\n" +
                "AND f.film_id = ?";// "SELECT * FROM films WHERE film_id = ?";
        //return jdbcTemplate.query(sql, filmRowMapper)
        return jdbcTemplate.query(sql, filmMapper, id)
                .stream()
                .findAny()
                .orElseThrow(() -> new NotFoundException("temp" + id));
    }

    @Override
    public void addLike(int filmId, int userId) {
        final String sql = "INSERT INTO popular_films (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        final String sqlAddLike = "UPDATE films SET rate = rate + 1 WHERE film_id=?";//here was count
        jdbcTemplate.update(sqlAddLike, filmId);
    }

    @Override
    public void deleteLike(int filmId, int userId) {
        final String sql = "DELETE FROM popular_films WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
        final String sqlDeleteLike = "UPDATE films SET rate = rate - 1 WHERE film_id=?";
        jdbcTemplate.update(sqlDeleteLike, filmId);
    }

    @Override
    public List<Film> getPopularFilms(int count) { //Integer
        String sql = "SELECT f.*, mpa.name_rating\n" +
                "FROM films AS f, ratings AS mpa\n" +
                "WHERE f.rating_id = mpa.rating_id\n" +
                "ORDER BY f.rate DESC, film_id ASC\n" +
                "LIMIT ?";
        return jdbcTemplate.query(sql, filmMapper, count);
    }

    @Override
    public void deleteFilm(int filmId) {
        jdbcTemplate.update("DELETE FROM films WHERE film_id = ?", filmId);
    }

    @Override
    public List<Film> searchFilms(String query, String by) {
        List<Film> listFilms = new ArrayList<>();
        String sql;
        if (by.contains("title") && by.contains("director")){
            sql = "SELECT f.name, f.description, f.release_date, f.duration, D.DIRECTOR_NAME, r.name_rating, f.rate" + //   ///   //////    ////////// режиссёр
                  "FROM films f " +
                  "LEFT JOIN ratings r ON f.rating_id = r.rating_id " +
                  "LEFT JOIN DIRECTOR D ON F.DIRECTOR_ID = D.DIRECTOR_ID";
            listFilms = jdbcTemplate.query(sql,(rs, rowNum) -> filmMapper.mapRow(rs,rowNum));
        }
        if (by.equals("title")){

        }
        if (by.equals("director")){

        }
        return listFilms;
    }

    private void updateGenreByFilm(Film data) {
        final long filmId = data.getId();
        final String sql = "DELETE FROM film_genre WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
        final Set<Genre> genres = data.getGenres();
        if (genres == null || genres.isEmpty()) {
            return;
        }
        List<Genre> genreList = new ArrayList<>(genres);
        jdbcTemplate.batchUpdate("INSERT INTO film_genre (film_id, genre_id) VALUES (?,?)",
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, data.getId());
                        ps.setInt(2, genreList.get(i).getId());
                    }

                    public int getBatchSize() {
                        return genreList.size();
                    }
                });
    }
}
