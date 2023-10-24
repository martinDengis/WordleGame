import java.io.*;
import java.net.*;
import java.util.Scanner;

public class WordleClient {
    private static final String SERVER_ADDRESS = "localhost"; 
    private static final int SERVER_PORT = 2348;

    public static void main(String[] args) throws Exception {
        
        try {
            // Initialise socket, reading from socket, writing to socket
            Socket s = new Socket(SERVER_ADDRESS, SERVER_PORT);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter writer = new PrintWriter(s.getOutputStream(), true);
            System.out.println("-- Successful connection with server '" + SERVER_ADDRESS + "' on port " + SERVER_PORT + ".\n");

            // Initialise user input scanner
            Scanner scanner = new Scanner(System.in);
            System.out.println("1) Cheat\n2) Propose a word\n3) Quit\n");

            // Wordle game loop
            gameLoop(reader, writer, scanner);

            // Closing game
            scanner.close();
            writer.close();
            reader.close();
            s.close();
        } catch (IOException e) { 
            System.out.println("-- Error connecting with server '" + SERVER_ADDRESS + "' on port " + SERVER_PORT + ".\nMake sure server is running.\n");
            e.printStackTrace(); 
        }
   
    }

    private static void gameLoop(BufferedReader reader, PrintWriter writer, Scanner scanner) throws IOException {
        int attemptCounter = 0;

        while(attemptCounter < 6) {
            // Prompting user for input and checking input
            String input = userInputPrompt(scanner);
            int inputNumber = userInputCheck(input);

            // Valid entry
            if (inputNumber != 0) {
                // CHEAT
                if (inputNumber == 1) {
                    writer.print("CHEAT\r\n");
                    writer.flush();
                    getServerResponse(reader, attemptCounter);
                    continue;
                }

                // GUESS
                else if (inputNumber == 2) {
                    String guess = scanner.nextLine();

                    if (guess.length() == 5) {
                        String message = guess.toUpperCase();
                    
                        writer.print("TRY " + message + "\r\n"); // Send the message
                        writer.flush();

                        attemptCounter++; // Increment the attempt counter

                        String response = getServerResponse(reader, attemptCounter); // Read the response
                        System.out.println("DEBUG: Number of attempts left = " + (6-attemptCounter));

                        // If game finished, stop the game
                        if (response.contains("GAMEOVER")) break;
                    } else {
                        System.out.println("Incorrect entry. Please try a five-letter word.\n");
                        continue;
                    } 
                }

                // QUIT
                else {
                    writer.print("QUIT\r\n");
                    writer.flush(); 
                    return;                   
                } 
            } 

            // Invalid entry
            else { 
                System.out.println("Incorrect entry. Please choose number 1, 2 or 3.\n");
                continue;
            }

        }            
    }

    private static String userInputPrompt(Scanner scanner) {
        System.out.print("Your choice: ");
        return scanner.nextLine();
    }
    
    private static int userInputCheck(String input) {
        try{ 
            int inputNumber = Integer.parseInt(input); 
            if(inputNumber == 1 || inputNumber == 2 || inputNumber == 3) return inputNumber;
            else return 0;
        } catch (NumberFormatException e) { return 0; }
    }
    
    private static String getServerResponse(BufferedReader reader, int attemptNumber) throws IOException {
        String response = reader.readLine(); // Read the server's response
        response = response.toUpperCase();
        if(attemptNumber > 5 || response.equals("GGGGG")) response += " GAMEOVER";

        if(response.equals("GGGGG GAMEOVER")) response += "\n\nYOU WON!";
        else if (response.contains("GAMEOVER")) response += "\n\nYOU LOOSE!";
        
        System.out.println(response + "\n");
        return response;
    }

}
