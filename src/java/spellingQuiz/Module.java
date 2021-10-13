package spellingQuiz;

import spellingQuizUtil.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public abstract class Module {
    private final static String FESTIVAL_CMD_FILE = ".scm";


    protected int currentIndex, speechSpeed;
    protected String currentWord, mainLabelText, promptLabelText, userInput;
    protected QuizState currentQuizState;
    protected Result currentResult;
    protected static ModuleType moduleType;
    protected final Words words;
    protected EncouragingMessage correctMessage, incorrectMessage, tryAgainMessage;


    // this method will only run once and will run at the start of the program
    // create a file that will be used to run the festival
    public static void initialise() throws IOException {
        File file = new File(FESTIVAL_CMD_FILE);
        file.createNewFile();
    }

    // Constructor
    public Module(int numOfQuestions) {
        // setting up the words
        words = new Words(numOfQuestions);

        correctMessage = new EncouragingMessage("Correct");
        incorrectMessage = new EncouragingMessage("Incorrect");
        tryAgainMessage = new EncouragingMessage("TryAgain");
        currentIndex = 0;
        currentWord = "";
        mainLabelText = "";
        promptLabelText = "";
        setUserInput("");
        setQuizState(QuizState.ready);
        setResult(Result.mastered);

        // update the the total score
        Score.changeTotalScore(numOfQuestions);
    }

    // this function generate a new word and then ask the user
    public abstract boolean newQuestion();

    // this function check the spelling (input) and then set up a range of stuff
    public abstract void checkSpelling();

    // this function will speak out the message using bash and festival scm
    protected void speak(String englishMessage, String maoriMessage) {
        try {
            // write the festival command into .scm file
            PrintWriter writeFile = new PrintWriter(new FileWriter(FESTIVAL_CMD_FILE));

            // adjust the speed first

            // speak english / maori message if there is any
            if (!englishMessage.equals("")) {
                writeFile.println("(Parameter.set 'Duration_Stretch' " + (200 - speechSpeed) / 100.0 + ")");
                writeFile.println("(SayText \"" + englishMessage + "\")");
            }
            if (!maoriMessage.equals("")) {
                writeFile.println("(voice_akl_mi_pk06_cg)");  // change to maori voice
                writeFile.println("(Parameter.set 'Duration_Stretch' " + (200 - speechSpeed) / 100.0 + ")");
                writeFile.println("(SayText \"" + maoriMessage + "\")");
            }

            writeFile.close();

            // run festival schema file
            String command = "festival -b " + FESTIVAL_CMD_FILE;
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
            pb.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // this method will speak the word again, only the word
    public void speakWordAgain() {
        speak("", currentWord);
    }

    // QuizState's getter, setter and equals to
    protected void setQuizState(QuizState newQuizState) {
        currentQuizState = newQuizState;
    }

    private QuizState getQuizState() {
        return currentQuizState;
    }

    public boolean quizStateEqualsTo(QuizState quizState) {
        return getQuizState() == quizState;
    }

    // Result's getter, setter and equals to
    public void setResult(Result newResult) {
        currentResult = newResult;
    }

    private Result getResult() {
        return currentResult;
    }

    public boolean resultEqualsTo(Result result) {
        return getResult() == result;
    }

    // moduleType's getter, setter and equals to
    public static void setModuleType(ModuleType newModuleType) {
        moduleType = newModuleType;
    }

    private static ModuleType getModuleType() {
        return moduleType;
    }

    public static boolean moduleTypeEqualsTo(ModuleType newModuleType) {
        return getModuleType() == newModuleType;
    }

    // userInput's getter and setter
    public void setUserInput(String newUserInput) {
        userInput = newUserInput;
    }

    protected String getUserInput() {
        return userInput;
    }

    // speechSpeed's setter
    public void setSpeechSpeed(int speed) {
        speechSpeed = speed;
    }

    // mainLabelText's getter
    public String getMainLabelText() {
        return mainLabelText;
    }

    // promptLabelText's getter
    public String getPromptLabelText() {
        return promptLabelText;
    }

    // get number of letters of the current word
    public int getNumOfLettersOfWord() {
        return words.getNumOfLettersOfWord();
    }

    // score increases, the score multiplier depends on the result
    public void increaseScore() {
        if (resultEqualsTo(Result.mastered)) {
            Score.increaseBy(2);
        } else {
            Score.increaseBy(1);
        }
    }

}
