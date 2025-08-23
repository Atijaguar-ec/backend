package com.abelium.inatrace.components.document;

import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller for serving documents from the configured document root directory.
 * Handles requests to /api/documents/{filename} and serves files securely.
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Value("${INATrace.documents.root:documents/}")
    private String documentsRoot;

    /**
     * Serve a document file by filename.
     * Security: Only allows files within the configured documents root directory.
     * 
     * @param filename the filename to serve
     * @return ResponseEntity with the file content
     * @throws ApiException if file not found or access denied
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveDocument(@PathVariable String filename) throws ApiException {
        try {
            // Normalize and validate the path to prevent directory traversal attacks
            Path documentPath = Paths.get(documentsRoot).normalize();
            Path filePath = documentPath.resolve(filename).normalize();
            
            // Security check: ensure the resolved path is still within the documents root
            if (!filePath.startsWith(documentPath)) {
                throw new ApiException(ApiStatus.UNAUTHORIZED, "Access denied: Path traversal not allowed");
            }
            
            File file = filePath.toFile();
            if (!file.exists() || !file.isFile()) {
                throw new ApiException(ApiStatus.NOT_FOUND, "Document not found: " + filename);
            }
            
            if (!file.canRead()) {
                throw new ApiException(ApiStatus.UNAUTHORIZED, "Access denied: Cannot read file");
            }
            
            Resource resource = new FileSystemResource(file);
            
            // Determine content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                    .body(resource);
                    
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(ApiStatus.ERROR, "Error serving document: " + e.getMessage());
        }
    }
    
    /**
     * Handle legacy /api/document requests by redirecting to /api/documents
     */
    @RequestMapping("/api/document/**")
    public ResponseEntity<Void> redirectLegacyDocument() {
        return ResponseEntity.status(301)
                .header(HttpHeaders.LOCATION, "/api/documents/")
                .build();
    }
}
