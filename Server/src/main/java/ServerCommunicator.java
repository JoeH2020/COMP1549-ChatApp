import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class ServerCommunicator implements ICommunicator {


    public Member coordinator;
    public HashSet<Member> members;
    private static class Handler implements Runnable {
        private String name;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;
        private static Set<String> names = new HashSet<>();
        private static Set<PrintWriter> writers = new HashSet<>();

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {



            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                // Keep requesting a name until we get a unique one.
                while (true) {
                    out.println("What is your name?");
                    name = in.nextLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!name.isEmpty() && !names.contains(name)) {
                            //Checking if name is already in names
                            names.add(name);
                            break;
                        }else {
                            out.println("Your name seems familiar. Try adding a number to the end.");
                        }
                    }
                }

                out.println("Hello" + name + ", your name has been accepted.");
                for (PrintWriter writer : writers) {
                    writer.println("MESSAGE " + name + " has joined the chat");
                }
                writers.add(out);

                // Accept messages from this client and broadcast them.
                while (true) {
                    String input = in.nextLine();
                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    }
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + ": " + input);
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
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
        return (Member[]) members.toArray();
    }
}
