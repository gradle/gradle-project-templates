package org.gradlex.templates.standalone;

import org.gradlex.templates.InputHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class StandaloneUserInputHandler implements UserInputHandler, InputHandler {

    // TODO make behavior same as in the init plugin (questionnaire retry, etc.)

    @Override
    public Boolean askYesNoQuestion(String question) {
        System.out.print(question + ": ");
        String answer = formatResponse(readInput());
        return answer.contains("yes");
    }

    @Override
    public boolean askYesNoQuestion(String question, boolean defaultValue) {
        System.out.print(question + "[default: " + booleanToString(defaultValue) + "]: ");
        Boolean result = stringToBooleanOrNull(formatResponse(readInput()));
        if (result == null) {
            return defaultValue;
        } else {
            return result.booleanValue();
        }
    }

    @Override
    public <T> T selectOption(String question, Collection<T> options, T defaultOption) {
        Map<Integer, T> choices = new LinkedHashMap<>();
        int idx = 1;
        int defaultIdx = -1;
        for (T option : options) {
            if (option.equals(defaultOption)) {
                defaultIdx = idx;
            }
            choices.put(idx++, option);
        }

        if (defaultIdx < 0) {
            throw new RuntimeException("Invalid default option");
        }

        System.out.println(question + ":");
        for (int i = 1; i < idx; i++) {
            System.out.println("  " + i + ": " + choices.get(i));
        }

        System.out.print("Enter selection (default: " + defaultOption + ") [1.." + choices.size() + "] ");
        Integer response = stringToIntegerOrNull(formatResponse(readInput()));
        if (response == null || response <= 0  || response > idx) {
            return defaultOption;
        } else {
            return choices.get(response);
        }
    }

    @Override
    public String askQuestion(String question, String defaultValue) {
        System.out.println(question + ": ");
        String response = formatResponse(readInput());
        if ("".equals(response)) {
            return defaultValue;
        } else {
            return response;
        }
    }

    private static String readInput() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String formatResponse(String response) {
        return response.trim().toLowerCase(Locale.ROOT);
    }

    private static Boolean stringToBooleanOrNull(String formattedString) {
        if ("yes".equals(formattedString)) {
            return true;
        } else if ("no".equals(formattedString)) {
            return false;
        } else {
            return null;
        }
    }

    private static Integer stringToIntegerOrNull(String formattedString) {
        if ("".equals(formattedString)) {
            return null;
        }
        try {
            return Integer.parseInt(formattedString);
        } catch (Exception e) {
            return null;
        }
    }

    private static String booleanToString(boolean b) {
        if (b) {
            return "yes";
        } else {
            return "no";
        }
    }

    public static void main(String[] args) {
        //System.out.println(new StandaloneUserInputHandler().askYesNoQuestion("Would you date me if I'm dead ", true));
        System.out.println(new StandaloneUserInputHandler().selectOption("What's your favourite planet", Arrays.asList("Earth", "Sol"), "Earth"));

    }
}
