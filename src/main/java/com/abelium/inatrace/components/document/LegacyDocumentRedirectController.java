package com.abelium.inatrace.components.document;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles legacy /api/document/** requests by redirecting to /api/documents/**.
 * This controller deliberately has no class-level base path, so the mapping is absolute.
 */
@RestController
public class LegacyDocumentRedirectController {

    @RequestMapping("/api/document/**")
    public ResponseEntity<Void> redirectLegacy(HttpServletRequest request) {
        String uri = request.getRequestURI(); // e.g., /api/document/foo.pdf
        String rest = uri.replaceFirst("^/api/document/?", "");
        String target = "/api/documents/" + rest;
        return ResponseEntity.status(308)
                .header(HttpHeaders.LOCATION, target)
                .build();
    }

    // Handle exact base path without wildcard so /api/document and /api/document/ are redirected too
    @RequestMapping({"/api/document", "/api/document/"})
    public ResponseEntity<Void> redirectLegacyBase() {
        return ResponseEntity.status(308)
                .header(HttpHeaders.LOCATION, "/api/documents/")
                .build();
    }
}
