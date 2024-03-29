package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.ErrorResponse;
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
                "FROM directors d " +
                "LEFT JOIN directors_films df ON d.director_id = df.director_id ";
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
        return findById(film.getId());
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
        return findById(film.getId());
    }

    @Override
    public Film findById(int id) {
        Set<Director> filmDirectors = new HashSet<>();
        final String sql = "SELECT f.*, mpa.name_rating\n" +
                "FROM films AS f, ratings AS mpa\n" +
                "WHERE f.rating_id = mpa.rating_id\n" +
                "AND f.film_id = ?";
        Film film = jdbcTemplate.query(sql, filmMapper, id)
                .stream()
                .findAny()
                .orElseThrow(() -> new NotFoundException("temp" + id));
        jdbcTemplate.query("SELECT *,df.film_id " +
                        "FROM directors d " +
                        "LEFT JOIN directors_films df ON d.director_id = df.director_id " +
                        "WHERE film_id=?",
                ((rs, rowNum) -> assignDirector(rs, film, filmDirectors)), id);
        return film;
    }

    @Override
    public void addLike(int filmId, int userId) {
        Integer likesCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM popular_films " +
                "WHERE film_id = ? AND user_id = ?", Integer.class, filmId, userId);
        if (likesCount == 1) {
            return;
        }
        final String sql = "INSERT INTO popular_films (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        final String sqlAddLike = "UPDATE films SET rate = rate + 1 WHERE film_id=?";
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
    public List<Film> getPopularFilms(int count) {
        Map<Integer, Set<Director>> filmsDirectors = new HashMap<>();
        String sql = "SELECT f.*, mpa.name_rating\n" +
                "FROM films AS f, ratings AS mpa\n" +
                "WHERE f.rating_id = mpa.rating_id\n" +
                "ORDER BY f.rate DESC, film_id ASC\n" +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, filmMapper, count);
        String sql1 = "SELECT *,df.film_id " +
                "FROM directors d " +
                "LEFT JOIN directors_films df ON d.director_id = df.director_id " +
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
        String queryLowerCase = "%" + query.toLowerCase(Locale.ENGLISH) + "%";
        if (by.contains("title") && by.contains("director")) {
            Map<Integer, Set<Director>> filmsDirectors = new HashMap<>();
            String sql = "SELECT *, r.name_rating FROM films f " +
                    "LEFT JOIN ratings r ON f.rating_id = r.rating_id " +
                    "LEFT JOIN directors_films df ON f.film_id = df.film_id " +
                    "LEFT JOIN directors d ON d.director_id = df.director_id " +
                    "WHERE LOWER(d.name_director) LIKE ? OR LOWER(name) LIKE ? " +
                    "ORDER BY f.rate DESC";
            List<Film> films = jdbcTemplate.query(sql, filmMapper, queryLowerCase, queryLowerCase);
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
                    "WHERE LOWER(name) LIKE ? " +
                    "ORDER BY f.rate DESC";
            List<Film> films = jdbcTemplate.query(sql, filmMapper, queryLowerCase);
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
                "WHERE LOWER(d.name_director) LIKE ? " +
                "ORDER BY f.rate DESC";
        List<Film> films = jdbcTemplate.query(sql, filmMapper, queryLowerCase);
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

    private List<String> mapperNameDirector(ResultSet rs, List<String> names) {
        try {
            names.add(rs.getString("name_director"));
        } catch (SQLException e) {
            throw new ErrorResponse("Произошла непредвиденная ошибка.");
        }
        return names;
    }

    @Override
    public List<Film> getFilmByDirectorByYear(int directorId, String sortBy) {
        Director director = directorStorage.findById(directorId);

        List<Film> films = jdbcTemplate.query("SELECT *, mpa.name_rating " +
                        "FROM films f " +
                        "LEFT JOIN ratings mpa ON f.rating_id = mpa.rating_id " +
                        "LEFT JOIN directors_films df ON f.film_id = df.film_id " +
                        "WHERE df.director_id = ? " +
                        "ORDER BY f.release_date",
                filmMapper, directorId);
        if (films.isEmpty()) {
            return films;
        }
        Set<Director> directors = new HashSet<>();
        directors.add(director);
        films.forEach(film -> film.setDirectors(directors));
        return films;
    }

    @Override
    public List<Film> getFilmByDirectorByLikes(int directorId, String sortBy) {
        Director director = directorStorage.findById(directorId);

        List<Film> films = jdbcTemplate.query("SELECT *, mpa.name_rating " +
                        "FROM films f " +
                        "LEFT JOIN ratings mpa ON f.rating_id = mpa.rating_id " +
                        "LEFT JOIN directors_films df ON f.film_id = df.film_id " +
                        "WHERE df.director_id = ? " +
                        "ORDER BY f.rate",
                filmMapper, directorId);
        if (films.isEmpty()) {
            return films;
        }
        Set<Director> directors = new HashSet<>();
        directors.add(director);
        films.forEach(film -> film.setDirectors(directors));
        return films;
    }

    @Override
    public List<Film> assignDirectors(ResultSet rs, List<Film> films, Map<Integer, Set<Director>> filmsDirectors) {
        final int filmId;
        try {
            filmId = rs.getInt("film_id");
        } catch (SQLException e) {
            throw new ErrorResponse("Произошла непредвиденная ошибка.");
        }
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

    private Director directorMapper(ResultSet rs) {
        Director director = new Director();
        try {
            director.setId(rs.getInt("director_id"));
            director.setName(rs.getString("name_director"));
        } catch (SQLException e) {
            throw new ErrorResponse("Произошла непредвиденная ошибка.");
        }
        return director;
    }

    private void saveDirector(Film film) {
        jdbcTemplate.update("DELETE directors_films WHERE film_id=?", film.getId());
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            return;
        }
        final Set<Director> directors = new HashSet<>(film.getDirectors());
        final ArrayList<Director> directors1 = new ArrayList<>(directors);
        jdbcTemplate.batchUpdate("INSERT INTO directors_films(director_id,film_id) VALUES (?,?)",
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

    @Override
    public List<Film> getRecommendation(int id) {
        String filmsId = "SELECT film_id FROM popular_films " +
                "WHERE user_id = ?";
        List<Integer> usersFilms = new ArrayList<>();
        jdbcTemplate.query(filmsId, (rs, rowNum) -> mapFilmsId(rs, usersFilms), id);
        String recommendUserId = "SELECT user_id FROM popular_films " +
                "GROUP BY user_id,film_id " +
                "HAVING film_id IN (" + usersFilms.stream().map(String::valueOf).collect(Collectors.joining(",")) + ") AND user_id != ? " +
                "ORDER BY COUNT(film_id) desc " +
                "LIMIT 1";
        List<Integer> usersId = new ArrayList<>();
        jdbcTemplate.query(recommendUserId, (rs, rowNum) -> mapUsersId(rs, usersId), id);

        String recommendations = "SELECT f.*, mpa.name_rating " +
                "FROM films AS f " +
                "LEFT JOIN ratings AS mpa ON f.rating_id = mpa.rating_id " +
                "LEFT JOIN popular_films AS likes ON f.film_id = likes.film_id " +
                "WHERE likes.user_id IN (" + usersId.stream().map(String::valueOf).collect(Collectors.joining(",")) + ") " +
                "AND f.film_id NOT IN (" + usersFilms.stream().map(String::valueOf).collect(Collectors.joining(",")) + ") ";

        Map<Integer, Set<Director>> filmsDirectors = new HashMap<>();
        List<Film> films = jdbcTemplate.query(recommendations, filmMapper);
        if (films.isEmpty()) {
            return films;
        }
        String sql3 = "SELECT *,df.film_id " +
                "FROM DIRECTORS d " +
                "LEFT JOIN DIRECTORS_FILMS df ON d.director_id = df.director_id ";
        jdbcTemplate.query(sql3, (rs, rowNum) -> assignDirectors(rs, films, filmsDirectors));
        return films;
    }

    private List<Integer> mapFilmsId(ResultSet resultSet, List<Integer> usersFilms) throws SQLException {
        usersFilms.add(resultSet.getInt("film_id"));
        return usersFilms;
    }

    private List<Integer> mapUsersId(ResultSet resultSet, List<Integer> usersId) throws SQLException {
        usersId.add(resultSet.getInt("user_id"));
        return usersId;
    }
}
