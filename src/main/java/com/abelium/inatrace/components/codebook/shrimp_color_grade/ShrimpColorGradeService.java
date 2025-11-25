package com.abelium.inatrace.components.codebook.shrimp_color_grade;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.api.ApiPaginatedList;
import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.shrimp_color_grade.api.ApiShrimpColorGrade;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.db.entities.codebook.ShrimpColorGrade;
import com.abelium.inatrace.db.enums.CodebookStatus;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.tools.PaginationTools;
import com.abelium.inatrace.tools.Queries;
import com.abelium.inatrace.tools.QueryTools;
import com.abelium.inatrace.types.UserRole;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.torpedoquery.jakarta.jpa.Torpedo;

import java.util.List;

/**
 * Service for ShrimpColorGrade entity.
 *
 * @author INATrace Team
 */
@Lazy
@Service
public class ShrimpColorGradeService extends BaseService {

    public ApiPaginatedList<ApiShrimpColorGrade> getShrimpColorGradeList(ApiPaginatedRequest request) {
        return PaginationTools.createPaginatedResponse(em, request, () -> queryObject(request),
                ShrimpColorGradeMapper::toApiShrimpColorGrade);
    }

    public List<ApiShrimpColorGrade> getActiveShrimpColorGrades() {
        List<ShrimpColorGrade> entities = em.createNamedQuery("ShrimpColorGrade.listActive", ShrimpColorGrade.class)
                .getResultList();
        return entities.stream()
                .map(ShrimpColorGradeMapper::toApiShrimpColorGrade)
                .toList();
    }

    private ShrimpColorGrade queryObject(ApiPaginatedRequest request) {
        ShrimpColorGrade proxy = Torpedo.from(ShrimpColorGrade.class);

        switch (request.sortBy) {
            case "code":
                QueryTools.orderBy(request.sort, proxy.getCode());
                break;
            case "label":
                QueryTools.orderBy(request.sort, proxy.getLabel());
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

    public ApiShrimpColorGrade getShrimpColorGrade(Long id) throws ApiException {
        return ShrimpColorGradeMapper.toApiShrimpColorGrade(fetchEntity(id));
    }

    @Transactional
    public ApiBaseEntity createOrUpdateShrimpColorGrade(CustomUserDetails authUser,
                                                        ApiShrimpColorGrade api) throws ApiException {
        ShrimpColorGrade entity;

        if (api.getId() != null) {
            if (authUser.getUserRole() == UserRole.REGIONAL_ADMIN) {
                throw new ApiException(ApiStatus.UNAUTHORIZED, "Regional admin not authorized!");
            }
            entity = fetchEntity(api.getId());
        } else {
            entity = new ShrimpColorGrade();
            entity.setCode(api.getCode());
        }

        entity.setLabel(api.getLabel());
        entity.setDescription(api.getDescription());
        entity.setDisplayOrder(api.getDisplayOrder());
        entity.setStatus(api.getStatus() != null ? api.getStatus() : CodebookStatus.ACTIVE);

        if (entity.getId() == null) {
            em.persist(entity);
        }

        return new ApiBaseEntity(entity);
    }

    @Transactional
    public void deleteShrimpColorGrade(Long id) throws ApiException {
        ShrimpColorGrade entity = fetchEntity(id);
        em.remove(entity);
    }

    public ShrimpColorGrade fetchEntity(Long id) throws ApiException {
        ShrimpColorGrade entity = Queries.get(em, ShrimpColorGrade.class, id);
        if (entity == null) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, "Invalid shrimp color grade ID");
        }
        return entity;
    }
}
