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

    private ClientCoordinator coordinator;
    private ServerSingleton serverSingleton = ServerSingleton.getInstance();


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
                        // Need to call the method to update the GUI window with the username and allow user entry
                        System.out.println("Connected to server.");
                    } else if (line.startsWith("NAMEREFUSED")) {
                        System.out.println("Username is already in use. Please choose a new username.");
                        // Quit the program because the username is specified in command line
                        System.exit(0);
                    } else if (line.startsWith("MESSAGE")) {
                        // Need to call the method to add the user message to the active window
                        System.out.println(line.substring(7));
                    } else if (line.startsWith("SETCOORDINATOR")) {
                        coordinator = new ClientCoordinator(this);
                        coordinator.start();
                    } else if (line.startsWith("CHECKALIVE")) {
                        // this is a ping from the coordinator, respond appropriately
                        out.println("ALIVE" + self.getUID());
                    } else if (line.startsWith("ALIVE")) {
                        if (coordinator != null) coordinator.confirmedAlive(line.substring(5));
                    } else if (line.startsWith("JOIN")) {
                        // someone has joined the server, add them to the list
                        String name = line.substring(4);
                        System.out.println(name + " has joined the server.");
                        members.add(new Member(name, null, null));
                    } else if (line.startsWith("TIMEOUT")) {
                        // someone has timed out, remove them from the members list
                        String name = line.substring(7);
                        Member toRemove = new Member(name, null, null);
                        members.remove(toRemove);
                        // now tell the user
                        System.out.println(name + " has timed out.");
                    }else if (line.startsWith("/WHISPER")) {
                        //Need to send a message to just one user.
                        String targetMemberMessage = line.substring(8);
                        String targetMember = targetMemberMessage.substring(0, targetMemberMessage.indexOf(' ')).trim();
                        String message = line.substring(targetMember.length() + 8);
                        serverSingleton.whisper(targetMember, message);

                    }else if (line.startsWith("VIEWMEMBERS")) {
                        System.out.println("There are "+members.size()+ "current members - "+members);
                    } else {
                        System.out.println(line);
                    }
                }

                if (userInputQueue.hasItems()) {
                    String item = userInputQueue.nextItem();
                    if (!item.startsWith("/WHISPER")) {
                        out.println("MESSAGE" + self.getUID() + ":" + item);
                    } else {
                        out.println("/WHISPER" + self.getUID() + ":" + item);
                    }

                }
                // if there is nothing to do sleep for a while
                if (serverInputQueue.hasItems() || userInputQueue.hasItems()) continue;
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

    public void sendMessage(String message) {
        out.println(message);
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
        return this.members.toArray(new Member[0]);
    }

    public Member getSelf() {
        return self;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Please specify port and IP.");
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