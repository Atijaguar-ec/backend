package com.abelium.inatrace.db.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;

/**
 * Base class for all entities with timestamp columns `creationTimestamp` and `updateTimestamp`
 * managed by Hibernate using @CreationTimestamp and @UpdateTimestamp, matching the initial SQL schema.
 */
@MappedSuperclass
public class TimestampEntity extends BaseEntity {

    @CreationTimestamp
    @Column(name = "creationTimestamp", updatable = false)
    private Instant creationTimestamp;
    
    @UpdateTimestamp
    @Column(name = "updateTimestamp")
    private Instant updateTimestamp;
    
    public Instant getCreationTimestamp() {
        return creationTimestamp;
    }
    
    public Instant getUpdateTimestamp() {
        return updateTimestamp;
    }

}
