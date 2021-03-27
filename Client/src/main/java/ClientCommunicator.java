import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

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

    private boolean timedOut;


    public ClientCommunicator(String uniqueID, String selfIP, String selfPort, String serverIP, String serverPort) {
        this.self = new Member(uniqueID, selfIP, selfPort);
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.members = new HashSet<>();
    }

    public void openSession() throws Exception {
        // open the socket and instantiate all necessary classes
        Socket socket = new Socket(serverIP, Integer.parseInt(serverPort));
        serverInputQueue = new InputQueue(socket.getInputStream());
        serverInputQueue.start();
        userInputQueue.start();
        out = new PrintWriter(socket.getOutputStream(), true);

        // instantiate our input handlers
        ServerInputHandlerFacade serverInputHandler = new ServerInputHandlerFacade(this);
        UserInputHandlerFacade userInputHandler = new UserInputHandlerFacade(this);

        while (true) {
            if (!timedOut) {
                if(serverInputQueue.hasItems()) {
                    String line = serverInputQueue.nextItem();
                    serverInputHandler.handleInput(line);
                }

                if (userInputQueue.hasItems()) {
                    String item = userInputQueue.nextItem();
                    userInputHandler.handleInput(item);
                }
            }
            // if there is nothing to do sleep for a while
            if (serverInputQueue.hasItems() || userInputQueue.hasItems()) continue;
            Thread.sleep(500);
        }
    }

    // set this client as coordinator
    public void setCoordinator() {
        coordinator = new ClientCoordinator(this);
        coordinator.start();
    }

    // get the instance of the coordinator
    public ClientCoordinator getCoordinator() {
        return coordinator;
    }

    public void addMembers(String[] memberNames) {
        for (String name: memberNames) {
            members.add(new Member(name, null, null));
        }
    }

    public void addMember(Member member) {
        members.add(member);
    }

    public void removeMember(Member member) {
        members.remove(member);
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    // make this client simulate a timeout
    public void timeOut() {
        System.out.println("Simulating a timeout...");
        timedOut = true;
        try {
            Thread.sleep(60000);
        } catch (Exception e) {}
        System.exit(0);
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
}