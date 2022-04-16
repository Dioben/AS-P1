package hc;

import hc.active.TCommsServer;
import hc.interfaces.IFIFO;
import hc.queue.MFIFO;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static java.lang.Math.ceil;

public class TGUI extends Thread {

    private final IFIFO<Map<String, String[]>> updates;
    private final JFrame frame;
    private JLayeredPane[] enHARSeats;
    private JLayeredPane[] enHCRSeats;
    private JLayeredPane[] wHARSeats;
    private JLayeredPane[] wHCRSeats;
    private final ImageIcon[] loadedIcons = new ImageIcon[8];
    private final JLabel[] loadedIconLabels = new JLabel[700];

    private JPanel mainPanel;
    private JLabel inLine;
    private JLabel outLine;
    private JLabel inLabel;
    private JLabel midLine1;
    private JLabel midLine2;
    private JLabel midLine3;
    private JLabel midLine4;
    private JLabel outLabel;
    private JPanel entranceHallPanel;
    private JPanel evaluationHallPanel;
    private JPanel waitingHallPanel;
    private JPanel medicalHallPanel;
    private JPanel paymentHallPanel;
    private JPanel enHAdultRoomPanel;
    private JPanel enHChildRoomPanel;
    private JPanel evHRoom1Panel;
    private JPanel evHRoom2Panel;
    private JPanel evHRoom3Panel;
    private JPanel evHRoom4Panel;
    private JPanel wHAdultRoomPanel;
    private JPanel wHChildRoomPanel;
    private JPanel mHWaitingRoomPanel;
    private JPanel mHChildRoom1Panel;
    private JPanel mHChildRoom2Panel;
    private JPanel mHAdultRoom1Panel;
    private JPanel mHAdultRoom2Panel;
    private JPanel pHEntranceHallPanel;
    private JPanel pHCashierPanel;
    private JLayeredPane pHCSeat;
    private JLayeredPane enHARSeat1;
    private JLayeredPane enHARSeat2;
    private JLayeredPane enHARSeat3;
    private JLayeredPane enHARSeat4;
    private JLayeredPane enHARSeat5;
    private JLayeredPane enHCRSeat1;
    private JLayeredPane enHCRSeat2;
    private JLayeredPane enHCRSeat3;
    private JLayeredPane enHCRSeat4;
    private JLayeredPane enHCRSeat5;
    private JLayeredPane evHR1Seat;
    private JLayeredPane evHR2Seat;
    private JLayeredPane evHR3Seat;
    private JLayeredPane evHR4Seat;
    private JLayeredPane wHARSeat1;
    private JLayeredPane wHARSeat2;
    private JLayeredPane wHARSeat3;
    private JLayeredPane wHARSeat4;
    private JLayeredPane wHARSeat5;
    private JLayeredPane wHCRSeat1;
    private JLayeredPane wHCRSeat2;
    private JLayeredPane wHCRSeat3;
    private JLayeredPane wHCRSeat4;
    private JLayeredPane wHCRSeat5;
    private JLayeredPane mHWRAdultSeat;
    private JLayeredPane mHWRChildSeat;
    private JLayeredPane mHCR1Seat;
    private JLayeredPane mHCR2Seat;
    private JLayeredPane mHAR1Seat;
    private JLayeredPane mHAR2Seat;
    private JLayeredPane pHOverflow1;
    private JLayeredPane pHOverflow2;
    private JLayeredPane pHOverflow3;
    private JLayeredPane enHAROverflow1;
    private JLayeredPane enHAROverflow2;
    private JLayeredPane enHAROverflow3;
    private JLayeredPane enHCROverflow1;
    private JLayeredPane enHCROverflow2;
    private JLayeredPane enHCROverflow3;
    private JLayeredPane wHAROverflow1;
    private JLayeredPane wHAROverflow2;
    private JLayeredPane wHAROverflow3;
    private JLayeredPane wHCROverflow1;
    private JLayeredPane wHCROverflow2;
    private JLayeredPane wHCROverflow3;
    private JLabel pHOverflowLabel;
    private JLabel enHAROverflowLabel;
    private JLabel wHAROverflowLabel;
    private JPanel cardPanel;
    private JButton confirmLoginButton;
    private JSpinner portSpinner;
    private JLabel enHCROverflowLabel;
    private JLabel wHCROverflowLabel;
    private JLabel loadingLabel;

