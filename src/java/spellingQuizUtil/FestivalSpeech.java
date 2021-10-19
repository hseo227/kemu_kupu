package spellingQuizUtil;

import fileManager.FileControl;

import java.io.IOException;
import java.util.ArrayList;

import static controllers.ModuleBaseController.indexOfSpeaking;
import static fileManager.FileManager.FESTIVAL_CMD_FILE;
import static controllers.ModuleBaseController.isSpeaking;

/**
 * This class contains all the methods that are related to festival tts
 */
public class FestivalSpeech {
    private static double speechSpeed;
    private static ArrayList<Integer> currentIndexOfSpeaking = new ArrayList<>();

    /**
     * Calculate the speech speed that festival will understand and set it
     * speechSpeed = 2.0 - SayText slower
     *               1.0 - SayText normal speed
     *               0.5 - SayText faster
     * @param speed This value is inverted, this is why (200 - speed)
     */
    public static void setSpeechSpeed(int speed) {
        speechSpeed = (200 - speed) / 100.0;
    }

    /**
     * Speak out / SayText the message using bash and festival scm
     * Speak the English and Maori words separately
     * Speak the English words first then followed by Maori words
     * Also speak both words/messages in selected speech speed
     * To SayText, first write the commands into .scm file and then run the scheme file
     *
     * @param englishMessage Message in English only
     * @param maoriMessage Message in Maori only
     */
    public static void speak(String englishMessage, String maoriMessage) {
        try {
            ArrayList<String> festivalCommands = new ArrayList<>();

            // speak english / maori message if there is any
            if (!englishMessage.equals("")) {
                // adjust the speed before say text
                festivalCommands.add("(Parameter.set 'Duration_Stretch' " + speechSpeed + ")");
                festivalCommands.add("(SayText \"" + englishMessage + "\")");
            }
            if (!maoriMessage.equals("")) {
                festivalCommands.add("(voice_akl_mi_pk06_cg)");  // change to maori voice
                // adjust the speed before say text
                festivalCommands.add("(Parameter.set 'Duration_Stretch' " + speechSpeed + ")");
                festivalCommands.add("(SayText \"" + maoriMessage.replace('-', ' ') + "\")");  // remove '-'
            }

            // write the festival commands into the scheme file
            FileControl.writeFile(FESTIVAL_CMD_FILE, festivalCommands);

            // speak/run festival scheme file
            String command = "festival -b " + FESTIVAL_CMD_FILE;
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
            Process process = pb.start();

            // when set isSpeaking to true, disable all the input related buttons
            currentIndexOfSpeaking.add(indexOfSpeaking++);
            indexOfSpeaking = indexOfSpeaking % 3;
            isSpeaking.get(currentIndexOfSpeaking.get(currentIndexOfSpeaking.size()-1)).set(true);
            new Thread(() -> {
                try {
                    process.waitFor();  // wait for the festival is done
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                isSpeaking.get(currentIndexOfSpeaking.remove(0)).set(false);  // un-disable the buttons after festival is done

            }).start();

        } catch (IOException e) {
            System.err.println("Failed to run linux command that speaks out the scheme file");
        }
    }

    /**
     * Kill all festival tts instantly
     */
    public static void shutDownAllFestival() {
        try {
            String command = "killall festival; killall aplay";
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
            pb.start();

        } catch (IOException e) {
            System.err.println("Failed to run linux command that stops the festival");
        }
    }
}
