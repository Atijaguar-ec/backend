package com.abelium.inatrace.db.entities.audit;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CustomRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        if (revisionEntity instanceof CustomRevisionEntity) {
            CustomRevisionEntity customRevisionEntity = (CustomRevisionEntity) revisionEntity;
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                    customRevisionEntity.setUsername(authentication.getName());
                }
            } catch (Exception ignored) {
                // Background tasks or migrations without an active SecurityContext
            }
        }
    }
}
