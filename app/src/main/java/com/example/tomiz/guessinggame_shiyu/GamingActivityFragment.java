package com.example.tomiz.guessinggame_shiyu;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.media.SoundPool;
import android.media.MediaPlayer;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class GamingActivityFragment extends Fragment {

        // String used when logging error messages
        private static final String TAG = "CurrencyQuiz Activity";

        private static final int QUESTIONS_IN_QUIZ = 10;

        private List<String> fileNameList; // currency file names
        private List<String> quizCurrencyList; // countries in current quiz
        private Set<String> regionsSet; // world regions in current quiz
        private String correctAnswer; // correct answer for the current image
        private int totalGuesses; // number of guesses made
        private int correctAnswers; // number of correct guesses
        private int guessRows; // number of rows displaying guess Buttons
        private SecureRandom random; // used to randomize the quiz
        private Handler handler; // used to delay loading next image
        private Animation shakeAnimation; // animation for incorrect guess
        private static boolean soundOn = false; // sound default off

        private LinearLayout quizLinearLayout; // layout that contains the quiz
        private TextView questionNumberTextView; // shows current question #
        private ImageView questionImageView; // displays a quiz image
        private LinearLayout[] guessLinearLayouts; // rows of answer Buttons



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view =
                inflater.inflate(R.layout.fragment_gaming, container, false);

        fileNameList = new ArrayList<>();
        quizCurrencyList = new ArrayList<>();
        random = new SecureRandom();
        handler = new Handler();

        // load the shake animation that's used for incorrect answers
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.incorrect_shake);
        shakeAnimation.setRepeatCount(3); // animation repeats 3 times

        // get references to GUI components
        quizLinearLayout =
                (LinearLayout) view.findViewById(R.id.quizLinearLayout);
        questionNumberTextView =
                (TextView) view.findViewById(R.id.questionNumberTextView);
        questionImageView = (ImageView) view.findViewById(R.id.questionImageView);
        guessLinearLayouts = new LinearLayout[4];
        guessLinearLayouts[0] =
                (LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayouts[1] =
                (LinearLayout) view.findViewById(R.id.row2LinearLayout);
        guessLinearLayouts[2] =
                (LinearLayout) view.findViewById(R.id.row3LinearLayout);
        guessLinearLayouts[3] =
                (LinearLayout) view.findViewById(R.id.row4LinearLayout);

        // configure listeners for the guess Buttons
        for (LinearLayout row : guessLinearLayouts) {
            for (int column = 0; column < row.getChildCount(); column++) {
                Button button = (Button) row.getChildAt(column);
                button.setOnClickListener(guessButtonListener);
            }
        }

        // set questionNumberTextView's text
        questionNumberTextView.setText(
                getString(R.string.question, 1, QUESTIONS_IN_QUIZ));
        return view;
    }


    // update soundOn based on value in SharedPreferences
    public void updateSoundsOnOff(SharedPreferences sharedPreferences) {
        // get the number of guess buttons that should be displayed
        String soundOnOff =
                sharedPreferences.getString(GamingActivity.SOUNDS_ON_OFF, null);
        if (soundOnOff.compareToIgnoreCase("ON") == 0) {
            soundOn = true;
        }
    }

    // update guessRows based on value in SharedPreferences
    public void updateGuessRows(SharedPreferences sharedPreferences) {
        // get the number of guess buttons that should be displayed
        String choices =
                sharedPreferences.getString(GamingActivity.CHOICES, null);
        guessRows = Integer.parseInt(choices) / 2;

        // hide all quess button LinearLayouts
        for (LinearLayout layout : guessLinearLayouts)
            layout.setVisibility(View.GONE);

        // display appropriate guess button LinearLayouts
        for (int row = 0; row < guessRows; row++)
            guessLinearLayouts[row].setVisibility(View.VISIBLE);
    }

    // update world regions for quiz based on values in SharedPreferences
    public void updateRegions(SharedPreferences sharedPreferences) {
        regionsSet =
                sharedPreferences.getStringSet(GamingActivity.REGIONS, null);
    }

    // set up and start the next quiz
    public void resetQuiz() {
        // use AssetManager to get image file names for enabled regions
        AssetManager assets = getActivity().getAssets();
        fileNameList.clear(); // empty list of image file names

        try {
            // loop through each region
            for (String region : regionsSet) {
                // get a list of all Currency image files in this region
                String[] paths = assets.list(region);

                for (String path : paths) {
                    fileNameList.add(path.replace(".jpg", ""));
                }
            }
        }
        catch (IOException exception) {
            Log.e(TAG, "Error loading image file names", exception);
        }

        correctAnswers = 0; // reset the number of correct answers made
        totalGuesses = 0; // reset the total number of guesses the user made
        quizCurrencyList.clear(); // clear prior list of quiz countries

        int currencyCounter = 1;
        int numberOfCurrencies = fileNameList.size();

        // add QUESTIONS_IN_QUIZ random file names to the quizCountriesList
        while (currencyCounter <= QUESTIONS_IN_QUIZ) {
            int randomIndex = random.nextInt(numberOfCurrencies);

            // get the random file name
            String filename = fileNameList.get(randomIndex);

            // if the region is enabled and it hasn't already been chosen
            if (!quizCurrencyList.contains(filename)) {
                quizCurrencyList.add(filename); // add the file to the list
                ++currencyCounter;
            }
        }

        loadNextQuestion(); // start the quiz by loading the first question
    }

    // after the user guesses a correct Currency, load the next Currency image
    private void loadNextQuestion() {
        // get file name of the next Currency and remove it from the list
        String nextImage = quizCurrencyList.remove(0);
        correctAnswer = nextImage; // update the correct answer

        // display current question number
        questionNumberTextView.setText(getString(
                R.string.question, (correctAnswers + 1), QUESTIONS_IN_QUIZ));

        // extract the region from the next image's name
        String region = nextImage.substring(0, nextImage.indexOf('-'));

        // use AssetManager to load next image from assets folder
        AssetManager assets = getActivity().getAssets();

        // get an InputStream to the asset representing the next question
        // and try to use the InputStream
        try (InputStream stream =
                     assets.open(region + "/" + nextImage + ".jpg")) {
            // load the asset as a Drawable and display on the questionImageView
            Drawable question = Drawable.createFromStream(stream, nextImage);
            questionImageView.setImageDrawable(question);
        }
        catch (IOException exception) {
            Log.e(TAG, "Error loading " + nextImage, exception);
        }

        Collections.shuffle(fileNameList); // shuffle file names

        // put the correct answer at the end of fileNameList
        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));

        // add 2, 4, 6 or 8 guess Buttons based on the value of guessRows
        for (int row = 0; row < guessRows; row++) {
            // place Buttons in currentTableRow
            for (int column = 0;
                 column < guessLinearLayouts[row].getChildCount();
                 column++) {
                // get reference to Button to configure
                Button newGuessButton =
                        (Button) guessLinearLayouts[row].getChildAt(column);
                newGuessButton.setEnabled(true);

                // get country name and set it as newGuessButton's text
                String filename = fileNameList.get((row * 2) + column);
                newGuessButton.setText(getCurrencyName(filename));
            }
        }

        // randomly replace one Button with the correct answer
        int row = random.nextInt(guessRows); // pick random row
        int column = random.nextInt(2); // pick random column
        LinearLayout randomRow = guessLinearLayouts[row]; // get the row
        String countryName = getCurrencyName(correctAnswer);
        ((Button) randomRow.getChildAt(column)).setText(countryName);
    }

    // parses the Currency image file name and returns the Currency name
    private String getCurrencyName(String name) {
        return name.substring(name.indexOf('-') + 1).replace('_', ' ');
    }

    // slideIn the entire quizLinearLayout
    private void animate(boolean animateOut) {

        if (correctAnswers == 0)
            return;

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(quizLinearLayout, "alpha",0f);

        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                loadNextQuestion();
            }
        });
        fadeOut.setDuration(1000);
        ObjectAnimator mover = ObjectAnimator.ofFloat(quizLinearLayout,"translationY", 0.25f, 0f);
        mover.setDuration(1000);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(quizLinearLayout, "alpha",0f, 1f);
        fadeIn.setDuration(100);
        AnimatorSet animatorSet = new AnimatorSet();

        animatorSet.play(mover).with(fadeIn).after(fadeOut);
        animatorSet.start();

    }

    // called when a guess Button is touched
    private OnClickListener guessButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Button guessButton = ((Button) v);
            String guess = guessButton.getText().toString();
            String answer = getCurrencyName(correctAnswer);
            ++totalGuesses; // increment number of guesses the user has made

            if (guess.equals(answer)) { // if the guess is correct
                ++correctAnswers; // increment the number of correct answers

                Toast.makeText(getActivity(),
                        R.string.correct_message,
                        Toast.LENGTH_SHORT).show();

                disableButtons(); // disable all guess Buttons
                // if the user has correctly identified QUESTIONS_IN_QUIZ
                if (correctAnswers == QUESTIONS_IN_QUIZ) {
                    // DialogFragment to display quiz stats and start new quiz

                             AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                    alertDialog.setTitle("GAME RESULT:");
                    alertDialog.setMessage(
                            getString(R.string.results,
                                    totalGuesses,
                                    ((QUESTIONS_IN_QUIZ * 100) / (double) totalGuesses)));
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Play Again",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.dismiss();
                                    resetQuiz();
                                }
                            });
                    alertDialog.show();
                }




                else { // answer is correct but quiz is not over
                    // load the next question after a 2-second delay
                    handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    animate(true);
                                }
                            }, 2000); // 1000 milliseconds for 1-second delay
                }

            }
            else { // answer was incorrect

                questionImageView.startAnimation(shakeAnimation); // play shake

                Toast.makeText(getActivity(),
                        R.string.incorrect_message,
                        Toast.LENGTH_SHORT).show();


                if (soundOn) {
                    MainActivity.soundPool.play(MainActivity.sound1, 1, 1, 0, 0, 1);
                }

                guessButton.setEnabled(false); // disable incorrect answer

            }
        }
    };

    // utility method that disables all answer Buttons
    private void disableButtons() {
        for (int row = 0; row < guessRows; row++) {
            LinearLayout guessRow = guessLinearLayouts[row];
            for (int i = 0; i < guessRow.getChildCount(); i++)
                guessRow.getChildAt(i).setEnabled(false);
        }
    }
}
