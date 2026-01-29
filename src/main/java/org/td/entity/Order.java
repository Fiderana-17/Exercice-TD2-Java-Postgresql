package org.td.entity;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Order {
    private Integer id;
    private String reference;
    private Instant creationDatetime;
    private List<DishOrder> dishOrders;

    private PaymentStatusEnum paymentStatus;   // NEW
    private Sale sale;                         // NEW

    public Order(Integer id, String reference, Instant creationDatetime, List<DishOrder> dishOrders) {
        this.id = id;
        this.reference = reference;
        this.creationDatetime = creationDatetime;
        this.dishOrders = dishOrders;
        this.paymentStatus = PaymentStatusEnum.UNPAID; // valeur par défaut logique
    }

    public Order() {
        this.paymentStatus = PaymentStatusEnum.UNPAID; // valeur par défaut logique
    }

    // ---------------- GETTERS / SETTERS ----------------

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    public List<DishOrder> getDishOrders() {
        return dishOrders;
    }

    public void setDishOrders(List<DishOrder> dishOrders) {
        this.dishOrders = dishOrders;
    }

    public PaymentStatusEnum getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatusEnum paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    // ---------------- MÉTHODES MÉTIER ----------------

    /* Amount calcul */
    public Double getTotalAmountWithoutVAT(){
        double totalAmount = 0;
        if(dishOrders != null){
            for (DishOrder dishOrder : dishOrders) {
                totalAmount += dishOrder.getDish().getPrice() * dishOrder.getQuantity();
            }
        }
        return totalAmount;
    }

    public Double getTotalAmountWithVAT(){
        return getTotalAmountWithoutVAT() + (getTotalAmountWithoutVAT() * 0.2);
    }

    // ---------------- OBJECT METHODS ----------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", creationDatetime=" + creationDatetime +
                ", paymentStatus=" + paymentStatus +
                ", sale=" + (sale != null ? sale.getId() : null) +
                '}';
    }
}
