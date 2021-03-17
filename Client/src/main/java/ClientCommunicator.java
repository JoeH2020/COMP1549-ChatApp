import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;

public class ClientCommunicator implements ICommunicator {

    private static String serverPort;
    public Member self;
    public String serverIP;
    public static HashSet<Member> members;

    public Scanner in;
    public PrintWriter out;

    private InputQueue userInputQueue = new InputQueue(System.in);
    private InputQueue serverInputQueue;


    public ClientCommunicator(String uniqueID, String selfIP, String selfPort, String serverIP, String serverPort) {
        this.self = new Member(uniqueID, selfIP, selfPort);
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.members = new HashSet<>();
    }

    private void openSession() throws IOException {

        try {
            Socket socket = new Socket(serverIP, 59001);
            serverInputQueue = new InputQueue(socket.getInputStream());
            serverInputQueue.start();
            userInputQueue.start();
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                if(serverInputQueue.hasItems()) {
                    String line = serverInputQueue.nextItem();
                    if (line.startsWith("SUBMITNAME")) {
    //                  Need to call the method to print out the username here
                        out.println(self.getUID());
                    } else if (line.startsWith("NAMEACCEPTED")) {
                        //                  Need to call the method to update the GUI window with the username and allow user entry
                        System.out.println("Connected to server.");
                        //                    this.frame.setTitle("Chatter - " + line.substring(13));
                        //                    textField.setEditable(true);
                    } else if (line.startsWith("NAMEREFUSED")) {
                        System.out.println("Username is already in use. Please choose a new username.");
                        // Quit the program because the username is specified in command line
                        System.exit(0);
                    } else if (line.startsWith("MESSAGE")) {
    //                  Need to call the method to add the user message to the active window
                        //out.println("SYSTEM: CALL METHOD TO APPEND INCOMING MESSAGE");
    //                    messageArea.append(line.substring(8) + "\n");
                        System.out.println(line.substring(7));
                    } else {
                        System.out.println(line);
                    }
                }

                if (userInputQueue.hasItems()) {
                    String item = userInputQueue.nextItem();
                    out.println("MESSAGE" + self.getUID() + ":" + item);
                }
                // sleep so that every thread has a chance to run
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            //e.printStackTrace();
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

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Please specify name and IP.");
            return;
        }


//        int membersSize = members.size(); //To do this I made members static, change if this is a problem.
//        membersSize++;
//        String uniqueID = String.valueOf(membersSize); //Used to create a unique ID, check length and add 1
        String serverIP = args[1];
//
//        Socket socket = new Socket(args[0], 59001);
//
//        String selfIP = String.valueOf(socket.getInetAddress()); //Getting the IP of the Client as a String
//        String selfPort = String.valueOf(socket.getPort()); //Getting the port number of client as a String



        ClientCommunicator client = new ClientCommunicator(args[0], "127.0.0.1", "59001", serverIP, serverPort);

        client.openSession(); //Start the session
    }

}