package org.td;

import org.td.entity.*;
import org.td.service.DataRetriever;

import java.time.Instant;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        //Test PaymentStatusEnum
        Order order = new Order();
        order.setPaymentStatus(PaymentStatusEnum.UNPAID);
        System.out.println("Payment status = " + order.getPaymentStatus()); // UNPAID

        order.setPaymentStatus(PaymentStatusEnum.PAID);
        System.out.println("Payment status = " + order.getPaymentStatus()); // PAID

        //Test Sale entity
        Sale sale = new Sale();
        sale.setId(1);
        sale.setCreationDatetime(Instant.now());
        sale.setOrder(order);

        System.out.println("Sale id = " + sale.getId());
        System.out.println("Sale datetime = " + sale.getCreationDatetime());
        System.out.println("Sale linked to order = " + (sale.getOrder() != null));

         //Test Order ↔ Sale relation
        order.setSale(sale);
        System.out.println("Order has sale = " + (order.getSale() != null));

        DataRetriever dataRetriever = new DataRetriever();

        // 1. Préparation d'une commande de test
        Order testOrder = new Order();
        testOrder.setReference("ORDER-TEST-001");
        testOrder.setCreationDatetime(Instant.now());
        testOrder.setPaymentStatus(PaymentStatusEnum.UNPAID); // On commence en UNPAID
        testOrder.setDishOrders(new ArrayList<>()); // Liste vide pour le test

        System.out.println("--- Étape 1 : Création de la commande ---");
        try {
            dataRetriever.saveOrder(testOrder);
            System.out.println("Commande enregistrée avec succès (Statut: UNPAID)");
        } catch (Exception e) {
            System.err.println("Erreur imprévue : " + e.getMessage());
        }

        // 2. Passage au statut PAID
        System.out.println("\n--- Étape 2 : Passage au statut PAID ---");
        testOrder.setPaymentStatus(PaymentStatusEnum.PAID);
        dataRetriever.saveOrder(testOrder);
        System.out.println("Commande mise à jour en statut PAID.");

        // 3. TEST DE LA QUESTION 2 : Tentative de modification interdite
        System.out.println("\n--- Étape 3 : Test de modification sur commande PAYÉE ---");
        try {
            // On simule un changement de référence ou de contenu
            testOrder.setReference("ORDER-MODIFIED");

            dataRetriever.saveOrder(testOrder); // C'est ici que l'exception doit être levée

            System.out.println("ERREUR : La modification a été acceptée alors qu'elle devrait être bloquée !");
        } catch (RuntimeException e) {
            // C'est ce message que l'on attend selon la consigne
            System.out.println("SUCCÈS : L'application a bien bloqué la modification.");
            System.out.println("Message d'erreur reçu : " + e.getMessage());
        }
    }
}
