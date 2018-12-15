package kocom;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Calendar;

interface DVRControlListener {
    void onConnectionEvent(int i);

    void onResponseData(int i, byte[] bArr, int i2);

    void onStreamData(DvrStreamInfo dvrStreamInfo, byte[] bArr);
}

class DvrStreamInfo {
    public int ch;
    public int cur_size;
    public int data_type;
    public int height;
    public int picture_type;
    public long time;
    public int width;
}

public class DVRControl extends Thread {
    public static final short CMD_CAMERA_NAME_GET_REQ = (short) 12049;
    public static final short CMD_CAMERA_NAME_GET_RES = (short) 12050;
    public static final short CMD_MULTI_CHG_CH_MASK_REQ = (short) 16646;
    public static final short CMD_MULTI_CHG_CH_MASK_RES = (short) 16647;
    public static final short CMD_MULTI_CH_STATUS_RES = (short) 16645;
    public static final short CMD_MULTI_LIVE_START_REQ = (short) 16640;
    public static final short CMD_MULTI_LIVE_START_RES = (short) 16641;
    public static final short CMD_MULTI_LIVE_STOP_REQ = (short) 16642;
    public static final short CMD_MULTI_LIVE_STOP_RES = (short) 16643;
    public static final short CMD_MULTI_PB_CHG_MODE_REQ = (short) 16684;
    public static final short CMD_MULTI_PB_CHG_MODE_RES = (short) 16685;
    public static final short CMD_MULTI_PB_END_RES = (short) 16660;
    public static final short CMD_MULTI_PB_MOVE_POS_REQ = (short) 16678;
    public static final short CMD_MULTI_PB_MOVE_POS_RES = (short) 16679;
    public static final short CMD_MULTI_PB_NEXT_FRAME_REQ = (short) 16680;
    public static final short CMD_MULTI_PB_NEXT_FRAME_RES = (short) 16681;
    public static final short CMD_MULTI_PB_PREV_FRAME_REQ = (short) 16682;
    public static final short CMD_MULTI_PB_PREV_FRAME_RES = (short) 16683;
    public static final short CMD_MULTI_PB_STOP_REQ = (short) 16658;
    public static final short CMD_MULTI_PB_STOP_RES = (short) 16659;
    public static final short CMD_MULTI_STREAM_DATA_RES = (short) 16644;
    public static final short CMD_PB_REC_DATE_REQ = (short) 16688;
    public static final short CMD_PB_REC_DATE_RES = (short) 16689;
    public static final short CMD_PB_REC_EVENT_REQ = (short) 16692;
    public static final short CMD_PB_REC_EVENT_RES = (short) 16693;
    public static final short CMD_PB_REC_TIME_REQ = (short) 16690;
    public static final short CMD_PB_REC_TIME_RES = (short) 16691;
    public static final short CMD_PB_START_REQ = (short) 16694;
    public static final short CMD_PB_START_RES = (short) 16695;
    public static final short CMD_PTZ_MOVE = (short) 20481;
    public static final short CMD_PTZ_ZOOM = (short) 20482;
    public static final short CMD_SYS_ALIVE_RES = (short) 4160;
    public static final short CMD_SYS_INFO_GET_REQ_RES = (short) 12033;
    public static final short CMD_USERLOGON_REQ = (short) 4115;
    public static final short CMD_USERLOGON_RES = (short) 4116;
    public static final int DATA_TYPE_AUDIO = 3;
    public static final int DATA_TYPE_VIDEO = 2;
    public static final int DVR_EVENT_ACCESS_DENIED = 6;
    public static final int DVR_EVENT_ADMIN_ALREADY_CONNECTED = 1;
    public static final int DVR_EVENT_AUTH_FAIL = 7;
    public static final int DVR_EVENT_CONNECT_SUCCESS = 0;
    public static final int DVR_EVENT_CONNECT_TIMEOUT = 11;
    public static final int DVR_EVENT_DISCONNECTED = 12;
    public static final int DVR_EVENT_INAVAILD_ADDRESS = 13;
    public static final int DVR_EVENT_INAVAILD_VERSION = 14;
    public static final int DVR_EVENT_INVALID_PASSWORD = 4;
    public static final int DVR_EVENT_NORMAL_CLOSE = 0;
    public static final int DVR_EVENT_NO_CAMERA_NAME_RESPONSE = 10;
    public static final int DVR_EVENT_NO_CAPABILITY_RESPONSE = 9;
    public static final int DVR_EVENT_NO_LOGON_RESPONSE = 8;
    public static final int DVR_EVENT_NO_MORE_SESSION = 2;
    public static final int DVR_EVENT_SYSTEM_ERROR = 5;
    public static final int DVR_EVENT_UNKNOWN_USER_ID = 3;
    public static final short FRAME_TYPE_ANY = (short) 0;
    public static final short FRAME_TYPE_I_ONLY = (short) 1;
    public static final int MAX_CHANNELS = 16;
    public static final int MAX_PACKET_LENGTH = 4500;
    public static final int MAX_STREAM_LENGTH = 131072;
    public static final int PACKET_CONNECT_TIMEOUT = 60;
    public static final int PACKET_HDR_LENGTH = 6;
    public static final short PACKET_HDR_MAGIC = (short) 10264;
    public static final int PACKET_RCV_TIMEOUT = 30;
    public static final int PACKET_SEND_TIMEOUT = 30;
    public static final short PB_DIRECTION_BACK = (short) 1;
    public static final short PB_DIRECTION_FWD = (short) 0;
    public static final short PB_MODE_PAUSE = (short) 2;
    public static final short PB_MODE_PLAY = (short) 1;
    public static final short PB_MODE_STOP = (short) 0;
    public static final int PICTURE_TYPE_I = 1;
    public static final int PICTURE_TYPE_P = 2;
    public static final int PTZ_MOVE_DOWN = 2;
    public static final int PTZ_MOVE_LEFT = 3;
    public static final int PTZ_MOVE_RIGHT = 4;
    public static final int PTZ_MOVE_STOP = 0;
    public static final int PTZ_MOVE_UP = 1;
    public static final int PTZ_ZOOM_IN = 5;
    public static final int PTZ_ZOOM_OUT = 6;
    public static final int PTZ_ZOOM_STOP = 7;
    public static final short STREAM_DATA_AUDIO = (short) 2;
    private static final int STREAM_DATA_HDR_LENGTH = 24;
    public static final short STREAM_DATA_VIDEO = (short) 1;
    public static final short STREAM_DATA_VIDEO_AND_AUDIO = (short) 3;
    int audioMask = 0;
    String[] cameraName = new String[16];
    boolean connectFlag = false;
    String dvrAddress;
    int dvrCapabilityMask = 0;
    int dvrPort;
    int dvrVersion = 1;
    DVRControlListener eventListener;
    BufferedInputStream iStream;
    boolean isLiveFlag;
    String loginId;
    String loginPasswd;
    int numChannels = 16;
    BufferedOutputStream oStream;
    DvrStreamInfo pic;
    long pingPongTime;
    byte[] pktBuf = new byte[MAX_PACKET_LENGTH];
    int pktId;
    int pktLen;
    boolean runFlag;
    ByteBuffer streamBuf = ByteBuffer.allocate(MAX_STREAM_LENGTH);
    int streamBufRcvd;
    int streamCh;
    int streamDataType;
    int streamSeq;
    Socket streamSocket;
    int videoMask = 65535;

