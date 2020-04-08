package gce.textanalyzerserver.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;
import java.util.StringTokenizer;

/**
 * Database class
 * <p>
 * Requirements:
 * 1. The MySQL Java Connector is properly included in the project's libraries
 * 2. Will connect to host:localhost at default MySQL port 3306
 * 3. Assumes that a user exists with username/password textanalyzer/textanalyzer
 */
public class Database {
    private static Connection dbConnection;
    private static final String databaseName = "word_occurrences";
    private static final String databaseTable = "word";
    private static String sql;

    /**
     * Creates a database connection.
     *
     * @return The database connection
     */
    public static Connection dbConnect(String databaseName) {
        try {
            String mysqlDriver = "com.mysql.cj.jdbc.Driver";
            Class.forName(mysqlDriver);
            try {
                String databaseHost = "localhost";
                String databasePort = "3306";
                String databaseUser = "textanalyzer";
                String databasePass = "textanalyzer";

                String connectionUrl = "jdbc:mysql://" + databaseHost + ":" + databasePort + "/" + databaseName +
                        "?useUnicode=true" +
                        "&useJDBCCompliantTimezoneShift=true" +
                        "&useLegacyDatetimeCode=false" +
                        "&serverTimezone=UTC";
                dbConnection = DriverManager.getConnection(connectionUrl, databaseUser, databasePass);
            } catch (SQLException ex) {
                System.out.println("Failed to create the database connection.");
                ex.printStackTrace();
            }
        } catch (ClassNotFoundException ex) {
            System.out.println("Driver not found.");
            ex.printStackTrace();
        }

        return dbConnection;
    }

    /**
     * Stores words found in the target URL and their frequencies in the database
     *
     * @param urlContent The content of the target URL
     */
    public static void storeWordsIntoDatabase(BufferedReader urlContent) {
        try {
            // Open a connection to the database
            dbConnection = dbConnect(databaseName);

            PreparedStatement preparedStatement;

            // Temporary string to store each line of the buffered urlContent
            String inputLine;

            // Add words and their frequency to the database
            while ((inputLine = urlContent.readLine()) != null) {
                // convert the html formatted line to plain text
                String filteredInputLine = TextAnalyzerServerController.htmlToText(inputLine);

                // extract words from filteredInputLine using StringTokenizer
                StringTokenizer wordsInLine = new StringTokenizer(filteredInputLine);

                // add words and their frequencies to the database
                while (wordsInLine.hasMoreTokens()) {
                    String word = wordsInLine.nextToken();

                    // Limit word length to 255 characters
                    if (word.length() > 254) {
                        word = word.substring(0, 254);
                    }

                    sql = "SELECT `wordFrequency` FROM " + databaseTable + " WHERE `wordContent`= '" + word.replace("'", "\\'") + "'";

                    Statement statement = dbConnection.createStatement();
                    ResultSet resultSet = statement.executeQuery(sql);

                    if (resultSet.next()) {
                        int currentWordFrequency = resultSet.getInt("wordFrequency");
                        int newWordFrequency = currentWordFrequency + 1;

                        sql = "UPDATE " + databaseTable + " SET `wordFrequency`=? WHERE `wordContent`=?";

                        preparedStatement = dbConnection.prepareStatement(sql);
                        preparedStatement.setInt(1, newWordFrequency);
                        preparedStatement.setString(2, word);
                    } else {
                        sql = "INSERT INTO " + databaseTable + " (`wordContent`, `wordFrequency`) VALUES (?,?)";

                        preparedStatement = dbConnection.prepareStatement(sql);
                        preparedStatement.setString(1, word);
                        preparedStatement.setInt(2, 1);
                    }

                    preparedStatement.executeUpdate();
                }
            }

            urlContent.close();

            closeConnection();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads all word/frequency pairs from the database.
     */
    public static ResultSet getAllWords() {
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            dbConnection = dbConnect(databaseName);

            sql = "SELECT `wordContent`, `wordFrequency` FROM `word` ORDER BY `wordFrequency` DESC";

            statement = dbConnection.createStatement();

            resultSet = statement.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultSet;
    }

    /**
     * Get number of words in database
     *
     * @throws SQLException If an SQL exception occurs
     */
    public static int getUniqueWordCount() throws SQLException {
        dbConnection = dbConnect(databaseName);

        sql = "SELECT count(*) as `unique_count` FROM " + databaseTable;
        Statement statement = dbConnection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);

        int out = 0;

        if (resultSet.next()) {
            out = resultSet.getInt("unique_count");
        }

        resultSet.close();

        closeConnection();

        return out;
    }

    /**
     * Get number of words in database
     *
     * @throws SQLException If an SQL exception occurs
     */
    public static int getAllWordCount() throws SQLException {
        dbConnection = dbConnect(databaseName);

        sql = "SELECT SUM(`wordFrequency`) as `total_count` FROM " + databaseTable;
        Statement statement = dbConnection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);

        int out = 0;

        if (resultSet.next()) {
            out = resultSet.getInt("total_count");
        }

        resultSet.close();

        closeConnection();

        return out;
    }

    /**
     * Creates the database schema and table if they do not already exist.
     */
    public static void createSchema() {
        try {
            dbConnection = dbConnect("");
            // Schema and table creation do not rely on user input. No need to use prepared statements.
            Statement statement = dbConnection.createStatement();

            // Create the schema if it does not already exist
            sql = "CREATE SCHEMA IF NOT EXISTS `word_occurrences` DEFAULT CHARACTER SET utf8";
            statement.executeUpdate(sql);

            // Select the database
            sql = "USE `word_occurrences`";
            statement.executeUpdate(sql);

            // Create the table if it does not already exist
            sql = "CREATE TABLE IF NOT EXISTS " + databaseTable + " (" +
                    "`wordContent` VARCHAR(255) NOT NULL, " +
                    "`wordFrequency` INT(11) NOT NULL, " +
                    "UNIQUE INDEX `wordContent_unique` (`wordContent`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8";
            statement.executeUpdate(sql);

            // Clear the table
            sql = "TRUNCATE " + databaseTable;
            statement.executeUpdate(sql);

            statement.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    /**
     * Closes the database connection
     */
    public static void closeConnection() {
        try {
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
