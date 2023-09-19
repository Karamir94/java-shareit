package ru.practicum.shareit.service;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StartBeforeEndValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface StartBeforeEnd {

    String message() default "{дата и время возврата должна быть позже даты начала аренды}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
