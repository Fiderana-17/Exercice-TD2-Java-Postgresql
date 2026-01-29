package org.td.entity;

import java.time.Instant;

public class Sale {
    private Integer id;
    private Instant creationDatetime;
    private Order order;

    // ---------------- GETTERS / SETTERS ----------------

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    // ---------------- OBJECT METHODS ----------------

    @Override
    public String toString() {
        return "Sale{" +
                "id=" + id +
                ", creationDatetime=" + creationDatetime +
                ", orderId=" + (order != null ? order.getId() : null) +
                '}';
    }
}
