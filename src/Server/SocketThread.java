/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 *
 */
public class SocketThread implements Runnable {

    Socket socket;
    MainForm main;
    DataInputStream dis;
    StringTokenizer st;
    String client, filesharing_username;

    private final int BUFFER_SIZE = 100;

    public SocketThread(Socket socket, MainForm main) {
        this.main = main;
        this.socket = socket;

        try {
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            main.appendMessage("[SocketThreadIOException]: " + e.getMessage());
        }
    }

    /*   Hàm này sẽ lấy client socket trong danh sách client socket sau đó sẽ thiết lập kết nối    */
    private void createConnection(String receiver, String sender, String filename) {
        try {
            main.appendMessage("Đang tạo kết nối chia sẻ file.");
            Socket s = main.getClientList(receiver);
            if (s != null) { // Client đã tồn tại
//                main.appendMessage("[createConnection]: Socket OK");
                DataOutputStream dosS = new DataOutputStream(s.getOutputStream());
//                main.appendMessage("[createConnection]: DataOutputStream OK");
                // Format:  CMD_FILE_XD [sender] [receiver] [filename]
                String format = "CMD_FILE_XD " + sender + " " + receiver + " " + filename;
                dosS.writeUTF(format);
//                main.appendMessage("[createConnection]: " + format);
            } else {// Client không tồn tại, gửi lại cho sender rằng receiver không tìm thấy.
                main.appendMessage("Không tìm thấy Client '" + receiver + "'");
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF("CMD_SENDFILEERROR " + "Client '" + receiver + "' không được tìm thấy trong danh sách, bảo đảm rằng user đang online.!");
            }
        } catch (IOException e) {
            main.appendMessage("[createConnection]: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                /**
                 * nhận CMD key từ Client *
                 */
                String data = dis.readUTF();
                st = new StringTokenizer(data);
                String CMD = st.nextToken();
                /**
                 * Kiểm tra CMD key*
                 */
                switch (CMD) {
                    case "CMD_JOIN":
                        /**
                         * CMD_JOIN [clientUsername] *
                         */
                        String clientUsername = st.nextToken();
                        client = clientUsername;
                        main.setClientList(clientUsername);
                        main.setSocketList(socket);
                        main.appendMessage("[Client]: " + clientUsername + " tham gia chatroom.!");
                        break;

                    case "CMD_CHAT":
                        /**
                         * CMD_CHAT [from] [sendTo] [message] *
                         */
                        String from = st.nextToken();
                        String sendTo = st.nextToken();
                        String msg = "";
                        while (st.hasMoreTokens()) {
                            msg = msg + " " + st.nextToken();
                        }
                        Socket tsoc = main.getClientList(sendTo);
                        try {
                            DataOutputStream dos = new DataOutputStream(tsoc.getOutputStream());
                            /**
                             * CMD_MESSAGE *
                             */
                            String content = from + ": " + msg;
                            dos.writeUTF("CMD_MESSAGE " + content);
                            main.appendMessage("[Message]: Từ " + from + " Đến " + sendTo + " : " + msg);
                        } catch (IOException e) {
                            main.appendMessage("[IOException]: Không thể gửi tin nhắn đến " + sendTo);
                        }
                        break;

                    case "CMD_CHATALL":
                        /**
                         * CMD_CHATALL [from] [message] *
                         */
                        String chatall_from = st.nextToken();
                        String chatall_msg = "";
                        while (st.hasMoreTokens()) {
                            chatall_msg = chatall_msg + " " + st.nextToken();
                        }
                        String chatall_content = chatall_from + " " + chatall_msg;
                        for (int x = 0; x < main.clientList.size(); x++) {
                            if (!main.clientList.elementAt(x).equals(chatall_from)) {
                                try {
                                    Socket tsoc2 = (Socket) main.socketList.elementAt(x);
                                    DataOutputStream dos2 = new DataOutputStream(tsoc2.getOutputStream());
                                    dos2.writeUTF("CMD_MESSAGE " + chatall_content);
                                } catch (IOException e) {
                                    main.appendMessage("Chat all: " + e.getMessage());
                                }
                            }
                        }
                        main.appendMessage("Chat all: " + chatall_content);
                        break;

                    case "CMD_SHARINGSOCKET":
                        main.appendMessage("Client thiết lập một socket cho kết nối chia sẻ file...");
                        String file_sharing_username = st.nextToken();
                        filesharing_username = file_sharing_username;
                        main.setClientFileSharingUsername(file_sharing_username);
                        main.setClientFileSharingSocket(socket);
                        main.appendMessage("Username: " + file_sharing_username);
//                        main.appendMessage("Chia Sẻ File đang được mở");
                        break;

                    case "CMD_SENDFILE":
                        main.appendMessage("Client đang gửi một file...");
                        /*
                         Format: CMD_SENDFILE [Filename] [Size] [Recipient] [Consignee]  from: Sender Format
                         Format: CMD_SENDFILE [Filename] [Size] [Consignee] to Receiver Format
                         */
                        String file_name = st.nextToken();
                        String filesize = st.nextToken();
                        String sendto = st.nextToken();
                        String consignee = st.nextToken();
                        main.appendMessage("Từ: " + consignee);
                        main.appendMessage("Đến: " + sendto);
                        /**
                         * Nhận client Socket *
                         */
                        main.appendMessage("Sẵn sàng cho các kết nối..");
                        Socket cSock = main.getClientFileSharingSocket(sendto); /* Consignee Socket  */
                        /*   Now Check if the consignee socket was exists.   */

                        if (cSock != null) { /* Exists   */

                            try {
                                main.appendMessage("Đã được kết nối..!");
                                /**
                                 * viết filename..  *
                                 */
                                main.appendMessage("Đang gửi file đến client...");
                                DataOutputStream cDos = new DataOutputStream(cSock.getOutputStream());
                                cDos.writeUTF("CMD_SENDFILE " + file_name + " " + filesize + " " + consignee);
                                /**
                                 * đọc nội dung file   *
                                 */
                                InputStream input = socket.getInputStream();
                                OutputStream sendFile = cSock.getOutputStream();
                                byte[] buffer = new byte[BUFFER_SIZE];
                                int cnt;
                                //send File to Client
                                while ((cnt = input.read(buffer)) > 0) {
                                    sendFile.write(buffer, 0, cnt);
                                }
                                //xoa bo dem 
                                sendFile.flush();
                                sendFile.close();
                                /**
                                 * Xóa danh sách client *
                                 */
                                main.removeClientFileSharing(sendto);
                                main.removeClientFileSharing(consignee);
                                main.appendMessage("File đã được gửi đến client...");
                            } catch (IOException e) {
                                main.appendMessage("" + e.getMessage());
                            }
                        } else { /*   Không tồn tại, return error  */
                            /*   FORMAT: CMD_SENDFILEERROR  */

                            main.removeClientFileSharing(consignee);
                            main.appendMessage("Client '" + sendto + "' không tìm thấy.!");
                            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                            dos.writeUTF("CMD_SENDFILEERROR " + "Client '" + sendto + "' không tìm thấy, Chia Sẻ File sẽ thoát.");
                        }
                        break;

                    case "CMD_SENDFILERESPONSE":
                        /*
                         Format: CMD_SENDFILERESPONSE [username] [Message]
                         */
                        String receiver = st.nextToken(); // phương thức nhận receiver username
                        String rMsg = ""; // phương thức nhận error message
                        main.appendMessage("Username: " + receiver);
                        while (st.hasMoreTokens()) {
                            rMsg = rMsg + " " + st.nextToken();
                        }
                        try {
                            Socket rSock = (Socket) main.getClientFileSharingSocket(receiver);
                            DataOutputStream rDos = new DataOutputStream(rSock.getOutputStream());
                            rDos.writeUTF("CMD_SENDFILERESPONSE" + " " + receiver + " " + rMsg);
                        } catch (IOException e) {
                            main.appendMessage("" + e.getMessage());
                        }
                        break;

                    case "CMD_SEND_FILE_XD":  // Format: CMD_SEND_FILE_XD [sender] [receiver]                        
                        try {
                            String send_sender = st.nextToken();
                            String send_receiver = st.nextToken();
                            String send_filename = st.nextToken();
                            main.appendMessage("Host: " + send_sender);
                            this.createConnection(send_receiver, send_sender, send_filename);
                        } catch (Exception e) {
                            main.appendMessage("" + e.getLocalizedMessage());
                        }
                        break;

                    case "CMD_SEND_FILE_ERROR":  // Format:  CMD_SEND_FILE_ERROR [receiver] [Message]
                        String eReceiver = st.nextToken();
                        String eMsg = "";
                        while (st.hasMoreTokens()) {
                            eMsg = eMsg + " " + st.nextToken();
                        }
                        try {
                            /*  Gửi Error đến File Sharing host  */
                            Socket eSock = main.getClientFileSharingSocket(eReceiver); // phương thức nhận file sharing host socket cho kết nối
                            DataOutputStream eDos = new DataOutputStream(eSock.getOutputStream());
                            //  Format:  CMD_RECEIVE_FILE_ERROR [Message]
                            eDos.writeUTF("CMD_RECEIVE_FILE_ERROR " + eMsg);
                        } catch (IOException e) {
                            main.appendMessage("Error: " + e.getMessage());
                        }
                        break;

                    case "CMD_SEND_FILE_ACCEPT": // Format:  CMD_SEND_FILE_ACCEPT [receiver] [Message]
                        String aReceiver = st.nextToken();
                        String aMsg = "";
                        while (st.hasMoreTokens()) {
                            aMsg = aMsg + " " + st.nextToken();
                        }
                        try {
                            /*  Send Error to the File Sharing host  */
                            Socket aSock = main.getClientFileSharingSocket(aReceiver); // get the file sharing host socket for connection
                            DataOutputStream aDos = new DataOutputStream(aSock.getOutputStream());
                            //  Format:  CMD_RECEIVE_FILE_ACCEPT [Message]
                            aDos.writeUTF("CMD_RECEIVE_FILE_ACCEPT " + aMsg);
                        } catch (IOException e) {
                            main.appendMessage("Error " + e.getMessage());
                        }
                        break;

                    default:
                        main.appendMessage("Không rõ lệnh " + CMD);
                        break;
                }
            }
        } catch (IOException e) {
            /*chatting client, remove nếu như nó tồn tại..   */
            System.out.println(client);
            System.out.println("File Sharing: " + filesharing_username);
            main.removeFromTheList(client);
            if (filesharing_username != null) {
                main.removeClientFileSharing(filesharing_username);
            }
            main.appendMessage("Kết nối client bị đóng..!");
        }
    }

}