    public static void logd(String prefix, String message) {
        System.err.printf("%s: %s\n", prefix, message);
    }

    public static void logd(String prefix, String message, Exception e) {
        System.err.printf("%s: %s\n", prefix, message);
        e.printStackTrace();
    }

    public static String getEventName(int id) {

        switch (id) {
            case DVR_EVENT_CONNECT_SUCCESS:
                return "CONNECT_SUCCESS";
            case DVR_EVENT_ADMIN_ALREADY_CONNECTED:
                return "ADMIN_ALREADY_CONNECTED";
            case DVR_EVENT_NO_MORE_SESSION:
                return "NO_MORE_SESSION";
            case DVR_EVENT_UNKNOWN_USER_ID:
                return "UNKNOWN_USER";
            case DVR_EVENT_INVALID_PASSWORD:
                return "INVALID_PASSWORD";
            case DVR_EVENT_SYSTEM_ERROR:
                return "SYSTEM_ERROR";
            case DVR_EVENT_ACCESS_DENIED:
                return "ACCESS_DENIED";
            case DVR_EVENT_AUTH_FAIL:
                return "AUTH_FAIL";
            case DVR_EVENT_NO_LOGON_RESPONSE /*8*/:
                return "NO_LOGON_RESPONSE";
            case DVR_EVENT_NO_CAPABILITY_RESPONSE /*9*/:
                return "NO_CAPABILITY_RESPONSE";
            case DVR_EVENT_NO_CAMERA_NAME_RESPONSE /*10*/:
                return "NO_CAMERA_NAME_RESPONS";
            case DVR_EVENT_CONNECT_TIMEOUT /*11*/:
                return "TIMEOUT";
            case DVR_EVENT_DISCONNECTED /*12*/:
                return "DISCONNECTED";
            case DVR_EVENT_INAVAILD_ADDRESS /*13*/:
                return "INAVAILD_ADDRESS";
            case DVR_EVENT_INAVAILD_VERSION /*14*/:
                return "INVALID VERSION\nUpgrade DVR firmware";
            default:
                return "Unknown(" + id + ")";
        }
    }

