import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Please specify a port (and nothing else)");
        }

        int port = Integer.parseInt(args[0]);

        ExecutorService pool = Executors.newFixedThreadPool(500);
        System.out.println("Waiting for clients to join...");
        ServerSocket listener = new ServerSocket(port);
            while (true) {
                pool.execute(new ServerThread(listener.accept()));
            }
    }
}
