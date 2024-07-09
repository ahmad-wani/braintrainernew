package com.example.braintrainer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    Button goButton;
    ArrayList<Integer> answers = new ArrayList<>();
    int locationOfCorrectAnswer;
    int correctAnswer;
    TextView result;
    int score = 0;
    int numberOfQuestions = 0;
    TextView scoreTextView;
    Button button0;
    Button button1;
    Button button2;
    Button buttonWordScramble;
    Button button3;
    TextView sumTextView;
    TextView timerTextView;
    Button playAgainButton;
    ConstraintLayout gameLayout;
    SharedPreferences sharedPreferences;
    // Add these member variables
    private String currentScrambledWord;
    private String currentCorrectWord;
    private int wordGameScore = 0;
    private ArrayList<String> unusedWords = new ArrayList<>();
    private ArrayList<String> usedWords = new ArrayList<>();
    TextView wordGameScoreTextView;
    private int currentLevel = 1;
    private final int maxLevel = 4; // Maximum number of levels
    private Map<Integer, List<String>> levelWords = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonWordScramble = findViewById(R.id.buttonWordScramble);
        sumTextView = findViewById(R.id.textView3);
        wordGameScoreTextView = findViewById(R.id.wordGameScoreTextView);
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        result = findViewById(R.id.textView4);
        scoreTextView = findViewById(R.id.textView2);
        timerTextView = findViewById(R.id.textView);
        playAgainButton = findViewById(R.id.button4);
        gameLayout = findViewById(R.id.gameLayout);
        goButton = findViewById(R.id.goButton);

        goButton.setVisibility(View.VISIBLE);
        gameLayout.setVisibility(View.INVISIBLE);

        sharedPreferences = this.getSharedPreferences("com.example.braintrainer", Context.MODE_PRIVATE);
        initializeLevelWords();
    }

    public void playAgain(View view) {
        score = 0;
        numberOfQuestions = 0;
        timerTextView.setText("30s");
        scoreTextView.setText(score + "/" + numberOfQuestions);
        newQuestion();
        playAgainButton.setVisibility(View.INVISIBLE);
        result.setText("");

        new CountDownTimer(30100, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerTextView.setText(millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                result.setText("Done!");
                playAgainButton.setVisibility(View.VISIBLE);

                int highScore = sharedPreferences.getInt("highScore", 0);
                if (score > highScore) {
                    sharedPreferences.edit().putInt("highScore", score).apply();
                    result.setText("New High Score: " + score);
                } else {
                    result.setText("Your Score: " + score + "\nHigh Score: " + highScore);
                }
                promptForName(score);
            }
        }.start();
    }

    public void chooseAns(View view) {
        if (Integer.toString(locationOfCorrectAnswer).equals(view.getTag().toString())) {
            result.setText("Correct!");
            score++;
        } else {
            result.setText("Wrong:(");
        }
        numberOfQuestions++;
        scoreTextView.setText(score + "/" + numberOfQuestions);
        newQuestion();
    }

    public void start(View view) {
        goButton.setVisibility(View.INVISIBLE);
        buttonWordScramble.setVisibility(View.INVISIBLE);
        hideWordScrambleUI();
        playAgainButton.setVisibility(View.INVISIBLE);
        playAgain(findViewById(R.id.textView));
        gameLayout.setVisibility(View.VISIBLE);

        int highScore = sharedPreferences.getInt("highScore", 0);
        Toast.makeText(this, "High Score: " + highScore, Toast.LENGTH_SHORT).show();
    }

    public void newQuestion() {
        Random rand = new Random();

        // Randomly choose the operation: 0 for addition, 1 for subtraction, 2 for multiplication, 3 for division
        int operationType = rand.nextInt(4);
        int a, b;

        switch (operationType) {
            case 0: // Addition
                a = rand.nextInt(21);
                b = rand.nextInt(21);
                sumTextView.setText(a + " + " + b);
                correctAnswer = a + b;
                break;
            case 1: // Subtraction
                a = rand.nextInt(21);
                b = rand.nextInt(21);
                sumTextView.setText(a + " - " + b);
                correctAnswer = a - b;
                break;
            case 2: // Multiplication
                a = rand.nextInt(13); // Reduce range for multiplication to keep numbers manageable
                b = rand.nextInt(13);
                sumTextView.setText(a + " * " + b);
                correctAnswer = a * b;
                break;
            case 3: // Division
                a = rand.nextInt(10) + 1; // Start from 1 to avoid division by zero
                b = (rand.nextInt(10) + 1) * a; // Ensure result is a whole number
                sumTextView.setText(b + " / " + a);
                correctAnswer = b / a;
                break;
            default:
                correctAnswer = 0; // Default case, should not be reached
        }

        generateAnswers();
    }

    private void generateAnswers() {
        Random rand = new Random();
        locationOfCorrectAnswer = rand.nextInt(4);
        answers.clear();

        for (int i = 0; i < 4; i++) {
            if (i == locationOfCorrectAnswer) {
                answers.add(correctAnswer);
            } else {
                // Generate plausible incorrect answers
                int wrongAnswer;
                do {
                    wrongAnswer = generateWrongAnswer(correctAnswer);
                } while (answers.contains(wrongAnswer) || wrongAnswer == correctAnswer);
                answers.add(wrongAnswer);
            }
        }

        button0.setText(Integer.toString(answers.get(0)));
        button1.setText(Integer.toString(answers.get(1)));
        button2.setText(Integer.toString(answers.get(2)));
        button3.setText(Integer.toString(answers.get(3)));
    }

    private int generateWrongAnswer(int correctAnswer) {
        Random rand = new Random();
        // Generate a number within a reasonable range around the correct answer
        return correctAnswer + rand.nextInt(21) - 10; // Example range, adjust as needed
    }

    private void promptForName(final int score) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Over! Score: " + score);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveScore(input.getText().toString(), score);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void saveScore(String name, int score) {
        // Concatenate the new score
        String scores = sharedPreferences.getString("scores", "");
        scores += name + ":" + score + ";";
        sharedPreferences.edit().putString("scores", scores).apply();
        showLeaderboard();
    }

    private void showLeaderboard() {
        String scores = sharedPreferences.getString("scores", "");
        String[] scoreArray = scores.split(";");
        Arrays.sort(scoreArray, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int score1 = Integer.parseInt(o1.split(":")[1]);
                int score2 = Integer.parseInt(o2.split(":")[1]);
                return Integer.compare(score2, score1); // Descending order
            }
        });
        StringBuilder leaderboard = new StringBuilder("Leaderboard:\n");
        for (String entry : scoreArray) {
            if (!entry.isEmpty()) {
                String[] parts = entry.split(":");
                leaderboard.append(parts[0]).append(" - ").append(parts[1]).append("\n");
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Leaderboard");
        builder.setMessage(leaderboard.toString());
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void initializeLevelWords() {
        levelWords.put(1, Arrays.asList("easy", "word", "list", "test"));
        levelWords.put(2, Arrays.asList("medium", "range", "harder", "longer"));
        levelWords.put(3, Arrays.asList("difficult", "challenge", "complex", "intricate"));
        levelWords.put(4, Arrays.asList("advanced", "pinnacle", "zenith", "apex"));
        setLevel(currentLevel);
    }

    private void setLevel(int level) {
        unusedWords.clear();
        unusedWords.addAll(levelWords.get(level));
        usedWords.clear();
        wordGameScore = 0;
    }

    public void startWordScramble(View view) {
        // Set up the Word Scramble game UI and logic
        buttonWordScramble.setVisibility(View.INVISIBLE);
        goButton.setVisibility(View.INVISIBLE);
        hideWordScrambleUI();
        setupWordScrambleGame();
    }

    private void hideWordScrambleUI() {
        // Hide the components related to the word scramble game
        TextView scrambledWordTextView = findViewById(R.id.scrambledWordTextView);
        EditText userGuessEditText = findViewById(R.id.userGuessEditText);
        Button submitGuessButton = findViewById(R.id.submitGuessButton);

        scrambledWordTextView.setVisibility(View.GONE);
        userGuessEditText.setVisibility(View.GONE);
        submitGuessButton.setVisibility(View.GONE);
    }

    private void setupWordScrambleGame() {
        // Check if there are no more words left in the current level
        if (unusedWords.isEmpty()) {
            if (currentLevel < maxLevel) {
                // Move to the next level
                currentLevel++;
                unusedWords.addAll(levelWords.get(currentLevel));
                Toast.makeText(this, "Level Completed! Moving to level " + currentLevel, Toast.LENGTH_SHORT).show();
                // Prompt to start the next level or show a dialog that allows the user to start the next level
                promptForNextLevel();
            } else {
                // All levels completed, show winning message
                showWinningMessage();
                return;
            }
        }
        // Continue with setting up the word scramble for the next word
        prepareNextScramble();
    }
    private void promptForNextLevel() {
        new AlertDialog.Builder(this)
                .setTitle("Level Complete!")
                .setMessage("You've completed Level " + (currentLevel - 1) + ". Move to Level " + currentLevel + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setupWordScrambleGame();
                    }
                })
                .show();
    }
    private void showWinningMessage() {
        new AlertDialog.Builder(this)
                .setTitle("Congratulations!")
                .setMessage("You've completed all levels. You're a winner!")
                .setPositiveButton("Play Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        restartGame();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish(); // Close the activity
                    }
                })
                .show();
        // Optionally call showGameOver here if you want to handle "game over" logic separately
    }


    private void prepareNextScramble() {
        Random random = new Random();
        int wordIndex = random.nextInt(unusedWords.size());
        currentCorrectWord = unusedWords.get(wordIndex);
        currentScrambledWord = scrambleWord(currentCorrectWord);
        unusedWords.remove(wordIndex);
        updateUIForWordScrambleGame();
    }


    private void updateUIForWordScrambleGame() {
        TextView scrambledWordTextView = findViewById(R.id.scrambledWordTextView);
        scrambledWordTextView.setText(currentScrambledWord);
        scrambledWordTextView.setVisibility(View.VISIBLE);

        wordGameScoreTextView.setText("Score: " + wordGameScore);
        wordGameScoreTextView.setVisibility(View.VISIBLE);

        EditText userGuessEditText = findViewById(R.id.userGuessEditText);
        userGuessEditText.setText("");
        userGuessEditText.setVisibility(View.VISIBLE);

        Button submitGuessButton = findViewById(R.id.submitGuessButton);
        submitGuessButton.setVisibility(View.VISIBLE);
    }
    private void showGameOver() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Over");
        builder.setMessage("Congratulations! Your final score is: " + wordGameScore + "\nWould you like to play again?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                restartGame(); // Call restartGame() to reset and restart the game
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Possibly exit the game or provide other options
                dialog.dismiss();
            }
        });

        AlertDialog gameOverDialog = builder.create();
        gameOverDialog.show();
    }


    private String scrambleWord(String word) {
        List<Character> characters = new ArrayList<Character>();
        for (char c : word.toCharArray()) {
            characters.add(c);
        }
        Collections.shuffle(characters);
        StringBuilder shuffledWord = new StringBuilder();
        for (char c : characters) {
            shuffledWord.append(c);
        }
        return shuffledWord.toString();
    }

    public void submitWordGuess(View view) {
        EditText userGuessEditText = findViewById(R.id.userGuessEditText);
        String userGuess = userGuessEditText.getText().toString();
        if (userGuess.equalsIgnoreCase(currentCorrectWord)) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            wordGameScore++; // Increase the score
            if (unusedWords.isEmpty() && currentLevel < maxLevel) {
                currentLevel++; // Advance to the next level
                Toast.makeText(this, "Level Up!", Toast.LENGTH_SHORT).show();
            }
            setupWordScrambleGame(); // Prepare the next scramble or level
        } else {
            Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show();
        }
    }
    private void restartGame() {
        // Reset score and level
        wordGameScore = 0;
        currentLevel = 1;
        // Reset the word lists
        unusedWords.clear();
        usedWords.clear();
        // Reinitialize the levels
        setLevel(currentLevel);
        // Setup the game
        setupWordScrambleGame();
    }

}
