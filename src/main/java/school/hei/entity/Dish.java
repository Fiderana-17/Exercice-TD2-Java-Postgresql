package school.hei.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dish {
    private Integer id;
    private String name;
    private DishTypeEnum dishType;
    private List<Ingredient> ingredient = new ArrayList<>();
    private Double getDishCost;


    public Dish(Integer id, String name, DishTypeEnum dishType, List<Integer> ingredients, Double getDishCost){
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.ingredient = ingredient;
        this.getDishCost = getDishCost;

    }

    public Dish(int id, String name, String dishType, double DishCost) {
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    public List<Ingredient> getIngredient() {
        return ingredient;
    }

    public Double getDishCost() {
        if (ingredient == null || ingredient.isEmpty()) {
            return 0.0;
        }

        double totalCost = 0.0;

        for (Ingredient ing : ingredient) {
            Double qty = (Double) ing.getRequiredQuantity();

            if (qty == null) {
                throw new RuntimeException(
                        "Quantité nécessaire inconnue pour l'ingrédient : " + ing.getName() +
                                " dans le plat : " + name + ". Impossible de calculer le coût."
                );
            }

            totalCost += ing.getPrice() * qty;
        }

        return totalCost;
    }


    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    public void setPrice(Double price) {
        this.getDishCost = price;
    }


    public void setIngredient(List<Ingredient> ingredient) {
        this.ingredient = ingredient;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return Objects.equals(id, dish.id) && Objects.equals(name, dish.name) && Objects.equals(dishType, dish.dishType) && Objects.equals(ingredient, dish.ingredient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dishType, ingredient);
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", ingredients=" + ingredient +
                '}';
    }
}
