package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import spellingQuiz.ModulePractise;
import spellingQuizUtil.FestivalSpeech;
import spellingQuizUtil.QuizState;
import spellingQuizUtil.Result;
import spellingQuizUtil.Words;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * This class stores only the display (GUI) functionalities of Practise Module
 */
public class ModulePractiseController extends ModuleBaseController {

    @FXML
    private TextField inputField;
    @FXML
    private Slider speechSpeed;
    @FXML
    private Button skipBtn;
    @FXML
    private ChoiceBox<Integer> numOfQCheckBox;


    /**
     * Setting up the fxml beforehand
     * Also, setting up the check box
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        settingUp();

        // add check box from 1 to number of words in the words list, maximum is 10
        for (int i = 1; i <= Math.min(Words.getNumOfWordsInWordsList(), 10); i++) {
            numOfQCheckBox.getItems().add(i);
        }
        // set default number to 3
        numOfQCheckBox.setValue(3);
    }

    /**
     * Start the quiz with number of question that user picked
     * And also update the display
     */
    @FXML
    protected void startQuiz() {
        // start a new game with specific number of questions
        quiz = new ModulePractise(numOfQCheckBox.getValue());
        updateStartDisplay();
    }

    /**
     * When the user press 'Enter' key or press the 'Check' button,
     * if the question is finished, go to next question
     * otherwise, check the spelling
     * But do not go to next question or check the spelling when it is speaking the word
     */
    @FXML
    protected void onEnter() {
        if (!inhibitSubmitAction) {
            if (quiz.quizStateEqualsTo(QuizState.READY)) {
                skipBtn.setDisable(false);
                newQuestion();
            } else {
                checkSpelling();
            }
        }
    }

    /**
     * Check the spelling, and then update the display
     * Only pause 2 seconds and go to next question if the user got the spelling correct
     */
    protected void checkSpelling() {
        String textColour;

        FestivalSpeech.setSpeechSpeed((int) speechSpeed.getValue());  // set up speech speed
        quiz.setUserInput(inputField.getText());  // get user input/spelling
        quiz.checkSpelling();

        // update the display

        // the quiz is done, so either Mastered, Faulted or Failed
        if (quiz.quizStateEqualsTo(QuizState.READY)) {

            // correct spelling (Mastered and Faulted)
            if (quiz.resultEqualsTo(Result.MASTERED) || quiz.resultEqualsTo(Result.FAULTED)) {
                textColour = GREEN;
                updateScore();
                pauseBetweenEachQ();

            // incorrect spelling (Failed) OR the word is skipped
            } else {
                textColour = RED;
            }

        // incorrect spelling (1st attempt)
        } else {
            textColour = RED;
            inputField.clear();
        }

        updateLabels(textColour);  // update the labels with corresponding text colour
    }

}
