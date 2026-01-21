package school.hei.server;

import school.hei.config.DBConnection;
import school.hei.entity.CategoryEnum;
import school.hei.entity.Dish;
import school.hei.entity.DishTypeEnum;
import school.hei.entity.Ingredient;
import school.hei.Util.Util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DataRetriever {
    private final Util util = new Util();
    private final DBConnection dbConnection = new DBConnection();

    public DataRetriever() throws SQLException {}

    public Dish findDishById(Integer id) {
        Dish dish = null;

        String dishSql = """
                SELECT id, name, dish_type, price
                FROM Dish
                WHERE id = ?
                """;

        String ingredientSql = """
                SELECT i.*
                FROM Ingredient i
                WHERE i.id_dish = ?
                """;

        try (Connection con = dbConnection.getConnection())
        {


            PreparedStatement dishStmt = con.prepareStatement(dishSql);
            dishStmt.setInt(1, id);
            ResultSet dishRs = dishStmt.executeQuery();

            if (dishRs.next()) {
                dish = new Dish(
                        dishRs.getInt("id"),
                        dishRs.getString("name"),
                        dishRs.getString("dish_type"),
                        dishRs.getDouble("price")
                );


                PreparedStatement ingStmt = con.prepareStatement(ingredientSql);
                ingStmt.setInt(1, id);
                ResultSet ingRs = ingStmt.executeQuery();

                List<Ingredient> ingredients = new ArrayList<>();

                while (ingRs.next()) {
                    ingredients.add(new Ingredient(
                            ingRs.getInt("id"),
                            ingRs.getString("name"),
                            ingRs.getDouble("price"),
                            CategoryEnum.valueOf(ingRs.getString("category")),
                            ingRs.getObject("required_quantity", Double.class),
                            dish
                    ));
                }

                dish.setIngredient(ingredients);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return dish;
    }

}
