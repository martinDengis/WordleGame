import java.io.*;
import java.net.*;

/** Wordle project (part I).
 * 
 * @Course INFO0010 - Introduction to computer networking
 * @Instructor Pr. Guy Leduc
 * @author Martin Dengis (s193348)
 * @AcademicYear 2023-2024
 * --------------------------------------------------------
 * The ClientConnection class handles each individual client connection as a thread.
 * It implements the Runnable interface.
 * 
 * The class includes the following methods:
 * @constructor ClientConnection : Specify connection details (socket, secret word and connection ID).
 * @method run : Handle client's requests (QUIT, CHEAT, TRY x).
 * @method checkWord : Handles client's attempts to find secret word by calling
 *  @submethod isValidGuess : Return true is guess word is 5 letters long and exists, false otherwise
 *  @submethod responseConstructor : Return string representation of well-placed, misplaced 
 *                                  and wrong letters in guess word (see Project details)
 */
public class ClientConnection implements Runnable {
    private final Socket CLIENT_SOCKET;
    private final String SECRET_WORD;
    private final int CONNECTION_ID;

    public ClientConnection(Socket clienSocket, String secretWord, int connectionID) {
        this.CLIENT_SOCKET = clienSocket;
        this.SECRET_WORD = secretWord;
        this.CONNECTION_ID = connectionID;
    }

    @Override
    public void run() {
        try {
            // Set a socket timeout (for 2 min 30 sec)
            CLIENT_SOCKET.setSoTimeout(150000);
            CLIENT_SOCKET.setTcpNoDelay(true);

            // Initialize reader and writer
            BufferedReader reader = new BufferedReader(new InputStreamReader(CLIENT_SOCKET.getInputStream(), "UTF-8"));
            PrintWriter writer = new PrintWriter(CLIENT_SOCKET.getOutputStream(), true);

            String input;

            // Loop handling client's requests
            while ((input = reader.readLine()) != null) {
                if (input.equals("QUIT")) { break; } 
                else if (input.equals("CHEAT")) { 
                    writer.print(this.SECRET_WORD + "\r\n");
                    writer.flush(); 
                } 
                else if (input.startsWith("TRY")) {
                    String guess = input.substring(4).trim().toLowerCase();
                    String response = checkWord(guess);
                    writer.print(response + "\r\n");
                    writer.flush();
                } 
                else { 
                    writer.print("WRONG\r\n"); 
                    writer.flush();
                }
            }
        
            reader.close();
            writer.close();
            CLIENT_SOCKET.close();
        } 
        catch (SocketTimeoutException e1) { System.err.println("Client connection (connection ID: "+ this.CONNECTION_ID + ") timed out."); }
        catch (SocketException e2) { System.err.println("Client connection (connection ID: " + this.CONNECTION_ID + ") was reset."); }
        catch (IOException e3) { e3.printStackTrace(); }
    }

    private String checkWord(String guess) {
        if (!isValidGuess(guess)) { return "NONEXISTENT"; } 
        else { return responseConstructor(guess); }
    }
    
    private boolean isValidGuess(String guess) {
        // Check if the word is a valid 5-letter word
        return guess.length() == 5 && WordleWordSet.WORD_SET.contains(guess);
    }
    
    private String responseConstructor(String guess) {
        
        // Initialise response and tracking arrays
        char[] response = new char[5];
        boolean[] usedInGuess = new boolean[5];
        boolean[] usedInSecret = new boolean[5];
    
        // GREEN: Mark well-placed letters
        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == this.SECRET_WORD.charAt(i)) {
                response[i] = 'G';
                usedInGuess[i] = usedInSecret[i] = true;
            }
        }
    
        // YELLOW: Mark misplaced letters
        for (int i = 0; i < 5; i++) {
            if (!usedInGuess[i]) {
                for (int j = 0; j < 5; j++) {
                    if (!usedInSecret[j] && guess.charAt(i) == this.SECRET_WORD.charAt(j)) {
                        response[i] = 'Y';
                        usedInGuess[i] = usedInSecret[j] = true;
                        break;
                    }
                }
            }
        }
    
        // BLACK: Mark incorrect letters
        for (int i = 0; i < 5; i++)
            if (!usedInGuess[i]) { response[i] = 'B'; }
    
        return new String(response);
    }

}
