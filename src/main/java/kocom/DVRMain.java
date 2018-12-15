package kocom;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class DVRMain {

    public static void main(String[] args) throws InterruptedException {
        JFrame frame = new JFrame();
        frame.setLayout(new GridLayout(2, 2));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setTitle("DVR");
        frame.setResizable(false);
        frame.setSize(300, 200);

        JLabel[] labels = new JLabel[]{
                new JLabel(), new JLabel(), new JLabel(), new JLabel()};
        for (JLabel label : labels) {
            frame.add(label);
        }

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final DVRControl control = new DVRControl();
        control.dvrAddress = System.getProperty("dvr.addr", "127.0.0.1");
        control.dvrPort = Integer.parseInt(System.getProperty("dvr.port", "7620"));
        control.loginId = System.getProperty("dvr.user", "ADMIN");
        control.loginPasswd = System.getProperty("dvr.pass", "0000");

        control.eventListener = new DVRControlListener() {

            @Override
            public void onConnectionEvent(int eventId) {
                DVRControl.logd("DVRView", "onConnectionEvent, code=" + DVRControl.getEventName(eventId));
                if (eventId == 0) {
                    control.startLive();
                    control.setChMask(0xF);
                }
            }

            @Override
            public void onResponseData(int pktId, byte[] pktBuf, int pktlen) {
                DVRControl.logd("onResponseData()", "Got " + DVRControl.getPacketName(pktId) + ", len=" + pktlen);
                switch (pktId) {
                    case DVRControl.CMD_MULTI_PB_END_RES:
                        DVRControl.logd("onResponseData", "got CMD_PB_END_RES");
                        return;
                    case DVRControl.CMD_PB_REC_DATE_RES:
                        ByteBuffer buf = ByteBuffer.wrap(pktBuf);
                        if (buf.getShort(0) != (short) 0) {
                            DVRControl.logd("onResponseData", "CMD_PB_REC_DATE_RES = Not OK");
                            return;
                        } else {
                            short cnt = buf.getShort(2);
                            return;
                        }
                    case DVRControl.CMD_PB_REC_TIME_RES:
                        if (ByteBuffer.wrap(pktBuf).getShort(0) != (short) 0) {
                            DVRControl.logd("onResponseData", "CMD_PB_REC_TIME_RES = Not OK");
                            return;
                        }
                        return;
                    case DVRControl.CMD_PB_REC_EVENT_RES:
                        if (ByteBuffer.wrap(pktBuf).getShort(0) != (short) 0) {
                            DVRControl.logd("onResponseData", "CMD_PB_REC_TIME_RES = Not OK");
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }

            @Override
            public void onStreamData(DvrStreamInfo info, byte[] image) {
                try {
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(image, 0, info.cur_size));
                    labels[info.ch].setIcon(new ImageIcon(img));
                    frame.pack();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        };

        control.run();
    }

}
