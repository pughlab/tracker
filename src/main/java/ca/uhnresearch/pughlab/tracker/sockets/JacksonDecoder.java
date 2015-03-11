package ca.uhnresearch.pughlab.tracker.sockets;

import org.atmosphere.config.managed.Decoder;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Decode a String into a {@link UpdateEvent}.
 */
public class JacksonDecoder implements Decoder<String, UpdateEvent> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public UpdateEvent decode(String s) {
        try {
            return mapper.readValue(s, UpdateEvent.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
