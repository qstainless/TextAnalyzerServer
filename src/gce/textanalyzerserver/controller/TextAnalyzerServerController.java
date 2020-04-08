package gce.textanalyzerserver.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;

/**
 * This is the main controller for the TextAnalyzerServer application.
 */
public class TextAnalyzerServerController {

    /**
     * Takes the URL sent by the client and processes it for analysis, as follows:
     * <ol>
     *     <li>First, it attempts to fetch the content from the URL by
     *     calling the {@link TextAnalyzerServerController#fetchUrlContent}
     *     method. URL validation is processed on the client side.</li>
     *     <li>Next, the program will call the
     *     {@link Database#storeWordsIntoDatabase} method to store the
     *     unique words and their frequencies in the database, after
     *     stripping away all HTML tags and some punctuation. </li>
     * </ol>
     *
     * @param targetUrl The URL submitted by the client
     */
    public static void processRequest(String targetUrl) {
        try {
            // Fetch the URL content
            BufferedReader targetHtmlContent = TextAnalyzerServerController.fetchUrlContent(targetUrl);

            // Extract the words from the URL content and insert them into the database
            Database.storeWordsIntoDatabase(targetHtmlContent);
        } catch (IOException e) {
            System.out.println("Error: Invalid URL. Unable to process.");
            e.printStackTrace();
        }
    }

    /**
     * Attempts to fetch the URL provided by the user in the GUI.
     *
     * @param targetUrl the target url
     * @return The buffered URL content
     * @throws IOException the IO Exception
     */
    public static BufferedReader fetchUrlContent(String targetUrl) throws IOException {
        return new BufferedReader(new InputStreamReader(new URL(targetUrl).openStream()));
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
                .replaceAll("<.*?>", "")
                .replaceAll("<.*", "")  // hack to strip unclosed html tags
                .replaceAll(".*?>", "") // hack to strip unopened html tags
                .replaceAll(" '", " ")
                .replaceAll("[!.,]'", "")
                .replaceAll("[\\[|.?!,;:{}()\\]]", "")
                .replaceAll("--", " ")
                .trim();
    }
}
