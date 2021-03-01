import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;

public class ClientCommunicator implements ICommunicator {

    public Member self;
    public String serverIP;
    public String serverPort;
    public HashSet<Member> members;

    public Scanner in;
    public PrintWriter out;


    public ClientCommunicator(String uniqueID, String selfIP, String selfPort, String serverIP, String serverPort) {
        this.self = new Member(uniqueID, selfIP, selfPort);
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.members = new HashSet<>();
    }

    private void openSession() throws IOException {
        try {
            Socket socket = new Socket(serverIP, 59001);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) {
                String line = in.nextLine();
                if (line.startsWith("SUBMITNAME")) {
//                  Need to call the method to print out the username here
                    out.println("SYSTEM: CALL METHOD TO PRINT NAME");
                } else if (line.startsWith("NAMEACCEPTED")) {
//                  Need to call the method to update the GUI window with the username and allow user entry
                    out.println("SYSTEM: CALL METHOD TO UPDATE USERNAME");
//                    this.frame.setTitle("Chatter - " + line.substring(13));
//                    textField.setEditable(true);
                } else if (line.startsWith("MESSAGE")) {
//                  Need to call the method to add the user message to the active window
                    out.println("SYSTEM: CALL METHOD TO APPEND INCOMING MESSAGE");
//                    messageArea.append(line.substring(8) + "\n");
                }
            }
        } finally {
//            Call methods to update the frame and make it visible (window needs to have focus)
//            frame.setVisible(false);
//            frame.dispose();
        }
    }


    @Override
    public String getTargetIP() {
        return this.serverIP;
    }
    @Override
    public String getTargetPort() {
        return this.serverPort;
    }
    @Override
    public Member[] getMembers() {
        return (Member[]) this.members.toArray();
    }
}