    public static String getPacketName(int id) {
        switch (id) {
            case CMD_USERLOGON_REQ:
                return "CMD_USERLOGON_REQ";
            case CMD_USERLOGON_RES:
                return "CMD_USERLOGON_RES";
            case CMD_SYS_ALIVE_RES:
                return "CMD_SYS_ALIVE_RES";
            case CMD_SYS_INFO_GET_REQ_RES:
                return "CMD_SYS_INFO_GET_REQ_RES";
            case CMD_CAMERA_NAME_GET_REQ:
                return "CMD_CAMERA_NAME_GET_REQ";
            case CMD_CAMERA_NAME_GET_RES:
                return "CMD_CAMERA_NAME_GET_RES";
            case CMD_MULTI_LIVE_START_REQ:
                return "CMD_MULTI_LIVE_START_REQ";
            case CMD_MULTI_LIVE_START_RES:
                return "CMD_MULTI_LIVE_START_RES";
            case CMD_MULTI_LIVE_STOP_REQ:
                return "CMD_MULTI_LIVE_STOP_REQ";
            case CMD_MULTI_LIVE_STOP_RES:
                return "CMD_MULTI_LIVE_STOP_RES";
            case CMD_MULTI_STREAM_DATA_RES:
                return "CMD_MULTI_STREAM_DATA_RES";
            case CMD_MULTI_CH_STATUS_RES:
                return "CMD_MULTI_CH_STATUS_RES";
            case CMD_MULTI_CHG_CH_MASK_REQ:
                return "CMD_MULTI_CH_STATUS_REQ";
            case CMD_MULTI_CHG_CH_MASK_RES:
                return "CMD_MULTI_CHG_CH_MASK_RES";
            case CMD_PB_REC_DATE_REQ:
                return "CMD_PB_REC_DATE_REQ";
            case CMD_PB_REC_DATE_RES:
                return "CMD_PB_REC_DATE_RES";
            case CMD_PB_REC_TIME_REQ:
                return "CMD_PB_REC_TIME_REQ";
            case CMD_PB_REC_TIME_RES:
                return "CMD_PB_REC_TIME_RES";
            case CMD_PB_REC_EVENT_REQ:
                return "CMD_PB_REC_EVENT_REQ";
            case CMD_PB_REC_EVENT_RES:
                return "CMD_PB_REC_EVENT_RES";
            case CMD_PB_START_REQ:
                return "CMD_PB_START_REQ";
            case CMD_PB_START_RES:
                return "CMD_PB_START_RES";
            case CMD_PTZ_MOVE:
                return "CMD_PTZ_MOVE";
            case CMD_PTZ_ZOOM:
                return "CMD_PTZ_ZOOM";
            default:
                return "Unknown(" + id + ")";
        }
    }

    public static String getPtzName(int id) {
        switch (id) {
            case PTZ_MOVE_STOP:
                return "PTZ_MOVE_STOP";
            case PTZ_MOVE_UP:
                return "PTZ_MOVE_UP";
            case PTZ_MOVE_DOWN:
                return "PTZ_MOVE_DOWN";
            case PTZ_MOVE_LEFT:
                return "PTZ_MOVE_LEFT";
            case PTZ_MOVE_RIGHT:
                return "PTZ_MOVE_RIGHT";
            case PTZ_ZOOM_IN:
                return "PTZ_ZOOM_IN";
            case PTZ_ZOOM_OUT:
                return "PTZ_ZOOM_OUT";
            case PTZ_ZOOM_STOP:
                return "PTZ_ZOOM_STOP";
            default:
                return "Unknown(" + id + ")";
        }
    }

    private int getBytes(byte[] buf, int pos, int timeout) {
        try {
            this.streamSocket.setSoTimeout(timeout * 1000);
            return this.iStream.read(buf, pos, 1);
        } catch (SocketTimeoutException e) {
            logd("getByte", "Timeout", e);
            return 0;
        } catch (Exception e2) {
            logd("getByte", "Error", e2);
            return -1;
        }
    }

    private int getNbyte(byte[] buf, int n, int timeout) {
        int rcvd = 0;
        while (rcvd < n) {
            try {
                this.streamSocket.setSoTimeout(timeout * 1000);
                int ret = this.iStream.read(buf, rcvd, n - rcvd);
                if (ret < 0) {
                    logd("getNbytes", "read error, code=" + ret);
                    return ret;
                }
                rcvd += ret;
            } catch (SocketTimeoutException e) {
                logd("getNbytes", "Timeout", e);
                return 0;
            } catch (Exception e2) {
                logd("getNbytes", "Error,n=" + n + ", rcvd=" + rcvd, e2);
                return -1;
            }
        }
        return rcvd;
    }

