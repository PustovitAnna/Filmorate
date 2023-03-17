package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRatings;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MpaRatingsDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<MpaRatings> getRatings() {
        System.out.println("in DB MPA ALL");
        final String sql = "SELECT rating_id, name_rating FROM ratings ORDER BY rating_id ASC";
        return jdbcTemplate.query(sql, mpaRatingsRowMapper);
    }

    public MpaRatings getRatingById(int ratingId) {
        System.out.println("in DB MPA ID");
        final String sql = "SELECT * FROM ratings WHERE rating_id = ?";//
        return jdbcTemplate.query(sql, mpaRatingsRowMapper, ratingId)
                .stream()
                .findAny()
                .orElseThrow(() -> new NotFoundException("temp" + ratingId));
    }

    private final RowMapper<MpaRatings> mpaRatingsRowMapper = (resultSet, rowNum) -> {
        MpaRatings mpa = new MpaRatings();
        mpa.setId(resultSet.getInt("rating_id"));
        mpa.setName(resultSet.getString("name_rating"));
        return mpa;
    };
}
