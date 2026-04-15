package com.abelium.inatrace.components.codebook.certification_type;

import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.certification_type.api.ApiCertificationType;
import com.abelium.inatrace.types.Language;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/codebook/certification-type")
@Tag(name = "Codebook Certification Type")
public class CertificationTypeController {

    private final CertificationTypeService service;

    @Autowired
    public CertificationTypeController(CertificationTypeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List active certification types")
    public List<ApiCertificationType> listActive(@RequestHeader(value = "Language") Language language) {
        return service.listActiveCertificationTypes(language);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get certification type by ID")
    public ApiCertificationType getById(
            @PathVariable Long id,
            @RequestHeader(value = "Language") Language language) throws ApiException {
        return service.getCertificationType(id, language);
    }

    @PostMapping
    @Operation(summary = "Create or update certification type")
    public ApiCertificationType createOrUpdate(@RequestBody ApiCertificationType apiDTO) throws ApiException {
        return service.createOrUpdateCertificationType(apiDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete certification type")
    public void delete(@PathVariable Long id) throws ApiException {
        service.deleteCertificationType(id);
    }
}