    private int skipToNextPacket() {
        int nRead;
        byte[] bArr = new byte[6];
        bArr = this.pktBuf;
        ByteBuffer hdr = ByteBuffer.wrap(bArr);
        int nSkipped = 1;
        while (true) {
            int ret;
            if (nSkipped < 5 && hdr.getShort(nSkipped) != PACKET_HDR_MAGIC) {
                nSkipped++;
            } else if (nSkipped < 5) {
                break;
            } else {
                nRead = 0;
                bArr[0] = bArr[5];
                while (nRead < 6) {
                    ret = getBytes(bArr, nRead, 30);
                    if (ret <= 0) {
                        return ret;
                    }
                    nRead++;
                }
                nSkipped = 0;
            }
        }
        nRead = 0;
        while (nRead < 6 - nSkipped) {
            this.pktBuf[nRead] = bArr[nSkipped + nRead];
            nRead++;
        }
        while (nRead < 6) {
            int ret = getBytes(this.pktBuf, nRead, 30);
            if (ret <= 0) {
                return ret;
            }
            nRead++;
        }
        return 1;
    }

    private int getPacket() {
        this.pktLen = -1;
        int ret = getNbyte(this.pktBuf, 6, 30);
        if (ret <= 0) {
            logd("PACKET", "get header error");
            return ret;
        }
        ByteBuffer hdr = ByteBuffer.wrap(this.pktBuf);
        if (hdr.getShort(0) != PACKET_HDR_MAGIC) {
            logd("PACKET", "Invalid Magic");
            logd("PACKET", String.format("%#x %#x %#x %#x %#x %#x ", this.pktBuf[0], this.pktBuf[1], this.pktBuf[2], this.pktBuf[3], this.pktBuf[4], this.pktBuf[6]));
            ret = skipToNextPacket();
            if (ret <= 0) {
                return ret;
            }
        }
        this.pktId = hdr.getShort(2);
        this.pktLen = hdr.getShort(4);
        if (this.pktLen - 6 > 0) {
            ret = getNbyte(this.pktBuf, this.pktLen - 6, 30);
            if (ret <= 0) {
                logd("PACKET", "get body error");
                return ret;
            }
        }
        return this.pktLen + 6;
    }

    private int waitPacket(int cmdId) {
        while (getPacket() > 0) {
            if (this.pktId == cmdId) {
                return 1;
            }
        }
        return -1;
    }

    private int sendNbyte(byte[] buf, int n, int timeout) {
        try {
            this.oStream.write(buf, 0, n);
            this.oStream.flush();
            return n;
        } catch (Exception e) {
            logd("sendNbyte", "Error", e);
            return -1;
        }
    }

    private int sendPacket(int cmdId, byte[] buf, int len) {
        ByteBuffer hdr = ByteBuffer.allocate(6);
        hdr.putShort(0, PACKET_HDR_MAGIC);
        hdr.putShort(2, (short) cmdId);
        hdr.putShort(4, (short) (len + 6));
        if (sendNbyte(hdr.array(), 6, 30) <= 0) {
            logd("sendPacket", "send Hdr error");
            return -1;
        } else if (len <= 0 || sendNbyte(buf, len, 30) > 0) {
            return len + 6;
        } else {
            logd("sendPacket", "send Body error");
            return -1;
        }
    }

    private int getStringLength(ByteBuffer buf, int start, int maxLen) {
        int i = 0;
        while (i < maxLen && buf.getChar(start + i) != '\u0000') {
            i++;
        }
        return i;
    }

