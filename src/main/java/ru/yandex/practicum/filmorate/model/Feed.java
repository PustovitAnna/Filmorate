package ru.yandex.practicum.filmorate.model;

import lombok.*;
import ru.yandex.practicum.filmorate.util.EventType;
import ru.yandex.practicum.filmorate.util.Operation;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Feed {
    private int timestamp;
    private Integer userId;
    private EventType eventType;
    private Operation operation;
    private Integer eventId;
    private Integer entityId;
}
