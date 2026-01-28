package org.td.service;

import org.td.config.DBConnection;
import org.td.entity.*;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataRetriever {
    public Dish findDishById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            select dish.id as dish_id, dish.name as dish_name, dish_type, dish.selling_price as dish_price
                            from dish
                            where dish.id = ?;
                            """);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Dish dish = new Dish();
                dish.setId(resultSet.getInt("dish_id"));
                dish.setName(resultSet.getString("dish_name"));
                dish.setDishType(DishTypeEnum.valueOf(resultSet.getString("dish_type")));
                dish.setPrice(resultSet.getObject("dish_price") == null
                        ? null : resultSet.getDouble("dish_price"));
                dish.setDishIngredientList(findDishIngredientById(resultSet.getInt("dish_id")));

                return dish;
            }
            dbConnection.closeConnection(connection);
            throw new RuntimeException("Dish not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Ingredient findIngredientById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            select ingredient.id, ingredient.name, ingredient.price, ingredient.category
                            from ingredient
                            where ingredient.id = ?;
                            """);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("id"));
                ingredient.setName(resultSet.getString("name"));
                ingredient.setPrice(resultSet.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));
                ingredient.setStockMovementList(getStockMovementByIngredientId(id));
                return ingredient;
            }
            dbConnection.closeConnection(connection);
            throw new RuntimeException("Ingredient not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<StockMovement> getStockMovementByIngredientId(int id){
        String sql = """
                select id, id_ingredient, quantity,type,unit, creation_datetime
                from stockmovement
                where id_ingredient = ?;
                """;
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            List<StockMovement> stockMovements = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                StockMovement stockMovement = new StockMovement();
                stockMovement.setId(resultSet.getInt("id"));
                StockValue stockValue = new StockValue();
                stockValue.setQuantity(resultSet.getDouble("quantity"));
                stockValue.setUnit(UnitType.valueOf(resultSet.getString("unit")));
                stockMovement.setValue(stockValue);
                stockMovement.setType(MovementTypeEnum.valueOf(resultSet.getString("type")));
                stockMovement.setCreationDatetime(resultSet.getTimestamp("creation_datetime").toInstant());
                stockMovements.add(stockMovement);
            }
            return stockMovements;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Dish saveDish(Dish toSave) {
        String upsertDishSql = """
                    INSERT INTO dish (id, selling_price, name, dish_type)
                    VALUES (?, ?, ?, ?::dish_type)
                    ON CONFLICT (id) DO UPDATE
                    SET name = EXCLUDED.name,
                        dish_type = EXCLUDED.dish_type,
                        selling_price = EXCLUDED.selling_price
                    RETURNING id
                """;

        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);
            Integer dishId;
            try (PreparedStatement ps = conn.prepareStatement(upsertDishSql)) {
                if (toSave.getId() != null) {
                    ps.setInt(1, toSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "dish", "id"));
                }
                if (toSave.getPrice() != null) {
                    ps.setDouble(2, toSave.getPrice());
                } else {
                    ps.setNull(2, Types.DOUBLE);
                }
                ps.setString(3, toSave.getName());
                ps.setString(4, toSave.getDishType().name());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    dishId = rs.getInt(1);
                }
            }

            List<Ingredient> newIngredients = toSave.getDishIngredientList().stream().map(DishIngredient::getIngredient).toList();

            detachIngredients(conn, dishId, newIngredients);
            attachIngredients(conn, dishId, toSave.getDishIngredientList());

            conn.commit();
            return findDishById(dishId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        if (newIngredients == null || newIngredients.isEmpty()) {
            return List.of();
        }
        List<Ingredient> savedIngredients = new ArrayList<>();
        DBConnection dbConnection = new DBConnection();
        Connection conn = dbConnection.getConnection();
        try {
            conn.setAutoCommit(false);
            String insertSql = """
                        INSERT INTO ingredient (id, name, category, price)
                        VALUES (?, ?, ?::ingredient_category, ?)
                        RETURNING id
                    """;
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Ingredient ingredient : newIngredients) {
                    if (ingredient.getId() != null) {
                        ps.setInt(1, ingredient.getId());
                    } else {
                        ps.setInt(1, getNextSerialValue(conn, "ingredient", "id"));
                    }
                    ps.setString(2, ingredient.getName());
                    ps.setString(3, ingredient.getCategory().name());
                    ps.setDouble(4, ingredient.getPrice());

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        int generatedId = rs.getInt(1);
                        ingredient.setId(generatedId);
                        savedIngredients.add(ingredient);
                    }
                }
                conn.commit();
                return savedIngredients;
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    public Ingredient saveIngredient(Ingredient ingredient) {
        String baseSql = """
                insert into stockmovement (id, id_ingredient, quantity, type, unit, creation_datetime)
                values (?, ?, ?, ?::mouvement_type, ?::unit_type, ?)
                on conflict (id) do nothing;
                """;
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        try(PreparedStatement ps = connection.prepareStatement(baseSql)) {
            for (StockMovement mvt : ingredient.getStockMovementList()){
                ps.setInt(1, mvt.getId());
                ps.setInt(2, ingredient.getId());
                ps.setDouble(3, mvt.getValue().getQuantity());
                ps.setString(4, mvt.getType().name());
                ps.setString(5, mvt.getValue().getUnit().name());
                ps.setTimestamp(6, Timestamp.from(mvt.getCreationDatetime()));

                ps.addBatch();
            }
            ps.executeBatch();
            return ingredient;
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private void detachIngredients(Connection conn, Integer dishId, List<Ingredient> ingredients)
            throws SQLException {
        if (ingredients == null || ingredients.isEmpty()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "delete from dishingredient where id_dish = ?")) {
                ps.setInt(1, dishId);
                ps.executeUpdate();
            }
            return;
        }
        String baseSql = """
                delete from dishingredient where id_dish = ?
                and id_ingredient not in (%s)
                """;
        String inClause = ingredients.stream().map(i -> "?").collect(Collectors.joining(","));
        String finalSql = String.format(baseSql, inClause);

        try(PreparedStatement ps = conn.prepareStatement(finalSql)) {
            ps.setInt(1, dishId);
            int idx = 2;
            for (Ingredient ingredient : ingredients) {
                ps.setInt(idx, ingredient.getId());
                idx++;
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void attachIngredients(Connection conn, Integer dishId, List<DishIngredient> dishIngredients)
            throws SQLException {

        if (dishIngredients == null || dishIngredients.isEmpty()) {
            return;
        }

        String attachSql = """
                   insert into dishingredient (id_dish, id_ingredient, quantity_required, unit)
                   values (?,?,?,?::unit_type)
                   on conflict do update
                   set quantity_required= EXCLUDED.quantity_required,
                       unit= EXCLUDED.unit;
                """;

        try (PreparedStatement ps = conn.prepareStatement(attachSql)) {
            for(DishIngredient dishIngredient : dishIngredients) {
                ps.setInt(1, dishIngredient.getDish().getId());
                ps.setInt(2, dishIngredient.getIngredient().getId());
                ps.setDouble(3, dishIngredient.getQuantity_required());
                ps.setString(4, dishIngredient.getUnit().name());

                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<Ingredient> findIngredientByDishId(Integer idDish) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<Ingredient> ingredients = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            select ingredient.id, ingredient.name, ingredient.price, ingredient.category
                            from ingredient join dishingredient on ingredient.id = dishingredient.id_ingredient where dishingredient.id_dish = ?;
                            """);
            preparedStatement.setInt(1, idDish);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("id"));
                ingredient.setName(resultSet.getString("name"));
                ingredient.setPrice(resultSet.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));
                ingredients.add(ingredient);
            }
            dbConnection.closeConnection(connection);
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* Insert ingredient - dish relation */
    private DishIngredient createLinkedDishIngredient(DishIngredient dishIngredient) {
        String sql= """
                insert into dishingredient (id_dish, id_ingredient, quantity_required, unit) values (?, ?, ?, ?::unit_type)
                """;
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        try{
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, dishIngredient.getDish().getId());
            preparedStatement.setInt(2, dishIngredient.getIngredient().getId());
            preparedStatement.setDouble(3, dishIngredient.getQuantity_required());
            preparedStatement.setObject(4, dishIngredient.getUnit());
            ResultSet resultSet = preparedStatement.executeQuery();
            return dishIngredient;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    };

    private List<DishIngredient> findDishIngredientById(Integer idDish) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<DishIngredient> dishIngredients = new ArrayList<>();
        String sql = """
                select d.id , id_dish,id_ingredient, quantity_required, unit, i.id as ing_id , i.name as ing_name, i.price as ing_price, i.category as ing_category
                from dishingredient d join ingredient i
                on d.id_ingredient = i.id
                where id_dish = ?
               ;
                """;
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);
            preparedStatement.setInt(1, idDish);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                DishIngredient dishIng = new DishIngredient();
                Ingredient ing = new Ingredient();
                dishIng.setId(resultSet.getInt(1));
                dishIng.setDish(findDishById(resultSet.getInt("id_dish")));
                dishIng.setIngredient(findIngredientById(resultSet.getInt("id_ingredient")));
                dishIng.setQuantity_required(resultSet.getDouble("quantity_required"));
                dishIng.setUnit(UnitType.valueOf(resultSet.getString("unit")));

                ing.setId(resultSet.getInt("ing_id"));
                ing.setName(resultSet.getString("ing_name"));
                ing.setPrice(resultSet.getDouble("ing_price"));
                ing.setCategory(CategoryEnum.valueOf(resultSet.getString("ing_category")));
                dishIng.setIngredient(ing);
                dishIngredients.add(dishIng);

            }
            connection.commit();
            dbConnection.closeConnection(connection);
            return dishIngredients;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* Order operations */
    private Order saveOrder(Order orderToSave){
        /*check if stock ingredient is sufficient */
        for(DishOrder dishOrder: orderToSave.getDishOrders()){
            for (DishIngredient dishIng: dishOrder.getDish().getDishIngredientList()){
                double requiredQuantity = dishIng.getQuantity_required() * dishOrder.getQuantity();
                Ingredient ingredient = findIngredientById(dishIng.getIngredient().getId());
                double availableQuantity = ingredient.getStockValueAt(Instant.now()).getQuantity();

                if(availableQuantity < requiredQuantity){
                    throw new RuntimeException("Insufficient stock for ingredient: " + ingredient.getName());
                }
            }
        }

        String sql = """
                insert into "Order" (id, creation_datetime, total_price)
                values (?, ?, ?)
                on conflict (id) do update
                set creation_datetime = EXCLUDED.creation_datetime,
                    total_price = EXCLUDED.total_price
                returning id
                """;

    }

    private String getSerialSequenceName(Connection conn, String tableName, String columnName)
            throws SQLException {

        String sql = "SELECT pg_get_serial_sequence(?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    private int getNextSerialValue(Connection conn, String tableName, String columnName)
            throws SQLException {

        String sequenceName = getSerialSequenceName(conn, tableName, columnName);
        if (sequenceName == null) {
            throw new IllegalArgumentException(
                    "Any sequence found for " + tableName + "." + columnName
            );
        }
        updateSequenceNextValue(conn, tableName, columnName, sequenceName);

        String nextValSql = "SELECT nextval(?)";

        try (PreparedStatement ps = conn.prepareStatement(nextValSql)) {
            ps.setString(1, sequenceName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void updateSequenceNextValue(Connection conn, String tableName, String columnName, String sequenceName) throws SQLException {
        String setValSql = String.format(
                "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 0) FROM %s))",
                sequenceName, columnName, tableName
        );

        try (PreparedStatement ps = conn.prepareStatement(setValSql)) {
            ps.executeQuery();
        }
    }
}