    private int connectDVR(String ipAddress, int port, String id, String passwd) {
        logd("connect", "Connecting: ip=" + ipAddress + "(" + port + "), id=" + id + "/" + passwd);
        try {
            this.streamSocket.connect(new InetSocketAddress(ipAddress, port), 60000);
            this.iStream = new BufferedInputStream(this.streamSocket.getInputStream());
            this.oStream = new BufferedOutputStream(this.streamSocket.getOutputStream());
            ByteBuffer loginPkt = ByteBuffer.allocate(64);
            loginPkt.put(this.loginId.getBytes());
            loginPkt.put(new byte[(32 - this.loginId.length())]);
            loginPkt.put(this.loginPasswd.getBytes());
            loginPkt.put(new byte[(32 - this.loginPasswd.length())]);
            logd("connect", "Send CMD_USERLOGON_REQ");
            if (sendPacket(CMD_USERLOGON_REQ, loginPkt.array(), 64) < 0) {
                logd("connect", "Error send CMD_USERLOGON_REQ");
                return -13;
            }
            logd("connect", "Wait CMD_USERLOGON_RES");
            if (waitPacket(CMD_USERLOGON_RES) < 0) {
                logd("connect", "Error wait CMD_USERLOGON_RES");
                return -8;
            }
            ByteBuffer buf = ByteBuffer.wrap(this.pktBuf);
            int result = buf.getShort(0) & 65535;
            logd("connect", "Result= !!" + String.format("%#x", result));
            switch (result) {
                case 61441:
                    return -1;
                case 61442:
                    return -2;
                case 61443:
                    return -3;
                case 61444:
                    return -4;
                case 61445:
                    return -5;
                case 61446:
                    return -6;
                case 65535:
                    return -7;
                default:
                    logd("connect", "Login Success !!");
                    logd("connect", "pktLen=" + this.pktLen);
                    if (this.pktLen - 6 == 4) {
                        this.dvrVersion = 1;
                    } else {
                        this.dvrVersion = buf.getShort(4);
                        this.dvrCapabilityMask = buf.getShort(6) & 65535;
                    }
                    logd("connect", "dvrVersion=" + this.dvrVersion);
                    logd("connect", "Send CMD_SYS_INFO_GET_REQ");
                    if (sendPacket(CMD_SYS_INFO_GET_REQ_RES, null, 0) < 0) {
                        logd("connect", "Error send CMD_SYS_INFO_GET_REQ_RES");
                        return -5;
                    }
                    logd("connect", "Wait CMD_SYS_INFO_GET_REQ_RES");
                    if (waitPacket(CMD_SYS_INFO_GET_REQ_RES) < 0) {
                        logd("connect", "Error wait CMD_SYS_INFO_GET_REQ_RES");
                        return -9;
                    }
                    buf = ByteBuffer.wrap(this.pktBuf);
                    if (buf.getShort(0) != 0) {
                        logd("connect", "CMD_SYS_INFO_GET_RES = Not OK");
                        return -5;
                    }
                    this.numChannels = buf.getShort(34);
                    logd("connect", "numChannels=" + this.numChannels);
                    logd("connect", "Send CMD_CAMERA_NAME_GET_REQ");
                    if (sendPacket(CMD_CAMERA_NAME_GET_REQ, null, 0) < 0) {
                        logd("connect", "Error send CMD_CAMERA_NAME_GET_REQ");
                        return -5;
                    }
                    logd("connect", "Wait CMD_CAMERA_NAME_GET_RES");
                    if (waitPacket(CMD_CAMERA_NAME_GET_RES) < 0) {
                        logd("connect", "Error wait CMD_CAMERA_NAME_GET_RES");
                        return -10;
                    }
                    buf = ByteBuffer.wrap(this.pktBuf);
                    if (buf.getShort(0) != 0) {
                        logd("connect", "CMD_CAMERA_NAME_GET_RES = Not OK");
                        return -5;
                    }
                    logd("connect", "Camera Names: ");
                    for (int i = 0; i < this.numChannels; i++) {
                        this.cameraName[i] = new String(buf.array(), (i * 32) + 4, getStringLength(buf, (i * 32) + 4, 32));
                        logd("  ", "CH[" + (i + 1) + "] = " + this.cameraName[i]);
                    }
                    return 1;
            }
        } catch (IllegalArgumentException e) {
            logd("connect", "DVR_EVENT_SYSTEM_ERROR", e);
            return -5;
        } catch (IOException e2) {
            logd("connect", "DVR_EVENT_CONNECT_TIMEOUT", e2);
            return -11;
        }
    }

    public int connect(String ipAddress, int port, String id, String passwd, DVRControlListener listener) {
        this.dvrAddress = ipAddress;
        this.dvrPort = port;
        this.loginId = id;
        this.loginPasswd = passwd;
        this.eventListener = listener;
        if (this.connectFlag) {
            disconnect();
        }
        start();
        return 1;
    }

    public void disconnect() {
        try {
            this.runFlag = false;
            this.streamSocket.close();
            sleep(500);
        } catch (Exception e) {
        }
        this.connectFlag = false;
    }

