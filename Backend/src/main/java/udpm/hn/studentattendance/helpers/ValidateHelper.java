package udpm.hn.studentattendance.helpers;

import java.util.regex.Pattern;

public class ValidateHelper {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private static final String EMAIL_FE_REGEX = "^[A-Za-z0-9._%+-]+@fe.edu.vn$";

    private static final String EMAIL_FPT_REGEX = "^[A-Za-z0-9._%+-]+@fpt.edu.vn$";

    private static final String PHONE_REGEX = "^0[0-9]{9,10}$";

    public static boolean isValidPhoneNumber(String phoneNumber) {
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        return phoneNumber != null && pattern.matcher(phoneNumber).matches();
    }

    public static boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        return email != null && pattern.matcher(email).matches();
    }

    public static boolean isValidEmailFE(String email) {
        Pattern pattern = Pattern.compile(EMAIL_FE_REGEX);
        return email != null && pattern.matcher(email).matches();
    }

    public static boolean isValidEmailFPT(String email) {
        Pattern pattern = Pattern.compile(EMAIL_FPT_REGEX);
        return email != null && pattern.matcher(email).matches();
    }

}
