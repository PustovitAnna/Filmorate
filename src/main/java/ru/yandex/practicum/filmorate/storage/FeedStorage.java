package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.util.EventType;
import ru.yandex.practicum.filmorate.util.Operation;

import java.util.List;

public interface FeedStorage {

    List<Feed> getFeed(Integer id);

    void saveFeed(Integer userId, EventType ev, Operation op, Integer entityId);
}
