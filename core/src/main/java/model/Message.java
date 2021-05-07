package model;

import lombok.Builder;
import lombok.Data;


import java.io.Serializable;
import java.time.LocalDateTime;
@Data
@Builder
public class Message implements Serializable {
    private String text;
    private LocalDateTime created;
    private String author;


}
