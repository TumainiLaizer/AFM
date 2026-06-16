package com.fameafrica.afm.data.database

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

class BooleanAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): Boolean {
        return when (reader.peek()) {
            JsonReader.Token.BOOLEAN -> reader.nextBoolean()
            JsonReader.Token.NUMBER -> reader.nextDouble() != 0.0
            JsonReader.Token.STRING -> {
                val s = reader.nextString().lowercase()
                s == "true" || s == "1" || s == "yes"
            }
            JsonReader.Token.NULL -> {
                reader.nextNull<Any?>()
                false
            }
            else -> {
                reader.skipValue()
                false
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: Boolean) {
        writer.value(value)
    }

    @FromJson
    fun fromNullableJson(reader: JsonReader): Boolean? {
        if (reader.peek() == JsonReader.Token.NULL) {
            return reader.nextNull()
        }
        return fromJson(reader)
    }

    @ToJson
    fun toNullableJson(writer: JsonWriter, value: Boolean?) {
        writer.value(value)
    }
}

class IntAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): Int {
        return when (reader.peek()) {
            JsonReader.Token.NUMBER -> reader.nextDouble().toInt()
            JsonReader.Token.STRING -> reader.nextString().toIntOrNull() ?: 0
            JsonReader.Token.BOOLEAN -> if (reader.nextBoolean()) 1 else 0
            JsonReader.Token.NULL -> {
                reader.nextNull<Any?>()
                0
            }
            else -> {
                reader.skipValue()
                0
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: Int) {
        writer.value(value)
    }

    @FromJson
    fun fromNullableJson(reader: JsonReader): Int? {
        if (reader.peek() == JsonReader.Token.NULL) {
            return reader.nextNull()
        }
        return fromJson(reader)
    }

    @ToJson
    fun toNullableJson(writer: JsonWriter, value: Int?) {
        writer.value(value)
    }
}

class LongAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): Long {
        return when (reader.peek()) {
            JsonReader.Token.NUMBER -> reader.nextDouble().toLong()
            JsonReader.Token.STRING -> reader.nextString().toLongOrNull() ?: 0L
            JsonReader.Token.BOOLEAN -> if (reader.nextBoolean()) 1L else 0L
            JsonReader.Token.NULL -> {
                reader.nextNull<Any?>()
                0L
            }
            else -> {
                reader.skipValue()
                0L
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: Long) {
        writer.value(value)
    }

    @FromJson
    fun fromNullableJson(reader: JsonReader): Long? {
        if (reader.peek() == JsonReader.Token.NULL) {
            return reader.nextNull()
        }
        return fromJson(reader)
    }

    @ToJson
    fun toNullableJson(writer: JsonWriter, value: Long?) {
        writer.value(value)
    }
}
