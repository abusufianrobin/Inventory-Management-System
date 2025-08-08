import db.MyJDBC;
import guis.Form;
import guis.LoginFormGUI;
import guis.RegisterFormGUI;

import javax.swing.*;

public class AppLauncher {
    public static void main(String[] args) {
        // Ensures that Swing components are updated on the Event Dispatch Thread (EDT).
        // All GUI-related work in Swing should be performed on the EDT.
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                // Instantiate a LoginFormGUI object and make it visible.
                // This is the starting point of your application's user interface.
                new LoginFormGUI().setVisible(true);

                // The following lines are commented out as they appear to be for testing JDBC methods.
                // They are not part of the standard application launch flow.
                // check user test
                //System.out.println(MyJDBC.checkUser("username1234"));

                // check register test
                // System.out.println(MyJDBC.register("username1234", "password"));

                // check validate login test
                //System.out.println(MyJDBC.validateLogin("username1234", "password"));
            }
        });
    }
}
