package com.careplus.main;

import javax.swing.SwingUtilities;

// TODO: import Member 4's LoginFrame once it's created
// import com.careplus.ui.LoginFrame;

public class ClientLauncher {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // TODO: new LoginFrame();
            System.out.println("CarePlus client started. Connect LoginFrame here.");
        });
    }
}