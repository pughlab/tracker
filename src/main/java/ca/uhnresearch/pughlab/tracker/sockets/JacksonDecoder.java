package ca.uhnresearch.pughlab.tracker.sockets;

import org.atmosphere.config.managed.Decoder;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Decode a String into a {@link Message}.
 */
public class JacksonDecoder implements Decoder<String, Message> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Message decode(String s) {
        try {
            return mapper.readValue(s, Message.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
