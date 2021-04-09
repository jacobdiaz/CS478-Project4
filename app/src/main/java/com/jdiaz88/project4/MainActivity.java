package com.jdiaz88.project4;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
// Resources: https://stackoverflow.com/questions/17472572/execute-two-threads-which-wait-one-for-the-other-while-main-thread-continues
// https://stackoverflow.com/questions/15530484/how-to-switch-between-two-thread-back-and-forth

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final Handler ticHandler = new Handler(Looper.myLooper());
    private int playerOneScoreCount, playerTwoScoreCount, roundCount;
    private TextView playerOneScoreTv, playerTwoScoreTv, playerWinningTv;
    private final Button[] cells = new Button[9];
    private Button resetGameBtn, runThreadBtn;
    boolean isRunning, isThreadGame; // Is thread game describes if it is a game against threads or a game against players
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
        runThreadBtn = (Button) findViewById(R.id.btn_StartThreads);
        resetGameBtn.setOnClickListener(this::onResetBtnClicked);
        runThreadBtn.setOnClickListener(this::onRunThreadBtnClicked);
        // Initialize Grid Cells
        initGrid();

        // TODO Set bold on current players turn

        // Initialize score
        playerOneScoreCount = 0;
        playerTwoScoreCount = 0;
        isRunning = true;
        isThreadGame = false; // Your able to play the game if you dont click run threads
        activePlayer = true;
        roundCount = 0;
    }


// Game Logic and UI


    // Reset the game board
    public void onResetBtnClicked(View v) {
        resetGame();
    }

    public void onRunThreadBtnClicked(View v) {
        Thread t1 = new Thread(new ThreadOne());
        Thread t2 = new Thread(new ThreadTwo());
        t1.start();
        t2.start();
    }

    public synchronized void incrementCount() {
        roundCount++;
    }

    public static class GlobalClass {
        public static boolean isThread1done = false;
        public static boolean isThread2done = false;
    }

    public class ThreadOne implements Runnable {
        public void methodAandB() {
                for (int i = 0; i < 5; i++) {
                    while (GlobalClass.isThread1done) {
                        //Do something
                    }
                    if (roundCount != 9) {
                        waitSeconds(1);
                        Log.i("test", "Thread (1): Inc round: " + roundCount);
                        incrementCount();
                    GlobalClass.isThread1done = true;
                    GlobalClass.isThread2done = false;
                }
            }
        }
        @Override
        public void run() {
            methodAandB();
        }
    }

    public class ThreadTwo implements Runnable {
        public void methodAorB() {
                for (int i = 0; i < 5; i++) {
                    while (GlobalClass.isThread2done) {
                        // Do Something
                    }
                    if (roundCount != 9) {
                        waitSeconds(1);
                        Log.i("test", "Thread (2): Inc round: " + roundCount);
                        incrementCount();
                        GlobalClass.isThread2done = true;
                        GlobalClass.isThread1done = false;
                    }
                }
        }

        @Override
        public void run() {
            methodAorB();
        }
    }

    // Whenever a cell is clicked update the ui and check whether someone has one
    @Override
    public void onClick(View v) {
        Button currentCell = (Button) v;
        while (isRunning && !isThreadGame) { // While playable and not a thread game
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
            else if (roundCount == cells.length) {
                playerWinningTv.setText("Tie!");
            }
        }
    }

    // Populate cells array and set listeners
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
                isRunning = false;
            }
        }
//        logBoard();

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

    // Reset the board
    public void resetGame() {
        roundCount = 0;
        activePlayer = true;
        isRunning = true;
        playerWinningTv.setText("");
        for (int i = 0; i < cells.length; i++) {
            gameState[i] = 2;
            cells[i].setText("");
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

    public void waitSeconds(long secondsToWait) {
        try {
            Thread.sleep(secondsToWait * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

// 0 1 2 3 4 5 6 7 8 Picks(4)
// 0 1 2 3 5 6 7 8 Picks(3)
// 0 1 2 5 6 7 8 Picks (2)
// 0 1 5 6 7 8 Picks(8)
// 0 1 5 6 7 Picks(0)
// 1 5 6 7

