package com.smart.smartcontactmanager.helper;

import lombok.Data;

@Data
public class Message {

    private String content;
    private String type;

    public Message(String content, String type) {
        this.content = content;
        this.type = type;
    }
}
