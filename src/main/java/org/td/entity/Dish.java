package org.td.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dish {
    private Integer id;
    private Double price;
    private String name;
    private DishTypeEnum dishType;
    private List<DishIngredient> dishIngredientList;

    public List<DishIngredient> getDishIngredientList() {
        return dishIngredientList;
    }

    public void setDishIngredientList(List<DishIngredient> dishIngredientList) {
        if(this.dishIngredientList != null && !this.dishIngredientList.isEmpty()){
            this.dishIngredientList.clear();
        }
        this.dishIngredientList = dishIngredientList == null ? new ArrayList<>() : dishIngredientList;
        for (DishIngredient dishIngredient : this.dishIngredientList) {
            if(dishIngredient != null){
            dishIngredient.setDish(this);
            }
        }
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getDishCost() {
        double totalPrice = 0;
        for (DishIngredient dishIngredient : dishIngredientList) {
            totalPrice += dishIngredient.getIngredientCost();
        }
        return totalPrice;
    }

    public Dish() {
    }

    public Dish(Integer id, String name, DishTypeEnum dishType,Double price) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.price = price;
    }
    public Dish(Integer id, String name, DishTypeEnum dishType, List<DishIngredient> dishIngredientList) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.dishIngredientList = dishIngredientList;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return Objects.equals(id, dish.id) && Objects.equals(price, dish.price) && Objects.equals(name, dish.name) && dishType == dish.dishType && Objects.equals(dishIngredientList, dish.dishIngredientList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, price, name, dishType, dishIngredientList);
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", price=" + price +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                '}';
    }

    public Double getGrossMargin() {
        if (price == null) {
            throw new RuntimeException("Price is null");
        }
        return price - getDishCost();
    }
}