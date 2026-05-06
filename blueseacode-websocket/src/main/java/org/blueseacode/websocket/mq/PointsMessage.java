package org.blueseacode.websocket.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointsMessage implements Serializable {

    private Long userId;
    private int points;
    private String reason;
    private String type;   // ADD / DEDUCT
}
