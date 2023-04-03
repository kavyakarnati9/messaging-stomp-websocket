package com.example.messagingstompwebsocket.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HelloMessage {

    @JsonProperty("name")
    private String name;

    public HelloMessage() {
    }

    public HelloMessage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
