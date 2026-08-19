package com.example.backend.dto.observateur;

import org.springframework.core.io.Resource;

public class DocumentFileDto {
    private final Resource resource;
    private final String filename;
    private final String contentType;

    public DocumentFileDto(Resource resource, String filename, String contentType) {
        this.resource = resource;
        this.filename = filename;
        this.contentType = contentType;
    }

    public Resource getResource() { return resource; }
    public String getFilename() { return filename; }
    public String getContentType() { return contentType; }
}