package gce.textanalyzerserver;

import gce.textanalyzerserver.controller.Database;
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

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        try {
            System.out.println("TextAnalyzer Server ready. Listening for client request.");
            ServerSocket serverSocket = new ServerSocket(9876);

            while (true) {

                try {
                    // Open the socket to accept requests
                    Socket socket = serverSocket.accept();

                    ObjectInputStream serverIn = new ObjectInputStream(socket.getInputStream());

                    // Received the URL from the client
                    String targetUrl = (String) serverIn.readObject();
                    System.out.println("\n<== Request received. URL location of file to be parsed: " + targetUrl);

                    // Connect to the database. Create the schema if it does not already exist.
                    // Truncate the word table
                    Database.createSchema();

                    // Parse the target URL. Save words and their frequencies to the database.
                    TextAnalyzerServerController.processRequest(targetUrl);

                    int uniqueWords = Database.getUniqueWordCount();
                    int totalWords = Database.getAllWordCount();

                    System.out.println("\nDone parsing URL.");

                    ObjectOutputStream serverOut = new ObjectOutputStream(socket.getOutputStream());

                    serverOut.writeObject(uniqueWords);
                    serverOut.writeObject(totalWords);

                    // Query the database for the word pairs
                    ResultSet wordPairs = Database.getAllWords();

                    int rank = 0;

                    // Sends each word and its frequency to the outputStream
                    // This worked better and more efficiently than trying to
                    // send the entire ObservableList<Word> because it is not
                    // serializable.
                    // TODO: move to different method
                    while (wordPairs.next()) {
                        String wordContent = wordPairs.getString("wordContent");
                        int wordFrequency = wordPairs.getInt("wordFrequency");
                        serverOut.writeObject(wordContent);
                        serverOut.writeObject(wordFrequency);
                        System.out.println(++rank + ". " + wordContent + " (" + wordFrequency + ")");
                    }

                    wordPairs.close();

                    System.out.println("\nUnique words: " + uniqueWords + "  Total words: " + totalWords);
                    System.out.println("\n==> Data sent to client. Server ready for next request.");

                    serverIn.close();
                    serverOut.close();
                    socket.close();

                    // It never gets to this point as it continues to loop after only
                    // one click of the 'Analyze!' button.
                    if (targetUrl.equalsIgnoreCase("exit")) {
                        break;
                    }
                } catch (IOException e) {
                    System.out.println(e.toString());
                }
            }

            serverSocket.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}
