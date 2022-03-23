package HC;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class GUI {
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
    private JLayeredPane pHEHOverflow1;
    private JLayeredPane pHEHOverflow2;
    private JLayeredPane pHEHOverflow3;
    private JLabel pHEHOverflowLabel;

    private enum SeverityColor {
        BLUE,
        YELLOW,
        RED,
        UNASSIGNED
    }

    public GUI() {
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

    public void start() {
        addExtraStyle();
        JFrame frame = new JFrame("HCP");
        frame.setContentPane(this.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(870, 450));
        frame.setPreferredSize(new Dimension(870, 450));
        frame.pack();
        frame.setVisible(true);
        frame.setTitle("HCP - Unknown");
    }

    private void addExtraStyle() {
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
            line.setBorder(new LineBorder(line.getForeground(), 1));
        }
        for (JPanel panel : new JPanel[]{
                entranceHallPanel,
                evaluationHallPanel,
                waitingHallPanel,
                medicalHallPanel,
                paymentHallPanel
        }) {
            panel.setBorder(new MatteBorder(0, 1, 0, 1, panel.getForeground()));
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
        pHEHOverflow1 = new JLayeredPane();
        pHEHOverflow2 = new JLayeredPane();
        pHEHOverflow3 = new JLayeredPane();

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
//            setIcon(pHCSeat, false, SeverityColor.YELLOW, "50");
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
        for (JLayeredPane[] fifo3 : new JLayeredPane[][] {
                {pHEHOverflow1, pHEHOverflow2, pHEHOverflow3}
        }) {
            fifo3[0].setMinimumSize(new Dimension(46,48));
            fifo3[0].setMaximumSize(new Dimension(46,48));
            fifo3[0].setPreferredSize(new Dimension(46,48));
            fifo3[1].setMinimumSize(new Dimension(44,44));
            fifo3[1].setMaximumSize(new Dimension(44,44));
            fifo3[1].setPreferredSize(new Dimension(44,44));
            fifo3[2].setMinimumSize(new Dimension(40,40));
            fifo3[2].setMaximumSize(new Dimension(40,40));
            fifo3[2].setPreferredSize(new Dimension(40,40));
            fifo3[0].setBorder(new CompoundBorder(new EmptyBorder(2, 0, 2, 2), new SoftBevelBorder(1)));
            fifo3[1].setBorder(new CompoundBorder(new EmptyBorder(2, 0, 2, 0), new SoftBevelBorder(1)));
            fifo3[2].setBorder(new CompoundBorder(new EmptyBorder(2, 2, 2, 0), new SoftBevelBorder(1)));
        }

        setIcon(enHARSeat1, false, SeverityColor.BLUE, "1");
        setIcon(enHARSeat2, false, SeverityColor.BLUE, "2");
        setIcon(enHARSeat3, false, SeverityColor.BLUE, "3");
        setIcon(enHARSeat4, false, SeverityColor.BLUE, "4");
        setIcon(enHARSeat5, false, SeverityColor.BLUE, "5");
        setIcon(enHCRSeat1, true, SeverityColor.BLUE, "1");
        setIcon(enHCRSeat2, true, SeverityColor.BLUE, "2");
        setIcon(enHCRSeat3, true, SeverityColor.BLUE, "3");
        setIcon(enHCRSeat4, true, SeverityColor.BLUE, "4");
        setIcon(enHCRSeat5, true, SeverityColor.BLUE, "5");
        setIcon(evHR1Seat, false, SeverityColor.BLUE, "1");
        setIcon(evHR2Seat, false, SeverityColor.BLUE, "2");
        setIcon(evHR3Seat, false, SeverityColor.BLUE, "3");
        setIcon(evHR4Seat, false, SeverityColor.BLUE, "4");
        setIcon(wHARSeat1, false, SeverityColor.BLUE, "1");
        setIcon(wHARSeat2, false, SeverityColor.BLUE, "2");
        setIcon(wHARSeat3, false, SeverityColor.BLUE, "3");
        setIcon(wHARSeat4, false, SeverityColor.BLUE, "4");
        setIcon(wHARSeat5, false, SeverityColor.BLUE, "5");
        setIcon(wHCRSeat1, true, SeverityColor.BLUE, "1");
        setIcon(wHCRSeat2, true, SeverityColor.BLUE, "2");
        setIcon(wHCRSeat3, true, SeverityColor.BLUE, "3");
        setIcon(wHCRSeat4, true, SeverityColor.BLUE, "4");
        setIcon(wHCRSeat5, true, SeverityColor.BLUE, "5");
        setIcon(mHWRAdultSeat, false, SeverityColor.BLUE, "0");
        setIcon(mHWRChildSeat, true, SeverityColor.BLUE, "0");
        setIcon(mHCR1Seat, true, SeverityColor.BLUE, "0");
        setIcon(mHCR2Seat, true, SeverityColor.BLUE, "0");
        setIcon(mHAR1Seat, false, SeverityColor.BLUE, "0");
        setIcon(mHAR2Seat, false, SeverityColor.BLUE, "0");
        setIcon(pHEHOverflow1, false, SeverityColor.BLUE, "1");
        setIcon(pHEHOverflow2, false, SeverityColor.BLUE, "2");
        setIcon(pHEHOverflow3, false, SeverityColor.BLUE, "3");
    }

    private void setIcon(JLayeredPane seat, boolean isChild, SeverityColor severityColor, String id) {
        String imagePath;
        switch (severityColor) {
            case BLUE -> imagePath = isChild ? "resources/user24blue.png" : "resources/user32blue.png";
            case YELLOW -> imagePath = isChild ? "resources/user24yellow.png" : "resources/user32yellow.png";
            case RED -> imagePath = isChild ? "resources/user24red.png" : "resources/user32red.png";
            default -> imagePath = isChild ? "resources/user24.png" : "resources/user32.png";
        }
        ImageIcon imageIcon = new ImageIcon(imagePath);
        JLabel iconLabel = new JLabel(imageIcon);
        int seatWidth = seat.getPreferredSize().width;
        int seatHeight = seat.getPreferredSize().height;
        int iconWidth = iconLabel.getIcon().getIconWidth();
        int iconHeight = iconLabel.getIcon().getIconHeight();
        iconLabel.setBounds(seatWidth/2-iconWidth/2, seatHeight/2-iconHeight/2, iconWidth, iconHeight);
        seat.add(iconLabel, 1);

        JLabel idLabel = new JLabel(id);
        idLabel.setHorizontalAlignment(SwingConstants.CENTER);
        Font oldFont = idLabel.getFont();
        idLabel.setFont(new Font(oldFont.getName(), Font.BOLD, iconHeight/2 - (isChild ? 2 : 4)));
        idLabel.setForeground(Color.BLACK);
        idLabel.setBounds(0, seatHeight/2 + (isChild ? 1 : 2), seatWidth, iconHeight/2 - (isChild ? 2 : 4));
        seat.add(idLabel, 0);
    }
}
