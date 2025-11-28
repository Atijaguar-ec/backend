package com.abelium.inatrace.components.codebook.shrimp_size_grade;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.api.ApiPaginatedList;
import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.shrimp_size_grade.api.ApiShrimpSizeGrade;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.db.entities.codebook.ShrimpSizeGrade;
import com.abelium.inatrace.db.enums.CodebookStatus;
import com.abelium.inatrace.db.enums.ShrimpSizeType;
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
 * Service for ShrimpSizeGrade entity.
 *
 * @author INATrace Team
 */
@Lazy
@Service
public class ShrimpSizeGradeService extends BaseService {

    public ApiPaginatedList<ApiShrimpSizeGrade> getShrimpSizeGradeList(ApiPaginatedRequest request) {
        return PaginationTools.createPaginatedResponse(em, request, () -> queryObject(request),
                ShrimpSizeGradeMapper::toApiShrimpSizeGrade);
    }

    public List<ApiShrimpSizeGrade> getActiveShrimpSizeGrades() {
        List<ShrimpSizeGrade> entities = em.createNamedQuery("ShrimpSizeGrade.listActive", ShrimpSizeGrade.class)
                .getResultList();
        return entities.stream()
                .map(ShrimpSizeGradeMapper::toApiShrimpSizeGrade)
                .toList();
    }

    public List<ApiShrimpSizeGrade> getShrimpSizeGradesByType(ShrimpSizeType sizeType) {
        List<ShrimpSizeGrade> entities = em.createNamedQuery("ShrimpSizeGrade.listByType", ShrimpSizeGrade.class)
                .setParameter("sizeType", sizeType)
                .getResultList();
        return entities.stream()
                .map(ShrimpSizeGradeMapper::toApiShrimpSizeGrade)
                .toList();
    }

    private ShrimpSizeGrade queryObject(ApiPaginatedRequest request) {
        ShrimpSizeGrade proxy = Torpedo.from(ShrimpSizeGrade.class);

        switch (request.sortBy) {
            case "code":
                QueryTools.orderBy(request.sort, proxy.getCode());
                break;
            case "label":
                QueryTools.orderBy(request.sort, proxy.getLabel());
                break;
            case "sizeType":
                QueryTools.orderBy(request.sort, proxy.getSizeType());
                break;
            case "displayOrder":
                QueryTools.orderBy(request.sort, proxy.getDisplayOrder());
                break;
            case "status":
                QueryTools.orderBy(request.sort, proxy.getStatus());
                break;
            default:
                QueryTools.orderBy(request.sort, proxy.getSizeType());
                QueryTools.orderBy(request.sort, proxy.getDisplayOrder());
        }

        return proxy;
    }

    public ApiShrimpSizeGrade getShrimpSizeGrade(Long id) throws ApiException {
        return ShrimpSizeGradeMapper.toApiShrimpSizeGrade(fetchEntity(id));
    }

    @Transactional
    public ApiBaseEntity createOrUpdateShrimpSizeGrade(CustomUserDetails authUser,
                                                       ApiShrimpSizeGrade api) throws ApiException {
        ShrimpSizeGrade entity;

        if (api.getId() != null) {
            if (authUser.getUserRole() == UserRole.REGIONAL_ADMIN) {
                throw new ApiException(ApiStatus.UNAUTHORIZED, "Regional admin not authorized!");
            }
            entity = fetchEntity(api.getId());
        } else {
            entity = new ShrimpSizeGrade();
            entity.setCode(api.getCode());
        }

        entity.setLabel(api.getLabel());
        entity.setSizeType(api.getSizeType());
        entity.setDisplayOrder(api.getDisplayOrder());
        entity.setStatus(api.getStatus() != null ? api.getStatus() : CodebookStatus.ACTIVE);

        if (entity.getId() == null) {
            em.persist(entity);
        }

        return new ApiBaseEntity(entity);
    }

    @Transactional
    public void deleteShrimpSizeGrade(Long id) throws ApiException {
        ShrimpSizeGrade entity = fetchEntity(id);
        em.remove(entity);
    }

    public ShrimpSizeGrade fetchEntity(Long id) throws ApiException {
        ShrimpSizeGrade entity = Queries.get(em, ShrimpSizeGrade.class, id);
        if (entity == null) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, "Invalid shrimp size grade ID");
        }
        return entity;
    }
}
