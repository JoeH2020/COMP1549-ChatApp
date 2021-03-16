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

    public Member coordinator;
    private PrintWriter out;
    private String name;
    private Socket socket;
    private Scanner in;
    private Set<String> names = new HashSet<>();
    private Set<PrintWriter> writers = new HashSet<>();

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
                    // out.println("New Coordinator:" + name);
                    coordinator = prospectiveMember;
                    members.add(prospectiveMember);

                } else if (!members.contains(prospectiveMember)) {
                    //Checking if name is not already in names
                    // name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
                    // Capitalising the first letter of their names then adding them
                    members.add(prospectiveMember);
                } else {
                    out.println("NAMEREFUSED");
                }
                out.println("NAMEACCEPTED");
                serverSingleton.broadcast("test broadcast");
                for (PrintWriter writer : writers) {
                    writer.println("MESSAGE " + name + " has joined the chat");
                }
                writers.add(out);


                // Accept messages from this client and broadcast them.
                while (true) {
                    String input = in.nextLine();
                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    } else if (input.startsWith("MESSAGE")) {
                        serverSingleton.broadcast(input);
                    }
//                    for (PrintWriter writer : writers) {
//                        writer.println("MESSAGE " + name + ": " + input);
//                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                writers.remove(out);
            }


            if (name != null) {
                System.out.println(name + " is leaving");
                names.remove(name);
                for (PrintWriter writer : writers) {
                    writer.println("MESSAGE " + name + " has left");
                }
            }
            try { socket.close(); } catch (IOException e) {}
        }
    }



    @Override
    public String getTargetIP() {
        return coordinator.getIP();
    }

    @Override
    public String getTargetPort() {
        return coordinator.getPort();
    }

    @Override
    public Member[] getMembers() {
        return serverSingleton.getMembers().toArray(new Member[0]);
    }

    public PrintWriter getPrintWriter() {
        return out;
    }

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
