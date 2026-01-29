package org.td.entity;

import java.util.Objects;

public class DishOrder {
    private Integer id;
    private Dish dish;
    private int quantity;

    public DishOrder(Integer id, Dish dish, int quantity) {
        this.id = id;
        this.dish = dish;
        this.quantity = quantity;
    }

    public DishOrder() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DishOrder dishOrder = (DishOrder) o;
        return id == dishOrder.id && quantity == dishOrder.quantity && Objects.equals(dish, dishOrder.dish);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dish, quantity);
    }

    @Override
    public String toString() {
        return "DishOrder{" +
                "id=" + id +
                ", dish=" + dish +
                ", quantity=" + quantity +
                '}';
    }
}
