package cc;

import hc.MFIFO;
import hc.interfaces.IFIFO;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI {
    private TCommsClient commsClient;
    private final IFIFO<String> requests;
    private int requestN;

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
    private JComboBox appointmentTimeComboBox;
    private JComboBox paymentTimeComboBox;
    private JButton resetFormButton;
    private JTextField hostField;
    private JSpinner portSpinner;
    private JButton confirmLoginButton;

    public GUI() {
        requests = new MFIFO(String.class, 50);
        requestN = 0;

        adultPatientsSpinner.setModel(new SpinnerNumberModel(10, 1, 50, 1));
        childrenPatientsSpinner.setModel(new SpinnerNumberModel(10, 1, 50, 1));
        seatsSpinner.setModel(new SpinnerNumberModel(4, 2, 10, 2));
        portSpinner.setModel(new SpinnerNumberModel(8000, 0, 65535, 1));
        hostField.setText("localhost");
        suspendButton.setEnabled(false);
        resumeButton.setEnabled(false);
        stopButton.setEnabled(false);
        allowPatientButton.setEnabled(false);

        if (UIManager.getLookAndFeel().getName().equals("GTK look and feel"))
            for (JSpinner spinner : new JSpinner[] {adultPatientsSpinner, childrenPatientsSpinner, seatsSpinner, portSpinner})
                spinner.setBorder(new LineBorder(new Color(39, 39, 39), 1, true));

        GUI gui = this;
        confirmLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String host = hostField.getText().trim();
                int port = (int) portSpinner.getValue();
                commsClient = new TCommsClient(host, port, gui);
                commsClient.start();
                ((CardLayout) cardPanel.getLayout()).next(cardPanel);
            }
        });
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int adults = (int) adultPatientsSpinner.getValue();
                int children = (int) childrenPatientsSpinner.getValue();
                int seats = ((int) seatsSpinner.getValue())/2;
                int[] times = new int[4];
                JComboBox[] fields = new JComboBox[] {
                        evaluationTimeComboBox, appointmentTimeComboBox, paymentTimeComboBox, moveTimeComboBox
                };
                for (int i = 0; i < 4; i++)
                    switch ((String) fields[i].getSelectedItem()) {
                        case "0" -> times[i] = 0;
                        case "[0, 250]" -> times[i] = 250;
                        case "[0, 500]" -> times[i] = 500;
                        case "[0, 1000]" -> times[i] = 1000;
                        default -> times[i] = 100;
                    }
                commsClient.startSim(adults, children, seats, times[0], times[1], times[2], times[3]);
                setStatusLabel("Running");
                startButton.setEnabled(false);
                suspendButton.setEnabled(true);
                stopButton.setEnabled(true);

                adultPatientsSpinner.setEnabled(false);
                childrenPatientsSpinner.setEnabled(false);
                seatsSpinner.setEnabled(false);
                evaluationTimeComboBox.setEnabled(false);
                appointmentTimeComboBox.setEnabled(false);
                paymentTimeComboBox.setEnabled(false);
                moveTimeComboBox.setEnabled(false);
            }
        });
        suspendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commsClient.pauseSim();
                setStatusLabel("Suspended");
                suspendButton.setEnabled(false);
                resumeButton.setEnabled(true);
            }
        });
        resumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commsClient.resumeSim();
                setStatusLabel("Running");
                resumeButton.setEnabled(false);
                suspendButton.setEnabled(true);
            }
        });
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commsClient.stopSim();
                setStatusLabel("Stopped");
                setStopUIState();
            }
        });
        endButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commsClient.endSim();
            }
        });
        operatingModeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ("Manual".equals(operatingModeComboBox.getSelectedItem())) {
                    commsClient.SwapManual();
                } else {
                    while (requestN > 0) {
                        commsClient.authorize(requests.get());
                        requestN--;
                    }
                    allowPatientButton.setEnabled(false);
                    commsClient.SwapAuto();
                }
            }
        });
        allowPatientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (requestN > 0) {
                    commsClient.authorize(requests.get());
                    requestN--;
                    if (requestN == 0)
                        allowPatientButton.setEnabled(false);
                }
            }
        });
        resetFormButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adultPatientsSpinner.setValue(10);
                childrenPatientsSpinner.setValue(10);
                seatsSpinner.setValue(4);
                evaluationTimeComboBox.setSelectedIndex(1);
                appointmentTimeComboBox.setSelectedIndex(1);
                paymentTimeComboBox.setSelectedIndex(1);
                moveTimeComboBox.setSelectedIndex(1);
            }
        });
    }

    public void putRequest(String roomID) {
        requests.put(roomID);
        requestN++;
        allowPatientButton.setEnabled(true);
    }

    public void setStatusLabel(String status) {
        statusLabel.setText("Status: " + status);
    }

    public void setStopUIState() {
        stopButton.setEnabled(false);
        startButton.setEnabled(true);
        suspendButton.setEnabled(false);
        resumeButton.setEnabled(false);

        adultPatientsSpinner.setEnabled(true);
        childrenPatientsSpinner.setEnabled(true);
        seatsSpinner.setEnabled(true);
        evaluationTimeComboBox.setEnabled(true);
        appointmentTimeComboBox.setEnabled(true);
        paymentTimeComboBox.setEnabled(true);
        moveTimeComboBox.setEnabled(true);
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
