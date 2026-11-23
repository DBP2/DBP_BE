package controller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class JsonUtils {
    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private static final ObjectMapper mapper = createMapper();

    private static ObjectMapper createMapper() {
        SimpleModule dateModule = new SimpleModule();
        dateModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(dateModule);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }

    public static String toJson(Object value) throws IOException {
        return mapper.writeValueAsString(value);
    }

    public static <T> T readBody(HttpServletRequest request, Class<T> type) throws IOException {
        return mapper.readValue(request.getReader(), type);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZONE)
                .toOffsetDateTime()
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public static String toId(Long id) {
        return id == null ? null : String.valueOf(id);
    }

    public static Long parseId(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
        @Override
        public void serialize(
                LocalDateTime value, JsonGenerator generator, SerializerProvider provider)
                throws IOException {
            generator.writeString(formatDateTime(value));
        }
    }
}
