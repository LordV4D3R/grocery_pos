package fourty3.grocerypos.util;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.function.UnaryOperator;

public class MoneyInputUtils {

    private static final DecimalFormat INPUT_FORMATTER;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        INPUT_FORMATTER = new DecimalFormat("#,##0", symbols);
    }

    private MoneyInputUtils() {
    }

    public static void installMoneyFormatter(TextField textField) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            if (!change.isContentChange()) {
                return change;
            }

            String newText = change.getControlNewText();

            if (newText == null || newText.isEmpty()) {
                return change;
            }

            if (!newText.matches("[\\d\\s,\\.]*")) {
                return null;
            }

            String digitsOnly = normalizeDigits(newText);

            if (digitsOnly.isEmpty()) {
                return change;
            }

            int digitsBeforeCaret = countDigits(newText.substring(0, change.getCaretPosition()));
            String formattedText = formatWithGrouping(digitsOnly);
            int newCaretPosition = calculateCaretPosition(formattedText, digitsBeforeCaret);

            change.setText(formattedText);
            change.setRange(0, change.getControlText().length());
            change.setCaretPosition(newCaretPosition);
            change.setAnchor(newCaretPosition);

            return change;
        };

        textField.setTextFormatter(new TextFormatter<>(filter));
    }

    public static double parseMoney(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " không được để trống.");
        }

        String normalized = normalizeDigits(text.trim());

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " không hợp lệ.");
        }

        if (!normalized.matches("\\d+")) {
            throw new IllegalArgumentException(fieldName + " không hợp lệ.");
        }

        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " không hợp lệ.");
        }
    }

    public static String formatMoney(double amount) {
        long roundedAmount = Math.round(amount);
        return INPUT_FORMATTER.format(roundedAmount);
    }

    public static void setMoneyText(TextField textField, double amount) {
        textField.setText(formatMoney(amount));
    }

    private static String normalizeDigits(String text) {
        return text.replace(" ", "")
                .replace(",", "")
                .replace(".", "");
    }

    private static int countDigits(String text) {
        int count = 0;

        for (int i = 0; i < text.length(); i++) {
            if (Character.isDigit(text.charAt(i))) {
                count++;
            }
        }

        return count;
    }

    private static String formatWithGrouping(String digits) {
        StringBuilder builder = new StringBuilder();
        int count = 0;

        for (int i = digits.length() - 1; i >= 0; i--) {
            builder.insert(0, digits.charAt(i));
            count++;

            if (count % 3 == 0 && i > 0) {
                builder.insert(0, ',');
            }
        }

        return builder.toString();
    }

    private static int calculateCaretPosition(String formattedText, int digitsBeforeCaret) {
        if (digitsBeforeCaret <= 0) {
            return 0;
        }

        int digitCount = 0;

        for (int i = 0; i < formattedText.length(); i++) {
            if (Character.isDigit(formattedText.charAt(i))) {
                digitCount++;
            }

            if (digitCount == digitsBeforeCaret) {
                return i + 1;
            }
        }

        return formattedText.length();
    }
}