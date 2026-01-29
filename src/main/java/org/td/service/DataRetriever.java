package org.td.service;

import org.td.config.DBConnection;
import org.td.entity.*;
import org.td.util.UnitConverter;

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
                List<DishIngredient> dishIngredients = findDishIngredientById(resultSet.getInt("dish_id"));
                dish.setDishIngredientList(dishIngredients);
                dbConnection.closeConnection(connection);
                return dish;
            }
            throw new RuntimeException("Dish not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(connection);
        }
    }

    /* for main */
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
                ingredient.setStockMovementList(getStockMovementByIngredientId(connection,id));
            dbConnection.closeConnection(connection);
                return ingredient;
            }
            throw new RuntimeException("Ingredient not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(connection);
        }
    }

        /* optimized version */
        private Ingredient findIngredientById(Connection conn, Integer id) {
            try {
                PreparedStatement ps = conn.prepareStatement("""
            select id, name, price, category
            from ingredient
            where id = ?
        """);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    throw new RuntimeException("Ingredient not found " + id);
                }

                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setPrice(rs.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));

                ingredient.setStockMovementList(
                        getStockMovementByIngredientId(conn, id)
                );

                return ingredient;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    public List<StockMovement> getStockMovementByIngredientId(Connection conn , int id){
        String sql = """
                select id, id_ingredient, quantity,type,unit, creation_datetime
                from stockmovement
                where id_ingredient = ?;
                """;
        try(PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
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
                dbConnection.closeConnection(conn);
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
            connection.setAutoCommit(false);
            for (StockMovement mvt : ingredient.getStockMovementList()){
                if (mvt.getId() != null) {
                ps.setInt(1, mvt.getId());
                }else{
                    ps.setInt(1, getNextSerialValue(connection, "stockmovement", "id"));
                }
                ps.setInt(2, ingredient.getId());
                ps.setDouble(3, mvt.getValue().getQuantity());
                ps.setString(4, mvt.getType().name());
                ps.setString(5, mvt.getValue().getUnit().name());
                ps.setTimestamp(6, Timestamp.from(mvt.getCreationDatetime()));

                ps.addBatch();
            }
            ps.executeBatch();
            connection.commit();
            dbConnection.closeConnection(connection);
            return ingredient;
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally {
            dbConnection.closeConnection(connection);
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
                ps.setInt(1, dishId);
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
        finally {
            dbConnection.closeConnection(connection);
        }
    }

    private List<DishIngredient> findDishIngredientById(Integer idDish) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<DishIngredient> dishIngredients = new ArrayList<>();
        String sql = """
                select d.id , id_dish,id_ingredient, quantity_required, unit, i.id as ing_id , i.name as ing_name, i.price as ing_price, i.category as ing_category
                from dishingredient d join ingredient i
                on d.id_ingredient = i.id
                where id_dish = ?
                """;
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, idDish);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                DishIngredient dishIng = new DishIngredient();
                Ingredient ing = new Ingredient();
                        dishIng.setId(resultSet.getInt(1));
//                        dishIng.setDish(findDishById(resultSet.getInt("id_dish")));
                        dishIng.setQuantity_required(resultSet.getDouble("quantity_required"));
                        dishIng.setUnit(UnitType.valueOf(resultSet.getString("unit")));

                        ing.setId(resultSet.getInt("ing_id"));
                        ing.setName(resultSet.getString("ing_name"));
                        ing.setPrice(resultSet.getDouble("ing_price"));
                        ing.setCategory(CategoryEnum.valueOf(resultSet.getString("ing_category")));
                        dishIng.setIngredient(ing);
                        dishIngredients.add(dishIng);

            }
            dbConnection.closeConnection(connection);
            return dishIngredients;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally {
            dbConnection.closeConnection(connection);
        }
    }

    /* Order operations */
    public Order saveOrder(Order orderToSave) throws SQLException {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();

        try {
            connection.setAutoCommit(false);

            TableOrder tableOrder = orderToSave.getTableOrder();
            if (tableOrder != null) {
                Table table = tableOrder.getTable();
                if (!isTableAvailable(connection, table.getId(), tableOrder.getArrivalDatetime(), tableOrder.getDepartureDatetime())) {
                    List<Table> availableTables = getAvailableTables(connection, tableOrder.getArrivalDatetime(), tableOrder.getDepartureDatetime());
                    String available = availableTables.stream()
                            .map(t -> String.valueOf(t.getNumber()))
                            .collect(Collectors.joining(", "));
                    throw new RuntimeException(
                            "Table " + table.getNumber() + " is not available at the requested time. " +
                                    (available.isEmpty() ? "No tables available." : "Available tables: " + available)
                    );
                }

            }

            for (DishOrder dishOrder : orderToSave.getDishOrders()) {
                for (DishIngredient dishIng : dishOrder.getDish().getDishIngredientList()) {
                    double requiredQuantity = UnitConverter.convertTo(
                            dishIng.getIngredient().getName(),
                            dishIng.getUnit(),
                            UnitType.KG,
                            dishIng.getQuantity_required()
                    ) * dishOrder.getQuantity();

                    Ingredient ingredient = findIngredientById(connection, dishIng.getIngredient().getId());
                    double availableQuantity = ingredient.getStockValueAt(Instant.now()).getQuantity();
                    if (availableQuantity < requiredQuantity) {
                        throw new RuntimeException("Insufficient stock for ingredient: " + ingredient.getName());
                    }
                }
            }

            int orderId = orderToSave.getId() != null ? orderToSave.getId() : getNextSerialValue(connection, "Order", "id");
            String insertOrderSql = """
            insert into "Order" (id, reference, creation_datetime, id_table, installation_time, departure_time)
            values (?, ?, ?, ?, ?, ?)
            """;
            try (PreparedStatement ps = connection.prepareStatement(insertOrderSql)) {
                ps.setInt(1, orderId);
                ps.setString(2, orderToSave.getReference());
                ps.setTimestamp(3, Timestamp.from(orderToSave.getCreationDatetime()));
                if (tableOrder != null) {
                    ps.setInt(4, tableOrder.getTable().getId());
                    ps.setTimestamp(5, Timestamp.from(tableOrder.getArrivalDatetime()));
                    ps.setTimestamp(6, Timestamp.from(tableOrder.getDepartureDatetime()));
                } else {
                    ps.setNull(4, Types.INTEGER);
                    ps.setNull(5, Types.TIMESTAMP);
                    ps.setNull(6, Types.TIMESTAMP);
                }
                ps.executeUpdate();
            }
            saveDishOrder(connection, orderToSave.getDishOrders(), orderId);
            connection.commit();
            return orderToSave;
        } catch (SQLException e) {
            connection.rollback();
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(connection);
        }
    }

    private boolean isTableAvailable(Connection conn, int tableId, Instant arrival, Instant departure) throws SQLException {
        String sql = """
        select count(*)
        from "Order"
        where id_table = ?
          and installation_time < ?
          and departure_time > ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tableId);
            ps.setTimestamp(2, Timestamp.from(departure));
            ps.setTimestamp(3, Timestamp.from(arrival));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) == 0;
            }
        }
    }

    private List<Table> getAvailableTables(Connection conn, Instant arrival, Instant departure) throws SQLException {
        String sql = """
        select t.id, t.number
        from "table" t
        where t.id not in (
            select id_table
            from "Order"
            where installation_time < ? and departure_time > ?
        )
        """;

        List<Table> availableTables = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.from(departure));
            ps.setTimestamp(2, Timestamp.from(arrival));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Table table = new Table();
                    table.setId(rs.getInt("id"));
                    table.setNumber(rs.getInt("number"));
                    availableTables.add(table);
                }
            }
        }
        return availableTables;
    }

    private void saveTableOrder(Connection conn, TableOrder tableOrder, int orderId) throws SQLException {
        int tableOrderId = tableOrder.getId() != null ? tableOrder.getId() : getNextSerialValue(conn, "tableorder", "id");
        String sql = """
        insert into tableorder (id, id_order, id_table)
        values (?, ?, ?)
        on conflict (id) do update
        set id_order = EXCLUDED.id_order,
            id_table = EXCLUDED.id_table
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tableOrderId);
            ps.setInt(2, orderId);
            ps.setInt(3, tableOrder.getTable().getId());
            ps.executeUpdate();
        }
    }

    public TableOrder createTableOrder(TableOrder tableOrderToSave) throws SQLException {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();

        try {
            connection.setAutoCommit(false);

            Table table = tableOrderToSave.getTable();
            Instant arrival = tableOrderToSave.getArrivalDatetime();
            Instant departure = tableOrderToSave.getDepartureDatetime();

            if (!isTableAvailable(connection, table.getId(), arrival, departure)) {
                List<Table> availableTables = getAvailableTables(connection, arrival, departure);
                String available = availableTables.stream()
                        .map(t -> String.valueOf(t.getNumber()))
                        .collect(Collectors.joining(", "));
                throw new RuntimeException(
                        "Table " + table.getNumber() + " is not available at the requested time. " +
                                (available.isEmpty() ? "No tables available." : "Available tables: " + available)
                );
            }


            int tableOrderId = tableOrderToSave.getId() != null ? tableOrderToSave.getId() : getNextSerialValue(connection, "tableorder", "id");
            String sql = "insert into tableorder (id_order, id_table) values (?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, tableOrderId);
                ps.setInt(2, table.getId());
                ps.executeUpdate();
            }

            connection.commit();

            tableOrderToSave.setId(tableOrderId);
            return tableOrderToSave;
        } catch (SQLException e) {
            connection.rollback();
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(connection);
        }
    }


    public void saveDishOrder(Connection conn, List<DishOrder> dishOrders, int orderId) throws SQLException {
      if (dishOrders.isEmpty()){
          throw new RuntimeException("No dish order found");
      }
        String dishOrderSql = """
                insert into dishorder (id, id_order, id_dish, quantity) values
                (? , ? ,? ,?)
                returning id;
                """;
      try{
          PreparedStatement ps = conn.prepareStatement(dishOrderSql);
          for (DishOrder dishOrder: dishOrders){
              if(dishOrder.getId() != null){
            ps.setInt(1, dishOrder.getId());

              }
              else{
                  ps.setInt(1, getNextSerialValue(conn, "dishorder", "id"));
              }
            ps.setInt(2 , orderId);
            ps.setInt(3 , dishOrder.getDish().getId());
            ps.setInt(4, dishOrder.getQuantity());
            ps.addBatch();
          }
          ps.executeBatch();
      } catch (SQLException e) {
          throw new RuntimeException(e);
      }
    };

    public List<DishOrder> findDishOrdersByOrderReference(String reference){
        String sql = """
                select dso.id as dish_order_id, dso.id_dish as dish_order_id_dish,
                       dso.quantity as dish_order_quantity , dso.id_order as dish_order_id_order
                from "Order" o
                join dishorder dso on o.id = dso.id_order
                where reference = ?;
                """;
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, reference);
            ResultSet rs = ps.executeQuery();
            List<DishOrder> dishOrders = new ArrayList<>();
            while(rs.next()){
                DishOrder dishOrder = new DishOrder();
                dishOrder.setId(rs.getInt("dish_order_id"));
                Dish dish = findDishById(rs.getInt("dish_order_id_dish"));
                dishOrder.setDish(dish);
                dishOrder.setQuantity(rs.getInt("dish_order_quantity"));
                dishOrders.add(dishOrder);
            }
            dbConnection.closeConnection(connection);
            return dishOrders;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally {
            dbConnection.closeConnection(connection);
        }
    }

    private Order findOrderByReference(String reference){
        String sql = """
                select o.id, o.reference, o.creation_datetime
                from "Order" o
                where reference = ?;
                """;
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, reference);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                List<DishOrder> dishOrders = new ArrayList<>();
                dishOrders = findDishOrdersByOrderReference(reference);
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setReference(rs.getString("reference"));
                order.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());
                order.setDishOrders(dishOrders);
                dbConnection.closeConnection(connection);
                return order;
            }
            dbConnection.closeConnection(connection);
            throw new RuntimeException("Order not found with reference: " + reference);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally {
            dbConnection.closeConnection(connection);
        }
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