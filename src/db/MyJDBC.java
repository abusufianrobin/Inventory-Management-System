package db;

import constants.CommonConstants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MyJDBC {

    // Establishes and returns a database connection.
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                CommonConstants.DB_URL,
                CommonConstants.DB_USERNAME,
                CommonConstants.DB_PASSWORD
        );
    }

    // Registers a new user in the database.
    // Returns true if registration is successful, false otherwise (e.g., username already exists).
    public static boolean register(String username, String password) {
        try (Connection connection = getConnection()) {
            // First, check if the username already exists
            if (checkUser(username)) {
                System.out.println("Registration failed: Username '" + username + "' already exists.");
                return false; // Username already taken
            }

            // Insert new user into the database
            PreparedStatement insertUser = connection.prepareStatement(
                    "INSERT INTO " + CommonConstants.DB_USERS_TABLE_NAME + " (username, password) VALUES (?, ?)"
            );
            insertUser.setString(1, username);
            insertUser.setString(2, password);

            int rowsAffected = insertUser.executeUpdate();
            return rowsAffected > 0; // Registration successful if at least one row was inserted
        } catch (SQLException e) {
            System.err.println("Database error during registration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Authenticates a user based on username and password.
    // Returns true if credentials are valid, false otherwise.
    public static boolean validateLogin(String username, String password) {
        try (Connection connection = getConnection()) {
            PreparedStatement checkLogin = connection.prepareStatement(
                    "SELECT * FROM " + CommonConstants.DB_USERS_TABLE_NAME + " WHERE username = ? AND password = ?"
            );
            checkLogin.setString(1, username);
            checkLogin.setString(2, password);

            ResultSet resultSet = checkLogin.executeQuery();
            return resultSet.next(); // Returns true if a matching record is found
        } catch (SQLException e) {
            System.err.println("Database error during login validation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Checks if a username already exists in the database.
    // Returns true if the username exists, false otherwise.
    public static boolean checkUser(String username) {
        try (Connection connection = getConnection()) {
            PreparedStatement checkUser = connection.prepareStatement(
                    "SELECT * FROM " + CommonConstants.DB_USERS_TABLE_NAME + " WHERE username = ?"
            );
            checkUser.setString(1, username);

            ResultSet resultSet = checkUser.executeQuery();
            return resultSet.next(); // True if a record with this username exists
        } catch (SQLException e) {
            System.err.println("Database error checking user existence: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Retrieves the price of an item by its ID.
    // This method is required by DashboardGUI for calculating total revenue.
    public static double getItemPrice(int itemId) {
        double price = 0.0;
        try (Connection connection = getConnection()) {
            PreparedStatement getPrice = connection.prepareStatement(
                    "SELECT price FROM items WHERE id = ?"
            );
            getPrice.setInt(1, itemId);
            ResultSet resultSet = getPrice.executeQuery();
            if (resultSet.next()) {
                price = resultSet.getDouble("price");
            }
        } catch (SQLException e) {
            System.err.println("Database error retrieving item price for ID " + itemId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return price;
    }
}
