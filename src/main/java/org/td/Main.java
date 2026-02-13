package org.td;

import org.td.entity.*;
import org.td.service.DataRetriever;

import java.time.Instant;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        DataRetriever dr = new DataRetriever();

// Stock à un instant T
        StockValue sv = dr.getStockValueAt(Instant.now(), 1);
        System.out.println("Stock = " + sv.getQuantity() + " " + sv.getUnit());

        System.out.println("Dish cost = " + dr.getDishCost(1));

        System.out.println("Gross margin = " + dr.getGrossMargin(1));

        dr.getStockStatsByPeriod(
                "day",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-05T23:59:59Z")
        );
    }
}
