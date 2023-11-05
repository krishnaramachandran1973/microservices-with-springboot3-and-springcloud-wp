package com.itskool.event;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import lombok.*;

import java.time.ZonedDateTime;

import static java.time.ZonedDateTime.now;

@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Getter
public class Event<K,T> {
    public enum Type {
        CREATE,
        DELETE
    }

    private final Type eventType;
    private final K key;
    private final T data;

    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    private ZonedDateTime eventCreatedAt = now();
}
