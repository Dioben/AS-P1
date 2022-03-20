package CC;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI {
    private JPanel mainPanel;
    private JLabel statusLabel;
    private JButton startButton;
    private JButton suspendButton;
    private JButton resumeButton;
    private JButton stopButton;
    private JButton endButton;
    private JComboBox operatingModeComboBox;
    private JButton allowPatientButton;
    private JPanel cardPanel;
    private JSpinner adultPatientsSpinner;
    private JSpinner childrenPatientsSpinner;
    private JSpinner seatsSpinner;
    private JComboBox moveTimeSpinner;
    private JComboBox evaluationTimeComboBox;
    private JComboBox appointmentTimeSpinner;
    private JComboBox paymentTimeSpinner;
    private JButton cancelFormButton;
    private JButton startFormButton;

    public GUI() {
        adultPatientsSpinner.setModel(new SpinnerNumberModel(10, 1, 50, 1));
        childrenPatientsSpinner.setModel(new SpinnerNumberModel(10, 1, 50, 1));
        seatsSpinner.setModel(new SpinnerNumberModel(4, 2, 10, 2));
        suspendButton.setEnabled(false);
        resumeButton.setEnabled(false);
        stopButton.setEnabled(false);
        allowPatientButton.setEnabled(false);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((CardLayout) cardPanel.getLayout()).next(cardPanel);
            }
        });
        startFormButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // startButton.setEnabled(false);
                ((CardLayout) cardPanel.getLayout()).next(cardPanel);
            }
        });
        cancelFormButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((CardLayout) cardPanel.getLayout()).next(cardPanel);
            }
        });
    }

    public void start() {
        JFrame frame = new JFrame("CCP");
        frame.setContentPane(this.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(380, 310));
        frame.setPreferredSize(new Dimension(400, 310));
        frame.pack();
        frame.setVisible(true);
    }

    public static void setGUILook(String wantedLook) {
        LookAndFeelInfo[] looks = UIManager.getInstalledLookAndFeels();
        String chosenLook = null;
        for (LookAndFeelInfo look: looks)
            if (wantedLook.equals(look.getName()))
                chosenLook = look.getClassName();
        if (chosenLook == null)
            chosenLook = UIManager.getSystemLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(chosenLook);
            JFrame.setDefaultLookAndFeelDecorated(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
