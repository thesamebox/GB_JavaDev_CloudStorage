import java.sql.*;

public class DBConnectionHandler {
    private static Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    public DBConnectionHandler() throws SQLException, ClassNotFoundException {
        setConnection();
        createDb();
        readDB();
    }

    public static void setConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:CloudStorageUsers.s2db");
    }

    public void createDb() throws SQLException {
        statement = connection.createStatement();
        statement.execute(
                "CREATE TABLE if not exists 'Users'" +
                        "('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'login' text, 'pass' text);");
    }

    public void readDB() throws SQLException, ClassNotFoundException {
        resultSet = statement.executeQuery("SELECT * FROM users");
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String login = resultSet.getString("login");
            String pass = resultSet.getString("pass");
            System.out.println(id + " " + login + " " + pass);
        }
    }


    public boolean registration(String login, String password) throws SQLException, ClassNotFoundException {
        resultSet = statement.executeQuery("SELECT * FROM users");
        while (resultSet.next()) {
            String loginDB = resultSet.getString("login");
            if (login.equals(loginDB)) {
                return false;
            }
        }
        statement.execute(String.format("INSERT INTO 'Users' ('login', 'pass') VALUES ('%s', '%s')", login, password));
        return true;
    }

    public boolean registeredLogin (String login) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM users");
        while (resultSet.next()) {
            String loginDB = resultSet.getString("login");
            if (login.equals(loginDB)) return true;
        }
        return false;
    }

    public boolean registeredPassword (String pass) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM users");
        while (resultSet.next()) {
            String passDB = resultSet.getString("pass");
            if (pass.equals(passDB)) return true;
        }
        return false;
    }

    public void closeDB() throws SQLException {
        resultSet.close();
        statement.close();
        connection.close();
    }


}
