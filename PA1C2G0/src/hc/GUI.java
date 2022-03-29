package hc;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

import static java.lang.Math.ceil;

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
    private JLayeredPane pHOverflow1;
    private JLayeredPane pHOverflow2;
    private JLayeredPane pHOverflow3;
    private JLayeredPane enHOverflow1;
    private JLayeredPane enHOverflow2;
    private JLayeredPane enHOverflow3;
    private JLayeredPane wHOverflow1;
    private JLayeredPane wHOverflow2;
    private JLayeredPane wHOverflow3;
    private JLabel pHOverflowLabel;
    private JLabel enHOverflowLabel;
    private JLabel wHOverflowLabel;
    private JPanel cardPanel;
    private JButton confirmLoginButton;
    private JSpinner portSpinner;

    private enum SeverityColor {
        BLUE,
        YELLOW,
        RED,
        UNASSIGNED
    }

    public GUI() {
        confirmLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((CardLayout) cardPanel.getLayout()).next(cardPanel);
            }
        });
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
        frame.setMinimumSize(new Dimension(1010, 450));
        frame.setPreferredSize(new Dimension(1010, 450));
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
        enHOverflowLabel.setBorder(new EmptyBorder(0,0,0,8));
        wHOverflowLabel.setBorder(new EmptyBorder(0,0,0,8));
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
        enHOverflow1 = new JLayeredPane();
        enHOverflow2 = new JLayeredPane();
        enHOverflow3 = new JLayeredPane();
        wHOverflow1 = new JLayeredPane();
        wHOverflow2 = new JLayeredPane();
        wHOverflow3 = new JLayeredPane();

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
                {enHOverflow1, enHOverflow2, enHOverflow3},
                {wHOverflow1, wHOverflow2, wHOverflow3}
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

    private void setIcon(JLayeredPane seat, boolean isChild, SeverityColor severityColor, String id) {
        String imagePath;
        switch (severityColor) {
            case BLUE -> imagePath = isChild ? "resources/childBlue.png" : "resources/adultBlue.png";
            case YELLOW -> imagePath = isChild ? "resources/childYellow.png" : "resources/adultYellow.png";
            case RED -> imagePath = isChild ? "resources/childRed.png" : "resources/adultRed.png";
            default -> imagePath = isChild ? "resources/child.png" : "resources/adult.png";
        }
        ImageIcon imageIcon = new ImageIcon(imagePath);
        JLabel iconLabel = new JLabel(imageIcon);
        Insets seatBorderInsets = seat.getBorder().getBorderInsets(null);
        int seatWidthBorders = seat.getPreferredSize().width + (seatBorderInsets.left - seatBorderInsets.right);
        int seatHeightBorders = seat.getPreferredSize().height + (seatBorderInsets.top - seatBorderInsets.bottom);
        int iconWidth = iconLabel.getIcon().getIconWidth();
        int iconHeight = iconLabel.getIcon().getIconHeight();
        iconLabel.setBounds(seatWidthBorders/2-iconWidth/2, seatHeightBorders/2-iconHeight/2, iconWidth, iconHeight);
        seat.add(iconLabel, 1);

        JLabel idLabel = new JLabel(id);
        idLabel.setHorizontalAlignment(SwingConstants.CENTER);
        Font oldFont = idLabel.getFont();
        int idLabelWidth = (int) ceil(oldFont.getStringBounds("50", new FontRenderContext(new AffineTransform(), true, true)).getWidth()) + 4;
        idLabel.setBounds(seat.getPreferredSize().width - idLabelWidth - seatBorderInsets.right + 2, seatHeightBorders - oldFont.getSize() - seatBorderInsets.bottom + 2, idLabelWidth, oldFont.getSize());
        idLabel.setOpaque(true);
        idLabel.setBorder(new LineBorder(new Color(39, 39, 39), 1, true));
        seat.add(idLabel, 0);
    }

    private void removeIcon(JLayeredPane seat) {
        seat.removeAll();
    }
}
