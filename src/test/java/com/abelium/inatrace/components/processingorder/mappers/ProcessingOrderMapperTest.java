package com.abelium.inatrace.components.processingorder.mappers;

import com.abelium.inatrace.db.entities.stockorder.StockOrder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessingOrderMapperTest {

    private static StockOrder stockOrder(long id, Integer sacNumber) {
        // setId is protected in BaseEntity
        StockOrder so = new StockOrder() {{ setId(id); }};
        so.setSacNumber(sacNumber);
        return so;
    }

    @Test
    void targetStockOrdersAreSortedBySacNumberThenId() {
        List<Long> ids = Stream.of(
                        stockOrder(2481, 15),
                        stockOrder(2510, 1),
                        stockOrder(2530, null),
                        stockOrder(2480, 1),
                        stockOrder(2504, 2))
                .sorted(ProcessingOrderMapper.TARGET_STOCK_ORDER_ORDER)
                .map(StockOrder::getId)
                .collect(Collectors.toList());

        assertEquals(List.of(2480L, 2510L, 2504L, 2481L, 2530L), ids);
    }
}
