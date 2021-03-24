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
                    out.println("NAMEREFUSED");
                    return;
                }
                serverSingleton.broadcast("JOIN" + name);

                // Accept messages from this client and broadcast them.
                while (true) {
                    String input = in.nextLine();
                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    } else if (input.startsWith("MESSAGE")) {
                        String message = input.substring(7);
                        String time = serverSingleton.getTime();
                        String toBroadcast = "MESSAGE" + time + message;
                        serverSingleton.broadcast(toBroadcast);
                    } else if (input.startsWith("VIEWMEMBERS")) {
                        HashSet<Member> members = serverSingleton.getMembers();
                        StringBuilder toReturn = new StringBuilder("MEMBERS");
                        Iterator<Member> membersIterator = members.iterator();
                        while(membersIterator.hasNext()) {
                            toReturn.append(membersIterator.next().getUID());
                            if (membersIterator.hasNext()) toReturn.append(',');
                        }
                        out.println(toReturn.toString());
                    } else if (input.startsWith("WHISPER")) {
                        String[] inputArray = input.split(":");
                        String firstPart = inputArray[0];
                        String message = String.join(":",
                                Arrays.copyOfRange(inputArray, 1, inputArray.length));
                        String target = firstPart.substring(7);
                        String sender = getName();
                        serverSingleton.whisper(target, message, sender);
                    } else if (input.startsWith("CHECKALIVE")) {
                        serverSingleton.broadcast(input);
                    } else if (input.startsWith("ALIVE")) {
                        // when getting an ALIVE response, forward it to the coordinator
                        serverSingleton.getCoordinatorOut().println(input);
                       if (isCoordinatorThread) {
                            // if this is the coordinator, tell the server coordinator this thread is alive
                            serverSingleton.getServerCoordinator().positiveResponse();
                        }
                    } else if (input.startsWith("TIMEOUT")) {
                        // the coordinator is telling us someone has timed out,
                        // propagate it to everyone else
                        serverSingleton.broadcast(input);

                        // also remove them from server list
                        String name = input.substring(7);
                        Member toRemove = new Member(name, null, null);
                        serverSingleton.removeMember(toRemove);
                    } else if (!socket.isConnected()) {
                        // the socket has been disconnected so we remove client and inform everyone else
                        serverSingleton.broadcast("DISCONNECTED"+ prospectiveMember.getUID());

                        // if this thread is the coordinator thread we should tell the singleton to choose a new one
                        System.out.println("Coordinator has disconnected.");
                        serverSingleton.selectNewCoordinator();

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
    public String getTargetIP() {
        return socket.getInetAddress().toString();
    }

    @Override
    public String getTargetPort() {
        return Integer.toString(socket.getPort());
    }

    @Override
    public Member[] getMembers() {
        return serverSingleton.getMembers().toArray(new Member[0]);
    }

    public PrintWriter getPrintWriter() {
        return out;
    }

    public Member getSelf() {
        Member m = new Member(getName(), getTargetIP(), getTargetPort());
        return m;
    }

    public String getName() {
        return name;
    }

    public PrintWriter getOut() {
        return out;
    }


    public void setAsCoordinator() {
        out.println("SETCOORDINATOR");
        isCoordinatorThread = true;
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

        }
    }
}
