package com.github.czyzby.websocket.serialization.impl;

import java.io.UnsupportedEncodingException;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Serializer;

/** Abstract base for serializers that convert objects to strings. Methods consuming and producing byte arrays convert
 * their data to strings and delegate the operations to string-based methods.
 *
 * @author MJ */
public abstract class AbstractStringSerializer implements Serializer {
    @Override
    public byte[] serialize(final Object object) {
        try {
            return serializeAsString(object).getBytes("UTF-8");
        } catch (final UnsupportedEncodingException exception) {
            throw new SerializationException("Unexpected: UTF-8 format not supported.", exception);
        }
    }

    @Override
    public Object deserialize(final byte[] data) {
        try {
            return deserialize(new String(data, "UTF-8"));
        } catch (final UnsupportedEncodingException exception) {
            throw new SerializationException("Unexpected: UTF-8 format not supported.", exception);
        }
    }
}
