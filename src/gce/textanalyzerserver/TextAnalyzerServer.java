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

    public static void main(String[] args) {
        System.out.println("TextAnalyzer Server ready. Listening for client request.");

        startServer();
     }

    public static void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(9876);

            while (true) {

                try {
                    // Open the socket to accept requests
                    Socket socket = serverSocket.accept();

                    ObjectInputStream serverIn = new ObjectInputStream(socket.getInputStream());

                    // Received the URL from the client
                    String targetUrl = (String) serverIn.readObject();

                    // Exit the program if the user sends 'exit' as the URL
                    if (targetUrl.equalsIgnoreCase("exit")) {
                        serverIn.close();
                        break;
                    }

                    String dateTime = TextAnalyzerServerController.getDateTime();

                    System.out.println("\n<== Request received at " + dateTime + ".");
                    System.out.println("URL received from client: " + targetUrl);
                    System.out.print("\nProcessing. This could take a while... ");

                    // Connect to the database. Create the schema if it does not already exist.
                    // Truncate the word table
                    DatabaseController.createSchema();

                    // Parse the target URL. Save words and their frequencies to the database.
                    TextAnalyzerServerController.processRequest(targetUrl);

                    int uniqueWords = DatabaseController.getUniqueWordCount();
                    int totalWords = DatabaseController.getAllWordCount();

                    System.out.println("Done!");
                    System.out.println("\n==> Sending data to client...");

                    ObjectOutputStream serverOut = new ObjectOutputStream(socket.getOutputStream());

                    serverOut.writeObject(uniqueWords);
                    serverOut.writeObject(totalWords);

                    // Query the database for the word pairs
                    ResultSet wordPairs = DatabaseController.getAllWords();

                    // Sends each word and its frequency to the outputStream
                    // This worked better and more efficiently than trying to
                    // send the entire ObservableList<Word> because it is not
                    // serializable.
                    while (wordPairs.next()) {
                        String wordContent = wordPairs.getString("wordContent");
                        int wordFrequency = wordPairs.getInt("wordFrequency");
                        serverOut.writeObject(wordContent);
                        serverOut.writeObject(wordFrequency);
                    }

                    wordPairs.close();

                    System.out.println("Data sent to client.\n\nTextAnalyzer Server ready for next request.");

                    serverIn.close();
                    serverOut.close();
                    socket.close();
                } catch (IOException | ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
            }

            // Client sent "exit"
            serverSocket.close();

            System.out.println("\n<== Client closed connection. Exiting.");

            System.exit(2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
