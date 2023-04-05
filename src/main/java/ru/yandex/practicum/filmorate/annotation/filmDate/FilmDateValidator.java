package ru.yandex.practicum.filmorate.annotation.filmDate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class FilmDateValidator implements ConstraintValidator<ValidDate, LocalDate> {
    private final LocalDate bd = LocalDate.of(1895, 12, 28);

    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        return localDate.isAfter(bd);
    }
}
