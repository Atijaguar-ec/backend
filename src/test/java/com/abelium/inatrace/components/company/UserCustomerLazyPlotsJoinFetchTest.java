package com.abelium.inatrace.components.company;

import com.abelium.inatrace.db.entities.common.UserCustomer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserCustomerLazyPlotsJoinFetchTest {

    @Autowired
    private EntityManager em;

    @Test
    void testUserCustomerPlotsCollectionIsConfiguredLazy() throws NoSuchFieldException {
        Field plotsField = UserCustomer.class.getDeclaredField("plots");
        OneToMany oneToMany = plotsField.getAnnotation(OneToMany.class);

        assertNotNull(oneToMany, "plots field must be annotated with @OneToMany");
        assertEquals(FetchType.LAZY, oneToMany.fetch(), "plots collection must be FetchType.LAZY to eradicate N+1 queries (HU-15)");
    }

    @Test
    void testNamedQueryGetUserCustomerWithPlotsByIdExistsAndParses() {
        TypedQuery<UserCustomer> query = em.createNamedQuery("UserCustomer.getUserCustomerWithPlotsById", UserCustomer.class);
        assertNotNull(query, "NamedQuery 'UserCustomer.getUserCustomerWithPlotsById' with JOIN FETCH must exist");
        query.setParameter("id", 1L);
        // Does not throw syntax/semantic error
    }
}
