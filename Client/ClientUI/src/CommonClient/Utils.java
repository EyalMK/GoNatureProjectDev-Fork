package CommonClient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static final String[] departmentReports = new String[]{"Visitations", "Cancellations"};
    public static Map<String, String> departmentReportsMap = new HashMap<String, String>() {{
        put("Visitations Statistics", "visitation");
        put("Cancellations Statistics", "cancellation");
    }};

    public static Map<String, String> parkManagerReportsMap = new HashMap<String, String>() {{
        put("Number of Visitors Statistics", "numofvisitors");
        put("Capacity Statistics", "usage");
    }};



    public static Boolean isIDValid(String ID) {
        // Check if the string length is exactly 9 characters
        if (ID == null) {
            return false;
        }
        if (ID.length() != 9) {
            return false;
        }

        // Check if each character is a digit
        return checkContainsDigitsOnly(ID);
    }

    public static Boolean checkContainsDigitsOnly(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Converts a date and time string to a {@link Timestamp} object.
     *
     * @param date The date string in ISO local date format.
     * @param time The time string in 24-hour format.
     * @return A {@link Timestamp} representing the combined date and time.
     */


    public static boolean isOrderTimeValid(String date, String time) {

        // Combine Date and Time Strings
        String dateTimeString = date + "T" + time;
        // Format of the date-time string
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        // Parse the date-time string
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);

        // Get the current date-time
        LocalDateTime now = LocalDateTime.now();

        // Calculate the duration between the given date-time and now
        Duration duration = Duration.between(now, dateTime);

        // Check if the duration is more than 24 hours
        return duration.toMinutes() <= 1440;
    }

    /**
     * Generates a list of time slots for the time selection combo box.
     * Time slots range from the provided start and end parameters.
     *
     * @param start The start time of the time slots.
     * @param end   The time slots will start from this time.
     */
    public static ObservableList<String> setComboBoxHours(int start, int end) {
        ArrayList<String> al = new ArrayList<String>();
        for (int i = start; i <= end; i++) {
            if (i < 10) {
                al.add("0" + i + ":00");
            } else {
                al.add("" + i + ":00");
            }
        }
        return FXCollections.observableArrayList(al);
    }
}
