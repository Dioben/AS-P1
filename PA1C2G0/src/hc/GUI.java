package hc;

import hc.active.TCommsServer;
import hc.interfaces.IFIFO;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.Map;

import static java.lang.Math.ceil;

public class GUI extends Thread {

    private final IFIFO<Map<String, String[]>> updates;
    private final JFrame frame;
    private JLayeredPane[] enHARSeats;
    private JLayeredPane[] enHCRSeats;
    private JLayeredPane[] wHARSeats;
    private JLayeredPane[] wHCRSeats;

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

    public GUI() {
        GUI gui = this;
        updates = new MFIFO(Map.class, 50);

        frame = new JFrame("HCP");
        frame.setContentPane(this.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1010, 450));
        frame.setPreferredSize(new Dimension(1010, 450));
        frame.pack();
        frame.setVisible(true);
        portSpinner.setModel(new SpinnerNumberModel(8000, 0, 65535, 1));
        ImageIcon loading = new ImageIcon("resources/loading.gif");
        loadingLabel.setIcon(loading);
        setBorders();

        confirmLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int port = (int) portSpinner.getValue();
                new TCommsServer(port, gui).start();
                ((CardLayout) cardPanel.getLayout()).next(cardPanel);
            }
        });
    }

    public void run() {
        Map<String, String[]> handling;
        while (!Thread.interrupted()) {
            handling = updates.get();
            for (Map.Entry<String, String[]> entry : handling.entrySet()) {
                JLayeredPane[] seats = new JLayeredPane[0];
                switch (entry.getKey()) {
                    case "ETH":
                        seats = new JLayeredPane[] {
                                enHAROverflow1, enHAROverflow2, enHAROverflow3, enHCROverflow1, enHCROverflow2, enHCROverflow3
                        };
                        break;
                    case "ET1":
                        seats = enHARSeats;
                        break;
                    case "ET2":
                        seats = enHCRSeats;
                        break;
                    case "EVR1":
                        seats = new JLayeredPane[] {
                                evHR1Seat
                        };
                        break;
                    case "EVR2":
                        seats = new JLayeredPane[] {
                                evHR2Seat
                        };
                        break;
                    case "EVR3":
                        seats = new JLayeredPane[] {
                                evHR3Seat
                        };
                        break;
                    case "EVR4":
                        seats = new JLayeredPane[] {
                                evHR4Seat
                        };
                        break;
                    case "WTH":
                        seats = new JLayeredPane[] {
                                wHAROverflow1, wHAROverflow2, wHAROverflow3, wHCROverflow1, wHCROverflow2, wHCROverflow3
                        };
                        break;
                    case "WTR1":
                        seats = wHARSeats;
                        break;
                    case "WTR2":
                        seats = wHCRSeats;
                        break;
                    case "MDW1":
                        seats = new JLayeredPane[] {
                                mHWRChildSeat
                        };
                        break;
                    case "MDW2":
                        seats = new JLayeredPane[] {
                                mHWRAdultSeat
                        };
                        break;
                    case "MDR1":
                        seats = new JLayeredPane[] {
                                mHCR1Seat
                        };
                        break;
                    case "MDR2":
                        seats = new JLayeredPane[] {
                                mHCR2Seat
                        };
                        break;
                    case "MDR3":
                        seats = new JLayeredPane[] {
                                mHAR1Seat
                        };
                        break;
                    case "MDR4":
                        seats = new JLayeredPane[] {
                                mHAR2Seat
                        };
                        break;
                    case "PYH":
                        seats = new JLayeredPane[] {
                                pHOverflow1, pHOverflow2, pHOverflow3
                        };
                        break;
                    case "PYR":
                        seats = new JLayeredPane[] {
                                pHCSeat
                        };
                        break;
                    default:
                        System.out.println("GUI got unknown entry: " + entry.getKey());
                }
                String[] value = entry.getValue();
                for (int i = 0; i < value.length; i++) {
                    seats[i].removeAll();
                    seats[i].revalidate();
                    if (value[i] != null && !value[i].isBlank())
                        setIcon(seats[i], value[i]);
                    else
                        seats[i].repaint();
                }
            }
        }
    }

    public void update(Map<String, String[]> info) {
        updates.put(info);
    }

    public void setStateLabel(String state) {
        frame.setTitle("HCP - " + state);
    }

    public void setSeatCount(int seatCount) {
        enHARSeats = new JLayeredPane[seatCount];
        enHCRSeats = new JLayeredPane[seatCount];
        wHARSeats = new JLayeredPane[seatCount];
        wHCRSeats = new JLayeredPane[seatCount];
        for (JLayeredPane[][] seats : new JLayeredPane[][][] {
                {enHARSeats, {enHARSeat1, enHARSeat2, enHARSeat3, enHARSeat4, enHARSeat5}},
                {enHCRSeats, {enHCRSeat1, enHCRSeat2, enHCRSeat3, enHCRSeat4, enHCRSeat5}},
                {wHARSeats, {wHARSeat1, wHARSeat2, wHARSeat3, wHARSeat4, wHARSeat5}},
                {wHCRSeats, {wHCRSeat1, wHCRSeat2, wHCRSeat3, wHCRSeat4, wHCRSeat5}}
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
        } catch (Exception ignored) {}
        setStateLabel("Stopped");
    }

    public static void setGUILook(String wantedLook) {
        UIManager.LookAndFeelInfo[] looks = UIManager.getInstalledLookAndFeels();
        String chosenLook = null;
        for (UIManager.LookAndFeelInfo look : looks)
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

    private void setBorders() {
        inLabel.setBorder(new EmptyBorder(new Insets(0, 5, 0, 5)));
        outLabel.setBorder(new EmptyBorder(new Insets(0, 5, 0, 5)));
        for (JLabel line : new JLabel[]{
                inLine,
                outLine,
                midLine1,
                midLine2,
                midLine3,
                midLine4
        }) {
            line.setBorder(new LineBorder(new Color(128, 128, 128), 1));
        }
        for (JPanel panel : new JPanel[]{
                entranceHallPanel,
                evaluationHallPanel,
                waitingHallPanel,
                medicalHallPanel,
                paymentHallPanel
        }) {
            panel.setBorder(new MatteBorder(0, 1, 0, 1, new Color(128, 128, 128)));
        }
        for (JPanel panel : new JPanel[]{
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
        enHAROverflowLabel.setBorder(new EmptyBorder(0,0,0,8));
        enHCROverflowLabel.setBorder(new EmptyBorder(0,0,0,8));
        wHAROverflowLabel.setBorder(new EmptyBorder(0,0,0,8));
        wHCROverflowLabel.setBorder(new EmptyBorder(0,0,0,8));
        if (UIManager.getLookAndFeel().getName().equals("GTK look and feel"))
            portSpinner.setBorder(new LineBorder(new Color(39, 39, 39), 1, true));
    }

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

        for (JLayeredPane adultSeat: new JLayeredPane[] {
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
                {pHOverflow1, pHOverflow2, pHOverflow3}
        }) {
            hFifo3[0].setMinimumSize(new Dimension(46,48));
            hFifo3[0].setMaximumSize(new Dimension(46,48));
            hFifo3[0].setPreferredSize(new Dimension(46,48));
            hFifo3[1].setMinimumSize(new Dimension(40,44));
            hFifo3[1].setMaximumSize(new Dimension(40,44));
            hFifo3[1].setPreferredSize(new Dimension(40,44));
            hFifo3[2].setMinimumSize(new Dimension(40,40));
            hFifo3[2].setMaximumSize(new Dimension(40,40));
            hFifo3[2].setPreferredSize(new Dimension(40,40));
            hFifo3[0].setBorder(new CompoundBorder(new EmptyBorder(2, 0, 2, 2), new SoftBevelBorder(1)));
            hFifo3[1].setBorder(new CompoundBorder(new EmptyBorder(2, 0, 2, 0), new SoftBevelBorder(1)));
            hFifo3[2].setBorder(new CompoundBorder(new EmptyBorder(2, 4, 2, 0), new SoftBevelBorder(1)));
        }
        for (JLayeredPane[] vFifo3 : new JLayeredPane[][] {
                {enHAROverflow1, enHAROverflow2, enHAROverflow3},
                {enHCROverflow1, enHCROverflow2, enHCROverflow3},
                {wHAROverflow1, wHAROverflow2, wHAROverflow3},
                {wHCROverflow1, wHCROverflow2, wHCROverflow3}
        }) {
            vFifo3[0].setMinimumSize(new Dimension(52,48));
            vFifo3[0].setMaximumSize(new Dimension(52,48));
            vFifo3[0].setPreferredSize(new Dimension(52,48));
            vFifo3[1].setMinimumSize(new Dimension(48,44));
            vFifo3[1].setMaximumSize(new Dimension(48,44));
            vFifo3[1].setPreferredSize(new Dimension(48,44));
            vFifo3[2].setMinimumSize(new Dimension(44,40));
            vFifo3[2].setMaximumSize(new Dimension(44,40));
            vFifo3[2].setPreferredSize(new Dimension(44,40));
            vFifo3[0].setBorder(new CompoundBorder(new EmptyBorder(0, 0, 2, 8), new SoftBevelBorder(1)));
            vFifo3[1].setBorder(new CompoundBorder(new EmptyBorder(0, 0, 0, 8), new SoftBevelBorder(1)));
            vFifo3[2].setBorder(new CompoundBorder(new EmptyBorder(2, 0, 0, 8), new SoftBevelBorder(1)));
        }
    }

    private void setIcon(JLayeredPane seat, String patientCode) {
        String imagePath;
        boolean isChild = patientCode.charAt(0) == 'C';
        if (patientCode.length() > 3)
            switch (patientCode.charAt(3)) {
                case 'B' -> imagePath = isChild ? "resources/childBlue.png" : "resources/adultBlue.png";
                case 'Y' -> imagePath = isChild ? "resources/childYellow.png" : "resources/adultYellow.png";
                case 'R' -> imagePath = isChild ? "resources/childRed.png" : "resources/adultRed.png";
                default -> imagePath = isChild ? "resources/child.png" : "resources/adult.png";
            }
        else
            imagePath = isChild ? "resources/child.png" : "resources/adult.png";
        ImageIcon imageIcon = new ImageIcon(imagePath);
        JLabel iconLabel = new JLabel(imageIcon);
        Insets seatBorderInsets = seat.getBorder().getBorderInsets(null);
        int seatWidthBorders = seat.getPreferredSize().width + (seatBorderInsets.left - seatBorderInsets.right);
        int seatHeightBorders = seat.getPreferredSize().height + (seatBorderInsets.top - seatBorderInsets.bottom);
        int iconWidth = iconLabel.getIcon().getIconWidth();
        int iconHeight = iconLabel.getIcon().getIconHeight();
        iconLabel.setBounds(seatWidthBorders/2-iconWidth/2, seatHeightBorders/2-iconHeight/2, iconWidth, iconHeight);
        seat.add(iconLabel, 1);

        try {
            JLabel idLabel = new JLabel(patientCode.substring(1, 3));
            idLabel.setHorizontalAlignment(SwingConstants.CENTER);
            Font oldFont = idLabel.getFont();
            int idLabelWidth = (int) ceil(oldFont.getStringBounds("50", new FontRenderContext(new AffineTransform(), true, true)).getWidth()) + 4;
            idLabel.setBounds(seat.getPreferredSize().width - idLabelWidth - seatBorderInsets.right + 2, seatHeightBorders - oldFont.getSize() - seatBorderInsets.bottom + 2, idLabelWidth, oldFont.getSize());
            idLabel.setOpaque(true);
            idLabel.setBorder(new LineBorder(new Color(39, 39, 39), 1, true));
            seat.add(idLabel, 0);
        } catch (StringIndexOutOfBoundsException ignored) {}
    }
}
