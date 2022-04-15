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

    private JPanel mainPanel;
    private JLabel statusLabel;
    private JButton startButton;
    private JButton suspendButton;
    private JButton resumeButton;
    private JButton stopButton;
    private JButton endButton;
    private JComboBox operatingModeComboBox;
    private JButton notifyButton;
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

        JFrame frame = new JFrame("CCP");
        frame.setContentPane(this.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(380, 340));
        frame.setPreferredSize(new Dimension(280, 340));
        frame.pack();
        frame.setVisible(true);

        adultPatientsSpinner.setModel(new SpinnerNumberModel(10, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
        childrenPatientsSpinner.setModel(new SpinnerNumberModel(10, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
        seatsSpinner.setModel(new SpinnerNumberModel(4, Integer.MIN_VALUE, Integer.MAX_VALUE, 2));
        portSpinner.setModel(new SpinnerNumberModel(8000, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
        hostField.setText("localhost");
        suspendButton.setEnabled(false);
        resumeButton.setEnabled(false);
        stopButton.setEnabled(false);
        notifyButton.setEnabled(false);

        if (UIManager.getLookAndFeel().getName().equals("GTK look and feel"))
            for (JSpinner spinner : new JSpinner[] {adultPatientsSpinner, childrenPatientsSpinner, seatsSpinner, portSpinner})
                spinner.setBorder(new LineBorder(new Color(39, 39, 39), 1, true));

        GUI gui = this;
        confirmLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String host = hostField.getText().trim();
                int port = (int) portSpinner.getValue();
                if (port > 65535) {
                    JOptionPane.showMessageDialog(null, "Port number is too high. Maximum is 65535.", "Input error", JOptionPane.WARNING_MESSAGE);
                } else if (port < 0) {
                    JOptionPane.showMessageDialog(null, "Port number is too low. Minimum is 0.", "Input error", JOptionPane.WARNING_MESSAGE);
                } else {
                    commsClient = new TCommsClient(host, port, gui);
                    commsClient.start();
                }
            }
        });
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int adults = (int) adultPatientsSpinner.getValue();
                int children = (int) childrenPatientsSpinner.getValue();
                int seats = (int) seatsSpinner.getValue();
                if (adults > 50) {
                    JOptionPane.showMessageDialog(null, "Adult count is too high. Maximum is 50.", "Input error", JOptionPane.WARNING_MESSAGE);
                } else if (adults < 0) {
                    JOptionPane.showMessageDialog(null, "Adult count is too low. Minimum is 0.", "Input error", JOptionPane.WARNING_MESSAGE);
                } else if (children > 50) {
                    JOptionPane.showMessageDialog(null, "Child count is too high. Maximum is 50.", "Input error", JOptionPane.WARNING_MESSAGE);
                } else if (children < 0) {
                    JOptionPane.showMessageDialog(null, "Child count is too low. Minimum is 0.", "Input error", JOptionPane.WARNING_MESSAGE);
                } else if (seats > 10) {
                    JOptionPane.showMessageDialog(null, "Seat count is too high. Maximum is 10.", "Input error", JOptionPane.WARNING_MESSAGE);
                } else if (seats < 2) {
                    JOptionPane.showMessageDialog(null, "Seat count is too low. Minimum is 2.", "Input error", JOptionPane.WARNING_MESSAGE);
                } else if ((seats & 1) == 1) {
                    JOptionPane.showMessageDialog(null, "Seat count has to be even.", "Input error", JOptionPane.WARNING_MESSAGE);
                } else {
                    seats /= 2;
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
                    resetFormButton.setEnabled(false);
                }
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
                notifyButton.setEnabled(false);
                while (!requests.isEmpty())
                    requests.get();
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
                    while (!requests.isEmpty())
                        commsClient.authorize(requests.get());
                    notifyButton.setEnabled(false);
                    commsClient.SwapAuto();
                }
            }
        });
        notifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!requests.isEmpty()) {
                    commsClient.authorize(requests.get());
                    if (requests.isEmpty())
                        notifyButton.setEnabled(false);
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
        notifyButton.setEnabled(true);
    }

    public void setStatusLabel(String status) {
        statusLabel.setText("Status: " + status);
    }

    public void connectionStatus(boolean successful) {
        if (successful) {
            ((CardLayout) cardPanel.getLayout()).next(cardPanel);
        } else {
            JOptionPane.showMessageDialog(null, "Connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
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
        resetFormButton.setEnabled(true);
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
}
