package org.blueseacode.websocket.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogMessage implements Serializable {

    private Long userId;
    private String module;
    private String action;
    private String targetType;
    private Long targetId;
    private String params;
    private String ip;
}
