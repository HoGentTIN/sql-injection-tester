
package be.hogent.sqlinjectiontester;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String JDBC_URL = "jdbc:h2:mem:accounts";
    private static final String JDBC_USERNAME = "sa";
    private static final String JDBC_PASSWORD = "";

    Connection connection = null;

    public Database() throws SQLException {
        connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE accounts (ID INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(200), password VARCHAR(200))");
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO accounts(username, password) values ('Joeri', 'guessthis')");
        }
    }

    public List<Account> getAccounts() throws SQLException {
        ArrayList<Account> accounts = new ArrayList<>();

        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT * FROM accounts");
            while (rs.next()) {
                accounts.add(new Account(rs.getString("username"), rs.getString("password")));
            }
        }

        return accounts;
    }

    public boolean checkAccount(String username, String password) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String sql = "SELECT * FROM accounts WHERE username = '" + username + "' AND password = '" + password + "'";
            ResultSet rs = statement.executeQuery(sql);

            return (rs.next());
        }
    }
}