    /**
     * Constructor of TGUI
     * <p>
     * Initializes the frame, variables and contains the action listener for the
     * login button
     */
    public TGUI() {
        TGUI gui = this;
        updates = new MFIFO(Map.class, 50);

        frame = new JFrame("HCP");
        frame.setContentPane(this.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1020, 450));
        frame.setPreferredSize(new Dimension(1020, 450));
        frame.pack();
        frame.setVisible(true);
        portSpinner.setModel(new SpinnerNumberModel(8000, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
        ImageIcon loading = new ImageIcon("resources/loading.gif");
        loadingLabel.setIcon(loading);
        setBorders();

        String[] imagePaths = new String[] {
                "resources/child.png", "resources/adult.png",
                "resources/childRed.png", "resources/adultRed.png",
                "resources/childYellow.png", "resources/adultYellow.png",
                "resources/childBlue.png", "resources/adultBlue.png"
        };
        for (int i = 0; i < 8; i++)
            loadedIcons[i] = new ImageIcon(imagePaths[i]);

        confirmLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int port = (int) portSpinner.getValue();
                if (port > 65535) {
                    JOptionPane.showMessageDialog(null, "Port number is too high. Maximum is 65535.", "Input error",
                            JOptionPane.WARNING_MESSAGE);
                } else if (port < 0) {
                    JOptionPane.showMessageDialog(null, "Port number is too low. Minimum is 0.", "Input error",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    new TCommsServer(port, gui).start();
                    ((CardLayout) cardPanel.getLayout()).next(cardPanel);
                }
            }
        });
    }

    /**
     * Makes the TGUI start listening for updates requests and update the UI
     */
    public void run() {
        Map<String, String[]> handling;
        JLayeredPane[] WTHAdultSeats = {
                wHAROverflow1, wHAROverflow2, wHAROverflow3
        };
        JLayeredPane[] WTHChildSeats = {
                wHCROverflow1, wHCROverflow2, wHCROverflow3
        };
        String[] WTHAdultRedValue = new String[0];
        String[] WTHAdultYellowValue = new String[0];
        String[] WTHAdultBlueValue = new String[0];
        String[] WTHChildRedValue = new String[0];
        String[] WTHChildYellowValue = new String[0];
        String[] WTHChildBlueValue = new String[0];
        int assigned;
        int overflow;
        int type;
        while (!Thread.interrupted()) {
            handling = updates.get();
            if (handling != null) {
                for (Map.Entry<String, String[]> entry : handling.entrySet()) {
                    JLayeredPane[] seats = new JLayeredPane[0];
                    switch (entry.getKey()) {
                        case "ETHA":
                            seats = new JLayeredPane[] {
                                    enHAROverflow1, enHAROverflow2, enHAROverflow3
                            };
                            type = 2;
                            break;
                        case "ETHC":
                            seats = new JLayeredPane[] {
                                    enHCROverflow1, enHCROverflow2, enHCROverflow3
                            };
                            type = 3;
                            break;
                        case "ET1":
                            seats = enHARSeats;
                            type = 1;
                            break;
                        case "ET2":
                            seats = enHCRSeats;
                            type = 1;
                            break;
                        case "EVR1":
                            seats = new JLayeredPane[] {
                                    evHR1Seat
                            };
                            type = 1;
                            break;
                        case "EVR2":
                            seats = new JLayeredPane[] {
                                    evHR2Seat
                            };
                            type = 1;
                            break;
                        case "EVR3":
                            seats = new JLayeredPane[] {
                                    evHR3Seat
                            };
                            type = 1;
                            break;
                        case "EVR4":
                            seats = new JLayeredPane[] {
                                    evHR4Seat
                            };
                            type = 1;
                            break;
                        case "WTHAR":
                            WTHAdultRedValue = entry.getValue();
                            type = 0;
                            break;
                        case "WTHAY":
                            WTHAdultYellowValue = entry.getValue();
                            type = 0;
                            break;
                        case "WTHAB":
                            WTHAdultBlueValue = entry.getValue();
                            type = 0;
                            break;
                        case "WTHCR":
                            WTHChildRedValue = entry.getValue();
                            type = 0;
                            break;
                        case "WTHCY":
                            WTHChildYellowValue = entry.getValue();
                            type = 0;
                            break;
                        case "WTHCB":
                            WTHChildBlueValue = entry.getValue();
                            type = 0;
                            break;
                        case "WTR1":
                            seats = wHARSeats;
                            type = 1;
                            break;
                        case "WTR2":
                            seats = wHCRSeats;
                            type = 1;
                            break;
                        case "MDW1":
                            seats = new JLayeredPane[] {
                                    mHWRChildSeat
                            };
                            type = 1;
                            break;
                        case "MDW2":
                            seats = new JLayeredPane[] {
                                    mHWRAdultSeat
                            };
                            type = 1;
                            break;
                        case "MDR1":
                            seats = new JLayeredPane[] {
                                    mHCR1Seat
                            };
                            type = 1;
                            break;
                        case "MDR2":
                            seats = new JLayeredPane[] {
                                    mHCR2Seat
                            };
                            type = 1;
                            break;
                        case "MDR3":
                            seats = new JLayeredPane[] {
                                    mHAR1Seat
                            };
                            type = 1;
                            break;
                        case "MDR4":
                            seats = new JLayeredPane[] {
                                    mHAR2Seat
                            };
                            type = 1;
                            break;
                        case "PYH":
                            seats = new JLayeredPane[] {
                                    pHOverflow1, pHOverflow2, pHOverflow3
                            };
                            type = 4;
                            break;
                        case "PYR":
                            seats = new JLayeredPane[] {
                                    pHCSeat
                            };
                            type = 1;
                            break;
                        default:
                            System.out.println("GUI got unknown entry: " + entry.getKey());
                            type = 0;
                    }
                    if (type == 1) {
                        String[] value = entry.getValue();
                        for (int i = 0; i < seats.length; i++) {
                            if (i < value.length)
                                changeSeat(seats[i], value[i]);
                            else
                                changeSeat(seats[i], null);
                        }
                    } else if (type > 1) {
                        String[] value = entry.getValue();
                        if (value.length < 3) {
                            String[] temp = new String[3];
                            System.arraycopy(value, 0, temp, 0, value.length);
                            value = temp;
                        }
                        overflow = 0;
                        for (int i = 0; i < value.length; i++) {
                            if (i < seats.length) {
                                changeSeat(seats[i], value[i]);
                            } else if (value[i] != null) {
                                overflow++;
                            } else {
                                break;
                            }
                        }
                        if (type == 2)
                            enHAROverflowLabel.setText("+" + overflow);
                        else if (type == 3)
                            enHCROverflowLabel.setText("+" + overflow);
                        else
                            pHOverflowLabel.setText("+" + overflow);
                    }
                }
                assigned = 0;
                overflow = 0;
                for (String[] value : new String[][] {
                        WTHAdultRedValue, WTHAdultYellowValue, WTHAdultBlueValue
                }) {
                    for (int i = 0; i < value.length || i < 3; i++) {
                        if (assigned < 3) {
                            if (i < value.length && value[i] != null) {
                                changeSeat(WTHAdultSeats[assigned], value[i]);
                                assigned++;
                            } else {
                                changeSeat(WTHAdultSeats[assigned], null);
                            }
                        } else if (i < value.length && value[i] != null) {
                            overflow++;
                        }
                    }
                }
                for (int i = assigned; i < 3; i++)
                    changeSeat(WTHAdultSeats[i], null);
                wHAROverflowLabel.setText("+" + overflow);
                assigned = 0;
                for (String[] value : new String[][] {
                        WTHChildRedValue, WTHChildYellowValue, WTHChildBlueValue
                }) {
                    for (int i = 0; i < value.length || i < 3; i++) {
                        if (assigned < 3) {
                            if (i < value.length && value[i] != null) {
                                changeSeat(WTHChildSeats[assigned], value[i]);
                                assigned++;
                            } else {
                                changeSeat(WTHChildSeats[assigned], null);
                            }
                        } else if (i < value.length && value[i] != null) {
                            overflow++;
                        }
                    }
                }
                for (int i = assigned; i < 3; i++)
                    changeSeat(WTHChildSeats[i], null);
                wHCROverflowLabel.setText("+" + overflow);
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        }
    }

    /**
     * Clears a JLayeredPane seat and loads the patient given
     *
     * @param seat        the seat to change
     * @param patientCode the patient code which informs what kind of patient to
     *                    load
     */
    private void changeSeat(JLayeredPane seat, String patientCode) {
        if (seat != null) {
            seat.removeAll();
            seat.revalidate();
            if (patientCode != null && !patientCode.isBlank())
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            setIcon(seat, patientCode);
                        }
                    });
                } catch (InterruptedException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            else
                seat.repaint();
        }
    }

    /**
     * Adds the update request to a FIFO to be processed
     *
     * @param info what the update entails
     */
    public void update(Map<String, String[]> info) {
        updates.put(info);
    }

    /**
     * Changes the title of the UI window
     *
     * @param state what will be added to the title
     */
    public void setStateLabel(String state) {
        if (state != null)
            frame.setTitle("HCP - " + state);
        else
            frame.setTitle("HCP");
    }

    /**
     * Changes the number of seats to be displayed in some rooms of the UI
     *
     * @param seatCount the number of seats to be displayed in each room
     */
    public void setSeatCount(int seatCount) {
        enHARSeats = new JLayeredPane[seatCount];
        enHCRSeats = new JLayeredPane[seatCount];
        wHARSeats = new JLayeredPane[seatCount];
        wHCRSeats = new JLayeredPane[seatCount];
        for (JLayeredPane[][] seats : new JLayeredPane[][][] {
                { enHARSeats, { enHARSeat1, enHARSeat2, enHARSeat3, enHARSeat4, enHARSeat5 } },
                { enHCRSeats, { enHCRSeat1, enHCRSeat2, enHCRSeat3, enHCRSeat4, enHCRSeat5 } },
                { wHARSeats, { wHARSeat1, wHARSeat2, wHARSeat3, wHARSeat4, wHARSeat5 } },
                { wHCRSeats, { wHCRSeat1, wHCRSeat2, wHCRSeat3, wHCRSeat4, wHCRSeat5 } }
        }) {
            if ((seatCount & 1) == 0) {
                System.arraycopy(seats[1], 0, seats[0], 0, seatCount);
                for (int i = 0; i < seatCount; i++)
                    seats[1][i].setVisible(true);
                for (int i = seatCount; i <= 4; i++)
                    seats[1][i].setVisible(false);
            } else {
                System.arraycopy(seats[1], 0, seats[0], 0, seatCount - 1);
                seats[0][seatCount - 1] = seats[1][4];
                seats[1][4].setVisible(true);
                for (int i = 0; i < seatCount - 1; i++)
                    seats[1][i].setVisible(true);
                for (int i = seatCount - 1; i <= 3; i++)
                    seats[1][i].setVisible(false);
            }
        }
        try {
            ((CardLayout) cardPanel.getLayout()).last(cardPanel);
        } catch (Exception ignored) {
        }
        setStateLabel("Stopped");
    }

    /**
     * Sets the UI window to its loading state
     * <p>
     * Only works if it's in the state showing the hospital
     */
    public void setLoadingScreen() {
        try {
            ((CardLayout) cardPanel.getLayout()).previous(cardPanel);
        } catch (Exception ignored) {
        }
    }

    /**
     * Changes the theme of the UI window
     * <p>
     * If computer doesn't have any of the themes provided the computer's default
     * one will be used
     *
     * @param wantedLooks list of theme names
     */
    public static void setGUILook(String[] wantedLooks) {
        UIManager.LookAndFeelInfo[] looks = UIManager.getInstalledLookAndFeels();
        String chosenLook = null;
        for (String wantedLook : wantedLooks) {
            if (chosenLook == null)
                for (UIManager.LookAndFeelInfo look : looks)
                    if (wantedLook.equals(look.getName())) {
                        chosenLook = look.getClassName();
                        break;
                    }
        }
        if (chosenLook == null)
            chosenLook = UIManager.getSystemLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(chosenLook);
            JFrame.setDefaultLookAndFeelDecorated(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Auxiliary function which adds borders to many UI elements
     */
    private void setBorders() {
        inLabel.setBorder(new EmptyBorder(new Insets(0, 5, 0, 5)));
        outLabel.setBorder(new EmptyBorder(new Insets(0, 5, 0, 5)));
        for (JLabel line : new JLabel[] {
                inLine,
                outLine,
                midLine1,
                midLine2,
                midLine3,
                midLine4
        }) {
            line.setBorder(new LineBorder(new Color(128, 128, 128), 1));
        }
        for (JPanel panel : new JPanel[] {
                entranceHallPanel,
                evaluationHallPanel,
                waitingHallPanel,
                medicalHallPanel,
                paymentHallPanel
        }) {
            panel.setBorder(new MatteBorder(0, 1, 0, 1, new Color(128, 128, 128)));
        }
        for (JPanel panel : new JPanel[] {
                enHAdultRoomPanel,
                enHChildRoomPanel,
                evHRoom1Panel,
                evHRoom2Panel,
                evHRoom3Panel,
                evHRoom4Panel,
                wHAdultRoomPanel,
                wHChildRoomPanel,
                mHWaitingRoomPanel,
                mHChildRoom1Panel,
                mHChildRoom2Panel,
                mHAdultRoom1Panel,
                mHAdultRoom2Panel,
                pHEntranceHallPanel,
                pHCashierPanel
        }) {
            panel.setBorder(new SoftBevelBorder(0));
        }
        enHAROverflowLabel.setBorder(new EmptyBorder(0, 0, 0, 8));
        enHCROverflowLabel.setBorder(new EmptyBorder(0, 0, 0, 8));
        wHAROverflowLabel.setBorder(new EmptyBorder(0, 0, 0, 8));
        wHCROverflowLabel.setBorder(new EmptyBorder(0, 0, 0, 8));
        if (UIManager.getLookAndFeel().getName().equals("GTK look and feel"))
            portSpinner.setBorder(new LineBorder(new Color(39, 39, 39), 1, true));
    }

    /**
     * Creates many custom elements not able to be initialized through the .form
     * file
     * <p>
     * Is automatically ran when TGUI is initialized
     */
    private void createUIComponents() {
        pHCSeat = new JLayeredPane();
        enHARSeat1 = new JLayeredPane();
        enHARSeat2 = new JLayeredPane();
        enHARSeat3 = new JLayeredPane();
        enHARSeat4 = new JLayeredPane();
        enHARSeat5 = new JLayeredPane();
        enHCRSeat1 = new JLayeredPane();
        enHCRSeat2 = new JLayeredPane();
        enHCRSeat3 = new JLayeredPane();
        enHCRSeat4 = new JLayeredPane();
        enHCRSeat5 = new JLayeredPane();
        evHR1Seat = new JLayeredPane();
        evHR2Seat = new JLayeredPane();
        evHR3Seat = new JLayeredPane();
        evHR4Seat = new JLayeredPane();
        wHARSeat1 = new JLayeredPane();
        wHARSeat2 = new JLayeredPane();
        wHARSeat3 = new JLayeredPane();
        wHARSeat4 = new JLayeredPane();
        wHARSeat5 = new JLayeredPane();
        wHCRSeat1 = new JLayeredPane();
        wHCRSeat2 = new JLayeredPane();
        wHCRSeat3 = new JLayeredPane();
        wHCRSeat4 = new JLayeredPane();
        wHCRSeat5 = new JLayeredPane();
        mHWRAdultSeat = new JLayeredPane();
        mHWRChildSeat = new JLayeredPane();
        mHCR1Seat = new JLayeredPane();
        mHCR2Seat = new JLayeredPane();
        mHAR1Seat = new JLayeredPane();
        mHAR2Seat = new JLayeredPane();
        pHOverflow1 = new JLayeredPane();
        pHOverflow2 = new JLayeredPane();
        pHOverflow3 = new JLayeredPane();
        enHAROverflow1 = new JLayeredPane();
        enHAROverflow2 = new JLayeredPane();
        enHAROverflow3 = new JLayeredPane();
        enHCROverflow1 = new JLayeredPane();
        enHCROverflow2 = new JLayeredPane();
        enHCROverflow3 = new JLayeredPane();
        wHAROverflow1 = new JLayeredPane();
        wHAROverflow2 = new JLayeredPane();
        wHAROverflow3 = new JLayeredPane();
        wHCROverflow1 = new JLayeredPane();
        wHCROverflow2 = new JLayeredPane();
        wHCROverflow3 = new JLayeredPane();

        for (JLayeredPane adultSeat : new JLayeredPane[] {
                enHARSeat1,
                enHARSeat2,
                enHARSeat3,
                enHARSeat4,
                enHARSeat5,
                evHR1Seat,
                evHR2Seat,
                evHR3Seat,
                evHR4Seat,
                wHARSeat1,
                wHARSeat2,
                wHARSeat3,
                wHARSeat4,
                wHARSeat5,
                mHWRAdultSeat,
                mHAR1Seat,
                mHAR2Seat,
                pHCSeat
        }) {
            adultSeat.setMinimumSize(new Dimension(48, 48));
            adultSeat.setMaximumSize(new Dimension(48, 48));
            adultSeat.setPreferredSize(new Dimension(48, 48));
            adultSeat.setBorder(new CompoundBorder(new EmptyBorder(2, 2, 2, 2), new SoftBevelBorder(1)));
        }
        for (JLayeredPane childSeat : new JLayeredPane[] {
                enHCRSeat1,
                enHCRSeat2,
                enHCRSeat3,
                enHCRSeat4,
                enHCRSeat5,
                wHCRSeat1,
                wHCRSeat2,
                wHCRSeat3,
                wHCRSeat4,
                mHWRChildSeat,
                mHCR1Seat,
                mHCR2Seat,
                wHCRSeat5
        }) {
            childSeat.setMinimumSize(new Dimension(40, 40));
            childSeat.setMaximumSize(new Dimension(40, 40));
            childSeat.setPreferredSize(new Dimension(40, 40));
            childSeat.setBorder(new CompoundBorder(new EmptyBorder(2, 2, 2, 2), new SoftBevelBorder(1)));
        }
        for (JLayeredPane[] hFifo3 : new JLayeredPane[][] {
                { pHOverflow1, pHOverflow2, pHOverflow3 }
        }) {
            hFifo3[0].setMinimumSize(new Dimension(46, 48));
            hFifo3[0].setMaximumSize(new Dimension(46, 48));
            hFifo3[0].setPreferredSize(new Dimension(46, 48));
            hFifo3[1].setMinimumSize(new Dimension(40, 44));
            hFifo3[1].setMaximumSize(new Dimension(40, 44));
            hFifo3[1].setPreferredSize(new Dimension(40, 44));
            hFifo3[2].setMinimumSize(new Dimension(40, 40));
            hFifo3[2].setMaximumSize(new Dimension(40, 40));
            hFifo3[2].setPreferredSize(new Dimension(40, 40));
            hFifo3[0].setBorder(new CompoundBorder(new EmptyBorder(2, 0, 2, 2), new SoftBevelBorder(1)));
            hFifo3[1].setBorder(new CompoundBorder(new EmptyBorder(2, 0, 2, 0), new SoftBevelBorder(1)));
            hFifo3[2].setBorder(new CompoundBorder(new EmptyBorder(2, 4, 2, 0), new SoftBevelBorder(1)));
        }
        for (JLayeredPane[] vFifo3 : new JLayeredPane[][] {
                { enHAROverflow1, enHAROverflow2, enHAROverflow3 },
                { enHCROverflow1, enHCROverflow2, enHCROverflow3 },
                { wHAROverflow1, wHAROverflow2, wHAROverflow3 },
                { wHCROverflow1, wHCROverflow2, wHCROverflow3 }
        }) {
            vFifo3[0].setMinimumSize(new Dimension(52, 48));
            vFifo3[0].setMaximumSize(new Dimension(52, 48));
            vFifo3[0].setPreferredSize(new Dimension(52, 48));
            vFifo3[1].setMinimumSize(new Dimension(48, 44));
            vFifo3[1].setMaximumSize(new Dimension(48, 44));
            vFifo3[1].setPreferredSize(new Dimension(48, 44));
            vFifo3[2].setMinimumSize(new Dimension(44, 40));
            vFifo3[2].setMaximumSize(new Dimension(44, 40));
            vFifo3[2].setPreferredSize(new Dimension(44, 40));
            vFifo3[0].setBorder(new CompoundBorder(new EmptyBorder(0, 0, 2, 8), new SoftBevelBorder(1)));
            vFifo3[1].setBorder(new CompoundBorder(new EmptyBorder(0, 0, 0, 8), new SoftBevelBorder(1)));
            vFifo3[2].setBorder(new CompoundBorder(new EmptyBorder(2, 0, 0, 8), new SoftBevelBorder(1)));
        }
    }

    /**
     * Adds the patient icon and label to a JLayeredPane seat according to a patient
     * code
     *
     * @param seat        the seat to change
     * @param patientCode the patient code which informs what icon and label to add
     */
    private void setIcon(JLayeredPane seat, String patientCode) {
        ImageIcon imageIcon;
        boolean isChild = patientCode.charAt(0) == 'C';
        if (patientCode.length() > 3)
            switch (patientCode.charAt(3)) {
                case 'B':
                    imageIcon = isChild ? loadedIcons[6] : loadedIcons[7];
                    break;
                case 'Y':
                    imageIcon = isChild ? loadedIcons[4] : loadedIcons[5];
                    break;
                case 'R':
                    imageIcon = isChild ? loadedIcons[2] : loadedIcons[3];
                    break;
                default:
                    imageIcon = isChild ? loadedIcons[0] : loadedIcons[1];
                    break;
            }
        else
            imageIcon = isChild ? loadedIcons[0] : loadedIcons[1];

        Insets seatBorderInsets = seat.getBorder().getBorderInsets(null);
        int seatWidthBorders = seat.getPreferredSize().width + (seatBorderInsets.left - seatBorderInsets.right);
        int seatHeightBorders = seat.getPreferredSize().height + (seatBorderInsets.top - seatBorderInsets.bottom);

        int seatSize;
        if (seatWidthBorders == 48 && seatHeightBorders == 48)
            seatSize = 0;
        else if (seatWidthBorders == 40 && seatHeightBorders == 40)
            seatSize = 1;
        else if (seatWidthBorders == 44 && seatHeightBorders == 46)
            seatSize = 2;
        else if (seatWidthBorders == 40 && seatHeightBorders == 44)
            seatSize = 3;
        else if (seatWidthBorders == 44 && seatHeightBorders == 48)
            seatSize = 4;
        else if (seatWidthBorders == 36 && seatHeightBorders == 42)
            seatSize = 5;
        else if (seatWidthBorders == 44 && seatHeightBorders == 40)
            seatSize = 6;
        else
            seatSize = -1;

        JLabel iconLabel = new JLabel(imageIcon);
        iconLabel.setBounds(seatWidthBorders / 2 - 28 / 2, seatHeightBorders / 2 - 32 / 2, 28, 32);
        seat.add(iconLabel, 1);

        try {
            String patientNumber = patientCode.substring(1, 3);
            int pos = (100 * seatSize) + Integer.parseInt(patientNumber);
            JLabel loadedIconLabel = loadedIconLabels[pos];
            if (loadedIconLabel != null) {
                seat.add(loadedIconLabel, 0);
            } else {
                JLabel idLabel = new JLabel(patientNumber);
                idLabel.setHorizontalAlignment(SwingConstants.CENTER);
                Font oldFont = idLabel.getFont();
                int idLabelWidth = (int) ceil(oldFont
                        .getStringBounds("50", new FontRenderContext(new AffineTransform(), true, true)).getWidth())
                        + 4;
                idLabel.setBounds(seat.getPreferredSize().width - idLabelWidth - seatBorderInsets.right + 2,
                        seatHeightBorders - oldFont.getSize() - seatBorderInsets.bottom + 2, idLabelWidth,
                        oldFont.getSize());
                idLabel.setOpaque(true);
                idLabel.setBorder(new LineBorder(new Color(39, 39, 39), 1, true));
                seat.add(idLabel, 0);
                loadedIconLabels[pos] = idLabel;
            }
        } catch (StringIndexOutOfBoundsException | NumberFormatException ignored) {
            // the first 2 lines of this try seems to fail sometimes, no idea why
        }
    }
}
