package templates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class StandaloneUserInputHandler implements UserInputHandler, InputHandler {

    @Override
    public Boolean askYesNoQuestion(String question) {
        System.out.print(question + ": ");
        return readBooleanInput();
    }

    @Override
    public boolean askYesNoQuestion(String question, boolean defaultValue) {
        System.out.print(question + "[default: " + booleanToString(defaultValue) + "]: ");
        return readBooleanInput(defaultValue);
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

        return choices.get(readIntInput(defaultIdx, defaultOption.toString(), choices.size()));
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

    private static boolean readBooleanInput() {
        String response = formatResponse(readInput());
        while (true) {
            if (response.equalsIgnoreCase("yes")) {
                return true;
            } else if (response.equalsIgnoreCase("no")) {
                return false;
            } else {
                System.out.print("Please enter 'yes' or 'no': ");
            }
        }
    }

    private static boolean readBooleanInput(boolean defaultResponse) {
        String response = formatResponse(readInput());
        while (true) {
            if ("".equals(response)) {
                return defaultResponse;
            } else if (response.equalsIgnoreCase("yes")) {
                return true;
            } else if (response.equalsIgnoreCase("no")) {
                return false;
            } else {
                System.out.print("Please enter 'yes' or 'no' [default: " + booleanToString(defaultResponse) + "]: ");
            }
        }
    }

    private static int readIntInput(int defaultAnswer, String defaultString, int maxAnswer) {
        System.out.print("Enter selection (default: " + defaultString + ") [1.." + maxAnswer + "] ");
        int result = -1;
        while (result < 0) {
            String response = formatResponse(readInput());
            if ("".equals(response)) {
                result = defaultAnswer;
            } else {
                int parsedResponse = 0;
                try {
                    parsedResponse = Integer.parseInt(response);
                } catch (NumberFormatException ignore) {
                }
                if (parsedResponse < 1 || parsedResponse > maxAnswer) {
                    System.out.print("Please enter a value between 1 and " + maxAnswer + ": ");
                } else {
                    result = parsedResponse;
                }
            }
        }
        return result;
    }

    private static String booleanToString(boolean b) {
        if (b) {
            return "yes";
        } else {
            return "no";
        }
    }
}