    public void run() {
        logd("DVRControl", "Start running...");
        this.runFlag = true;
        if (this.connectFlag) {
            disconnect();
        }
        try {
            this.streamSocket = new Socket();
            int ret = connectDVR(this.dvrAddress, this.dvrPort, this.loginId, this.loginPasswd);
            if (ret == 0) {
                logd("DVRControl_connect", "Timeout");
                this.eventListener.onConnectionEvent(DVR_EVENT_CONNECT_TIMEOUT);
            } else if (ret < 0) {
                logd("DVRControl_connect", "Error");
                this.eventListener.onConnectionEvent(ret * -1);
            } else {
                this.connectFlag = true;
                logd("DVRControl_connect", "Connected");
                this.eventListener.onConnectionEvent(DVR_EVENT_CONNECT_SUCCESS);
                int streamBufRcvd = 0;
                int streamDataType = -1;
                int streamCh = -1;
                DvrStreamInfo pic = new DvrStreamInfo();
                while (this.runFlag) {
                    ret = getPacket();
                    if (ret >= 0) {
                        if (ret != 0) {
                            if (this.pingPongTime < System.currentTimeMillis()) {
                                logd("StreamThread", "pingPongTime, send CMD_SYS_ALIVE");
                                if (sendPacket(CMD_SYS_ALIVE_RES, null, 0) < 0) {
                                    logd("StreamThread", "Error send CMD_SYS_ALIVE_RES");
                                } else {
                                    this.pingPongTime = System.currentTimeMillis() + 30000;
                                }
                            }
                            switch (this.pktId) {
                                case CMD_SYS_ALIVE_RES:
                                    break;
                                case CMD_MULTI_STREAM_DATA_RES:
                                    ByteBuffer pkt = ByteBuffer.wrap(this.pktBuf);
                                    short index = pkt.getShort(0);
                                    short chNum = pkt.getShort(2);
                                    short dataType = pkt.getShort(4);
                                    short frameType = pkt.getShort(6);
                                    short video_type = pkt.getShort(8);
                                    short resolution = pkt.getShort(10);
                                    int totalSize = pkt.getInt(12);
                                    int frameTime = pkt.getInt(16);
                                    short totalFragment = (short) pkt.get(20);
                                    short curFragment = (short) pkt.get(21);
                                    short curSize = pkt.getShort(22);
                                    int width = getWidth(video_type, resolution);
                                    int height = getHeight(video_type, resolution);
                                    if (curFragment == (short) 0 && totalSize < MAX_STREAM_LENGTH) {
                                        streamBufRcvd = 0;
                                        streamDataType = dataType;
                                        streamCh = chNum;
                                        this.streamBuf.rewind();
                                    }
                                    if (streamDataType == dataType && streamCh == chNum) {
                                        if (streamBufRcvd + curSize <= totalSize) {
                                            if (streamCh == (short) -1) {
                                                break;
                                            }
                                            int skipLen = 0;
                                            if (curFragment == (short) 0) {
                                                skipLen = 32;
                                                curSize = (short) (curSize - 32);
                                            }
                                            this.streamBuf.position(streamBufRcvd);
                                            this.streamBuf.put(this.pktBuf, skipLen + STREAM_DATA_HDR_LENGTH, curSize);
                                            streamBufRcvd += curSize;
                                            if (curFragment + 1 != totalFragment) {
                                                break;
                                            }
                                            pic.ch = streamCh;
                                            pic.data_type = streamDataType;
                                            pic.picture_type = frameType;
                                            pic.cur_size = streamBufRcvd;
                                            pic.time = ((long) frameTime) * 1000;
                                            pic.width = width;
                                            pic.height = height;
                                            if ((this.videoMask & (1 << pic.ch)) != 0) {
                                                this.eventListener.onStreamData(pic, this.streamBuf.array());
                                            }
                                            streamBufRcvd = 0;
                                            streamCh = -1;
                                            streamDataType = -1;
                                            break;
                                        }
                                        streamBufRcvd = 0;
                                        streamCh = -1;
                                        streamDataType = -1;
                                        logd("STREAM-DATA", "too big, len=" + curSize);
                                        break;
                                    }
                                    logd("STREAM-DATA", "Some packet lost, Type: old=" + (streamDataType == (short) 1 ? "Video" : "Audio") + "new=" + (dataType == (short) 1 ? "Video" : "Audio") + ", Ch: " + "old=" + streamCh + "new=" + chNum);
                                    streamBufRcvd = 0;
                                    streamCh = -1;
                                    streamDataType = -1;
                                    break;
                                default:
                                    this.eventListener.onResponseData(this.pktId, this.pktBuf, this.pktLen);
                                    break;
                            }
                        }
//                        logd("StreamThread", "Timeout, send CMD_SYS_ALIVE");
//                        if (sendPacket(CMD_SYS_ALIVE_RES, null, 0) < 0) {
//                            logd("StreamThread", "Error send CMD_SYS_ALIVE_RES");
//                        }
                    }
                }
                try {
                    this.streamSocket.close();
                } catch (Exception e) {
                }
                this.eventListener.onConnectionEvent(DVR_EVENT_DISCONNECTED);
                logd("DVRControl", "Stop !!!!");
            }
        } catch (Exception e2) {
            logd("DVRControl_connect", e2.getMessage());
            try {
                this.streamSocket.close();
            } catch (Exception e3) {
            }
            this.eventListener.onConnectionEvent(DVR_EVENT_SYSTEM_ERROR);
        }
    }

    public boolean isConnectd() {
        return this.connectFlag;
    }

    public int getVersion() {
        return this.dvrVersion;
    }

    public int getNumChannels() {
        return this.numChannels;
    }

    public String cameraName(int index) {
        if (index < this.numChannels) {
            return this.cameraName[index];
        }
        return null;
    }

