package org.td.entity;

import java.util.Objects;

public class DishIngredient {
    private Integer id;
    private double quantity_required;
    private UnitType unit;
    private Dish dish;
    private Ingredient ingredient;
    public DishIngredient() {

    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public DishIngredient( double quantityRequired, UnitType unit, Ingredient ingredient, Dish dish) {
        quantity_required = quantityRequired;
        this.unit = unit;
        this.ingredient = ingredient;
        this.dish = dish;
    }
    public DishIngredient( Integer id,double quantityRequired, UnitType unit) {
        this.id = id;
        quantity_required = quantityRequired;
        this.unit = unit;
    }
    public DishIngredient( double quantityRequired, UnitType unit) {
        quantity_required = quantityRequired;
        this.unit = unit;
    }

    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }




    public double getQuantity_required() {
        return quantity_required;
    }

    public void setQuantity_required(double quantity_required) {
        this.quantity_required = quantity_required;
    }

    public UnitType getUnit() {
        return unit;
    }

    public void setUnit(UnitType unit) {
        this.unit = unit;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public double getIngredientCost(){
        if(ingredient.getPrice() == null) throw new RuntimeException("ingredient price is null");
        return ingredient.getPrice() * getQuantity_required();
    }
    @Override
    public String toString() {
        return "DishIngredient{" +
                "id=" + id +
                ", quantity_required=" + quantity_required +
                ", unit=" + unit +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DishIngredient that = (DishIngredient) o;
        return id == that.id && Double.compare(quantity_required, that.quantity_required) == 0 && unit == that.unit && Objects.equals(ingredient, that.ingredient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, quantity_required, unit, ingredient);
    }
}
