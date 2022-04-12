package cc;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI {
    private final TCommsClient commsClient;

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
    private JComboBox moveTimeComboBox;
    private JComboBox evaluationTimeComboBox;
    private JComboBox appointmentTimeSpinner;
    private JComboBox paymentTimeComboBox;
    private JButton resetFormButton;
    private JTextField hostField;
    private JSpinner portSpinner;
    private JButton confirmLoginButton;

    public GUI(TCommsClient commsClient) {
        this.commsClient = commsClient;
        adultPatientsSpinner.setModel(new SpinnerNumberModel(10, 1, 50, 1));
        childrenPatientsSpinner.setModel(new SpinnerNumberModel(10, 1, 50, 1));
        seatsSpinner.setModel(new SpinnerNumberModel(4, 2, 10, 2));
        suspendButton.setEnabled(false);
        resumeButton.setEnabled(false);
        stopButton.setEnabled(false);
        allowPatientButton.setEnabled(false);

        if (UIManager.getLookAndFeel().getName().equals("GTK look and feel"))
            for (JSpinner spinner : new JSpinner[] {adultPatientsSpinner, childrenPatientsSpinner, seatsSpinner, portSpinner})
                spinner.setBorder(new LineBorder(new Color(39, 39, 39), 1, true));

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commsClient.startSim(10, 10, 4, 100, 100, 100, 100);
            }
        });
        confirmLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((CardLayout) cardPanel.getLayout()).next(cardPanel);
            }
        });
    }

    public static void setGUILook(String wantedLook) {
        LookAndFeelInfo[] looks = UIManager.getInstalledLookAndFeels();
        String chosenLook = null;
        for (LookAndFeelInfo look : looks)
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

    public void start() {
        JFrame frame = new JFrame("CCP");
        frame.setContentPane(this.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(380, 340));
        frame.setPreferredSize(new Dimension(280, 340));
        frame.pack();
        frame.setVisible(true);
    }
}
