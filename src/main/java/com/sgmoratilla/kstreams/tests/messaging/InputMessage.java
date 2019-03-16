package com.sgmoratilla.kstreams.tests.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import lombok.Data;

@Data
public class InputMessage {

    @Nonnull
    private String owner;

    @JsonCreator
    public InputMessage(@JsonProperty("owner") @Nonnull String owner) {
        this.owner = owner;
    }
}
