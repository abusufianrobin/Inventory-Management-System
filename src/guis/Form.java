package guis;

import constants.CommonConstants;

import javax.swing.*;

public class Form extends JFrame {
    // create constructor
    public Form(String title){
        // set the title of the title bar
        super(title);

        // set the size of the GUI
        setSize(520, 680);

        // configure GUI to end process after closing
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // set layout to null to disable layout management so we can use absolute positioning
        // to place the components wherever we want
        setLayout(null);

        // load GUI in the center of the screen
        setLocationRelativeTo(null);

        // prevent GUI from changing size
        setResizable(false);

        // change the background color of the GUI
        getContentPane().setBackground(CommonConstants.PRIMARY_COLOR);
    }
}


