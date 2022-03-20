package HC;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

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
    private JLabel enHARSeat1Label;
    private JLabel enHARSeat2Label;
    private JLabel enHARSeat3Label;
    private JLabel enHARSeat4Label;
    private JLabel enHARSeat5Label;
    private JLabel enHCRSeat1Label;
    private JLabel enHCRSeat2Label;
    private JLabel enHCRSeat3Label;
    private JLabel enHCRSeat4Label;
    private JLabel enHCRSeat5Label;
    private JLabel evHR1SeatLabel;
    private JLabel evHR2SeatLabel;
    private JLabel evHR3SeatLabel;
    private JLabel evHR4SeatLabel;
    private JLabel wHARSeat1Label;
    private JLabel wHARSeat2Label;
    private JLabel wHARSeat3Label;
    private JLabel wHARSeat4Label;
    private JLabel wHARSeat5Label;
    private JLabel wHCRSeat1Label;
    private JLabel wHCRSeat2Label;
    private JLabel wHCRSeat3Label;
    private JLabel wHCRSeat4Label;
    private JLabel wHCRSeat5Label;
    private JLabel mHWRAdultSeatLabel;
    private JLabel mHWRChildSeatLabel;
    private JLabel mHCR1SeatLabel;
    private JLabel mHCR2SeatLabel;
    private JLabel mHAR1SeatLabel;
    private JLabel mHAR2SeatLabel;
    private JLabel pHEHSeat1Label;
    private JLabel pHCSeatLabel;

    public GUI() {}

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
        for (JLabel line: new JLabel[] {
                inLine,
                outLine,
                midLine1,
                midLine2,
                midLine3,
                midLine4
        }) {
            line.setBorder(new LineBorder(line.getForeground(), 1));
        }
        for (JPanel panel: new JPanel[] {
                entranceHallPanel,
                evaluationHallPanel,
                waitingHallPanel,
                medicalHallPanel,
                paymentHallPanel
        }) {
            panel.setBorder(new MatteBorder(0, 1, 0, 1, panel.getForeground()));
        }
        for (JPanel panel: new JPanel[]{
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
            panel.setBorder(new SoftBevelBorder(1));
        }
        ImageIcon imageIcon = new ImageIcon("resources/user32.png");
        for (JLabel seat: new JLabel[] {
                enHARSeat1Label,
                enHARSeat2Label,
                enHARSeat3Label,
                enHARSeat4Label,
                enHARSeat5Label,
                evHR1SeatLabel,
                evHR2SeatLabel,
                evHR3SeatLabel,
                evHR4SeatLabel,
                wHARSeat1Label,
                wHARSeat2Label,
                wHARSeat3Label,
                wHARSeat4Label,
                wHARSeat5Label,
                mHWRAdultSeatLabel,
                mHAR1SeatLabel,
                mHAR2SeatLabel,
                pHEHSeat1Label,
                pHCSeatLabel
        }) {
            seat.setBorder(new CompoundBorder(new EmptyBorder(2,2,2,2), new SoftBevelBorder(0)));
            seat.setMinimumSize(new Dimension(48, 48));
            seat.setMaximumSize(new Dimension(48, 48));
            seat.setPreferredSize(new Dimension(48, 48));
            seat.setIcon(imageIcon);
        }
        imageIcon = new ImageIcon("resources/user24.png");
        for (JLabel seat: new JLabel[] {
                enHCRSeat1Label,
                enHCRSeat2Label,
                enHCRSeat3Label,
                enHCRSeat4Label,
                enHCRSeat5Label,
                wHCRSeat1Label,
                wHCRSeat2Label,
                wHCRSeat3Label,
                wHCRSeat4Label,
                wHCRSeat5Label,
                mHWRChildSeatLabel,
                mHCR1SeatLabel,
                mHCR2SeatLabel
        }) {
            seat.setBorder(new CompoundBorder(new EmptyBorder(2,2,2,2), new SoftBevelBorder(0)));
            seat.setMinimumSize(new Dimension(40, 40));
            seat.setMaximumSize(new Dimension(40, 40));
            seat.setPreferredSize(new Dimension(40, 40));
            seat.setIcon(imageIcon);
        }
    }

    public static void setGUILook(String wantedLook) {
        UIManager.LookAndFeelInfo[] looks = UIManager.getInstalledLookAndFeels();
        String chosenLook = null;
        for (UIManager.LookAndFeelInfo look: looks)
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
