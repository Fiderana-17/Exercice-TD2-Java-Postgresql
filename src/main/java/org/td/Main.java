package org.td;

import org.td.entity.*;

import java.time.Instant;

public class Main {

    public static void main(String[] args) {

        // 🔹 Test PaymentStatusEnum
        Order order = new Order();
        order.setPaymentStatus(PaymentStatusEnum.UNPAID);
        System.out.println("Payment status = " + order.getPaymentStatus()); // UNPAID

        order.setPaymentStatus(PaymentStatusEnum.PAID);
        System.out.println("Payment status = " + order.getPaymentStatus()); // PAID

        // 🔹 Test Sale entity
        Sale sale = new Sale();
        sale.setId(1);
        sale.setCreationDatetime(Instant.now());
        sale.setOrder(order);

        System.out.println("Sale id = " + sale.getId());
        System.out.println("Sale datetime = " + sale.getCreationDatetime());
        System.out.println("Sale linked to order = " + (sale.getOrder() != null));

        // 🔹 Test Order ↔ Sale relation
        order.setSale(sale);
        System.out.println("Order has sale = " + (order.getSale() != null));


    }
}
