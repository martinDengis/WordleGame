import java.io.IOException;

/**
 * The HTML class is responsible for generating the HTML page for the Wordle game.
 * It includes methods to generate the entire page, the Wordle board, keyboard layout, styles, and other necessary components.
 */
public class HTML {
    
    /**
     * Generates the Wordle game HTML page with the given game state.
     * This overloaded method calls the primary method with an empty error message.
     * 
     * @param gameState the current game state
     * @return the HTML page as a String
     */
    public String generateWordlePage(String gameState) {
        return generateWordlePage(gameState, "");
    }

    /**
     * Generates the Wordle game HTML page with the given game state and an optional error message.
     * This method orchestrates the creation of the entire page including the header, Wordle board, keyboard, and error messages if any.
     * 
     * @param gameState    the current game state
     * @param errorMessage an optional error message to display on the page
     * @return the HTML page as a String
     */
    public String generateWordlePage(String gameState, String errorMessage) {
        // Image to base64
        String base64Image = "";
        try { base64Image = ImageEncoder.encodeImageToBase64("logo.png"); } 
        catch (IOException e) { e.printStackTrace(); } 
        
        String title = "Wordle Game";
        String header = "<header><img src=\"data:image/png;base64,"+base64Image+"\" alt=\"WORDLE\"></header>";
        String wordleBoard = "";
        
        boolean isNewGame = true;
        String[] parts = gameState.split(";");
        // -1:secret:secret;0:guess:color;1:guess:color;2:guess:color;3:guess:color;4:guess:color;5:guess:color
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].equals(i-1 + "::")) {
                isNewGame = false;
                break;
            }
        }
        if (isNewGame) { wordleBoard = generateWordleBoard(); } // For a new game 
        else { wordleBoard = generateWordleBoardWithState(gameState); } // For a returning player

        // Generate HTML for displaying the error message
        String errorHtml = "";
        if (errorMessage != null && !errorMessage.isEmpty())
            errorHtml = "<div class=\"error-message\">" + errorMessage + "</div>";


        String keyboard = generateKeyboard();
        String styles = generateStyles();
        String fallbackForm = fallbackForm();

        String modalHtml =
                            "<div id='gameModal' class='modal'>" +
                            "   <div class='modal-content'>" +
                            "       <span class='close'>&times;</span>" +
                            "       <div id='modalText'></div>" +
                            "   </div>" +
                            "</div>";

        String fillCell = 
                            "function fillCell(key) {" +
                            "    const cell = document.getElementById(`cell-${currentRow}-${currentCell}`);" +
                            "    if (cell) {" +
                            "        cell.textContent = key;" +
                            "        cell.classList.add('filled');" +
                            "        cell.style.animation = 'none';" +
                            "        setTimeout(() => {" +
                            "            cell.style.animation = '';" +
                            "        }, 10);" +
                            "        currentCell++;" +
                            "    }" +
                            "    const allRows = document.querySelectorAll('.word-row');" +
                            "    allRows.forEach(row => row.classList.remove('highlight-row'));" +
                            "    const currentRowDiv = document.getElementById(`row-${currentRow}`);" +
                            "    if (currentRowDiv) {" +
                            "        currentRowDiv.classList.add('highlight-row');" +
                            "    }" +
                            "    if (currentCell === 1 && key) {" +
                            "        const firstRowDiv = document.getElementById(`row-0`);" +
                            "        if (firstRowDiv) {" +
                            "            firstRowDiv.classList.add('highlight-row');" +
                            "        }" +
                            "    }" +
                            "}";

        String highlightCurrentRowFunction =
                            "function highlightCurrentRow() {" +
                            "    const allRows = document.querySelectorAll('.word-row');" +
                            "    allRows.forEach(row => row.classList.remove('highlight-row'));" +
                            "    const currentRowDiv = document.getElementById(`row-${currentRow}`);" +
                            "    if (currentRowDiv) {" +
                            "        currentRowDiv.classList.add('highlight-row');" +
                            "    }" +
                            "}";

        String removeLastLetterFunction = 
                            "function removeLastLetter() {" +
                            "    if (currentCell > 0) {" +
                            "        currentCell--;" +
                            "        currentGuess = currentGuess.slice(0, -1);" +
                            "        const cell = document.getElementById(`cell-${currentRow}-${currentCell}`);" +
                            "        if (cell) {" +
                            "            cell.textContent = '';" +
                            "            cell.classList.remove('filled');" +
                            "        }" +
                            "    }" +
                            "}";  
                        
        String keydownEventListener =
                            "document.addEventListener('keydown', (event) => {" +
                            "    const keyName = event.key.toUpperCase();" +
                            "    if (keyName.match(/^[A-Z]$/) && currentGuess.length < 5) {" +
                            "        event.preventDefault();" +
                            "        keyPressed(keyName);" +
                            "    } else if (keyName === 'ENTER') {" +
                            "        event.preventDefault();" +
                            "        if (currentGuess.length === 5) {" +
                            "            onSubmitGuess();" +
                            "        } else {" +
                            "            alert('Your word is incomplete. Please enter 5 letters.');" +
                            "        }" +
                            "    } else if (keyName === 'BACKSPACE' && currentGuess.length > 0) {" +
                            "        event.preventDefault();" +
                            "        removeLastLetter();" +
                            "    }" +
                            "});";                      
                                
        String keyPressedFunction =
                            "function keyPressed(key) {" +
                            "    if (!key.match(/^[A-Z]$/)) return; " + 
                            "    if (currentGuess.length < 5) {" +
                            "        currentGuess += key;" +
                            "        fillCell(key);" +
                            "    }" +
                            "}";                      
        
        String onSubmitGuess = 
                            "function onSubmitGuess() {" +
                            "  if (currentGuess.length === 5) {" +
                            "      sendGuess(currentGuess);" +
                            "  } else {" +
                            "      alert('Your word is incomplete. Please enter 5 letters.');" +
                            "  }" +
                            "}";
        
        String onEraseFunction =
                            "function onErase() {" +
                            "    removeLastLetter();" +
                            "}";

        String sendGuess =                             
                            "function sendGuess(guess) {" +
                            "    const isJSEnabled = typeof window.addEventListener === 'function';" +
                            "    const headers = new Headers({"+
                            "        'JS-Enabled': isJSEnabled.toString(),"+
                            "        'Row': currentRow.toString()"+
                            "    });"+
                            "    console.log('Sending guess:', guess, 'Row:', currentRow);" + // Console log for debugging
                            "    fetch(`/play.html/guess?word=${guess}`, {" +
                            "        method: 'GET'," +
                            "        headers: headers" +
                            "    })" +
                            "    .then(response => response.json())" + // Processing text response
                            "    .then(data => {" +
                            "        console.log(data);" +
                            "        return data;" +
                            "    })" +
                            "    .then(processServerResponse)" +
                            "    .catch(error => {" +
                            // "        console.error('Error:', error);" +
                            "        alert('An error occurred while submitting the guess.');" +
                            "    });" +
                            "}";

        String processServerResponse =
                            "function processServerResponse(response) {"+
                            "   switch(response.Status) {" +
                            "       case 'Invalid':" +
                            "           alert(response.Message);" +
                            "           break;" +
                            "       case 'Gameover':" +
                            "           userLost(response.Message);" +
                            "           break;" +
                            "       case 'Win':" +
                            "           userWon(response.Message);" +
                            "           break;" +
                            "       case 'Playing':" +
                            "           var parts = response.Message.split(':');" +
                            "           updateBoardWithFeedback(parts[2]);"+
                            "       default:" +
                            "           console.error('Unknown status from server');" +
                            "    }"+
                            "}";

        String updateBoardWithFeedback =
                            "function updateBoardWithFeedback(feedback) {" +
                            "    const alreadyUsedKeys = {};" + // Track used keys
                            "    for (let i = 0; i < feedback.length; i++) {" +
                            "        const cell = document.getElementById(`cell-${currentRow}-${i}`);" +
                            "        const keyLetter = currentGuess[i].toUpperCase();" +
                            "        if (!alreadyUsedKeys[keyLetter]) {" + // Initialize tracking object for each letter
                            "            alreadyUsedKeys[keyLetter] = { 'green': 0, 'yellow': 0, 'darkened': 0 };" +
                            "        }" +
                            "        if (cell) {" +
                            "            cell.className = 'word-cell'; " + // Reset to default class
                            "            switch (feedback.charAt(i)) {" +
                            "                case 'G':" +
                            "                    cell.classList.add('green');" +
                            "                    alreadyUsedKeys[keyLetter].green++;" +
                            "                    break;" +
                            "                case 'Y':" +
                            "                    cell.classList.add('yellow');" +
                            "                    alreadyUsedKeys[keyLetter].yellow++;" +
                            "                    break;" +
                            "                case 'B':" +
                            "                    cell.classList.add('darkened');" +
                            "                    alreadyUsedKeys[keyLetter].darkened++;" +
                            "                    break;" +
                            "            }" +
                            "        }" +
                            "    }" +
                            "    updateKeyboard(alreadyUsedKeys);" + // Call to update keyboard
                            "    currentRow++;" + // Prepare for the next guess
                            "    currentGuess = '';" +
                            "    currentCell = 0;" +
                            "    highlightCurrentRow();" +
                            "};";
                        
        String updateKeyboard =
                            "function updateKeyboard(alreadyUsedKeys) {" +
                            "    for (const [keyLetter, counts] of Object.entries(alreadyUsedKeys)) {" +
                            "        const key = document.querySelector(`.key[data-letter='${keyLetter}']`);" +
                            "        if (key) {" +
                            "            key.classList.remove('green', 'yellow', 'darkened');" + // Reset color classes
                            "            if (counts.green > 0) {" +
                            "                key.classList.add('green');" +
                            "            } else if (counts.yellow > 0) {" +
                            "                key.classList.add('yellow');" +
                            "            } else if (counts.darkened > 0) {" +
                            "                key.classList.add('darkened');" +
                            "            }" +
                            "        }" +
                            "    }" +
                            "}";
                        
        String showModalFunction =
                            "function showModal(message) {" +
                            "  var gameModal = document.getElementById('gameModal');" +
                            "  var modalText = document.getElementById('modalText');" +
                            "  modalText.innerHTML = message;" +
                            "  gameModal.style.display = 'block';" +
                            "}";
        
        String userWonFunction = 
                            "function userWon() {" +
                            "    showModal('<p>Congratulations, You Won!</p><button onclick=\"restartGame()\">Restart Game</button>');" +
                            "}";
                        
        String userLostFunction = 
                            "function userLost(secretWord) {" +
                            "    showModal('<p>GAME OVER. Secret word was ' + secretWord.toUpperCase() + '</p><button onclick=\"restartGame()\">Restart Game</button>');" +
                            "}";         
        
        String restartGameFunction = 
                            "function restartGame() {" +
                            "    fetch('/play.html/reset', { method: 'GET' })" +
                            "    .then(() => window.location.reload());" +
                            "}";                   

        String closeModalFunction =
                            "var span = document.getElementsByClassName('close')[0];" +
                            "span.onclick = function() {" +
                            "  var gameModal = document.getElementById('gameModal');" +
                            "  gameModal.style.display = 'none';" +
                            "};"; 

        String script = "<script>" +
                        highlightCurrentRowFunction +
                        fillCell + 
                        removeLastLetterFunction +
                        onEraseFunction + 
                        keyPressedFunction +
                        showModalFunction+
                        userWonFunction +
                        userLostFunction +
                        processServerResponse +
                        sendGuess +
                        onSubmitGuess + 
                        updateKeyboard +
                        updateBoardWithFeedback +
                        closeModalFunction + 
                        restartGameFunction +
                        keydownEventListener +
                        "document.addEventListener('DOMContentLoaded', (event) => {" + // Fallback form
                        "  var fallbackForm = document.getElementById('fallbackForm');" +
                        "  if (fallbackForm) {" +
                        "    fallbackForm.style.display = 'none';" +
                        "  }" +
                        "});" +
                        "highlightCurrentRow();" +
                        "</script>";

        return "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "<meta charset=\"UTF-8\">\n" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "<link rel=\"icon\" type=\"image/x-icon\" href=\"data:image/x-icon;,\">\n" + // Empty favicon
            "<title>" + title + "</title>\n" +
            "<style>" + styles + "</style>\n" +
            "</head>\n" +
            "<body>\n" +
            header +
            errorHtml + 
            "<div id=\"wordle-board\">" + wordleBoard + "</div>\n" +
            "<div id=\"keyboard\">" + keyboard + "</div>\n" +
            fallbackForm +
            modalHtml +
            script +
            "</body>\n" +
            "</html>";
    }

    /**
     * Generates a blank Wordle board for a new game.
     * This method constructs the initial empty board layout used at the start of a new game.
     * 
     * @return a string representing the initial empty Wordle board HTML
     */
    private String generateWordleBoard() {
        StringBuilder boardBuilder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            boardBuilder.append("<div class=\"word-row\" id=\"row-").append(i).append("\">");
            for (int j = 0; j < 5; j++) {
                boardBuilder.append("<div class=\"word-cell\" id=\"cell-").append(i).append("-").append(j).append("\"></div>");
            }
            boardBuilder.append("</div>");
        }
    
        // Initialize the game state for a new game
        String scriptUpdate = "<script>let currentRow = 0; let currentGuess = ''; let currentCell = 0;</script>";
    
        return boardBuilder.toString() + scriptUpdate;
    }
    
    /**
     * Generates a Wordle board based on the given game state.
     * This method creates the board layout with guesses and color feedback for a game in progress.
     * 
     * @param gameState the game state represented as a string
     * @return a string representing the Wordle board HTML with the current game state
     */
    private String generateWordleBoardWithState(String gameState) {
        StringBuilder boardBuilder = new StringBuilder();
        String[] tries = gameState.split(";");
        int lastFilledRow = -1;
        boolean gameEnded = false;
        boolean playerWon = false;
        String secretWord = "";

        // Extract the secret word
        if (tries.length > 0) {
            String[] firstPart = tries[0].split(":");
            if (firstPart.length > 2) {
                secretWord = firstPart[2]; // Assuming the format is -1:secret:secretWord
            }
        }

        for (int i = 1; i < tries.length; i++) {
            String[] parts = tries[i].split(":");
            String guess = (parts.length > 1) ? parts[1].toUpperCase() : "";
            String color = (parts.length > 2) ? parts[2] : "";

            if (!guess.isEmpty() && !color.isEmpty()) {
                lastFilledRow = i - 1; // Update last filled row
            }
            
            boolean isCurrentRow = i-1 == lastFilledRow + 1; // Check if this is the current row
            String rowClass = isCurrentRow ? "word-row highlight-row" : "word-row";

            boardBuilder.append("<div class=\"").append(rowClass).append("\" id=\"row-").append(i-1).append("\">");

            for (int j = 0; j < 5; j++) {
                char letter = guess.length() > j ? guess.charAt(j) : ' ';
                char colorCode = color.length() > j ? color.charAt(j) : ' ';
                String colorClass = getColorClass(colorCode);
                String cellContent = letter != ' ' ? String.valueOf(letter) : "";
                String cellClass = "word-cell" + (colorClass.isEmpty() ? "" : " " + colorClass);

                boardBuilder.append("<div class=\"" + cellClass + "\" id=\"cell-")
                            .append(i-1)
                            .append("-")
                            .append(j)
                            .append("\">")
                            .append(cellContent)
                            .append("</div>");
            }
            boardBuilder.append("</div>");

            // Check for win condition
            if (color.equals("GGGGG")) {
                gameEnded = true;
                playerWon = true;
            }
        }
        
        System.out.println("lastFilledRow: " + lastFilledRow);

        if (!gameEnded && lastFilledRow == 5) gameEnded = true;

        if (gameEnded) {
            String modalMessage = playerWon ? "Congratulations, You Won!" : "Game Over. The correct word was: " + secretWord.toUpperCase();
            boardBuilder.append("<form action='/restart' method='post'>")
                        .append("<div class=\"modal\" style=\"display: block;\">")
                        .append("<p>").append(modalMessage).append("</p>")
                        .append("<input type='submit' value='Restart Game'>")
                        .append("</div></form>");
        }

        // Update currentRow and currentGuess in the script
        String scriptUpdate = "<script>let currentRow = " + (lastFilledRow + 1) + "; let currentGuess = ''; let currentCell = 0;</script>";
    
        return boardBuilder.toString() + scriptUpdate;
    }    
    
    /**
     * Returns the color class corresponding to the given color code.
     * 
     * @param colorCode the color code ('G' for green, 'Y' for yellow, 'B' for darkened)
     * @return the color class corresponding to the given color code, or an empty string if the color code is invalid
     */
    private String getColorClass(char colorCode) {
        switch (colorCode) {
            case 'G': return "green";
            case 'Y': return "yellow";
            case 'B': return "darkened";
            default: return "";
        }
    }    

    /**
     * Generates the HTML representation of the keyboard.
     * 
     * @return the HTML representation of the keyboard as a String
     */
    private String generateKeyboard() {
        String[][] keyRows = {
            {"A", "Z", "E", "R", "T", "Y", "U", "I", "O", "P"},
            {"Q", "S", "D", "F", "G", "H", "J", "K", "L", "M"},
            {"⇦", "W", "X", "C", "V", "B", "N", "⏎"}
        };

        StringBuilder keyboardBuilder = new StringBuilder();
        for (String[] keyRow : keyRows) {
            keyboardBuilder.append("<div class=\"keyboard-row\">");
            for (String key : keyRow) {
                // Add the data-letter attribute to each key except special keys like '⇦' and '⏎'
                if (!key.equals("⇦") && !key.equals("⏎")) {
                    keyboardBuilder.append("<button class='key' data-letter='").append(key)
                                .append("' onclick='keyPressed(\"").append(key).append("\")'>")
                                .append(key).append("</button>");
                } else {
                    // Handle special keys without the data-letter attribute
                    keyboardBuilder.append("<button class=\"key special-key\" onclick=\"")
                                .append(key.equals("⇦") ? "onErase()" : "onSubmitGuess()")
                                .append("\">").append(key).append("</button>");
                }
            }
            keyboardBuilder.append("</div>\n");
        }
        return keyboardBuilder.toString();
    }

    /**
     * Returns a fallback HTML form for the Wordle game.
     * This form is displayed when JavaScript is disabled in the user's browser.
     * The form allows the user to submit a guess for the game.
     *
     * @return the HTML form as a string
     */
    public String fallbackForm() {
        return "<noscript>" +
                "<form action=\"/play.html/guess\" method=\"POST\">" +
                "<input type=\"text\" name=\"guess\" required pattern=\"[A-Za-z]{5}\" maxlength=\"5\">" +
                "<input type=\"submit\" value=\"Submit Guess\">" +
                "</form>" +
                "</noscript>"; 
    }

    /**
     * Generates the styles for the HTML page.
     *
     * @return The generated styles as a String.
     */
    private String generateStyles() {
        return "body { font-family: Arial, sans-serif; background-color: #121213; color: white; }" +
                "header { text-align: center; padding: 20px; }" +
                "#wordle-board { margin-bottom: 20px; }" +
                ".word-row { display: flex; justify-content: center; margin-bottom: 5px; }" +
                ".word-cell {" +
                "   width: 50px;" +
                "   height: 50px;" + 
                "   background-color: #3a3a3c;" +
                "   margin: 2px;" +
                "   animation: popIn 0.3s;" +
                "}" +
                ".error-message{" +
                "   text-align: center;"+
                "}" +
                "@keyframes popIn {" +
                "   0% { transform: scale(0); }" +
                "   50% { transform: scale(1.2); }" +
                "   100% { transform: scale(1); }" +
                "}" +
                "#keyboard { margin-bottom: 20px; }" +
                ".keyboard-row { text-align: center; }" +
                ".key { margin: 5px; width: 40px; height: 40px; }" +
                ".key.green { background-color: #6aaa64; color: white; }" + 
                ".key.yellow { background-color: #c9b458; color: white; }" + 
                ".key.darkened { background-color: #787c7e; color: white; }" + 
                "form { text-align: center; }" + 
                "form input[type='text'] { margin: 0 5px; }" +
                "form input[type='submit'] { margin: 0 5px; }" + 
                ".word-cell {" +
                "   width: 50px;" +
                "   height: 50px;" +
                "   background-color: #3a3a3c;" +
                "   margin: 2px;" +
                "   display: flex;" +
                "   justify-content: center;" +
                "   align-items: center;" +
                "   font-size: 24px;" + 
                "   color: white;" + 
                "   font-weight: bold;" + 
                "}" + 
                ".word-cell.green { background-color: #6aaa64; }" +
                ".word-cell.yellow { background-color: #c9b458; }" +
                ".word-cell.darkened { background-color: #262626; }" +
                ".highlight-row {" +
                "   box-sizing: border-box;" +
                "   border: 2px solid rgba(255, 255, 255, 0.4); /* Light border for highlighting */" +
                "   border-radius: 10px; /* Rounded corners for the highlight */" +
                "}" +
                ".modal {" +
                "   display: none;" +
                "   position: fixed;" + 
                "   z-index: 1;" + 
                "   left: 0;" +
                "   top: 0;" +
                "   width: 100%;" +
                "   height: 100%;" +
                "   overflow: auto; " +
                "   background-color: rgb(0,0,0);" +
                "   background-color: rgba(0,0,0,0.4);" +
                "   backdrop-filter: blur(8px);" +
                "}" +
                ".modal-content {" +
                "   background-color: #fefefe;" +
                "   margin: 15% auto;" +
                "   padding: 20px;" + 
                "   border: 1px solid #888;" +
                "   width: 50%; " +
                "   text-align: center;" +
                "   box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2);" +
                "   animation: animatetop 0.4s;" +
                "   color: black;" +
                "}" +
                "@keyframes animatetop {" +
                "   from {top: -300px; opacity: 0}" +
                "   to {top: 0; opacity: 1}" +
                "}" +
                ".close {" +
                "   color: #aaa;" +
                "   float: right;" +
                "   font-size: 28px;" +
                "   font-weight: bold;" +
                "}" +
                ".close:hover," +
                ".close:focus {" +
                "   color: black;" +
                "   text-decoration: none;" +
                "   cursor: pointer;" +
                "}" ;
    }

}