    public int setChMask(int mask) {
        this.videoMask = mask;
        this.audioMask = mask;
        ByteBuffer pkt = ByteBuffer.allocate(2);
        pkt.putShort((short) mask);
        if (sendPacket(CMD_MULTI_CHG_CH_MASK_REQ, pkt.array(), 2) >= 0) {
            return 1;
        }
        logd("connect", "Error send CMD_MULTI_CHG_CH_MASK_REQ");
        return -1;
    }

    public int ptzControl(int channel, int cmd) {
        int pktCmdId;
        int ptzAction;
        switch (cmd) {
            case 0:
                pktCmdId = CMD_PTZ_MOVE;
                ptzAction = 65535;
                break;
            case 1:
                pktCmdId = CMD_PTZ_MOVE;
                ptzAction = 4096;
                break;
            case 2:
                pktCmdId = CMD_PTZ_MOVE;
                ptzAction = 0;
                break;
            case 3:
                pktCmdId = CMD_PTZ_MOVE;
                ptzAction = 256;
                break;
            case 4:
                pktCmdId = CMD_PTZ_MOVE;
                ptzAction = 4352;
                break;
            case 5:
                pktCmdId = CMD_PTZ_ZOOM;
                ptzAction = 256;
                break;
            case 6:
                pktCmdId = CMD_PTZ_ZOOM;
                ptzAction = 0;
                break;
            case 7:
                pktCmdId = CMD_PTZ_ZOOM;
                ptzAction = 65535;
                break;
            default:
                return 1;
        }
        ByteBuffer pkt = ByteBuffer.allocate(4);
        pkt.putShort((short) channel);
        pkt.putShort((short) ptzAction);
        if (sendPacket((short) pktCmdId, pkt.array(), 4) >= 0) {
            return 1;
        }
        logd("connect", "Error send CMD_PTZ_CONTROL_REQ");
        return -1;
    }

    public int startLive() {
        ByteBuffer pkt = ByteBuffer.allocate(4);
        pkt.putShort(0, (short) this.videoMask);
        pkt.putShort(2, (short) 1);
        logd("connect", "Send CMD_MULTI_LIVE_START_REQ");
        if (sendPacket(CMD_MULTI_LIVE_START_REQ, pkt.array(), 4) >= 0) {
            return 1;
        }
        logd("connect", "Error send CMD_MULTI_LIVE_START_REQ");
        return -1;
    }

    public int requestRecDate(int year, int month) {
        ByteBuffer pkt = ByteBuffer.allocate(4);
        pkt.putShort(0, (short) year);
        pkt.putShort(2, (short) month);
        logd("connect", "Send CMD_PB_REC_DATE_REQ");
        if (sendPacket(CMD_PB_REC_DATE_REQ, pkt.array(), 4) >= 0) {
            return 1;
        }
        logd("connect", "Error send CMD_PB_REC_DATE_REQ");
        return -1;
    }

    public int requestRecTime(int year, int month, int day) {
        ByteBuffer pkt = ByteBuffer.allocate(6);
        pkt.putShort(0, (short) year);
        pkt.putShort(2, (short) month);
        pkt.putShort(4, (short) day);
        logd("connect", "Send CMD_PB_REC_TIME_REQ");
        if (sendPacket(CMD_PB_REC_TIME_REQ, pkt.array(), 6) >= 0) {
            return 1;
        }
        logd("connect", "Error send CMD_PB_REC_TIME_REQ");
        return -1;
    }

    public int requestRecEvent(int year, int month, int day, short channelMask, short eventMask) {
        ByteBuffer pkt = ByteBuffer.allocate(10);
        pkt.putShort(0, (short) year);
        pkt.putShort(2, (short) month);
        pkt.putShort(4, (short) day);
        pkt.putShort(6, channelMask);
        pkt.putShort(8, eventMask);
        logd("connect", "Send CMD_PB_REC_EVENT_REQ");
        if (sendPacket(CMD_PB_REC_EVENT_REQ, pkt.array(), 10) >= 0) {
            return 1;
        }
        logd("connect", "Error send CMD_PB_REC_EVENT_REQ");
        return -1;
    }

    public int startPlayBack(Calendar startTime, short channelMask) {
        ByteBuffer pkt = ByteBuffer.allocate(14);
        pkt.putShort(0, (short) startTime.get(Calendar.YEAR));
        pkt.putShort(2, (short) startTime.get(Calendar.MONTH));
        pkt.putShort(4, (short) startTime.get(Calendar.DAY_OF_MONTH));
        pkt.putShort(6, (short) startTime.get(Calendar.HOUR_OF_DAY));
        pkt.putShort(8, (short) startTime.get(Calendar.MINUTE));
        pkt.putShort(10, (short) startTime.get(Calendar.SECOND));
        pkt.putShort(12, channelMask);
        logd("DVRControl::startPlayBack", "Send CMD_PB_START_REQ, time=" + startTime.get(Calendar.YEAR) + "/" + startTime.get(Calendar.MONTH) + "/" + startTime.get(Calendar.DAY_OF_MONTH) + " " + startTime.get(Calendar.HOUR_OF_DAY) + ":" + startTime.get(Calendar.MINUTE));
        if (sendPacket(CMD_PB_START_REQ, pkt.array(), 14) >= 0) {
            return 1;
        }
        logd("connect", "Error send CMD_PB_START_REQ");
        return -1;
    }

