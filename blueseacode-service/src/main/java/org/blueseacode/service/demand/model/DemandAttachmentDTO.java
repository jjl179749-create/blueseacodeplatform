package org.blueseacode.service.demand.model;

import lombok.Data;

@Data
public class DemandAttachmentDTO {
    private String fileName;
    private String fileUrl;
    private Long fileSize;
}
