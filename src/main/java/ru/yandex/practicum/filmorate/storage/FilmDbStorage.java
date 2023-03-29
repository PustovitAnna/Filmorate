package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import lombok.RequiredArgsConstructor;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmMapper filmMapper;
    private final DirectorStorage directorStorage;

    @Override
    public List<Film> findAll() {
        Map<Integer, Set<Director>> filmsDirectors = new HashMap<>();
        final String sql = "SELECT f.*, mpa.name_rating\n" +
                "FROM films AS f, ratings AS mpa\n" +
                "WHERE f.rating_id = mpa.rating_id\n" +
                "ORDER BY f.film_id ASC";
        List<Film> films = jdbcTemplate.query(sql, filmMapper);
        if (films.isEmpty()) {
            return films;
        }
        String sql1 = "SELECT *,df.film_id " +
                "FROM DIRECTORS d " +
                "LEFT JOIN DIRECTORS_FILMS df ON d.director_id = df.director_id ";
        jdbcTemplate.query(sql1, (rs, rowNum) -> assignDirectors(rs, films, filmsDirectors));
        return films;
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
        saveDirector(film);
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
        saveDirector(film);
        return getFilmById(film.getId());
    }

    @Override
    public Film getFilmById(int id) {
        Set<Director> filmDirectors = new HashSet<>();
        final String sql = "SELECT f.*, mpa.name_rating\n" +
                "FROM films AS f, ratings AS mpa\n" +
                "WHERE f.rating_id = mpa.rating_id\n" +
                "AND f.film_id = ?";// "SELECT * FROM films WHERE film_id = ?";
        //return jdbcTemplate.query(sql, filmRowMapper)
        Film film = jdbcTemplate.query(sql, filmMapper, id)
                .stream()
                .findAny()
                .orElseThrow(() -> new NotFoundException("temp" + id));
        jdbcTemplate.query("SELECT *,df.film_id " +
                        "FROM DIRECTORS d " +
                        "LEFT JOIN DIRECTORS_FILMS df ON d.director_id = df.director_id " +
                        "WHERE film_id=?",
                ((rs, rowNum) -> assignDirector(rs, film, filmDirectors)), id);
        return film;
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
        Map<Integer, Set<Director>> filmsDirectors = new HashMap<>();
        String sql = "SELECT f.*, mpa.name_rating\n" +
                "FROM films AS f, ratings AS mpa\n" +
                "WHERE f.rating_id = mpa.rating_id\n" +
                "ORDER BY f.rate DESC, film_id ASC\n" +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, filmMapper, count);
        String sql1 = "SELECT *,df.film_id " +
                "FROM DIRECTORS d " +
                "LEFT JOIN DIRECTORS_FILMS df ON d.director_id = df.director_id " +
                "WHERE df.film_id " +
                "IN (" + films.stream()
                .map(film -> String.valueOf(film.getId())).collect(Collectors.joining(",")) + ")";
        jdbcTemplate.query(sql1, (rs, rowNum) -> assignDirectors(rs, films, filmsDirectors));
        return films;
    }

    @Override
    public void deleteFilm(int filmId) {
        jdbcTemplate.update("DELETE FROM films WHERE film_id = ?", filmId);
    }

    @Override
    public List<Film> searchFilms(String query, String by) {
        String lowCase = "%" + query.toLowerCase(Locale.ENGLISH) + "%";
        if (by.contains("title") && by.contains("director")) {
            Map<Integer, Set<Director>> filmsDirectors = new HashMap<>();
            String sql = "SELECT *, r.name_rating FROM films f " +
                    "LEFT JOIN ratings r ON f.rating_id = r.rating_id " +
                    "LEFT JOIN directors_films df ON f.film_id = df.film_id " +
                    "LEFT JOIN directors d ON d.director_id = df.director_id " +
                    "WHERE LOWER(d.name_director) like ? OR LOWER(name) like ? " +
                    "ORDER BY f.rate DESC";
            List<Film> films = jdbcTemplate.query(sql, filmMapper, lowCase, lowCase);
            String sql2 = "SELECT *,df.film_id " +
                    "FROM directors d " +
                    "LEFT JOIN directors_films df ON d.director_id = df.director_id " +
                    "WHERE df.film_id IN (" + films.stream()
                    .map(film -> String.valueOf(film.getId())).collect(Collectors.joining(",")) + ")";
            jdbcTemplate.query(sql2, (rs, rowNum) -> assignDirectors(rs, films, filmsDirectors));
            return films;
        }
        if (by.equals("title")) {
            Map<Integer, Set<Director>> filmsDirectors = new HashMap<>();
            String sql = "SELECT *, r.name_rating FROM films f " +
                    "LEFT JOIN ratings r ON f.rating_id = r.rating_id " +
                    "WHERE LOWER(name) like ? " +
                    "ORDER BY f.rate DESC";
            List<Film> films = jdbcTemplate.query(sql, filmMapper, lowCase);
            String sql2 = "SELECT *,df.film_id " +
                    "FROM directors d " +
                    "LEFT JOIN directors_films df ON d.director_id = df.director_id " +
                    "WHERE df.film_id IN (" + films.stream()
                    .map(film -> String.valueOf(film.getId())).collect(Collectors.joining(",")) + ")";
            jdbcTemplate.query(sql2, (rs, rowNum) -> assignDirectors(rs, films, filmsDirectors));
            return films;
        }
        Map<Integer, Set<Director>> filmsDirectors = new HashMap<>();
        String sql = "SELECT *, r.name_rating FROM films f " +
                "LEFT JOIN ratings r ON f.rating_id = r.rating_id " +
                "LEFT JOIN directors_films df ON f.film_id = df.film_id " +
                "LEFT JOIN directors d ON d.director_id = df.director_id " +
                "WHERE LOWER(d.name_director) like ? " +
                "ORDER BY f.rate DESC";
        List<Film> films = jdbcTemplate.query(sql, filmMapper, lowCase);
        String sql2 = "SELECT *,df.film_id " +
                "FROM directors d " +
                "LEFT JOIN directors_films df ON d.director_id = df.director_id " +
                "WHERE df.film_id IN (" + films.stream()
                .map(film -> String.valueOf(film.getId())).collect(Collectors.joining(",")) + ")";
        jdbcTemplate.query(sql2, (rs, rowNum) -> assignDirectors(rs, films, filmsDirectors));
        return films;
    }

    private List<String> mapperName(ResultSet rs, List<String> names) throws SQLException {
        names.add(rs.getString("name"));
        return names;
    }

    private List<String> mapperNameDirector(ResultSet rs, List<String> names) throws SQLException {
        names.add(rs.getString("name_director"));
        return names;
    }

    @Override
    public List<Film> getFilmByDirectorByYear(int directorId, String sortBy) {
        Director director = directorStorage.getDirectorById(directorId);
        if (director == null) {
            throw new NotFoundException("Режиссера с идентификатором: " + directorId + " не существует.");
        }
        List<Film> films = jdbcTemplate.query("SELECT *, mpa.name_rating " +
                        "FROM films f " +
                        "LEFT JOIN ratings mpa ON f.rating_id = mpa.rating_id " +
                        "LEFT JOIN DIRECTORS_FILMS df ON f.film_id = df.film_id " +
                        "WHERE df.director_id = ? " +
                        "ORDER BY f.release_date",
                filmMapper, directorId);
        if (films.isEmpty()) {
            return films;
        }
        Set<Director> directors = new HashSet<>();
        directors.add(director);
        for (Film film1 : films) {
            film1.setDirectors(directors);
        }
        return films;
    }

    @Override
    public List<Film> getFilmByDirectorByLikes(int directorId, String sortBy) {
        Director director = directorStorage.getDirectorById(directorId);
        if (director == null) {
            throw new NotFoundException("Режиссера с идентификатором: " + directorId + " не существует.");
        }
        List<Film> films = jdbcTemplate.query("SELECT *, mpa.name_rating " +
                        "FROM films f " +
                        "LEFT JOIN ratings mpa ON f.rating_id = mpa.rating_id " +
                        "LEFT JOIN DIRECTORS_FILMS df ON f.film_id = df.film_id " +
                        "WHERE df.director_id = ? " +
                        "ORDER BY f.rate",
                filmMapper, directorId);
        if (films.isEmpty()) {
            return films;
        }
        Set<Director> directors = new HashSet<>();
        directors.add(director);
        for (Film film1 : films) {
            film1.setDirectors(directors);
        }
        return films;
    }

    private List<Film> assignDirectors(ResultSet rs, List<Film> films, Map<Integer, Set<Director>> filmsDirectors) throws SQLException {
        final int filmId = rs.getInt("film_id");
        Set<Director> setDirectors = filmsDirectors.getOrDefault(filmId, new HashSet<>());
        setDirectors.add(directorMapper(rs));
        filmsDirectors.put(filmId, setDirectors);
        films.forEach(film -> film.setDirectors(filmsDirectors.getOrDefault(film.getId(), new HashSet<>())));
        return films;
    }

    private Film assignDirector(ResultSet rs, Film film, Set<Director> filmDirectors) throws SQLException {
        filmDirectors.add(directorMapper(rs));
        film.setDirectors(filmDirectors);
        return film;
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

    private Director directorMapper(ResultSet rs) throws SQLException {
        Director director = new Director();
        director.setId(rs.getInt("director_id"));
        director.setName(rs.getString("name_director"));
        return director;
    }

    private void saveDirector(Film film) {
        jdbcTemplate.update("DELETE DIRECTORS_FILMS WHERE film_id=?", film.getId());
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            return;
        }
        final Set<Director> directors = new HashSet<>(film.getDirectors());
        final ArrayList<Director> directors1 = new ArrayList<>(directors);
        jdbcTemplate.batchUpdate("INSERT INTO DIRECTORS_FILMS(director_id,film_id) VALUES (?,?)",
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, directors1.get(i).getId());
                        ps.setLong(2, film.getId());
                    }

                    public int getBatchSize() {
                        return directors1.size();
                    }
                });
    }
}
