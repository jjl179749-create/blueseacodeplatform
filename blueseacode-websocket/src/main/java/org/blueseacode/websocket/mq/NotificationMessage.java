package org.blueseacode.websocket.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {

    private Long userId;
    private String type;
    private String title;
    private String content;
    private Long relatedId;
    private String relatedType;
}
