import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
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

    private ServerSingleton serverSingleton;


    public void run() {
        serverSingleton = ServerSingleton.getInstance();
        serverSingleton.addThreadInstance(this);

        try {
            System.out.println("New client joined: " + socket.getInetAddress() + ":" + socket.getPort());
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("SUBMITNAME");

            // Keep requesting a name until we get a unique one.
            while (in.hasNextLine()) {
                name = in.nextLine();
                if (name == null) {
                    return;
                }
                HashSet<Member> members = serverSingleton.getMembers();
                Member prospectiveMember = new Member(name, socket.getInetAddress().toString(), String.valueOf(socket.getPort()));
                if (members.isEmpty()) {
                    //This user will become the first coordinator
                    serverSingleton.setCoordinator(prospectiveMember, out);
                    // Communicate to the new coordinator their role
                    out.println("NAMEACCEPTED");
                    out.println("SETCOORDINATOR");
                    members.add(prospectiveMember);

                } else if (!members.contains(prospectiveMember)) {
                    //Checking if name is not already in names, if not add them
                    out.println("NAMEACCEPTED");
                    members.add(prospectiveMember);
                } else {
                    out.println("NAMEREFUSED");
                }
                serverSingleton.broadcast("JOIN" + name);


                // Accept messages from this client and broadcast them.
                while (true) {
                    String input = in.nextLine();
                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    } else if (input.startsWith("MESSAGE")) {
                        serverSingleton.broadcast(input);
                    } else if (input.startsWith("CHECKALIVE")) {
                        serverSingleton.broadcast(input);
                    } else if (input.startsWith("ALIVE")) {
                        // when getting an ALIVE response, forward it to the coordinator
                        serverSingleton.getCoordinatorOut().println(input);
                    } else if (input.startsWith("TIMEOUT")) {
                        // the coordinator is telling us someone has timed out,
                        // propagate it to everyone else
                        serverSingleton.broadcast(input);
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



    @Override
    public String getTargetIP() {
        return serverSingleton.getCoordinator().getIP();
    }

    @Override
    public String getTargetPort() {
        return serverSingleton.getCoordinator().getPort();
    }

    @Override
    public Member[] getMembers() {
        return serverSingleton.getMembers().toArray(new Member[0]);
    }

    public PrintWriter getPrintWriter() {
        return out;
    }

    public void sendMessage(String message) {}

    public static void main(String[] args) {
        System.out.println("Waiting for clients to join...");
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(59001)) {
            while (true) {
                pool.execute(new ServerThread(listener.accept()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
