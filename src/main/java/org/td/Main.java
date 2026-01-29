package org.td;

import org.td.entity.*;
import org.td.service.DataRetriever;

import java.time.Instant;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        DataRetriever data = new DataRetriever();

        // Récupère l'objet Order complet
        String reference = "201";
        Order order = data.findOrderByReference(reference);

        System.out.println("Order : " + order);
        System.out.println("DishOrders : " + order.getDishOrders());
    }

}
