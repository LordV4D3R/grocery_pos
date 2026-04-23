package fourty3.grocerypos.util;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class CurrencyUtils {

    private static final DecimalFormat FORMATTER;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        FORMATTER = new DecimalFormat("#,##0", symbols);
    }

    private CurrencyUtils() {
    }

    public static String formatVnd(double amount) {
        return FORMATTER.format(amount) + " VNĐ";
    }

    public static <T> Callback<TableColumn<T, Double>, TableCell<T, Double>> tableCellFactory() {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatVnd(item));
                }
            }
        };
    };
}
