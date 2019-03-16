package com.sergiomoratilla.kstreamtests.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Data;

@Data
public class OutputMessage implements Serializable {
    private long count;

    @JsonCreator
    public OutputMessage(@JsonProperty("count") long count) {
        this.count = count;
    }
}
