package gce.textanalyzerserver.tests;

import gce.textanalyzerserver.controller.DatabaseController;
import gce.textanalyzerserver.controller.TextAnalyzerServerController;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TextAnalyzerServerTests {

    @Test
    @Order(1)
    @DisplayName("A database connection is successfully created.")
    void testDatabaseConnection() {
        Connection connection = DatabaseController.dbConnect("word_occurrences");
        assertNotNull(connection);
    }

    @Test
    @Order(2)
    @DisplayName("The 'word_occurrences' schema and the 'word' table are created if they don't already exist.")
    void testSchemaAndTableCreation() {
        DatabaseController.createSchema();
    }

    @Test
    @Order(3)
    @DisplayName("Populates the database with words from http://shakespeare.mit.edu/macbeth/full.html")
    public void testGetData() throws IOException {
        BufferedReader targetHtmlContent = TextAnalyzerServerController.fetchUrlContent("http://shakespeare.mit.edu/macbeth/full.html");
        DatabaseController.storeWordsIntoDatabase(targetHtmlContent);
    }

    @Test
    @Order(4)
    @DisplayName("Fetches all words from the database.")
    void testGetAllWords() throws SQLException {
        ResultSet allWords = DatabaseController.getAllWords();
        while (allWords.next()) {
            System.out.println(allWords.getString("wordContent") + ": " + allWords.getInt("wordFrequency"));
        }

        allWords.close();
    }

    @Test
    @Order(5)
    @DisplayName("Verify that the target URL has 3394 unique words.")
    void testGetUniqueWordCount() throws SQLException {
        int uniqueWords = DatabaseController.getUniqueWordCount();
        assertEquals(3394, uniqueWords);
    }

    @Test
    @Order(6)
    @DisplayName("Verify that the target URL has 18122 total words.")
    void testGetAllWordCount() throws SQLException {
        int allWords = DatabaseController.getAllWordCount();
        assertEquals(18122, allWords);
    }

    @Test
    @Order(7)
    @DisplayName("Tests HTML to Text conversion.")
    void testHtmlToText() {
        String htmlSource = "<p class=\"some-class\">Hello World!</p>";
        String expected = "hello world";
        System.out.println(htmlSource + " becomes " + expected);
        assertEquals(expected, TextAnalyzerServerController.htmlToText(htmlSource));
    }
}