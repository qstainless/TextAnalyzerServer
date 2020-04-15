package gce.textanalyzerserver;

import gce.textanalyzerserver.controller.DatabaseController;
import gce.textanalyzerserver.controller.TextAnalyzerServerController;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This is the socket server for the TextAnalyzer program that implements an
 * application that reads a file from a given URL and outputs statistics
 * about the words found in that file. It outputs the total number of words
 * found in the file and the frequencies of unique words, sorted by the most
 * frequently used word in descending order.
 * <p>
 * Course: CEN 3024C-27021 Software Development I
 * Instructor: Dr. Lisa Macon
 *
 * @author Guillermo Castaneda Echegaray
 * @version 1.11
 * @since 2020-01-11
 */
public class TextAnalyzerServer {
    ServerSocket serverSocket;
    Socket connection = null;
    ObjectOutputStream serverOut;
    ObjectInputStream serverIn;

    TextAnalyzerServer() {
    }

    void run() {
        try {
            serverSocket = new ServerSocket(9876);

            // Open the socket to accept requests
            connection = serverSocket.accept();

            serverIn = new ObjectInputStream(connection.getInputStream());

            // Received the URL from the client
            String targetUrl = null;

            try {
                targetUrl = (String) serverIn.readObject();
            } catch (ClassNotFoundException e) {
                System.out.println("An error occurred while communicating with client.");
            }

            // Exit the program if the user sends 'exit' as the URL
            assert targetUrl != null;

            if (targetUrl.equalsIgnoreCase("exit")) {
                serverIn.close();
                serverSocket.close();
                System.out.println("\n<== Client closed connection. Exiting.");
                System.exit(0);
            }

            String dateTime = TextAnalyzerServerController.getDateTime();

            System.out.println("\n<== Request received at " + dateTime + ".");
            System.out.println("URL received from client: " + targetUrl);
            System.out.print("\nProcessing. This could take a while... ");

            // Parse the target URL. Save words and their frequencies to the database.
            TextAnalyzerServerController.processRequest(targetUrl);

            int uniqueWords = 0;
            int totalWords = 0;

            try {
                uniqueWords = DatabaseController.getUniqueWordCount();
                totalWords = DatabaseController.getAllWordCount();
            } catch (SQLException e) {
                System.out.println("Database error fetching words and their frequencies from the database.");
            }

            if (totalWords > 0) {
                System.out.println("Done!");
                System.out.println("\n==> Sending data to client...");

                serverOut = new ObjectOutputStream(connection.getOutputStream());

                serverOut.writeObject(uniqueWords);
                serverOut.writeObject(totalWords);

                /*
                 * Query the database for the word pairs and send them to the
                 * outputStream. This worked better and more efficiently than
                 * trying to send the entire ObservableList<Word> because it
                 * is not serializable.
                 */
                try {
                    ResultSet wordPairs = DatabaseController.getAllWords();

                    while (wordPairs.next()) {
                        String wordContent = wordPairs.getString("wordContent");
                        int wordFrequency = wordPairs.getInt("wordFrequency");
                        serverOut.writeObject(wordContent);
                        serverOut.writeObject(wordFrequency);
                    }

                    wordPairs.close();
                } catch (SQLException e) {
                    System.out.println("Database error fetching words and their frequencies from the database.");
                }

                System.out.println("Data sent to client.\n\nTextAnalyzer Server ready for next request.");

                serverOut.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverIn.close();
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        TextAnalyzerServer server = new TextAnalyzerServer();

        System.out.println("TextAnalyzer Server ready. Listening for client request.");

        while (true) {
            server.run();
        }
    }
}
