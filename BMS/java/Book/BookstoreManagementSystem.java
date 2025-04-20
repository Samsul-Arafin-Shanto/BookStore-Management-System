package Book;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import java.awt.print.PrinterJob;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class BookstoreManagementSystem {
    private static Connection connection;
    private static JFrame frame;
    private static JTabbedPane tabbedPane;

    // Main color palette - 7 colors for consistent design
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185); // Blue
    private static final Color SECONDARY_COLOR = new Color(46, 204, 113); // Green
    private static final Color ACCENT_COLOR = new Color(155, 89, 182); // Purple
    private static final Color WARNING_COLOR = new Color(241, 196, 15); // Yellow
    private static final Color ERROR_COLOR = new Color(231, 76, 60); // Red
    private static final Color INFO_COLOR = new Color(52, 152, 219); // Light Blue
    private static final Color NEUTRAL_COLOR = new Color(236, 240, 241); // Light Gray
    
    // Derived colors for UI elements
    private static final Color BACKGROUND_COLOR = new Color(252, 252, 252); // Off-white
    private static final Color TABLE_HEADER_COLOR = PRIMARY_COLOR; // Use primary color for headers
    private static final Color TABLE_ROW_ALT_COLOR = NEUTRAL_COLOR; // Use neutral color for alternating rows
    private static final Color TABLE_GRID_COLOR = new Color(230, 230, 230); // Light Gray
    private static final Color BORDER_COLOR = new Color(189, 195, 199); // Gray
    private static final Color TEXT_COLOR = Color.BLACK; // Changed to black for better readability
    private static final Color TEXT_LIGHT_COLOR = new Color(100, 100, 100); // Darker gray for secondary text
    
    // Hover and pressed states - improved for readability
    private static final Color HOVER_COLOR = new Color(41, 128, 185, 220); // Semi-transparent primary color
    private static final Color PRESSED_COLOR = new Color(44, 62, 80); // Dark Blue
    
    // Shadow colors for depth
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 30); // Semi-transparent black
    private static final Color CARD_BACKGROUND = new Color(255, 255, 255); // White for cards
    
    // Font settings
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    
    // Layout constants
    private static final int PADDING = 15;
    private static final int COMPONENT_HEIGHT = 35;
    private static final int DIALOG_MIN_WIDTH = 500;
    private static final int DIALOG_MIN_HEIGHT = 400;

    public static void main(String[] args) {
        try {
            // Set system look and feel with custom colors
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Button.background", PRIMARY_COLOR);
            UIManager.put("Button.foreground", Color.WHITE); // Keep button text white for contrast
            UIManager.put("Button.font", REGULAR_FONT);
            UIManager.put("Label.font", REGULAR_FONT);
            UIManager.put("Table.font", REGULAR_FONT);
            UIManager.put("TableHeader.font", TITLE_FONT);
            UIManager.put("TabbedPane.selected", ACCENT_COLOR);
            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("TextField.foreground", TEXT_COLOR);
            UIManager.put("TextField.font", REGULAR_FONT);
            UIManager.put("ComboBox.background", Color.WHITE);
            UIManager.put("ComboBox.foreground", TEXT_COLOR);
            UIManager.put("ComboBox.font", REGULAR_FONT);
            UIManager.put("Spinner.background", Color.WHITE);
            UIManager.put("Spinner.foreground", TEXT_COLOR);
            UIManager.put("Spinner.font", REGULAR_FONT);
            UIManager.put("Panel.background", BACKGROUND_COLOR);
            UIManager.put("TabbedPane.background", BACKGROUND_COLOR);
            UIManager.put("TabbedPane.foreground", TEXT_COLOR);
            
            // Enable antialiasing for smoother rendering
            System.setProperty("sun.java2d.opengl", "true");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize database connection
        connectToDatabase();

        // Create and set up the main window
        frame = new JFrame("Bookstore Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800); // Larger default size
        frame.setMinimumSize(new Dimension(800, 600)); // Set minimum size for responsiveness
        frame.setLocationRelativeTo(null);
        frame.setBackground(BACKGROUND_COLOR);

        // Create tabbed pane with custom styling
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(TITLE_FONT);
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        // Add tabs with icons
        tabbedPane.addTab("Books", createIcon("book.png"), createBooksPanel());
        tabbedPane.addTab("Authors", createIcon("author.png"), createAuthorsPanel());
        tabbedPane.addTab("Customers", createIcon("customer.png"), createCustomersPanel());
        tabbedPane.addTab("Orders", createIcon("order.png"), createOrdersPanel());
        tabbedPane.addTab("Reports", createIcon("report.png"), createReportsPanel());

        // Add keyboard shortcuts for tabs
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_B);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_A);
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_C);
        tabbedPane.setMnemonicAt(3, KeyEvent.VK_O);
        tabbedPane.setMnemonicAt(4, KeyEvent.VK_R);

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    private static void connectToDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:bookstore.db");
            System.out.println("Connected to SQLite database");
            
            // Enable foreign key constraints
            Statement stmt = connection.createStatement();
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.close();
            
            // Create tables if they do not exist
            createTables();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error connecting to database: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private static void createTables() {
        try {
            Statement stmt = connection.createStatement();
            // Create authors table
            stmt.execute("CREATE TABLE IF NOT EXISTS authors (author_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, birth_date TEXT)");
            // Create books table
            stmt.execute("CREATE TABLE IF NOT EXISTS books (book_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, author_id INTEGER, genre TEXT, price REAL, publication_date TEXT, FOREIGN KEY (author_id) REFERENCES authors(author_id))");
            // Create customers table
            stmt.execute("CREATE TABLE IF NOT EXISTS customers (customer_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, email TEXT, phone TEXT)");
            // Create orders table
            stmt.execute("CREATE TABLE IF NOT EXISTS orders (order_id INTEGER PRIMARY KEY AUTOINCREMENT, customer_id INTEGER, order_date TEXT, total_amount REAL, FOREIGN KEY (customer_id) REFERENCES customers(customer_id))");
            // Create order_items table
            stmt.execute("CREATE TABLE IF NOT EXISTS order_items (order_item_id INTEGER PRIMARY KEY AUTOINCREMENT, order_id INTEGER, book_id INTEGER, quantity INTEGER, unit_price REAL, FOREIGN KEY (order_id) REFERENCES orders(order_id), FOREIGN KEY (book_id) REFERENCES books(book_id))");
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error creating tables: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JPanel createBooksPanel() {
        JPanel panel = createStyledPanel();
        
        // Add a header panel with title and gradient background
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                int w = getWidth();
                int h = getHeight();
                
                // Create gradient from primary to accent color
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, w, h, ACCENT_COLOR);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 60));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, PADDING, 0));
        
        JLabel titleLabel = new JLabel("Book Management");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(Color.WHITE); // Keep header text white for contrast against gradient
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, PADDING, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        DefaultTableModel booksModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        JTable booksTable = new JTable(booksModel);
        applyTableStyle(booksTable);
        
        // Add columns
        booksModel.addColumn("ID");
        booksModel.addColumn("Title");
        booksModel.addColumn("Author");
        booksModel.addColumn("Genre");
        booksModel.addColumn("Price");
        booksModel.addColumn("Publication Date");

        // Make table responsive
        booksTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Add row selection listener for better UX
        booksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = booksTable.getSelectedRow();
                if (selectedRow >= 0) {
                    // Highlight the selected row
                    booksTable.setRowSelectionInterval(selectedRow, selectedRow);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(booksTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Create button panel with responsive layout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, PADDING, PADDING));
        buttonPanel.setBackground(Color.WHITE);

        JButton refreshButton = createStyledButton("Refresh", null);
        JButton addButton = createStyledButton("Add Book", null);
        JButton editButton = createStyledButton("Edit Book", null);
        JButton deleteButton = createStyledButton("Delete Book", null);
        
        // Add keyboard shortcuts
        refreshButton.setMnemonic(KeyEvent.VK_R);
        addButton.setMnemonic(KeyEvent.VK_A);
        editButton.setMnemonic(KeyEvent.VK_E);
        deleteButton.setMnemonic(KeyEvent.VK_D);
        
        // Add tooltips
        refreshButton.setToolTipText("Refresh book list (Ctrl+R)");
        addButton.setToolTipText("Add a new book (Ctrl+A)");
        editButton.setToolTipText("Edit selected book (Ctrl+E)");
        deleteButton.setToolTipText("Delete selected book (Ctrl+D)");
        
        // Add action listeners
        refreshButton.addActionListener(e -> refreshBooksTable(booksModel));
        addButton.addActionListener(e -> showAddBookDialog(booksModel));
        editButton.addActionListener(e -> {
            int selectedRow = booksTable.getSelectedRow();
            if (selectedRow >= 0) {
                showEditBookDialog(booksModel, selectedRow);
            } else {
                showNotification("Please select a book to edit", WARNING_COLOR);
            }
        });
        deleteButton.addActionListener(e -> {
            int selectedRow = booksTable.getSelectedRow();
            if (selectedRow >= 0) {
                int bookId = (int) booksModel.getValueAt(selectedRow, 0);
                deleteBook(bookId, booksModel);
            } else {
                showNotification("Please select a book to delete", WARNING_COLOR);
            }
        });

        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        // Add search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING, PADDING));
        searchPanel.setBackground(Color.WHITE);
        
        JTextField searchField = createStyledTextField();
        searchField.setPreferredSize(new Dimension(200, COMPONENT_HEIGHT));
        searchField.setToolTipText("Search books");
        
        JButton searchButton = createStyledButton("Search", null);
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                searchBooks(booksModel, searchText);
            } else {
                refreshBooksTable(booksModel);
            }
        });
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        // Combine button and search panels
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(buttonPanel, BorderLayout.WEST);
        bottomPanel.add(searchPanel, BorderLayout.EAST);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Load initial data
        refreshBooksTable(booksModel);

        return panel;
    }

    private static void refreshBooksTable(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            String query = "SELECT b.book_id, b.title, a.name, b.genre, b.price, b.publication_date " +
                           "FROM books b JOIN authors a ON b.author_id = a.author_id";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("book_id"));
                row.add(rs.getString("title"));
                row.add(rs.getString("name"));
                row.add(rs.getString("genre"));
                row.add(rs.getDouble("price"));
                row.add(rs.getString("publication_date"));
                model.addRow(row);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading books: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showAddBookDialog(DefaultTableModel booksModel) {
        JDialog dialog = createStyledDialog("Add New Book", 500, 400);
        
        // Main content panel with responsive layout
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(PADDING/2, PADDING/2, PADDING/2, PADDING/2);
        gbc.weightx = 1.0;

        // Create styled form fields
        JTextField titleField = createStyledTextField();
        JComboBox<String> authorCombo = createStyledComboBox();
        JTextField genreField = createStyledTextField();
        JTextField priceField = createStyledTextField();
        JTextField dateField = createStyledTextField();

        // Set date field to current date
        dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

        // Add form fields with responsive layout
        addFormField(contentPanel, "Title:", titleField, gbc, 0);
        addFormField(contentPanel, "Author:", authorCombo, gbc, 1);
        addFormField(contentPanel, "Genre:", genreField, gbc, 2);
        addFormField(contentPanel, "Price:", priceField, gbc, 3);
        addFormField(contentPanel, "Publication Date:", dateField, gbc, 4);
        
        // Load authors into combo box
        loadAuthorsIntoComboBox(authorCombo);

        dialog.add(contentPanel, BorderLayout.CENTER);

        // Button panel with responsive layout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING, PADDING));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = createStyledButton("Save", null);
        JButton cancelButton = createStyledButton("Cancel", null);
        
        // Add keyboard shortcuts
        saveButton.setMnemonic(KeyEvent.VK_S);
        cancelButton.setMnemonic(KeyEvent.VK_C);
        
        // Add tooltips
        saveButton.setToolTipText("Save book (Ctrl+S)");
        cancelButton.setToolTipText("Cancel (Ctrl+C)");

        // Add action listeners
        saveButton.addActionListener(e -> {
            try {
                // Validate inputs
                if (titleField.getText().trim().isEmpty()) {
                    showNotification("Title is required", WARNING_COLOR);
                    titleField.requestFocus();
                    return;
                }
                
                if (authorCombo.getSelectedItem() == null) {
                    showNotification("Author is required", WARNING_COLOR);
                    authorCombo.requestFocus();
                    return;
                }
                
                try {
                    Double.parseDouble(priceField.getText().trim());
                } catch (NumberFormatException ex) {
                    showNotification("Price must be a valid number", WARNING_COLOR);
                    priceField.requestFocus();
                    return;
                }
                
                // Get author ID from selected name
                int authorId = getAuthorId(authorCombo.getSelectedItem().toString());
                
                // Insert the book
                String query = "INSERT INTO books (title, author_id, genre, price, publication_date) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setString(1, titleField.getText().trim());
                pstmt.setInt(2, authorId);
                pstmt.setString(3, genreField.getText().trim());
                pstmt.setDouble(4, Double.parseDouble(priceField.getText().trim()));
                pstmt.setString(5, dateField.getText().trim());
                
                pstmt.executeUpdate();
                pstmt.close();
                
                // Refresh the table and close dialog
                refreshBooksTable(booksModel);
                dialog.dispose();
                
                // Show success notification
                showNotification("Book added successfully", SECONDARY_COLOR);
            } catch (SQLException ex) {
                showNotification("Error adding book: " + ex.getMessage(), ERROR_COLOR);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add keyboard shortcuts for the dialog
        dialog.getRootPane().registerKeyboardAction(
            e -> dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        dialog.getRootPane().registerKeyboardAction(
            e -> saveButton.doClick(),
            KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        dialog.setVisible(true);
    }

    private static void showEditBookDialog(DefaultTableModel booksModel, int selectedRow) {
        JDialog dialog = createStyledDialog("Edit Book", 500, 400);
        dialog.setLayout(new BorderLayout(PADDING, PADDING));
        
        int bookId = (int) booksModel.getValueAt(selectedRow, 0);
        String currentTitle = (String) booksModel.getValueAt(selectedRow, 1);
        String currentAuthor = (String) booksModel.getValueAt(selectedRow, 2);
        String currentGenre = (String) booksModel.getValueAt(selectedRow, 3);
        double currentPrice = (double) booksModel.getValueAt(selectedRow, 4);
        String currentDate = (String) booksModel.getValueAt(selectedRow, 5);
        
        // Main content panel with responsive layout
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(PADDING/2, PADDING/2, PADDING/2, PADDING/2);
        gbc.weightx = 1.0;
        
        // Create styled form fields with current values
        JTextField titleField = createStyledTextField();
        titleField.setText(currentTitle);
        
        JComboBox<String> authorCombo = createStyledComboBox();
        loadAuthorsIntoComboBox(authorCombo);
        authorCombo.setSelectedItem(currentAuthor);
        
        JTextField genreField = createStyledTextField();
        genreField.setText(currentGenre);
        
        JTextField priceField = createStyledTextField();
        priceField.setText(String.valueOf(currentPrice));
        
        JTextField dateField = createStyledTextField();
        dateField.setText(currentDate);

        // Add form fields with responsive layout
        addFormField(contentPanel, "Title:", titleField, gbc, 0);
        addFormField(contentPanel, "Author:", authorCombo, gbc, 1);
        addFormField(contentPanel, "Genre:", genreField, gbc, 2);
        addFormField(contentPanel, "Price:", priceField, gbc, 3);
        addFormField(contentPanel, "Publication Date:", dateField, gbc, 4);

        dialog.add(contentPanel, BorderLayout.CENTER);
        
        // Button panel with responsive layout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING, PADDING));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton saveButton = createStyledButton("Save", null);
        JButton cancelButton = createStyledButton("Cancel", null);
        
        // Add keyboard shortcuts
        saveButton.setMnemonic(KeyEvent.VK_S);
        cancelButton.setMnemonic(KeyEvent.VK_C);
        
        // Add tooltips
        saveButton.setToolTipText("Save changes (Ctrl+S)");
        cancelButton.setToolTipText("Cancel (Ctrl+C)");
        
        // Add action listeners
        saveButton.addActionListener(e -> {
            try {
                // Validate inputs
                if (titleField.getText().trim().isEmpty()) {
                    showNotification("Title is required", WARNING_COLOR);
                    titleField.requestFocus();
                    return;
                }
                
                if (authorCombo.getSelectedItem() == null) {
                    showNotification("Author is required", WARNING_COLOR);
                    authorCombo.requestFocus();
                    return;
                }
                
                try {
                    Double.parseDouble(priceField.getText().trim());
                } catch (NumberFormatException ex) {
                    showNotification("Price must be a valid number", WARNING_COLOR);
                    priceField.requestFocus();
                    return;
                }
                
                // Get author ID from selected name
                int authorId = getAuthorId(authorCombo.getSelectedItem().toString());
                
                // Update the book
                String query = "UPDATE books SET title=?, author_id=?, genre=?, price=?, publication_date=? WHERE book_id=?";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setString(1, titleField.getText().trim());
                pstmt.setInt(2, authorId);
                pstmt.setString(3, genreField.getText().trim());
                pstmt.setDouble(4, Double.parseDouble(priceField.getText().trim()));
                pstmt.setString(5, dateField.getText().trim());
                pstmt.setInt(6, bookId);
                
                pstmt.executeUpdate();
                pstmt.close();
                
                // Refresh the table and close dialog
                refreshBooksTable(booksModel);
                dialog.dispose();
                
                // Show success notification
                showNotification("Book updated successfully", SECONDARY_COLOR);
            } catch (SQLException ex) {
                showNotification("Error updating book: " + ex.getMessage(), ERROR_COLOR);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add keyboard shortcuts for the dialog
        dialog.getRootPane().registerKeyboardAction(
            e -> dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        dialog.getRootPane().registerKeyboardAction(
            e -> saveButton.doClick(),
            KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        dialog.setVisible(true);
    }

    private static void deleteBook(int bookId, DefaultTableModel booksModel) {
        int confirm = showConfirmDialog("Are you sure you want to delete this book?", "Confirm Deletion");
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String query = "DELETE FROM books WHERE book_id=?";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setInt(1, bookId);
                
                int rowsAffected = pstmt.executeUpdate();
                pstmt.close();
                
                if (rowsAffected > 0) {
                    showNotification("Book deleted successfully", SECONDARY_COLOR);
                    refreshBooksTable(booksModel);
                }
            } catch (SQLException e) {
                showNotification("Error deleting book: " + e.getMessage(), ERROR_COLOR);
            }
        }
    }

    private static JPanel createAuthorsPanel() {
        JPanel panel = createStyledPanel();
        
        // Add a header panel with title and gradient background
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                int w = getWidth();
                int h = getHeight();
                
                // Create gradient from primary to accent color
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, w, h, ACCENT_COLOR);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 60));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, PADDING, 0));
        
        JLabel titleLabel = new JLabel("Author Management");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(Color.WHITE); // Keep header text white for contrast against gradient
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, PADDING, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Table to display authors
        DefaultTableModel authorsModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        JTable authorsTable = new JTable(authorsModel);
        authorsModel.addColumn("ID");
        authorsModel.addColumn("Name");
        authorsModel.addColumn("Birth Date");
        
        applyTableStyle(authorsTable);
        
        JScrollPane scrollPane = new JScrollPane(authorsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Create a bottom panel with consistent styling
        JPanel bottomPanel = createBottomPanel(authorsModel, authorsTable, "author");
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Load initial data
        refreshAuthorsTable(authorsModel);
        
        return panel;
    }

    private static void refreshAuthorsTable(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            String query = "SELECT * FROM authors";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("author_id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("birth_date"));
                model.addRow(row);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading authors: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showAddAuthorDialog(DefaultTableModel authorsModel) {
        JDialog dialog = createStyledDialog("Add New Author", 500, 400);
        dialog.setLayout(new BorderLayout(PADDING, PADDING));
        
        // Main content panel with responsive layout
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(PADDING/2, PADDING/2, PADDING/2, PADDING/2);
        gbc.weightx = 1.0;

        // Create styled form fields
        JTextField nameField = createStyledTextField();
        JTextField birthDateField = createStyledTextField();

        // Add form fields with responsive layout
        addFormField(contentPanel, "Name:", nameField, gbc, 0);
        addFormField(contentPanel, "Birth Date (YYYY-MM-DD):", birthDateField, gbc, 1);

        dialog.add(contentPanel, BorderLayout.CENTER);

        // Button panel with responsive layout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING, PADDING));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = createStyledButton("Save", null);
        JButton cancelButton = createStyledButton("Cancel", null);
        
        // Add keyboard shortcuts
        saveButton.setMnemonic(KeyEvent.VK_S);
        cancelButton.setMnemonic(KeyEvent.VK_C);
        
        // Add tooltips
        saveButton.setToolTipText("Save author (Ctrl+S)");
        cancelButton.setToolTipText("Cancel (Ctrl+C)");
        
        // Add action listeners
        saveButton.addActionListener(e -> {
            try {
                // Validate inputs
                if (nameField.getText().trim().isEmpty()) {
                    showNotification("Name is required", WARNING_COLOR);
                    nameField.requestFocus();
                    return;
                }
                
                // Validate date format if provided
                if (!birthDateField.getText().trim().isEmpty()) {
                    try {
                        new SimpleDateFormat("yyyy-MM-dd").parse(birthDateField.getText().trim());
                    } catch (Exception ex) {
                        showNotification("Invalid date format. Please use YYYY-MM-DD", WARNING_COLOR);
                        birthDateField.requestFocus();
                        return;
                    }
                }
                
                // Insert the author
                String query = "INSERT INTO authors (name, birth_date) VALUES (?, ?)";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setString(1, nameField.getText().trim());
                pstmt.setString(2, birthDateField.getText().trim());
                
                pstmt.executeUpdate();
                pstmt.close();
                
                refreshAuthorsTable(authorsModel);
                dialog.dispose();
                
                showNotification("Author added successfully", SECONDARY_COLOR);
            } catch (SQLException ex) {
                showNotification("Error adding author: " + ex.getMessage(), ERROR_COLOR);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add keyboard shortcuts for the dialog
        dialog.getRootPane().registerKeyboardAction(
            e -> dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        dialog.getRootPane().registerKeyboardAction(
            e -> saveButton.doClick(),
            KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        dialog.setVisible(true);
    }

    private static void showEditAuthorDialog(DefaultTableModel authorsModel, int selectedRow) {
        JDialog dialog = createStyledDialog("Edit Author", 500, 400);
        dialog.setLayout(new BorderLayout(PADDING, PADDING));
        
        int authorId = (int) authorsModel.getValueAt(selectedRow, 0);
        String currentName = (String) authorsModel.getValueAt(selectedRow, 1);
        String currentBirthDate = (String) authorsModel.getValueAt(selectedRow, 2);
        
        // Main content panel with responsive layout
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(PADDING/2, PADDING/2, PADDING/2, PADDING/2);
        gbc.weightx = 1.0;
        
        // Create styled form fields with current values
        JTextField nameField = createStyledTextField();
        nameField.setText(currentName);
        
        JTextField birthDateField = createStyledTextField();
        birthDateField.setText(currentBirthDate);

        // Add form fields with responsive layout
        addFormField(contentPanel, "Name:", nameField, gbc, 0);
        addFormField(contentPanel, "Birth Date (YYYY-MM-DD):", birthDateField, gbc, 1);

        dialog.add(contentPanel, BorderLayout.CENTER);
        
        // Button panel with responsive layout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING, PADDING));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton saveButton = createStyledButton("Save", null);
        JButton cancelButton = createStyledButton("Cancel", null);
        
        // Add keyboard shortcuts
        saveButton.setMnemonic(KeyEvent.VK_S);
        cancelButton.setMnemonic(KeyEvent.VK_C);
        
        // Add tooltips
        saveButton.setToolTipText("Save changes (Ctrl+S)");
        cancelButton.setToolTipText("Cancel (Ctrl+C)");
        
        // Add action listeners
        saveButton.addActionListener(e -> {
            try {
                // Validate inputs
                if (nameField.getText().trim().isEmpty()) {
                    showNotification("Name is required", WARNING_COLOR);
                    nameField.requestFocus();
                    return;
                }
                
                // Validate date format if provided
                if (!birthDateField.getText().trim().isEmpty()) {
                    try {
                        new SimpleDateFormat("yyyy-MM-dd").parse(birthDateField.getText().trim());
                    } catch (Exception ex) {
                        showNotification("Invalid date format. Please use YYYY-MM-DD", WARNING_COLOR);
                        birthDateField.requestFocus();
                        return;
                    }
                }
                
                // Update the author
                String query = "UPDATE authors SET name=?, birth_date=? WHERE author_id=?";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setString(1, nameField.getText().trim());
                pstmt.setString(2, birthDateField.getText().trim());
                pstmt.setInt(3, authorId);
                
                pstmt.executeUpdate();
                pstmt.close();
                
                refreshAuthorsTable(authorsModel);
                dialog.dispose();
                
                showNotification("Author updated successfully", SECONDARY_COLOR);
            } catch (SQLException ex) {
                showNotification("Error updating author: " + ex.getMessage(), ERROR_COLOR);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add keyboard shortcuts for the dialog
        dialog.getRootPane().registerKeyboardAction(
            e -> dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        dialog.getRootPane().registerKeyboardAction(
            e -> saveButton.doClick(),
            KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        dialog.setVisible(true);
    }

    private static void deleteAuthor(int authorId, DefaultTableModel authorsModel) {
        int confirm = showConfirmDialog("Are you sure you want to delete this author?", "Confirm Deletion");
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String query = "DELETE FROM authors WHERE author_id=?";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setInt(1, authorId);
                
                int rowsAffected = pstmt.executeUpdate();
                pstmt.close();
                
                if (rowsAffected > 0) {
                    showNotification("Author deleted successfully", SECONDARY_COLOR);
                    refreshAuthorsTable(authorsModel);
                }
            } catch (SQLException e) {
                showNotification("Error deleting author: " + e.getMessage(), ERROR_COLOR);
            }
        }
    }

    private static JPanel createCustomersPanel() {
        JPanel panel = createStyledPanel();
        
        // Add a header panel with title and gradient background
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                int w = getWidth();
                int h = getHeight();
                
                // Create gradient from primary to accent color
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, w, h, ACCENT_COLOR);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 60));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, PADDING, 0));
        
        JLabel titleLabel = new JLabel("Customer Management");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(Color.WHITE); // Keep header text white for contrast against gradient
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, PADDING, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Table to display customers
        DefaultTableModel customersModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        JTable customersTable = new JTable(customersModel);
        customersModel.addColumn("ID");
        customersModel.addColumn("Name");
        customersModel.addColumn("Email");
        customersModel.addColumn("Phone");
        
        applyTableStyle(customersTable);
        
        JScrollPane scrollPane = new JScrollPane(customersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Create a bottom panel with consistent styling
        JPanel bottomPanel = createBottomPanel(customersModel, customersTable, "customer");
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Load initial data
        refreshCustomersTable(customersModel);
        
        return panel;
    }

    private static void refreshCustomersTable(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            String query = "SELECT * FROM customers";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("customer_id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("email"));
                row.add(rs.getString("phone"));
                model.addRow(row);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading customers: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showAddCustomerDialog(DefaultTableModel customersModel) {
        JDialog dialog = createStyledDialog("Add New Customer", 500, 400);
        dialog.setLayout(new BorderLayout(PADDING, PADDING));
        
        // Main content panel with responsive layout
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(PADDING/2, PADDING/2, PADDING/2, PADDING/2);
        gbc.weightx = 1.0;

        // Create styled form fields
        JTextField nameField = createStyledTextField();
        JTextField emailField = createStyledTextField();
        JTextField phoneField = createStyledTextField();

        // Add form fields with responsive layout
        addFormField(contentPanel, "Name:", nameField, gbc, 0);
        addFormField(contentPanel, "Email:", emailField, gbc, 1);
        addFormField(contentPanel, "Phone:", phoneField, gbc, 2);

        dialog.add(contentPanel, BorderLayout.CENTER);

        // Button panel with responsive layout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING, PADDING));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = createStyledButton("Save", null);
        JButton cancelButton = createStyledButton("Cancel", null);
        
        // Add keyboard shortcuts
        saveButton.setMnemonic(KeyEvent.VK_S);
        cancelButton.setMnemonic(KeyEvent.VK_C);
        
        // Add tooltips
        saveButton.setToolTipText("Save customer (Ctrl+S)");
        cancelButton.setToolTipText("Cancel (Ctrl+C)");
        
        // Add action listeners
        saveButton.addActionListener(e -> {
            try {
                String query = "INSERT INTO customers (name, email, phone) VALUES (?, ?, ?)";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setString(1, nameField.getText().trim());
                pstmt.setString(2, emailField.getText().trim());
                pstmt.setString(3, phoneField.getText().trim());
                
                pstmt.executeUpdate();
                pstmt.close();
                
                refreshCustomersTable(customersModel);
                dialog.dispose();
                
                // Show success notification
                showNotification("Customer added successfully", SECONDARY_COLOR);
            } catch (SQLException ex) {
                showNotification("Error adding customer: " + ex.getMessage(), ERROR_COLOR);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add keyboard shortcuts for the dialog
        dialog.getRootPane().registerKeyboardAction(
            e -> dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        dialog.getRootPane().registerKeyboardAction(
            e -> saveButton.doClick(),
            KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        dialog.setVisible(true);
    }

    private static void showEditCustomerDialog(DefaultTableModel customersModel, int selectedRow) {
        JDialog dialog = createStyledDialog("Edit Customer", 500, 400);
        dialog.setLayout(new BorderLayout(PADDING, PADDING));
        
        int customerId = (int) customersModel.getValueAt(selectedRow, 0);
        String currentName = (String) customersModel.getValueAt(selectedRow, 1);
        String currentEmail = (String) customersModel.getValueAt(selectedRow, 2);
        String currentPhone = (String) customersModel.getValueAt(selectedRow, 3);
        
        // Main content panel with responsive layout
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(PADDING/2, PADDING/2, PADDING/2, PADDING/2);
        gbc.weightx = 1.0;
        
        // Create styled form fields with current values
        JTextField nameField = createStyledTextField();
        nameField.setText(currentName);
        
        JTextField emailField = createStyledTextField();
        emailField.setText(currentEmail);
        
        JTextField phoneField = createStyledTextField();
        phoneField.setText(currentPhone);

        // Add form fields with responsive layout
        addFormField(contentPanel, "Name:", nameField, gbc, 0);
        addFormField(contentPanel, "Email:", emailField, gbc, 1);
        addFormField(contentPanel, "Phone:", phoneField, gbc, 2);

        dialog.add(contentPanel, BorderLayout.CENTER);
        
        // Button panel with responsive layout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING, PADDING));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton saveButton = createStyledButton("Save", null);
        JButton cancelButton = createStyledButton("Cancel", null);
        
        // Add keyboard shortcuts
        saveButton.setMnemonic(KeyEvent.VK_S);
        cancelButton.setMnemonic(KeyEvent.VK_C);
        
        // Add tooltips
        saveButton.setToolTipText("Save changes (Ctrl+S)");
        cancelButton.setToolTipText("Cancel (Ctrl+C)");
        
        // Add action listeners
        saveButton.addActionListener(e -> {
            try {
                String query = "UPDATE customers SET name=?, email=?, phone=? WHERE customer_id=?";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setString(1, nameField.getText().trim());
                pstmt.setString(2, emailField.getText().trim());
                pstmt.setString(3, phoneField.getText().trim());
                pstmt.setInt(4, customerId);
                
                pstmt.executeUpdate();
                pstmt.close();
                
                refreshCustomersTable(customersModel);
                dialog.dispose();
                
                // Show success notification
                showNotification("Customer updated successfully", SECONDARY_COLOR);
            } catch (SQLException ex) {
                showNotification("Error updating customer: " + ex.getMessage(), ERROR_COLOR);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add keyboard shortcuts for the dialog
        dialog.getRootPane().registerKeyboardAction(
            e -> dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        dialog.getRootPane().registerKeyboardAction(
            e -> saveButton.doClick(),
            KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        dialog.setVisible(true);
    }

    private static void deleteCustomer(int customerId, DefaultTableModel customersModel) {
        int confirm = showConfirmDialog("Are you sure you want to delete this customer?", "Confirm Deletion");
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Start transaction
                connection.setAutoCommit(false);
                
                // First delete order items for all orders of this customer
                String deleteOrderItemsQuery = "DELETE FROM order_items WHERE order_id IN (SELECT order_id FROM orders WHERE customer_id=?)";
                PreparedStatement deleteOrderItemsStmt = connection.prepareStatement(deleteOrderItemsQuery);
                deleteOrderItemsStmt.setInt(1, customerId);
                deleteOrderItemsStmt.executeUpdate();
                deleteOrderItemsStmt.close();
                
                // Then delete orders for this customer
                String deleteOrdersQuery = "DELETE FROM orders WHERE customer_id=?";
                PreparedStatement deleteOrdersStmt = connection.prepareStatement(deleteOrdersQuery);
                deleteOrdersStmt.setInt(1, customerId);
                deleteOrdersStmt.executeUpdate();
                deleteOrdersStmt.close();
                
                // Finally delete the customer
                String deleteCustomerQuery = "DELETE FROM customers WHERE customer_id=?";
                PreparedStatement deleteCustomerStmt = connection.prepareStatement(deleteCustomerQuery);
                deleteCustomerStmt.setInt(1, customerId);
                
                int rowsAffected = deleteCustomerStmt.executeUpdate();
                deleteCustomerStmt.close();
                
                // Commit the transaction
                connection.commit();
                
                if (rowsAffected > 0) {
                    showNotification("Customer deleted successfully", SECONDARY_COLOR);
                    refreshCustomersTable(customersModel);
                }
            } catch (SQLException e) {
                try {
                    // Rollback the transaction in case of error
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    showNotification("Error rolling back transaction: " + rollbackEx.getMessage(), ERROR_COLOR);
                }
                showNotification("Error deleting customer: " + e.getMessage(), ERROR_COLOR);
            } finally {
                try {
                    // Reset auto-commit
                    connection.setAutoCommit(true);
                } catch (SQLException autoCommitEx) {
                    showNotification("Error resetting auto-commit: " + autoCommitEx.getMessage(), ERROR_COLOR);
                }
            }
        }
    }

    private static JPanel createOrdersPanel() {
        JPanel panel = createStyledPanel();
        
        // Add a header panel with title and gradient background
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                int w = getWidth();
                int h = getHeight();
                
                // Create gradient from primary to accent color
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, w, h, ACCENT_COLOR);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 60));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, PADDING, 0));
        
        JLabel titleLabel = new JLabel("Order Management");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(Color.WHITE); // Keep header text white for contrast against gradient
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, PADDING, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Table to display orders
        DefaultTableModel ordersModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        JTable ordersTable = new JTable(ordersModel);
        ordersModel.addColumn("Order ID");
        ordersModel.addColumn("Customer");
        ordersModel.addColumn("Order Date");
        ordersModel.addColumn("Total Amount");
        
        applyTableStyle(ordersTable);
        
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Create a bottom panel with consistent styling
        JPanel bottomPanel = createBottomPanel(ordersModel, ordersTable, "order");
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Load initial data
        refreshOrdersTable(ordersModel);
        
        return panel;
    }

    private static void refreshOrdersTable(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            String query = "SELECT o.order_id, c.name, o.order_date, o.total_amount " +
                         "FROM orders o JOIN customers c ON o.customer_id = c.customer_id";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("order_id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("order_date"));
                row.add(rs.getDouble("total_amount"));
                model.addRow(row);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading orders: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showAddOrderDialog(DefaultTableModel ordersModel) {
        JDialog dialog = createStyledDialog("Add New Order", 700, 600);
        dialog.setLayout(new BorderLayout(PADDING, PADDING));

        // Customer selection panel
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(PADDING/2, PADDING/2, PADDING/2, PADDING/2);
        gbc.weightx = 1.0;
        
        JLabel customerLabel = new JLabel("Customer:");
        customerLabel.setFont(REGULAR_FONT);
        
        // Create an editable combo box for customer selection with search functionality
        JComboBox<String> customerCombo = new JComboBox<>();
        customerCombo.setEditable(true);
        customerCombo.setFont(REGULAR_FONT);
        customerCombo.setPreferredSize(new Dimension(200, COMPONENT_HEIGHT));
        customerCombo.setBackground(Color.WHITE);
        customerCombo.setForeground(TEXT_COLOR);
        
        // Add focus listener for better UX
        customerCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                customerCombo.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 2));
            }
            
            public void focusLost(java.awt.event.FocusEvent evt) {
                customerCombo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
            }
        });
        
        // Load customers into combo box
        loadCustomersIntoComboBox(customerCombo);
        
        // Add document listener for search functionality
        JTextField editor = (JTextField) customerCombo.getEditor().getEditorComponent();
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String searchText = editor.getText().toLowerCase();
                customerCombo.removeAllItems();
                
                try {
                    String query = "SELECT name FROM customers WHERE LOWER(name) LIKE ?";
                    PreparedStatement pstmt = connection.prepareStatement(query);
                    pstmt.setString(1, "%" + searchText + "%");
                    ResultSet rs = pstmt.executeQuery();
                    
                    while (rs.next()) {
                        customerCombo.addItem(rs.getString("name"));
                    }
                    
                    rs.close();
                    pstmt.close();
                    
                    // Show dropdown when typing
                    if (!searchText.isEmpty()) {
                        customerCombo.showPopup();
                    }
                } catch (SQLException ex) {
                    showNotification("Error searching customers: " + ex.getMessage(), ERROR_COLOR);
                }
            }
        });
        
        JLabel dateLabel = new JLabel("Order Date:");
        dateLabel.setFont(REGULAR_FONT);
        JTextField dateField = createStyledTextField();
        dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        
        addFormField(topPanel, "Customer:", customerCombo, gbc, 0);
        addFormField(topPanel, "Order Date:", dateField, gbc, 1);
        
        dialog.add(topPanel, BorderLayout.NORTH);

        // Order items table
        DefaultTableModel itemsModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only allow editing quantity
            }
        };
        JTable itemsTable = new JTable(itemsModel);
        itemsModel.addColumn("Book");
        itemsModel.addColumn("Quantity");
        itemsModel.addColumn("Unit Price");
        itemsModel.addColumn("Subtotal");
        
        applyTableStyle(itemsTable);
        
        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, PADDING, 0, PADDING));
        dialog.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel for buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        
        // Item manipulation buttons
        JPanel itemButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, PADDING, 0));
        itemButtonsPanel.setBackground(Color.WHITE);
        
        JButton addItemButton = createStyledButton("Add Book", null);
        JButton removeItemButton = createStyledButton("Remove Book", null);
        
        addItemButton.addActionListener(e -> {
            showAddOrderItemDialog(itemsModel);
            updateTotalAmount(itemsModel, dialog);
        });
        
        removeItemButton.addActionListener(e -> {
            int selectedRow = itemsTable.getSelectedRow();
            if (selectedRow >= 0) {
                itemsModel.removeRow(selectedRow);
                updateTotalAmount(itemsModel, dialog);
            }
        });
        
        itemButtonsPanel.add(addItemButton);
        itemButtonsPanel.add(removeItemButton);
        bottomPanel.add(itemButtonsPanel, BorderLayout.WEST);

        // Save/Cancel buttons
        JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING, 0));
        savePanel.setBackground(Color.WHITE);
        
        JLabel totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(REGULAR_FONT);
        savePanel.add(totalLabel);
        
        JButton saveButton = createStyledButton("Save Order", null);
        JButton cancelButton = createStyledButton("Cancel", null);
        
        // Add keyboard shortcuts
        saveButton.setMnemonic(KeyEvent.VK_S);
        cancelButton.setMnemonic(KeyEvent.VK_C);
        
        // Add tooltips
        saveButton.setToolTipText("Save order (Ctrl+S)");
        cancelButton.setToolTipText("Cancel (Ctrl+C)");
        
        saveButton.addActionListener(e -> {
            try {
                if (itemsModel.getRowCount() == 0) {
                    showNotification("Please add at least one book to the order", WARNING_COLOR);
                    return;
                }

                int customerId = getCustomerId(customerCombo.getSelectedItem().toString());
                double totalAmount = calculateTotalAmount(itemsModel);

                // Save the order
                String orderQuery = "INSERT INTO orders (customer_id, order_date, total_amount) VALUES (?, ?, ?)";
                PreparedStatement orderStmt = connection.prepareStatement(orderQuery, 
                    Statement.RETURN_GENERATED_KEYS);
                orderStmt.setInt(1, customerId);
                orderStmt.setString(2, dateField.getText());
                orderStmt.setDouble(3, totalAmount);
                
                orderStmt.executeUpdate();
                
                ResultSet generatedKeys = orderStmt.getGeneratedKeys();
                int orderId = -1;
                if (generatedKeys.next()) {
                    orderId = generatedKeys.getInt(1);
                }
                
                // Save order items
                insertOrderItems(itemsModel, orderId);
                
                refreshOrdersTable(ordersModel);
                dialog.dispose();
                
                showNotification("Order saved successfully", SECONDARY_COLOR);
                
            } catch (SQLException ex) {
                showNotification("Error saving order: " + ex.getMessage(), ERROR_COLOR);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        savePanel.add(saveButton);
        savePanel.add(cancelButton);
        bottomPanel.add(savePanel, BorderLayout.EAST);
        
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        
        // Add keyboard shortcuts for the dialog
        dialog.getRootPane().registerKeyboardAction(
            e -> dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        dialog.getRootPane().registerKeyboardAction(
            e -> saveButton.doClick(),
            KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        dialog.setVisible(true);
    }

    private static void showAddOrderItemDialog(DefaultTableModel itemsModel) {
        JDialog dialog = createStyledDialog("Add Book to Order", 500, 400);
        dialog.setLayout(new BorderLayout(PADDING, PADDING));
        
        // Main content panel with responsive layout
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(PADDING/2, PADDING/2, PADDING/2, PADDING/2);
        gbc.weightx = 1.0;
        
        JLabel bookLabel = new JLabel("Book:");
        bookLabel.setFont(REGULAR_FONT);
        JComboBox<String> bookCombo = createStyledComboBox();
        loadBooksIntoComboBox(bookCombo);
        
        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setFont(REGULAR_FONT);
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        quantitySpinner.setFont(REGULAR_FONT);
        
        addFormField(contentPanel, "Book:", bookCombo, gbc, 0);
        addFormField(contentPanel, "Quantity:", quantitySpinner, gbc, 1);
        
        dialog.add(contentPanel, BorderLayout.CENTER);
        
        // Button panel with responsive layout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING, PADDING));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton addButton = createStyledButton("Add", null);
        JButton cancelButton = createStyledButton("Cancel", null);
        
        // Add keyboard shortcuts
        addButton.setMnemonic(KeyEvent.VK_A);
        cancelButton.setMnemonic(KeyEvent.VK_C);
        
        // Add tooltips
        addButton.setToolTipText("Add book to order (Ctrl+A)");
        cancelButton.setToolTipText("Cancel (Ctrl+C)");
        
        addButton.addActionListener(e -> {
            String bookTitle = (String) bookCombo.getSelectedItem();
            int quantity = (int) quantitySpinner.getValue();
            double unitPrice = getBookPrice(bookTitle);
            double subtotal = quantity * unitPrice;
            
            Vector<Object> row = new Vector<>();
            row.add(bookTitle);
            row.add(quantity);
            row.add(unitPrice);
            row.add(subtotal);
            
            itemsModel.addRow(row);
            dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add keyboard shortcuts for the dialog
        dialog.getRootPane().registerKeyboardAction(
            e -> dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        dialog.getRootPane().registerKeyboardAction(
            e -> addButton.doClick(),
            KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        dialog.setVisible(true);
    }

    private static void showOrderDetailsDialog(int orderId) {
        JDialog dialog = createStyledDialog("Order Details", 700, 600);
        dialog.setLayout(new BorderLayout(PADDING, PADDING));
        
        // Order information
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        
        // Create editable fields
        JTextField customerField = createStyledTextField();
        JTextField dateField = createStyledTextField();
        JTextField totalField = createStyledTextField();
        
        try {
            String query = "SELECT o.order_id, c.name, o.order_date, o.total_amount " +
                         "FROM orders o JOIN customers c ON o.customer_id = c.customer_id " +
                         "WHERE o.order_id=?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, orderId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                customerField.setText(rs.getString("name"));
                dateField.setText(rs.getString("order_date"));
                totalField.setText(String.format("%.2f", rs.getDouble("total_amount")));
                totalField.setEditable(false); // Total amount should not be editable directly
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            showNotification("Error loading order details: " + e.getMessage(), ERROR_COLOR);
        }
        
        // Add form fields with labels
        addFormField(infoPanel, "Order ID:", new JLabel(String.valueOf(orderId)), new GridBagConstraints(), 0);
        addFormField(infoPanel, "Customer:", customerField, new GridBagConstraints(), 1);
        addFormField(infoPanel, "Order Date:", dateField, new GridBagConstraints(), 2);
        addFormField(infoPanel, "Total Amount:", totalField, new GridBagConstraints(), 3);
        
        dialog.add(infoPanel, BorderLayout.NORTH);
        
        // Order items table
        DefaultTableModel itemsModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only allow editing quantity
            }
            
            @Override
            public void setValueAt(Object value, int row, int column) {
                if (column == 1) { // Quantity column
                    try {
                        int newQuantity = Integer.parseInt(value.toString());
                        if (newQuantity > 0) {
                            super.setValueAt(newQuantity, row, column);
                            // Update subtotal
                            double unitPrice = (double) getValueAt(row, 2);
                            setValueAt(newQuantity * unitPrice, row, 3);
                            updateOrderTotal(this, totalField);
                        }
                    } catch (NumberFormatException e) {
                        showNotification("Please enter a valid quantity", WARNING_COLOR);
                    }
                }
            }
        };
        
        JTable itemsTable = new JTable(itemsModel);
        itemsModel.addColumn("Book");
        itemsModel.addColumn("Quantity");
        itemsModel.addColumn("Unit Price");
        itemsModel.addColumn("Subtotal");
        
        applyTableStyle(itemsTable);
        
        try {
            String query = "SELECT b.title, oi.quantity, oi.unit_price " +
                          "FROM order_items oi JOIN books b ON oi.book_id = b.book_id " +
                          "WHERE oi.order_id=?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, orderId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("title"));
                row.add(rs.getInt("quantity"));
                row.add(rs.getDouble("unit_price"));
                row.add(rs.getInt("quantity") * rs.getDouble("unit_price"));
                itemsModel.addRow(row);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            showNotification("Error loading order items: " + e.getMessage(), ERROR_COLOR);
        }
        
        JScrollPane itemsScrollPane = new JScrollPane(itemsTable);
        itemsScrollPane.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR));
        dialog.add(itemsScrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING, PADDING));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton addItemButton = createStyledButton("Add Book", null);
        JButton removeItemButton = createStyledButton("Remove Book", null);
        JButton saveButton = createStyledButton("Save Changes", null);
        JButton closeButton = createStyledButton("Close", null);
        
        addItemButton.addActionListener(e -> {
            showAddOrderItemDialog(itemsModel);
            updateOrderTotal(itemsModel, totalField);
        });
        
        removeItemButton.addActionListener(e -> {
            int selectedRow = itemsTable.getSelectedRow();
            if (selectedRow >= 0) {
                itemsModel.removeRow(selectedRow);
                updateOrderTotal(itemsModel, totalField);
            } else {
                showNotification("Please select a book to remove", WARNING_COLOR);
            }
        });
        
        saveButton.addActionListener(e -> {
            try {
                // Update order details
                String updateOrderQuery = "UPDATE orders SET order_date = ?, total_amount = ? WHERE order_id = ?";
                PreparedStatement orderStmt = connection.prepareStatement(updateOrderQuery);
                orderStmt.setString(1, dateField.getText());
                orderStmt.setDouble(2, Double.parseDouble(totalField.getText()));
                orderStmt.setInt(3, orderId);
                orderStmt.executeUpdate();
                orderStmt.close();
                
                // Delete existing order items
                String deleteItemsQuery = "DELETE FROM order_items WHERE order_id = ?";
                PreparedStatement deleteStmt = connection.prepareStatement(deleteItemsQuery);
                deleteStmt.setInt(1, orderId);
                deleteStmt.executeUpdate();
                deleteStmt.close();
                
                // Insert updated order items
                insertOrderItems(itemsModel, orderId);
                
                dialog.dispose();
                showNotification("Order updated successfully", SECONDARY_COLOR);
                
                // Refresh the orders table
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    if (tabbedPane.getTitleAt(i).equals("Orders")) {
                        Component comp = tabbedPane.getComponentAt(i);
                        if (comp instanceof JPanel) {
                            JPanel panel = (JPanel) comp;
                            for (Component innerComp : panel.getComponents()) {
                                if (innerComp instanceof JScrollPane) {
                                    JScrollPane scrollPane = (JScrollPane) innerComp;
                                    JTable table = (JTable) scrollPane.getViewport().getView();
                                    if (table != null) {
                                        DefaultTableModel model = (DefaultTableModel) table.getModel();
                                        refreshOrdersTable(model);
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
                
            } catch (SQLException ex) {
                showNotification("Error updating order: " + ex.getMessage(), ERROR_COLOR);
            }
        });
        
        closeButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(addItemButton);
        buttonPanel.add(removeItemButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);
        
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private static void updateOrderTotal(DefaultTableModel itemsModel, JTextField totalField) {
        double total = 0;
        for (int i = 0; i < itemsModel.getRowCount(); i++) {
            total += (double) itemsModel.getValueAt(i, 3); // Subtotal column
        }
        totalField.setText(String.format("%.2f", total));
    }

    private static void deleteOrder(int orderId, DefaultTableModel ordersModel) {
        int confirm = showConfirmDialog("Are you sure you want to delete this order?", "Confirm Deletion");
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // First delete order items (due to foreign key constraint)
                String deleteItemsQuery = "DELETE FROM order_items WHERE order_id=?";
                PreparedStatement deleteItemsStmt = connection.prepareStatement(deleteItemsQuery);
                deleteItemsStmt.setInt(1, orderId);
                deleteItemsStmt.executeUpdate();
                deleteItemsStmt.close();
                
                // Then delete the order
                String deleteOrderQuery = "DELETE FROM orders WHERE order_id=?";
                PreparedStatement deleteOrderStmt = connection.prepareStatement(deleteOrderQuery);
                deleteOrderStmt.setInt(1, orderId);
                
                int rowsAffected = deleteOrderStmt.executeUpdate();
                deleteOrderStmt.close();
                
                if (rowsAffected > 0) {
                    showNotification("Order deleted successfully", SECONDARY_COLOR);
                    refreshOrdersTable(ordersModel);
                }
            } catch (SQLException e) {
                showNotification("Error deleting order: " + e.getMessage(), ERROR_COLOR);
            }
        }
    }

    private static JPanel createReportsPanel() {
        JPanel panel = createStyledPanel();
        
        // Add a header panel with title and gradient background
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                int w = getWidth();
                int h = getHeight();
                
                // Create gradient from primary to accent color
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, w, h, ACCENT_COLOR);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 60));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, PADDING, 0));
        
        JLabel titleLabel = new JLabel("Reports");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, PADDING, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Report selection
        JPanel reportSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, PADDING, PADDING));
        reportSelectionPanel.setBackground(Color.WHITE);
        
        JLabel reportLabel = new JLabel("Select Report:");
        reportLabel.setFont(REGULAR_FONT);
        
        JComboBox<String> reportCombo = createStyledComboBox();
        reportCombo.setModel(new DefaultComboBoxModel<>(new String[] {
            "Sales by Genre",
            "Top Selling Books",
            "Customer Spending"
        }));
        
        // Create the report table model
        final DefaultTableModel reportModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable reportTable = new JTable(reportModel);
        applyTableStyle(reportTable);
        
        JButton generateButton = createStyledButton("Generate", null);
        generateButton.addActionListener(e -> {
            String selectedReport = (String) reportCombo.getSelectedItem();
            reportModel.setRowCount(0);
            reportModel.setColumnCount(0);
            
            try {
                switch (selectedReport) {
                    case "Sales by Genre":
                        generateSalesByGenreReport(reportModel);
                        break;
                    case "Top Selling Books":
                        generateTopSellingBooksReport(reportModel);
                        break;
                    case "Customer Spending":
                        generateCustomerSpendingReport(reportModel);
                        break;
                }
            } catch (SQLException ex) {
                showNotification("Error generating report: " + ex.getMessage(), ERROR_COLOR);
            }
        });
        
        reportSelectionPanel.add(reportLabel);
        reportSelectionPanel.add(reportCombo);
        reportSelectionPanel.add(generateButton);
        
        panel.add(reportSelectionPanel, BorderLayout.NORTH);
        
        // Report display area
        JScrollPane reportScrollPane = new JScrollPane(reportTable);
        reportScrollPane.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR));
        panel.add(reportScrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private static void generateSalesByGenreReport(DefaultTableModel model) throws SQLException {
        model.addColumn("Genre");
        model.addColumn("Total Quantity Sold");
        
        String query = "SELECT b.genre, SUM(oi.quantity) AS total_quantity " +
                     "FROM books b JOIN order_items oi ON b.book_id = oi.book_id " +
                     "GROUP BY b.genre " +
                     "ORDER BY total_quantity DESC";
        
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            row.add(rs.getString("genre"));
            row.add(rs.getInt("total_quantity"));
            model.addRow(row);
        }
        
        rs.close();
        stmt.close();
    }

    private static void generateTopSellingBooksReport(DefaultTableModel model) throws SQLException {
        model.addColumn("Book Title");
        model.addColumn("Author");
        model.addColumn("Total Quantity Sold");
        
        String query = "SELECT b.title, a.name, SUM(oi.quantity) AS total_quantity " +
                     "FROM books b " +
                     "JOIN order_items oi ON b.book_id = oi.book_id " +
                     "JOIN authors a ON b.author_id = a.author_id " +
                     "GROUP BY b.book_id, b.title, a.name " +
                     "ORDER BY total_quantity DESC " +
                     "LIMIT 10";
        
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            row.add(rs.getString("title"));
            row.add(rs.getString("name"));
            row.add(rs.getInt("total_quantity"));
            model.addRow(row);
        }
        
        rs.close();
        stmt.close();
    }

    private static void generateCustomerSpendingReport(DefaultTableModel model) throws SQLException {
        model.addColumn("Customer Name");
        model.addColumn("Total Amount Spent");
        
        String query = "SELECT c.name, SUM(o.total_amount) AS total_spent " +
                     "FROM customers c JOIN orders o ON c.customer_id = o.customer_id " +
                     "GROUP BY c.customer_id, c.name " +
                     "ORDER BY total_spent DESC";
        
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            row.add(rs.getString("name"));
            row.add(rs.getDouble("total_spent"));
            model.addRow(row);
        }
        
        rs.close();
        stmt.close();
    }

    // Helper methods
    private static int getAuthorId(String authorName) throws SQLException {
        String query = "SELECT author_id FROM authors WHERE name=?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setString(1, authorName);
        
        ResultSet rs = pstmt.executeQuery();
        int authorId = -1;
        if (rs.next()) {
            authorId = rs.getInt("author_id");
        }
        
        rs.close();
        pstmt.close();
        
        return authorId;
    }

    private static int getCustomerId(String customerName) throws SQLException {
        String query = "SELECT customer_id FROM customers WHERE name=?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setString(1, customerName);
        
        ResultSet rs = pstmt.executeQuery();
        int customerId = -1;
        if (rs.next()) {
            customerId = rs.getInt("customer_id");
        }
        
        rs.close();
        pstmt.close();
        
        return customerId;
    }

    private static int getBookId(String bookTitle) throws SQLException {
        String query = "SELECT book_id FROM books WHERE title=?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setString(1, bookTitle);
        
        ResultSet rs = pstmt.executeQuery();
        int bookId = -1;
        if (rs.next()) {
            bookId = rs.getInt("book_id");
        }
        
        rs.close();
        pstmt.close();
        return bookId;
    }

    private static void loadAuthorsIntoComboBox(JComboBox<String> authorCombo) {
        try {
            // Make the combo box editable
            authorCombo.setEditable(true);
            authorCombo.setMaximumRowCount(10); // Show max 10 items in dropdown
            
            // Get the editor component (the text field)
            JTextField editor = (JTextField) authorCombo.getEditor().getEditorComponent();
            editor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            
            // Add focus listener for better UX
            editor.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    editor.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    ));
                    // Show dropdown when focused
                    authorCombo.showPopup();
                }
                
                @Override
                public void focusLost(FocusEvent e) {
                    editor.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    ));
                }
            });
            
            // Add key listener for filtering and navigation
            editor.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    // Handle navigation keys
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_UP:
                        case KeyEvent.VK_DOWN:
                            if (!authorCombo.isPopupVisible()) {
                                authorCombo.showPopup();
                            }
                            break;
                        case KeyEvent.VK_ENTER:
                            if (authorCombo.isPopupVisible()) {
                                authorCombo.setSelectedItem(editor.getText());
                                authorCombo.hidePopup();
                            }
                            break;
                        case KeyEvent.VK_ESCAPE:
                            authorCombo.hidePopup();
                            break;
                    }
                }
                
                @Override
                public void keyReleased(KeyEvent e) {
                    // Don't filter on navigation keys
                    if (e.getKeyCode() == KeyEvent.VK_UP || 
                        e.getKeyCode() == KeyEvent.VK_DOWN || 
                        e.getKeyCode() == KeyEvent.VK_ENTER || 
                        e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        return;
                    }
                    
                    String searchText = editor.getText().toLowerCase();
                    authorCombo.removeAllItems();
                    
                    try {
                        String query = "SELECT name FROM authors WHERE LOWER(name) LIKE ? ORDER BY name";
                        PreparedStatement pstmt = connection.prepareStatement(query);
                        pstmt.setString(1, "%" + searchText + "%");
                        ResultSet rs = pstmt.executeQuery();
                        
                        while (rs.next()) {
                            String name = rs.getString("name");
                            authorCombo.addItem(name);
                        }
                        
                        // Show dropdown when typing
                        if (!searchText.isEmpty()) {
                            authorCombo.showPopup();
                        }
                        
                        rs.close();
                        pstmt.close();
                    } catch (SQLException ex) {
                        showNotification("Error searching authors: " + ex.getMessage(), ERROR_COLOR);
                    }
                }
            });
            
            // Load initial authors
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name FROM authors ORDER BY name");
            while (rs.next()) {
                authorCombo.addItem(rs.getString("name"));
            }
            rs.close();
            stmt.close();
            
            // Set custom renderer for better appearance
            authorCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, 
                        int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                    
                    if (value != null) {
                        label.setText(value.toString());
                        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                        
                        if (isSelected) {
                            label.setBackground(ACCENT_COLOR);
                            label.setForeground(Color.WHITE);
                        } else {
                            label.setBackground(Color.WHITE);
                            label.setForeground(TEXT_COLOR);
                        }
                    }
                    
                    return label;
                }
            });
            
        } catch (SQLException e) {
            showNotification("Error loading authors: " + e.getMessage(), ERROR_COLOR);
        }
    }

    private static void loadCustomersIntoComboBox(JComboBox<String> customerCombo) {
        try {
            // Make the combo box editable
            customerCombo.setEditable(true);
            customerCombo.setMaximumRowCount(10); // Show max 10 items in dropdown
            
            // Get the editor component (the text field)
            JTextField editor = (JTextField) customerCombo.getEditor().getEditorComponent();
            editor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            
            // Add key listener for filtering
            editor.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    String searchText = editor.getText().toLowerCase();
                    customerCombo.removeAllItems();
                    
                    try {
                        String query = "SELECT name FROM customers WHERE LOWER(name) LIKE ? ORDER BY name";
                        PreparedStatement pstmt = connection.prepareStatement(query);
                        pstmt.setString(1, "%" + searchText + "%");
                        ResultSet rs = pstmt.executeQuery();
                        
                        while (rs.next()) {
                            String name = rs.getString("name");
                            customerCombo.addItem(name);
                        }
                        
                        // Show dropdown when typing
                        if (!searchText.isEmpty()) {
                            customerCombo.showPopup();
                        }
                        
                        rs.close();
                        pstmt.close();
                    } catch (SQLException ex) {
                        showNotification("Error searching customers: " + ex.getMessage(), ERROR_COLOR);
                    }
                }
            });
            
            // Load initial customers
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name FROM customers ORDER BY name");
            while (rs.next()) {
                customerCombo.addItem(rs.getString("name"));
            }
            rs.close();
            stmt.close();
            
            // Set custom renderer for better appearance
            customerCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, 
                        int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                    
                    if (value != null) {
                        label.setText(value.toString());
                        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                        
                        if (isSelected) {
                            label.setBackground(ACCENT_COLOR);
                            label.setForeground(Color.WHITE);
                        } else {
                            label.setBackground(Color.WHITE);
                            label.setForeground(TEXT_COLOR);
                        }
                    }
                    
                    return label;
                }
            });
            
        } catch (SQLException e) {
            showNotification("Error loading customers: " + e.getMessage(), ERROR_COLOR);
        }
    }

    private static void loadBooksIntoComboBox(JComboBox<String> bookCombo) {
        try {
            // Make the combo box editable
            bookCombo.setEditable(true);
            bookCombo.setMaximumRowCount(10); // Show max 10 items in dropdown
            
            // Get the editor component (the text field)
            JTextField editor = (JTextField) bookCombo.getEditor().getEditorComponent();
            editor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            
            // Add key listener for filtering
            editor.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    String searchText = editor.getText().toLowerCase();
                    bookCombo.removeAllItems();
                    
                    try {
                        String query = "SELECT title FROM books WHERE LOWER(title) LIKE ? ORDER BY title";
                        PreparedStatement pstmt = connection.prepareStatement(query);
                        pstmt.setString(1, "%" + searchText + "%");
                        ResultSet rs = pstmt.executeQuery();
                        
                        while (rs.next()) {
                            String title = rs.getString("title");
                            bookCombo.addItem(title);
                        }
                        
                        // Show dropdown when typing
                        if (!searchText.isEmpty()) {
                            bookCombo.showPopup();
                        }
                        
                        rs.close();
                        pstmt.close();
                    } catch (SQLException ex) {
                        showNotification("Error searching books: " + ex.getMessage(), ERROR_COLOR);
                    }
                }
            });
            
            // Load initial books
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT title FROM books ORDER BY title");
            while (rs.next()) {
                bookCombo.addItem(rs.getString("title"));
            }
            rs.close();
            stmt.close();
            
            // Set custom renderer for better appearance
        } catch (SQLException e) {
            showNotification("Error loading books: " + e.getMessage(), ERROR_COLOR);
        }
    }

    // Helper method to create styled buttons
    private static JButton createStyledButton(String text, String iconName) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create gradient background
                GradientPaint gp = new GradientPaint(0, 0, getBackground(), 0, getHeight(), 
                    new Color(getBackground().getRed(), getBackground().getGreen(), getBackground().getBlue(), 200));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                // Draw text with proper contrast
                g2d.setColor(getForeground());
                FontMetrics fm = g2d.getFontMetrics(getFont());
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        button.setFont(REGULAR_FONT);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE); // Keep button text white for contrast
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, COMPONENT_HEIGHT));
        button.setMinimumSize(new Dimension(100, COMPONENT_HEIGHT));
        
        // Add padding
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        // Remove hover effects for consistent appearance

        return button;
    }

    // Helper method to create icons (you'll need to add actual icons to your resources)
    private static ImageIcon createIcon(String iconName) {
        try {
            return new ImageIcon(BookstoreManagementSystem.class.getResource("/icons/" + iconName));
        } catch (Exception e) {
            return null;
        }
    }

    // Add this method to apply consistent table styling
    private static void applyTableStyle(JTable table) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(35); // Increased row height for better readability
        table.setIntercellSpacing(new Dimension(10, 5));
        table.setGridColor(TABLE_GRID_COLOR);
        table.setFont(REGULAR_FONT);
        table.getTableHeader().setBackground(Color.WHITE); // Changed to white background
        table.getTableHeader().setForeground(TEXT_COLOR); // Changed to black text
        table.getTableHeader().setFont(TITLE_FONT);
        table.getTableHeader().setReorderingAllowed(false); // Prevent column reordering for simplicity
        table.setSelectionBackground(ACCENT_COLOR);
        table.setSelectionForeground(Color.WHITE); // Keep selected text white for contrast
        
        // Make table responsive
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Add zebra striping with improved colors
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);
                
                // Apply zebra striping with better contrast
                if (!isSelected) {
                    comp.setBackground(row % 2 == 0 ? Color.WHITE : TABLE_ROW_ALT_COLOR);
                    comp.setForeground(TEXT_COLOR); // Black text for better readability
                } else {
                    comp.setForeground(Color.WHITE); // White text for selected rows
                }
                
                // Center align all cells
                setHorizontalAlignment(JLabel.CENTER);
                
                // Format specific column types
                if (value instanceof Double) {
                    setText(String.format("$%.2f", (Double) value));
                }
                
                return comp;
            }
        };
        
        table.setDefaultRenderer(Object.class, renderer);
        
        // Add shadow to table
        table.setBorder(BorderFactory.createLineBorder(SHADOW_COLOR, 1));
        
        // Add mouse listener for better UX
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) { // Double-click
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        // Get the model from the table
                        DefaultTableModel model = (DefaultTableModel) table.getModel();
                        // Show edit dialog for the selected row
                        showEditBookDialog(model, row);
                    }
                }
            }
        });
        
        // Add keyboard navigation
        table.setFocusTraversalKeysEnabled(false);
        table.registerKeyboardAction(
            e -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    DefaultTableModel model = (DefaultTableModel) table.getModel();
                    showEditBookDialog(model, selectedRow);
                }
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
            JComponent.WHEN_FOCUSED
        );
    }

    // Add this method to create consistent panel styling
    private static JPanel createStyledPanel() {
        JPanel panel = new JPanel(new BorderLayout(PADDING, PADDING));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        
        // Add subtle shadow effect
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SHADOW_COLOR, 1),
            BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)
        ));
        
        return panel;
    }

    // Modify dialog creation method for better responsiveness
    private static JDialog createStyledDialog(String title, int width, int height) {
        JDialog dialog = new JDialog(frame, title, true);
        dialog.setSize(Math.max(width, DIALOG_MIN_WIDTH), Math.max(height, DIALOG_MIN_HEIGHT));
        dialog.setLayout(new BorderLayout(PADDING, PADDING));
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);
        dialog.setLocationRelativeTo(frame);
        dialog.setResizable(true);
        dialog.setMinimumSize(new Dimension(DIALOG_MIN_WIDTH, DIALOG_MIN_HEIGHT));
        
        // Add shadow effect to dialog
        dialog.getRootPane().setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SHADOW_COLOR, 1),
            BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)
        ));
        
        return dialog;
    }

    private static void addFormField(JPanel panel, String labelText, JComponent field, 
                                   GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(labelText);
        label.setFont(REGULAR_FONT);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        field.setPreferredSize(new Dimension(200, 30));
        panel.add(field, gbc);
    }

    // Add this method to show notifications
    private static void showNotification(String message, Color color) {
        JOptionPane optionPane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
        optionPane.setBackground(Color.WHITE);
        optionPane.setForeground(TEXT_COLOR); // Black text for better readability
        optionPane.setFont(REGULAR_FONT);
        
        JDialog dialog = optionPane.createDialog(frame, "Notification");
        dialog.setBackground(Color.WHITE);
        
        // Add shadow to dialog
        dialog.getRootPane().setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SHADOW_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        // Style the buttons
        for (Component comp : optionPane.getComponents()) {
            if (comp instanceof JPanel) {
                for (Component button : ((JPanel) comp).getComponents()) {
                    if (button instanceof JButton) {
                        JButton btn = (JButton) button;
                        btn.setBackground(PRIMARY_COLOR);
                        btn.setForeground(Color.WHITE); // Keep button text white for contrast
                        btn.setFont(REGULAR_FONT);
                        btn.setFocusPainted(false);
                        btn.setBorderPainted(false);
                        btn.setOpaque(true);
                        
                        // Remove hover effects
                    }
                }
            }
        }
        
        dialog.setVisible(true);
    }
    
    // Add this method to search books
    private static void searchBooks(DefaultTableModel model, String searchText) {
        model.setRowCount(0);
        try {
            String query = "SELECT b.book_id, b.title, a.name, b.genre, b.price, b.publication_date " +
                           "FROM books b JOIN authors a ON b.author_id = a.author_id " +
                           "WHERE LOWER(b.title) LIKE ? OR LOWER(a.name) LIKE ? OR LOWER(b.genre) LIKE ? " +
                           "OR CAST(b.price AS TEXT) LIKE ? OR b.publication_date LIKE ? " +
                           "ORDER BY b.title";
            PreparedStatement pstmt = connection.prepareStatement(query);
            String searchPattern = "%" + searchText.toLowerCase() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);
            pstmt.setString(5, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("book_id"));
                row.add(rs.getString("title"));
                row.add(rs.getString("name"));
                row.add(rs.getString("genre"));
                row.add(rs.getDouble("price"));
                row.add(rs.getString("publication_date"));
                model.addRow(row);
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            showNotification("Error searching books: " + e.getMessage(), ERROR_COLOR);
        }
    }

    // Add these helper methods
    private static JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(REGULAR_FONT);
        field.setPreferredSize(new Dimension(200, COMPONENT_HEIGHT));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_COLOR); // Black text for better readability
        
        // Add focus listener for better UX
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                    BorderFactory.createEmptyBorder(4, 9, 4, 9)
                ));
            }
            
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
        });
        
        return field;
    }

    private static JComboBox<String> createStyledComboBox() {
        JComboBox<String> combo = new JComboBox<>();
        combo.setFont(REGULAR_FONT);
        combo.setPreferredSize(new Dimension(200, COMPONENT_HEIGHT));
        combo.setBackground(Color.WHITE);
        combo.setForeground(TEXT_COLOR); // Black text for better readability
        
        // Add focus listener for better UX
        combo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                combo.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 2));
            }
            
            public void focusLost(java.awt.event.FocusEvent evt) {
                combo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
            }
        });
        
        return combo;
    }

    private static double getBookPrice(String bookTitle) {
        double price = 0.0;
        try {
            String query = "SELECT price FROM books WHERE title=?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, bookTitle);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                price = rs.getDouble("price");
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            showNotification("Error getting book price: " + e.getMessage(), ERROR_COLOR);
        }
        return price;
    }

    private static int showConfirmDialog(String message, String title) {
        JDialog dialog = new JDialog(frame, title, true);
        dialog.setLayout(new BorderLayout(PADDING, PADDING));
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Message panel
        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        messagePanel.setBackground(BACKGROUND_COLOR);
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(REGULAR_FONT);
        messageLabel.setForeground(TEXT_COLOR);
        messagePanel.add(messageLabel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        JButton yesButton = createStyledButton("Yes", null);
        JButton noButton = createStyledButton("No", null);
        
        final int[] result = new int[1];
        
        yesButton.addActionListener(e -> {
            result[0] = JOptionPane.YES_OPTION;
            dialog.dispose();
        });
        
        noButton.addActionListener(e -> {
            result[0] = JOptionPane.NO_OPTION;
            dialog.dispose();
        });
        
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        
        dialog.add(messagePanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Set dialog properties
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setResizable(false);
        dialog.setVisible(true);
        
        return result[0];
    }

    private static void exportReport(String title, DefaultTableModel model, String[] columnNames) {
        JDialog dialog = new JDialog(frame, "Export Report", true);
        dialog.setLayout(new BorderLayout(PADDING, PADDING));
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);
        
        // File selection panel
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filePanel.setBackground(BACKGROUND_COLOR);
        
        JTextField filePathField = createStyledTextField();
        filePathField.setPreferredSize(new Dimension(300, COMPONENT_HEIGHT));
        filePathField.setEditable(false);
        
        JButton browseButton = createStyledButton("Browse", null);
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Report");
            
            // Create a custom file filter for CSV
            fileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
                }
                
                @Override
                public String getDescription() {
                    return "CSV Files (*.csv)";
                }
            });
            
            int result = fileChooser.showSaveDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String filePath = file.getAbsolutePath();
                
                // Add extension if not provided
                if (!filePath.toLowerCase().endsWith(".csv")) {
                    filePath += ".csv";
                }
                
                filePathField.setText(filePath);
            }
        });
        
        filePanel.add(new JLabel("Save to:"));
        filePanel.add(filePathField);
        filePanel.add(browseButton);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        JButton exportButton = createStyledButton("Export", null);
        JButton cancelButton = createStyledButton("Cancel", null);
        
        exportButton.addActionListener(e -> {
            String filePath = filePathField.getText().trim();
            if (filePath.isEmpty()) {
                showNotification("Please select a file location", WARNING_COLOR);
                return;
            }
            
            try {
                exportToCSV(new File(filePath), model, columnNames);
                showNotification("Report exported successfully", SECONDARY_COLOR);
                dialog.dispose();
            } catch (IOException ex) {
                showNotification("Error exporting report: " + ex.getMessage(), ERROR_COLOR);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(exportButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(filePanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Set dialog properties
        dialog.setSize(600, 150);
        dialog.setLocationRelativeTo(frame);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    private static JPanel createBottomPanel(DefaultTableModel model, JTable table, String type) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, PADDING, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton refreshButton = createStyledButton("Refresh", null);
        JButton addButton = createStyledButton("Add " + type.substring(0, 1).toUpperCase() + type.substring(1), null);
        JButton editButton = createStyledButton("Edit " + type.substring(0, 1).toUpperCase() + type.substring(1), null);
        JButton deleteButton = createStyledButton("Delete " + type.substring(0, 1).toUpperCase() + type.substring(1), null);

        // Add action listeners
        refreshButton.addActionListener(e -> {
            switch (type) {
                case "author":
                    refreshAuthorsTable(model);
                    break;
                case "customer":
                    refreshCustomersTable(model);
                    break;
                case "order":
                    refreshOrdersTable(model);
                    break;
            }
        });

        addButton.addActionListener(e -> {
            switch (type) {
                case "author":
                    showAddAuthorDialog(model);
                    break;
                case "customer":
                    showAddCustomerDialog(model);
                    break;
                case "order":
                    showAddOrderDialog(model);
                    break;
            }
        });

        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                switch (type) {
                    case "author":
                        showEditAuthorDialog(model, selectedRow);
                        break;
                    case "customer":
                        showEditCustomerDialog(model, selectedRow);
                        break;
                    case "order":
                        int orderId = (int) model.getValueAt(selectedRow, 0);
                        showOrderDetailsDialog(orderId);
                        break;
                }
            } else {
                showNotification("Please select a " + type + " to edit", WARNING_COLOR);
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) model.getValueAt(selectedRow, 0);
                switch (type) {
                    case "author":
                        deleteAuthor(id, model);
                        break;
                    case "customer":
                        deleteCustomer(id, model);
                        break;
                    case "order":
                        deleteOrder(id, model);
                        break;
                }
            } else {
                showNotification("Please select a " + type + " to delete", WARNING_COLOR);
            }
        });

        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.WEST);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING, 0));
        searchPanel.setBackground(Color.WHITE);

        JTextField searchField = createStyledTextField();
        searchField.setPreferredSize(new Dimension(200, COMPONENT_HEIGHT));
        searchField.setToolTipText("Search " + type + "s");

        JButton searchButton = createStyledButton("Search", null);
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                switch (type) {
                    case "author":
                        searchAuthors(model, searchText);
                        break;
                    case "customer":
                        searchCustomers(model, searchText);
                        break;
                    case "order":
                        searchOrders(model, searchText);
                        break;
                }
            } else {
                switch (type) {
                    case "author":
                        refreshAuthorsTable(model);
                        break;
                    case "customer":
                        refreshCustomersTable(model);
                        break;
                    case "order":
                        refreshOrdersTable(model);
                        break;
                }
            }
        });

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        panel.add(searchPanel, BorderLayout.EAST);

        return panel;
    }

    private static void updateTotalAmount(DefaultTableModel model, JDialog dialog) {
        double total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            total += (double) model.getValueAt(i, 3); // Subtotal column
        }
        
        // Find the total label in the dialog
        for (Component comp : dialog.getContentPane().getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                for (Component innerComp : panel.getComponents()) {
                    if (innerComp instanceof JPanel) {
                        JPanel buttonPanel = (JPanel) innerComp;
                        for (Component buttonComp : buttonPanel.getComponents()) {
                            if (buttonComp instanceof JLabel) {
                                JLabel label = (JLabel) buttonComp;
                                if (label.getText().startsWith("Total:")) {
                                    label.setText(String.format("Total: $%.2f", total));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static double calculateTotalAmount(DefaultTableModel model) {
        double total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            total += (double) model.getValueAt(i, 3); // Subtotal column
        }
        return total;
    }

    private static void insertOrderItems(DefaultTableModel model, int orderId) throws SQLException {
        String query = "INSERT INTO order_items (order_id, book_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(query);

        for (int i = 0; i < model.getRowCount(); i++) {
            String bookTitle = (String) model.getValueAt(i, 0);
            int quantity = (int) model.getValueAt(i, 1);
            double unitPrice = (double) model.getValueAt(i, 2);

            int bookId = getBookId(bookTitle);
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, bookId);
            pstmt.setInt(3, quantity);
            pstmt.setDouble(4, unitPrice);
            pstmt.executeUpdate();
        }
        pstmt.close();
    }

    private static void exportToCSV(File file, DefaultTableModel model, String[] columnNames) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write header
            writer.println(String.join(",", columnNames));

            // Write data
            for (int i = 0; i < model.getRowCount(); i++) {
                StringBuilder row = new StringBuilder();
                for (int j = 0; j < model.getColumnCount(); j++) {
                    if (j > 0) row.append(",");
                    Object value = model.getValueAt(i, j);
                    if (value instanceof String) {
                        row.append("\"").append(value).append("\"");
                    } else {
                        row.append(value);
                    }
                }
                writer.println(row.toString());
            }
        }
    }

    private static void searchAuthors(DefaultTableModel model, String searchText) {
        model.setRowCount(0);
        try {
            String query = "SELECT * FROM authors WHERE LOWER(name) LIKE ? OR LOWER(birth_date) LIKE ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            String searchPattern = "%" + searchText.toLowerCase() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("author_id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("birth_date"));
                model.addRow(row);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            showNotification("Error searching authors: " + e.getMessage(), ERROR_COLOR);
        }
    }

    private static void searchCustomers(DefaultTableModel model, String searchText) {
        model.setRowCount(0);
        try {
            String query = "SELECT * FROM customers WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ? OR LOWER(phone) LIKE ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            String searchPattern = "%" + searchText.toLowerCase() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("customer_id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("email"));
                row.add(rs.getString("phone"));
                model.addRow(row);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            showNotification("Error searching customers: " + e.getMessage(), ERROR_COLOR);
        }
    }

    private static void searchOrders(DefaultTableModel model, String searchText) {
        model.setRowCount(0);
        try {
            String query = "SELECT o.order_id, c.name, o.order_date, o.total_amount " +
                         "FROM orders o JOIN customers c ON o.customer_id = c.customer_id " +
                         "WHERE LOWER(c.name) LIKE ? OR LOWER(o.order_date) LIKE ? " +
                         "OR CAST(o.total_amount AS TEXT) LIKE ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            String searchPattern = "%" + searchText.toLowerCase() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("order_id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("order_date"));
                row.add(rs.getDouble("total_amount"));
                model.addRow(row);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            showNotification("Error searching orders: " + e.getMessage(), ERROR_COLOR);
        }
    }
}