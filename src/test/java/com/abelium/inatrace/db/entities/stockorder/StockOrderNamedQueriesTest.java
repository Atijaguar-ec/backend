package com.abelium.inatrace.db.entities.stockorder;

import com.abelium.inatrace.db.entities.common.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StockOrderNamedQueriesTest {

    @PersistenceContext
    EntityManager em;

    private User newUser(String email) {
        User u = new User();
        u.setEmail(email);
        em.persist(u);
        return u;
    }

    private StockOrder newStockOrder(User createdBy, String qrTag) {
        StockOrder so = new StockOrder();
        so.setCreatedBy(createdBy);
        so.setQrCodeTag(qrTag);
        em.persist(so);
        return so;
    }

    @Test
    @DisplayName("StockOrder.getTopLevelStockOrdersForQrTag: excluye órdenes referenciadas por Transaction")
    void topLevelStockOrdersForQrTag_excludesReferenced() {
        User u = newUser("u" + System.nanoTime() + "@ex.com");

        StockOrder free = newStockOrder(u, "TAG1");
        StockOrder referenced = newStockOrder(u, "TAG1");

        Transaction t = new Transaction();
        t.setSourceStockOrder(referenced);
        em.persist(t);

        em.flush();
        em.clear();

        List<StockOrder> result = em.createNamedQuery("StockOrder.getTopLevelStockOrdersForQrTag", StockOrder.class)
                .setParameter("qrTag", "TAG1")
                .getResultList();

        assertEquals(1, result.size(), "Debe retornar solo órdenes no referenciadas");
        assertTrue(result.stream().anyMatch(so -> so.getId().equals(free.getId())), "Debe incluir la orden libre");
        assertTrue(result.stream().noneMatch(so -> so.getId().equals(referenced.getId())), "No debe incluir la orden referenciada");
    }

    @Test
    @DisplayName("Transaction.getOutputTransactionsByStockOrderId: retorna transacciones por sourceStockOrder")
    void transactionNamedQuery_returnsBySourceStockOrder() {
        User u = newUser("u" + System.nanoTime() + "@ex.com");
        StockOrder so = newStockOrder(u, "TAG2");

        Transaction t1 = new Transaction();
        t1.setSourceStockOrder(so);
        em.persist(t1);

        Transaction t2 = new Transaction();
        em.persist(t2); // no asociado

        em.flush();
        em.clear();

        List<Transaction> result = em.createNamedQuery("Transaction.getOutputTransactionsByStockOrderId", Transaction.class)
                .setParameter("stockOrderId", so.getId())
                .getResultList();

        assertEquals(1, result.size(), "Debe retornar solo transacciones del stock order dado");
        assertEquals(t1.getId(), result.get(0).getId());
    }
}
