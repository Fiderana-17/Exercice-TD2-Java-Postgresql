package school.hei;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {
    public Connection getDBConnection() throws SQLException {
        String jdbcurl = System.getenv("JDBC_URL");//jdbc:postgresql://localhost:5432/mini_dish_db
        String user = System.getenv("USERNAME");//mini_dish_db_manager
        String password = System.getenv("PASSWORD");//password

        return DriverManager.getConnection(jdbcurl, user, password);
    }
}
