package ca.uhnresearch.pughlab.tracker.sockets;

import org.atmosphere.config.managed.Encoder;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Encode a {@link UpdateEvent} into a String
 */
public class JacksonEncoder implements Encoder<UpdateEvent, String> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String encode(UpdateEvent m) {
        try {
            return mapper.writeValueAsString(m);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}