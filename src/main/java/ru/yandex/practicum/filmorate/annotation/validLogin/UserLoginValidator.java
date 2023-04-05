package ru.yandex.practicum.filmorate.annotation.validLogin;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UserLoginValidator implements ConstraintValidator<ValidLogin, String> {

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return (!(s.isBlank() || s.contains(" ")));
    }
}
