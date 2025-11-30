package com.abelium.inatrace.components.codebook.shrimp_machine;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.api.ApiPaginatedList;
import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.shrimp_machine.api.ApiShrimpMachine;
import com.abelium.inatrace.components.codebook.shrimp_machine.api.ApiShrimpMachineTranslation;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.db.entities.codebook.ShrimpMachine;
import com.abelium.inatrace.db.entities.codebook.ShrimpMachineTranslation;
import com.abelium.inatrace.db.enums.CodebookStatus;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.tools.PaginationTools;
import com.abelium.inatrace.tools.Queries;
import com.abelium.inatrace.tools.QueryTools;
import com.abelium.inatrace.types.Language;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.torpedoquery.jakarta.jpa.Torpedo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for ShrimpMachine catalog.
 */
@Service
public class ShrimpMachineService extends BaseService {

    public ApiPaginatedList<ApiShrimpMachine> getShrimpMachineList(ApiPaginatedRequest request, Language language) {
        return PaginationTools.createPaginatedResponse(em, request,
                () -> machineQueryObject(request),
                entity -> ShrimpMachineMapper.toApiShrimpMachine(entity, language));
    }

    private ShrimpMachine machineQueryObject(ApiPaginatedRequest request) {
        ShrimpMachine proxy = Torpedo.from(ShrimpMachine.class);

        switch (request.sortBy) {
            case "code":
                QueryTools.orderBy(request.sort, proxy.getCode());
                break;
            case "label":
                QueryTools.orderBy(request.sort, proxy.getLabel());
                break;
            case "machineType":
                QueryTools.orderBy(request.sort, proxy.getMachineType());
                break;
            case "displayOrder":
                QueryTools.orderBy(request.sort, proxy.getDisplayOrder());
                break;
            case "status":
                QueryTools.orderBy(request.sort, proxy.getStatus());
                break;
            default:
                QueryTools.orderBy(request.sort, proxy.getDisplayOrder());
        }

        return proxy;
    }

    public List<ApiShrimpMachine> getActiveShrimpMachines(Language language) {
        List<ShrimpMachine> machines = em.createQuery(
                "SELECT m FROM ShrimpMachine m WHERE m.status = :status ORDER BY m.displayOrder", 
                ShrimpMachine.class)
                .setParameter("status", CodebookStatus.ACTIVE)
                .getResultList();
        return machines.stream()
                .map(m -> ShrimpMachineMapper.toApiShrimpMachine(m, language))
                .collect(Collectors.toList());
    }

    public ApiShrimpMachine getShrimpMachine(Long id, Language language) throws ApiException {
        ShrimpMachine entity = fetchEntity(id, ShrimpMachine.class);
        return ShrimpMachineMapper.toApiShrimpMachine(entity, language);
    }

    @Transactional
    public ApiBaseEntity createOrUpdateShrimpMachine(CustomUserDetails authUser, ApiShrimpMachine api) throws ApiException {
        ShrimpMachine entity;

        if (api.getId() != null) {
            entity = fetchEntity(api.getId(), ShrimpMachine.class);
        } else {
            entity = new ShrimpMachine();
        }

        entity.setCode(api.getCode());
        entity.setLabel(api.getLabel());
        entity.setDescription(api.getDescription());
        entity.setMachineType(api.getMachineType());
        entity.setDisplayOrder(api.getDisplayOrder());
        entity.setStatus(api.getStatus() != null ? api.getStatus() : CodebookStatus.ACTIVE);

        // Handle translations
        if (api.getTranslations() != null) {
            entity.getTranslations().clear();
            for (ApiShrimpMachineTranslation apiT : api.getTranslations()) {
                ShrimpMachineTranslation translation = new ShrimpMachineTranslation();
                translation.setShrimpMachine(entity);
                translation.setLanguage(apiT.getLanguage());
                translation.setLabel(apiT.getLabel());
                translation.setDescription(apiT.getDescription());
                entity.getTranslations().add(translation);
            }
        }

        if (entity.getId() == null) {
            em.persist(entity);
        }

        return new ApiBaseEntity(entity);
    }

    @Transactional
    public ApiBaseEntity deleteShrimpMachine(Long id) throws ApiException {
        ShrimpMachine entity = fetchEntity(id, ShrimpMachine.class);
        em.remove(entity);
        return new ApiBaseEntity(entity);
    }

    private <E> E fetchEntity(Long id, Class<E> entityClass) throws ApiException {
        E entity = Queries.get(em, entityClass, id);
        if (entity == null) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, "Invalid " + entityClass.getSimpleName() + " ID");
        }
        return entity;
    }
}
