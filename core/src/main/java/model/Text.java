package model;

import java.time.LocalDateTime;

public class Text {

    public static void main(String[] args) {
        Message m = Message.builder()
                .text("Что это")
                .created(LocalDateTime.now())
                .author("Что происходит")
                .build();
    }
}
