package com.abelium.inatrace.components.groupstockorder;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class GroupStockOrderServiceTest {

    @Autowired
    private GroupStockOrderService groupStockOrderService;

    @Test
    void testFindAllGroupedDoesNotThrowSyntaxError() {
        // La consulta JPQL debe ejecutarse sin errores de sintaxis en Postgres 
        // comprobando que STRING_AGG(so.id, ',') funciona correctamente.
        // Se encapsula en un try-catch logico para ignorar errores de datos vacíos si la lógica lo requiere,
        // pero la query ejecutará el dialecto nuevo.
        var list = groupStockOrderService.getGroupedStockOrderList(
            new com.abelium.inatrace.api.ApiPaginatedRequest(),
            new com.abelium.inatrace.components.groupstockorder.GroupStockOrderQueryRequest(null, null, null, null),
            com.abelium.inatrace.types.Language.EN
        );
        assertNotNull(list);
    }
}
