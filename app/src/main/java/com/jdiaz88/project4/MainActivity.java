package com.jdiaz88.project4;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private int playerOneScoreCount, playerTwoScoreCount, roundCount;
    private TextView playerOneScoreTv, playerTwoScoreTv, playerWinningTv;
    private final Button[] cells = new Button[9];
    private Button resetGameBtn;
    boolean isRunning;
    boolean activePlayer; // player1 = 0 : player2 = 1

    // GameState -> (Player 1 = 0) (Player 2 = 1) (empty = 2)
    int[] gameState = {
            2, 2, 2,
            2, 2, 2,
            2, 2, 2
    };
    int[][] winningPositions = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // Rows
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // Cols
            {0, 4, 8}, {2, 4, 6}           // Diagnol
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide(); // Hides the title bar

        // Initialize UI elements
        playerOneScoreTv = (TextView) findViewById(R.id.playerOneScore);
        playerTwoScoreTv = (TextView) findViewById(R.id.playerTwoScore);
        playerWinningTv = (TextView) findViewById(R.id.playerWinning);
        resetGameBtn = (Button) findViewById(R.id.btn_ResetGame);

        resetGameBtn.setOnClickListener(this::onResetBtnClicked);

        // Initialize Grid Cells
        initGrid();

        // TODO Set bold on current players turn

        // Initialize score
        playerOneScoreCount = 0;
        playerTwoScoreCount = 0;
        isRunning = true;
        activePlayer = true;
        roundCount = 0;
    }




    // Reset the game board
    public void onResetBtnClicked(View v) {
        roundCount = 0;
        activePlayer = true;
        isRunning = true;
        for (int i = 0; i < cells.length; i++) {
            gameState[i] = 2;
            cells[i].setText("");
        }
    }

    // Whenever a cell is clicked update the ui and check whether someone has one
    @Override
    public void onClick(View v) {
        Button currentCell = (Button) v;
        while (isRunning) {
            // If the cell has already been clicked -> return
            if (!((Button) v).getText().toString().equals("")) {
                return;
            }
            String cellID = v.getResources().getResourceEntryName(v.getId()); // returns -> cell_2
            int gameStatePointer = Integer.parseInt(cellID.substring(cellID.length() - 1)); //returns -> 2

            // If no one has one
            if (!checkForWinner()) {
                if (activePlayer) { // If player 1 turn
                    currentCell.setText("X");
                    ((Button) v).setTextColor(Color.parseColor("#ff375f"));
                    // Change the state of the game
                    gameState[gameStatePointer] = 0;
                } else { // If player 2 turn
                    currentCell.setText("O");
                    ((Button) v).setTextColor(Color.parseColor("#007bff"));
                    // Change the state of the game
                    gameState[gameStatePointer] = 1;
                }
                roundCount++;
                activePlayer = !activePlayer; // Toggle Player turn
            }

            // If someone wins!
            if (checkForWinner()) {
                Toast.makeText(this, "Winner!", Toast.LENGTH_SHORT).show();
            }

            // If tie
            if (roundCount == cells.length) {
                playerWinningTv.setText("Tie!");
            }

        }
    }

    // initalize the grid
    public void initGrid() {
        for (int i = 0; i < cells.length; i++) {
            String cellID = "cell_" + i;
            int resourceId = getResources().getIdentifier(cellID, "id", getPackageName());
            cells[i] = (Button) findViewById(resourceId);

            // Add a listener for each cell
            cells[i].setOnClickListener(this);
        }
    }

    // Comparing the current state of the game with any possible winning state
    public boolean checkForWinner() {
        boolean result = false;

        // For every position in winning positions
        for (int[] winningPosition : winningPositions) {
            if (gameState[winningPosition[0]] == gameState[winningPosition[1]] &&
                    gameState[winningPosition[1]] == gameState[winningPosition[2]] &&
                    gameState[winningPosition[0]] != 2) {
                result = true;
                updateScores();
                isRunning=false;
            }
        }
        logBoard();

        return result;
    }

    // Update UI
    public void updateScores() {
        int player = activePlayer ? 1 : 0;

        // Player 1
        if (player == 0) {
            playerOneScoreCount += 1;
            playerOneScoreTv.setText(String.valueOf(playerOneScoreCount));
            playerWinningTv.setText("Player 1 Wins!");
        }
        // Player 2
        else {
            playerTwoScoreCount += 1;
            playerTwoScoreTv.setText(String.valueOf(playerTwoScoreCount));
            playerWinningTv.setText("Player 2 Wins!");
        }
    }

    // Helper functions
    public void logBoard() {
        String result = "\n";
        int counter = 1;
        for (int i = 0; i < gameState.length; i++) {
            if (counter % 3 == 0) {
                result += String.valueOf(gameState[i]);
                result += "\n";
            } else {
                result += String.valueOf(gameState[i]);
            }
            counter++;
        }
        Log.i("test", "Board:\n\n" + result);
    }
}

