package school.hei;

import school.hei.entity.Dish;
import school.hei.entity.Ingredient;
import school.hei.server.DataRetriever;

import java.sql.SQLException;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws SQLException {
        DataRetriever dr = new DataRetriever();

        try {
            Dish salade = dr.findDishById(1);
            System.out.println("Plat : " + salade.getName());
            System.out.println("Ingrédients :");
            for (Ingredient ing : salade.getIngredient()) {
                System.out.println(" - " + ing.getName() + " (" + ing.getRequiredQuantity() + ")");
            }
        } catch (RuntimeException e) {
            System.err.println("Erreur : " + e.getMessage());
        }
    }

}