package gce.textanalyzerserver.controller;

import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;

/**
 * This is the main controller for the TextAnalyzerServer application.
 */
public class TextAnalyzerServerController {

    /**
     * Takes the URL sent by the client and processes it for analysis, as follows:
     * <ol>
     *     <li>First, it uses Jsoup to fetch the content from the URL and clean
     *     up the HTML for better parsing. URL validation is processed on the
     *     client side.</li>
     *     <li>Next, the program will call the
     *     {@link DatabaseController#storeWordsIntoDatabase} method to store the
     *     unique words and their frequencies in the database, after
     *     stripping away all HTML tags and some punctuation. </li>
     * </ol>
     *
     * @param targetUrl The URL submitted by the client
     */
    public static void processRequest(String targetUrl) {
        // Connect to the database. Create the schema if it does not already exist.
        // Truncate the word table
        DatabaseController.createSchema();

        // Uses the Jsoup library to fetch the targetUrl and create a clean HTML string thereof
        String targetHtmlContent = null;

        try {
            targetHtmlContent = Jsoup.connect(targetUrl).get().text();
        } catch (IOException e) {
            System.out.println("Error.\n\n==> The URL sent by the client is invalid.");
        }

        if (targetHtmlContent != null) {
            // Buffer the targetHtmlContent String for parsing
            BufferedReader bufferedHtmlContent = new BufferedReader(new StringReader(targetHtmlContent));

            // Store the words and their frequencies into the database
            DatabaseController.storeWordsIntoDatabase(bufferedHtmlContent);
        }
    }

    /**
     * Converts each {@code inputLine} of the {@code inputFile} from HTML to
     * plain text by stripping select characters and strings using regular
     * expressions.
     *
     * @param inputLine The string to convert from html to plain text.
     * @return A plain text version of the {@code inputLine}
     */
    public static String htmlToText(String inputLine) {
        return inputLine
                .toLowerCase()
                .replaceAll(">'", ">")
                .replaceAll(" '", " ")
                .replaceAll("[!.,]'", "")
                .replaceAll("[\\[|.?!,;:{}()\\]]", "")
                .replaceAll("--", " ")
                .trim();
    }

    /**
     * Gets the current date time to display when a client request is received
     *
     * @return The date and time
     */
    public static String getDateTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        return dateTime.toString();
    }
}
