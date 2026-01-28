package org.td;

import org.td.entity.Ingredient;
import org.td.service.DataRetriever;
import org.td.entity.Dish;


import java.sql.SQLException;
import java.time.Instant;


public class Main {

    public static void main(String[] args) throws SQLException {

        /* comment out every test that would throw error before running all test to show all normal result */
        DataRetriever dr = new DataRetriever();

//        System.out.println("==== Test a) findDishById(1) ====");
//        Dish dish1 = dr.findDishById(1);
//        System.out.println("Dish: " + dish1.getName());
//        System.out.println("Ingredients:");
//        for (Ingredient ing : dish1.getIngredient()) {
//            System.out.println("- " + ing.getName());
//        }
//
////        System.out.println("\n==== Test b) findDishById(999) ====");
////        Dish dish999 = dr.findDishById(999);
////        System.out.println("Dish: " + dish999.getName());
//
//        System.out.println("\n==== Test c) findIngredients(page=2, size=2) ====");
//        List<Ingredient> page2Ingredients = dr.findIngredients(2, 2);
//        for (Ingredient ing : page2Ingredients) {
//            System.out.println("- " + ing.getName());
//        }
//
//        System.out.println("\n==== Test d) findIngredients(page=3, size=5) ====");
//        List<Ingredient> page3Ingredients = dr.findIngredients(3, 5);
//        if (page3Ingredients.isEmpty()) {
//            System.out.println("Liste vide");
//        } else {
//            for (Ingredient ing : page3Ingredients) {
//                System.out.println("- " + ing.getName());
//            }
//        }
//
//        System.out.println("\n==== Test e) findDishsByIngredientName('eur') ====");
//        List<Dish> dishesWithEur = dr.findDishesByIngredientName("eur");
//        for (Dish d : dishesWithEur) {
//            System.out.println("Dish: " + d.getName());
//        }
//
//        System.out.println("\n==== Test f) findIngredientsByCriteria(category=VEGETABLE) ====");
//        List<Ingredient> vegIngredients = dr.findIngredientByCriteria(
//                null, CategoryEnum.VEGETABLE, null, 1, 10
//        );
//        for (Ingredient ing : vegIngredients) {
//            System.out.println("- " + ing.getName());
//        }
//
//        System.out.println("\n==== Test g) findIngredientsByCriteria(name='cho', dishName='Sal') ====");
//        List<Ingredient> testG = dr.findIngredientByCriteria(
//                "cho", null, "Sal", 1, 10
//        );
//        System.out.println(testG.isEmpty() ? "Liste vide" : testG);
//
//        System.out.println("\n==== Test h) findIngredientsByCriteria(name='cho', dishName='gâteau') ====");
//        List<Ingredient> testH = dr.findIngredientByCriteria(
//                "cho", null, "gâteau", 1, 10
//        );
//        for (Ingredient ing : testH) {
//            System.out.println("- " + ing.getName());
//        }
//
//        System.out.println("\n==== Test i) createIngredients([Fromage, Oignon]) ====");
//        List<Ingredient> newIngredients = new ArrayList<>();
//        newIngredients.add(new Ingredient("Fromage", 1200.0, CategoryEnum.DAIRY));
//        newIngredients.add(new Ingredient("Oignon", 500.0, CategoryEnum.VEGETABLE));
//        List<Ingredient> createdIngredients = dr.createIngredients(newIngredients);
//        for (Ingredient ing : createdIngredients) {
//            System.out.println("- " + ing.getName());
//        }
//
////        System.out.println("\n==== Test j) createIngredients([Carotte, Laitue]) ====");
////        List<Ingredient> dupIngredients = new ArrayList<>();
////        dupIngredients.add(new Ingredient("Carotte", 2000.0, CategoryEnum.VEGETABLE));
////        dupIngredients.add(new Ingredient("Laitue", 2000.0, CategoryEnum.VEGETABLE));
////        dr.createIngredients(dupIngredients);
//
//
//        System.out.println("\n==== Test k) saveDish(Soupe de légumes) ====");
//        Dish soupe = new Dish();
//        soupe.setName("Soupe de légumes");
//        soupe.setDishType(DishTypeEnum.START);
//        List<Ingredient> soupeIngredients = new ArrayList<>();
//        soupeIngredients.add(new Ingredient("Oignon", 500.0, CategoryEnum.VEGETABLE));
//        soupe.setIngredient(soupeIngredients);
//        Dish savedSoupe = dr.saveDish(soupe);
//        System.out.println("Dish created: " + savedSoupe.getName());
//        for (Ingredient ing : savedSoupe.getIngredient()) {
//            System.out.println("- " + ing.getName());
//        }
//
//        System.out.println("\n==== Test l) saveDish(update Salade fraîche) ====");
//        Dish salade = dr.findDishById(1);
//        List<Ingredient> saladeIngredients = new ArrayList<>(salade.getIngredient());
//        saladeIngredients.add(new Ingredient("Oignon", 500.0, CategoryEnum.VEGETABLE));
//        saladeIngredients.add(new Ingredient("Fromage", 1200.0, CategoryEnum.DAIRY));
//        salade.setIngredient(saladeIngredients);
//        salade = dr.saveDish(salade);
//        System.out.println("Dish updated: " + salade.getName());
//        for (Ingredient ing : salade.getIngredient()) {
//            System.out.println("- " + ing.getName());
//        }
//
//        System.out.println("\n==== Test m) saveDish(update Salade de fromage) ====");
//        salade.setName("Salade de fromage");
//        List<Ingredient> fromageOnly = new ArrayList<>();
//        fromageOnly.add(new Ingredient("Fromage", 1200.0, CategoryEnum.DAIRY));
//        salade.setIngredient(fromageOnly);
//        salade = dr.saveDish(salade);
//        System.out.println("Dish updated: " + salade.getName());
//        for (Ingredient ing : salade.getIngredient()) {
//            System.out.println("- " + ing.getName());
//        }

        /* Normal behavior */
//        Dish dish1 = dr.findDishById(1);
//        System.out.println("Dish: " + dish1.getGrossMargin()); // should return 600
//
//        /* should throw unchecked exception if i invoke getGrossMargin on dish n3*/
//        Dish dish3 = dr.findDishById(3);
//        System.out.println("Dish: " + dish3.getGrossMargin());
//
//        dish1.setPrice(2500.0);
//        dr.saveDish(dish1);
//
//        Dish saladeUpdated = dr.findDishById(dish1.getId());
//        System.out.println("Nouveau prix : " + saladeUpdated.getPrice());
//        System.out.println("Gross Margin : " + saladeUpdated.getGrossMargin())
        Ingredient ing1 = dr.findIngredientById(1);
        Ingredient ing2 = dr.findIngredientById(2);
        Ingredient ing3 = dr.findIngredientById(3);
        Ingredient ing4 = dr.findIngredientById(4);
        Ingredient ing5 = dr.findIngredientById(5);
        Instant t = Instant.parse("2024-01-06T12:00:00Z");
        System.out.println(ing1.getStockValueAt(t));
        System.out.println(ing2.getStockValueAt(t));
        System.out.println(ing3.getStockValueAt(t));
        System.out.println(ing4.getStockValueAt(t));
        System.out.println(ing5.getStockValueAt(t));
    }
}