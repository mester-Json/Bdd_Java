package Bdd;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class ConnectBdd {

    private static final String PROPERTIES_FILE = "Application.properties";
    private static Connection conn;
    private static JTable usersTable;
    private static JTable productsTable;
    private static JTable ordersTable;
    private static Statement stmt;

    public static Connection getConnection() throws SQLException {
        Properties properties = new Properties();

        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new SQLException("Sorry, unable to find " + PROPERTIES_FILE);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new SQLException("Error loading properties file", e);
        }

        String driver = properties.getProperty("driver");
        String url = properties.getProperty("url");
        String user = properties.getProperty("user");
        String password = properties.getProperty("pwd");

        if (driver == null || url == null || user == null || password == null) {
            throw new SQLException("Missing database connection properties");
        }

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver class not found", e);
        }

        System.out.println("Connecting to database...");
        conn = DriverManager.getConnection(url, user, password);
        System.out.println("Connection successful!");
        return conn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Database Viewer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

            try {
                getConnection();
                stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                // Users Table
                String query = "SELECT * FROM users";
                try (ResultSet rs = stmt.executeQuery(query)) {
                    int rowCount = 0;
                    while (rs.next()) {
                        rowCount++;
                    }
                    System.out.println("Number of rows in Users table: " + rowCount);
                    rs.beforeFirst();
                    usersTable = buildTable(rs);
                    JScrollPane scrollPane = new JScrollPane(usersTable);
                    frame.add(new JLabel("Users"));
                    frame.add(scrollPane);
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(frame, "Error loading Users table: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }

                // Products Table
                query = "SELECT * FROM products";
                try (ResultSet rs = stmt.executeQuery(query)) {
                    int rowCount = 0;
                    while (rs.next()) {
                        rowCount++;
                    }
                    System.out.println("Number of rows in Products table: " + rowCount);
                    rs.beforeFirst();
                    productsTable = buildTable(rs);
                    JScrollPane scrollPane = new JScrollPane(productsTable);
                    frame.add(new JLabel("Products"));
                    frame.add(scrollPane);
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(frame, "Error loading Products table: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }

                // Orders Table
                query = "SELECT * FROM orders";
                try (ResultSet rs = stmt.executeQuery(query)) {
                    int rowCount = 0;
                    while (rs.next()) {
                        rowCount++;
                    }
                    System.out.println("Number of rows in Orders table: " + rowCount);
                    rs.beforeFirst();
                    ordersTable = buildTable(rs);
                    JScrollPane scrollPane = new JScrollPane(ordersTable);
                    frame.add(new JLabel("Orders"));
                    frame.add(scrollPane);
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(frame, "Error loading Orders table: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }

                JPanel updatePanel = new JPanel();
                updatePanel.setLayout(new BoxLayout(updatePanel, BoxLayout.Y_AXIS));

                // Update User Panel
                JPanel userUpdatePanel = new JPanel();
                userUpdatePanel.add(new JLabel("Update User:"));
                JTextField userIdField = new JTextField(5);
                JTextField userNameField = new JTextField(10);
                JTextField userEmailField = new JTextField(15);
                JTextField userPasswordField = new JTextField(10);
                JButton updateUserButton = new JButton("Update User");

                userUpdatePanel.add(new JLabel("User ID:"));
                userUpdatePanel.add(userIdField);
                userUpdatePanel.add(new JLabel("Username:"));
                userUpdatePanel.add(userNameField);
                userUpdatePanel.add(new JLabel("Email:"));
                userUpdatePanel.add(userEmailField);
                userUpdatePanel.add(new JLabel("Password:"));
                userUpdatePanel.add(userPasswordField);
                userUpdatePanel.add(updateUserButton);

                updateUserButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String userId = userIdField.getText();
                        String username = userNameField.getText();
                        String email = userEmailField.getText();
                        String password = userPasswordField.getText();
                        String updateQuery = "UPDATE users SET username = ?, email = ?, password = ? WHERE user_id = ?";
                        try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                            pstmt.setString(1, username);
                            pstmt.setString(2, email);
                            pstmt.setString(3, password);
                            pstmt.setInt(4, Integer.parseInt(userId));
                            int rowsAffected = pstmt.executeUpdate();
                            JOptionPane.showMessageDialog(frame, rowsAffected + " row(s) updated.");
                            if (usersTable != null) {
                                ((DefaultTableModel) usersTable.getModel()).setRowCount(0);
                                try (ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
                                    while (rs.next()) {
                                        Object[] row = new Object[rs.getMetaData().getColumnCount()];
                                        for (int i = 1; i <= row.length; i++) {
                                            row[i - 1] = rs.getObject(i);
                                        }
                                        ((DefaultTableModel) usersTable.getModel()).addRow(row);
                                    }
                                }
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(frame, "Error updating user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            ex.printStackTrace();
                        }
                    }
                });

                // Add User Panel
                JPanel userAddPanel = new JPanel();
                userAddPanel.add(new JLabel("Add User:"));
                JTextField addUserNameField = new JTextField(10);
                JTextField addUserEmailField = new JTextField(15);
                JTextField addUserPasswordField = new JTextField(10);
                JButton addUserButton = new JButton("Add User");

                userAddPanel.add(new JLabel("Username:"));
                userAddPanel.add(addUserNameField);
                userAddPanel.add(new JLabel("Email:"));
                userAddPanel.add(addUserEmailField);
                userAddPanel.add(new JLabel("Password:"));
                userAddPanel.add(addUserPasswordField);
                userAddPanel.add(addUserButton);

                addUserButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String username = addUserNameField.getText();
                        String email = addUserEmailField.getText();
                        String password = addUserPasswordField.getText();
                        String insertQuery = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
                        try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                            pstmt.setString(1, username);
                            pstmt.setString(2, email);
                            pstmt.setString(3, password);
                            int rowsAffected = pstmt.executeUpdate();
                            JOptionPane.showMessageDialog(frame, rowsAffected + " row(s) inserted.");
                            if (usersTable != null) {
                                ((DefaultTableModel) usersTable.getModel()).setRowCount(0);
                                try (ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
                                    while (rs.next()) {
                                        Object[] row = new Object[rs.getMetaData().getColumnCount()];
                                        for (int i = 1; i <= row.length; i++) {
                                            row[i - 1] = rs.getObject(i);
                                        }
                                        ((DefaultTableModel) usersTable.getModel()).addRow(row);
                                    }
                                }
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(frame, "Error adding user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            ex.printStackTrace();
                        }
                    }
                });

                JPanel productUpdatePanel = new JPanel();
                productUpdatePanel.add(new JLabel("Update Product:"));
                JTextField productIdField = new JTextField(5);
                JTextField productNameField = new JTextField(10);
                JTextField productPriceField = new JTextField(10);
                JButton updateProductButton = new JButton("Update Product");

                productUpdatePanel.add(new JLabel("Product ID:"));
                productUpdatePanel.add(productIdField);
                productUpdatePanel.add(new JLabel("Product Name:"));
                productUpdatePanel.add(productNameField);
                productUpdatePanel.add(new JLabel("Price:"));
                productUpdatePanel.add(productPriceField);
                productUpdatePanel.add(updateProductButton);

                updateProductButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String productId = productIdField.getText();
                        String productName = productNameField.getText();
                        String price = productPriceField.getText();
                        String updateQuery = "UPDATE products SET product_name = ?, price = ? WHERE product_id = ?";
                        try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                            pstmt.setString(1, productName);
                            pstmt.setBigDecimal(2, new BigDecimal(price));
                            pstmt.setInt(3, Integer.parseInt(productId));
                            int rowsAffected = pstmt.executeUpdate();
                            JOptionPane.showMessageDialog(frame, rowsAffected + " row(s) updated.");
                            if (productsTable != null) {
                                ((DefaultTableModel) productsTable.getModel()).setRowCount(0);
                                try (ResultSet rs = stmt.executeQuery("SELECT * FROM products")) {
                                    while (rs.next()) {
                                        Object[] row = new Object[rs.getMetaData().getColumnCount()];
                                        for (int i = 1; i <= row.length; i++) {
                                            row[i - 1] = rs.getObject(i);
                                        }
                                        ((DefaultTableModel) productsTable.getModel()).addRow(row);
                                    }
                                }
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(frame, "Error updating product: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            ex.printStackTrace();
                        }
                    }
                });

                // Add Product Panel
                JPanel productAddPanel = new JPanel();
                productAddPanel.add(new JLabel("Add Product:"));
                JTextField addProductNameField = new JTextField(10);
                JTextField addProductPriceField = new JTextField(10);
                JButton addProductButton = new JButton("Add Product");

                productAddPanel.add(new JLabel("Product Name:"));
                productAddPanel.add(addProductNameField);
                productAddPanel.add(new JLabel("Price:"));
                productAddPanel.add(addProductPriceField);
                productAddPanel.add(addProductButton);

                addProductButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String productName = addProductNameField.getText();
                        String price = addProductPriceField.getText();
                        String insertQuery = "INSERT INTO products (product_name, price) VALUES (?, ?)";
                        try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                            pstmt.setString(1, productName);
                            pstmt.setBigDecimal(2, new BigDecimal(price));
                            int rowsAffected = pstmt.executeUpdate();
                            JOptionPane.showMessageDialog(frame, rowsAffected + " row(s) inserted.");
                            if (productsTable != null) {
                                ((DefaultTableModel) productsTable.getModel()).setRowCount(0);
                                try (ResultSet rs = stmt.executeQuery("SELECT * FROM products")) {
                                    while (rs.next()) {
                                        Object[] row = new Object[rs.getMetaData().getColumnCount()];
                                        for (int i = 1; i <= row.length; i++) {
                                            row[i - 1] = rs.getObject(i);
                                        }
                                        ((DefaultTableModel) productsTable.getModel()).addRow(row);
                                    }
                                }
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(frame, "Error adding product: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            ex.printStackTrace();
                        }
                    }
                });

                JPanel orderUpdatePanel = new JPanel();
                orderUpdatePanel.add(new JLabel("Update Order:"));
                JTextField orderIdField = new JTextField(5);
                JTextField quantityField = new JTextField(5);
                JButton updateOrderButton = new JButton("Update Order");

                orderUpdatePanel.add(new JLabel("Order ID:"));
                orderUpdatePanel.add(orderIdField);
                orderUpdatePanel.add(new JLabel("Quantity:"));
                orderUpdatePanel.add(quantityField);
                orderUpdatePanel.add(updateOrderButton);

                updateOrderButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String orderId = orderIdField.getText();
                        String quantity = quantityField.getText();
                        String updateQuery = "UPDATE orders SET quantity = ? WHERE order_id = ?";
                        try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                            pstmt.setInt(1, Integer.parseInt(quantity));
                            pstmt.setInt(2, Integer.parseInt(orderId));
                            int rowsAffected = pstmt.executeUpdate();
                            JOptionPane.showMessageDialog(frame, rowsAffected + " row(s) updated.");
                            if (ordersTable != null) {
                                ((DefaultTableModel) ordersTable.getModel()).setRowCount(0); // Clear the table
                                try (ResultSet rs = stmt.executeQuery("SELECT * FROM orders")) {
                                    while (rs.next()) {
                                        Object[] row = new Object[rs.getMetaData().getColumnCount()];
                                        for (int i = 1; i <= row.length; i++) {
                                            row[i - 1] = rs.getObject(i);
                                        }
                                        ((DefaultTableModel) ordersTable.getModel()).addRow(row);
                                    }
                                }
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(frame, "Error updating order: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            ex.printStackTrace();
                        }
                    }
                });

                // Add Order Panel
                JPanel orderAddPanel = new JPanel();
                orderAddPanel.add(new JLabel("Add Order:"));
                JTextField addOrderUserIdField = new JTextField(5);
                JTextField addOrderProductIdField = new JTextField(5);
                JTextField addOrderQuantityField = new JTextField(5);
                JButton addOrderButton = new JButton("Add Order");

                orderAddPanel.add(new JLabel("User ID:"));
                orderAddPanel.add(addOrderUserIdField);
                orderAddPanel.add(new JLabel("Product ID:"));
                orderAddPanel.add(addOrderProductIdField);
                orderAddPanel.add(new JLabel("Quantity:"));
                orderAddPanel.add(addOrderQuantityField);
                orderAddPanel.add(addOrderButton);

                addOrderButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String userId = addOrderUserIdField.getText();
                        String productId = addOrderProductIdField.getText();
                        String quantity = addOrderQuantityField.getText();
                        String insertQuery = "INSERT INTO orders (user_id, product_id, quantity) VALUES (?, ?, ?)";
                        try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                            pstmt.setInt(1, Integer.parseInt(userId));
                            pstmt.setInt(2, Integer.parseInt(productId));
                            pstmt.setInt(3, Integer.parseInt(quantity));
                            int rowsAffected = pstmt.executeUpdate();
                            JOptionPane.showMessageDialog(frame, rowsAffected + " row(s) inserted.");
                            if (ordersTable != null) {
                                ((DefaultTableModel) ordersTable.getModel()).setRowCount(0);
                                try (ResultSet rs = stmt.executeQuery("SELECT * FROM orders")) {
                                    while (rs.next()) {
                                        Object[] row = new Object[rs.getMetaData().getColumnCount()];
                                        for (int i = 1; i <= row.length; i++) {
                                            row[i - 1] = rs.getObject(i);
                                        }
                                        ((DefaultTableModel) ordersTable.getModel()).addRow(row);
                                    }
                                }
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(frame, "Error adding order: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            ex.printStackTrace();
                        }
                    }
                });

                updatePanel.add(userUpdatePanel);
                updatePanel.add(userAddPanel);
                updatePanel.add(productUpdatePanel);
                updatePanel.add(productAddPanel);
                updatePanel.add(orderUpdatePanel);
                updatePanel.add(orderAddPanel);
                frame.add(updatePanel);

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

            frame.setVisible(true);
        });
    }

    private static JTable buildTable(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        String[] columnNames = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            columnNames[i - 1] = metaData.getColumnName(i);
        }

        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        while (rs.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = rs.getObject(i);
            }
            model.addRow(row);
        }

        return new JTable(model);
    }
}
