package constants;

import java.awt.*;

public class CommonConstants {
    // color hex values
    public static final Color PRIMARY_COLOR = new Color(48, 63, 159);   // Indigo 700
    public static final Color SECONDARY_COLOR = new Color(232, 234, 246); // Indigo 50
    public static final Color TEXT_COLOR = new Color(255, 255, 255);

    // mysql credentials

    // place the url of your db in this format -> jdbc:mysql:ip_address/schema-name
    public static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/login_schema";
    // place the username that you made here (might be different)
    public static final String DB_USERNAME = "root";
    // place the password that you made here (might be different)
    public static final String DB_PASSWORD = "AJecDSgxvS8eai4#";

    // IMPORTANT: Changed to lowercase 'users' to match your SQL schema for case sensitivity
    public static final String DB_USERS_TABLE_NAME = "users";
}
