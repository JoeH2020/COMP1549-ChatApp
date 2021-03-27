import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerThread implements ICommunicator, Runnable {

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    private PrintWriter out;
    private String name;
    private Socket socket;
    private Scanner in;

    private HashSet<Member> members;

    protected ServerSingleton serverSingleton;
    private boolean isCoordinatorThread = false;


    public void run() {
        serverSingleton = ServerSingleton.getInstance();
        serverSingleton.addThreadInstance(this);

        try {
            System.out.println("New client trying to join: " + socket.getInetAddress() + ":" + socket.getPort());
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("SUBMITNAME");
            ClientInputHandlerFacade inputHandler = new ClientInputHandlerFacade(this);

            // Keep requesting a name until we get a unique one.
            while (in.hasNextLine()) {
                name = in.nextLine();
                if (name == null) {
                    return;
                }
                // start the process for accepting a new member
                members = serverSingleton.getMembers();
                Member prospectiveMember = new Member(name, socket.getInetAddress().toString(), String.valueOf(socket.getPort()));
                if (members.isEmpty()) {
                    //This user will become the first coordinator
                    serverSingleton.setCoordinator(this);
                    // Communicate to the new coordinator their role
                    out.println("NAMEACCEPTED");
                    addMember(prospectiveMember);
                    setAsCoordinator();

                } else if (!members.contains(prospectiveMember)) {
                    //Checking if name is not already in names, if not add them
                    out.println("NAMEACCEPTED");
                    addMember(prospectiveMember);
                } else {
                    // there already is a member with this name, refuse them
                    out.println("NAMEREFUSED");
                    return;
                }
                // client is accepted, tell everyone they joined
                serverSingleton.broadcast("JOIN" + name);

                // Accept messages from this client and broadcast them.
                while (true) {
                    String input = in.nextLine();
                    inputHandler.handleInput(input);
                    if (!socket.isConnected()) {
                        // the socket has been disconnected so we remove client and inform everyone else
                        serverSingleton.broadcast("DISCONNECTED"+ prospectiveMember.getUID());

                        // if this thread is the coordinator thread we should tell the singleton to choose a new one
                        if (isCoordinatorThread){
                            System.out.println("Coordinator has disconnected.");
                            serverSingleton.selectNewCoordinator();
                        }

                        // also remove them from server list
                        serverSingleton.removeMember(prospectiveMember);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (name != null) {
                System.out.println(name + " is leaving");
            }
            try { socket.close(); } catch (IOException e) {}
        }
    }

    private void addMember(Member member) {
        members.add(member);
        serverSingleton.addMember(member, this);
    }

    @Override
    public Member[] getMembers() {
        return serverSingleton.getMembers().toArray(new Member[0]);
    }

    public Member getSelf() {
        Member m = new Member(name, getTargetIP(), getTargetPort());
        return m;
    }

    @Override
    public String getTargetIP() {
        return socket.getInetAddress().toString();
    }

    @Override
    public String getTargetPort() {
        return Integer.toString(socket.getPort());
    }

    public boolean isCoordinatorThread() {
        return isCoordinatorThread;
    }

    public void setAsCoordinator() {
        out.println("SETCOORDINATOR");
        isCoordinatorThread = true;
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}