    public int stopPlayBack() {
        logd("connect", "Send CMD_MULTI_PB_STOP_REQ");
        if (sendPacket(CMD_MULTI_PB_STOP_REQ, null, 0) >= 0) {
            return 1;
        }
        logd("connect", "Error send CMD_MULTI_PB_STOP_REQ");
        return -1;
    }

    public int changePlayBackChannel(int channel) {
        this.videoMask = 1 << channel;
        this.audioMask = 1 << channel;
        ByteBuffer pkt = ByteBuffer.allocate(2);
        pkt.putShort((short) this.videoMask);
        logd("connect", "changePlayBackChannel(), channel=" + channel);
        if (sendPacket(CMD_MULTI_CHG_CH_MASK_REQ, pkt.array(), 2) >= 0) {
            return 1;
        }
        logd("connect", "Error send CMD_MULTI_CHG_CH_MASK_REQ");
        return -1;
    }

    public int pausePlayBack() {
        ByteBuffer pkt = ByteBuffer.allocate(6);
        pkt.putShort(0, (short) 2);
        pkt.putShort(2, (short) 0);
        pkt.putShort(4, (short) 0);
        logd("connect", "Send CMD_MULTI_PB_CHG_MODE_REQ(PAUSE)");
        if (sendPacket(CMD_MULTI_PB_CHG_MODE_REQ, pkt.array(), 6) >= 0) {
            return 1;
        }
        logd("connect", "Error send CMD_MULTI_PB_CHG_MODE_REQ");
        return -1;
    }

    public int forwardPlayBack(int speed) {
        ByteBuffer pkt = ByteBuffer.allocate(6);
        pkt.putShort(0, (short) 1);
        pkt.putShort(2, (short) 0);
        pkt.putShort(4, (short) speed);
        logd("connect", "Send CMD_MULTI_PB_CHG_MODE_REQ(FWD, speed=" + speed);
        if (sendPacket(CMD_MULTI_PB_CHG_MODE_REQ, pkt.array(), 6) >= 0) {
            return 1;
        }
        logd("connect", "Error send CMD_MULTI_PB_CHG_MODE_REQ");
        return -1;
    }

    public int backwardPlayBack(int speed) {
        ByteBuffer pkt = ByteBuffer.allocate(6);
        pkt.putShort(0, (short) 1);
        pkt.putShort(2, (short) 1);
        pkt.putShort(4, (short) speed);
        logd("connect", "Send CMD_MULTI_PB_CHG_MODE_REQ(BACK, speed=" + speed);
        if (sendPacket(CMD_MULTI_PB_CHG_MODE_REQ, pkt.array(), 6) >= 0) {
            return 1;
        }
        logd("connect", "Error send CMD_MULTI_PB_CHG_MODE_REQ");
        return -1;
    }

    public int stepForwardPlayBack() {
        ByteBuffer pkt = ByteBuffer.allocate(2);
        pkt.putShort(0, (short) 0);
        logd("connect", "Send CMD_MULTI_PB_NEXT_FRAME_REQ");
        if (sendPacket(CMD_MULTI_PB_NEXT_FRAME_REQ, pkt.array(), 2) >= 0) {
            return 1;
        }
        logd("connect", "Error send CMD_MULTI_PB_NEXT_FRAME_REQ");
        return -1;
    }

    public int stepBackwardPlayBack() {
        ByteBuffer pkt = ByteBuffer.allocate(2);
        pkt.putShort(0, (short) 1);
        logd("connect", "Send CMD_MULTI_PB_PREV_FRAME_REQ");
        if (sendPacket(CMD_MULTI_PB_PREV_FRAME_REQ, pkt.array(), 2) >= 0) {
            return 1;
        }
        logd("connect", "Error send CMD_MULTI_PB_PREV_FRAME_REQ");
        return -1;
    }

    private int getWidth(short video_type, short resolution) {
        switch (resolution) {
            case (short) 2:
                return 352;
            default:
                return 704;
        }
    }

    private int getHeight(short video_type, short resolution) {
        switch (resolution) {
            case (short) 0:
                if (video_type == (short) 0) {
                    return 480;
                }
                return 576;
            case (short) 1:
                if (video_type != (short) 0) {
                    return 288;
                }
                return 240;
            case (short) 2:
                if (video_type != (short) 0) {
                    return 288;
                }
                return 240;
            default:
                return 704;
        }
    }
}