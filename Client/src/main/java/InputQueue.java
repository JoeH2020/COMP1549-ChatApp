import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

// This class has the purpose of reading and storing user input, so that callin Scanner.nextLine() doesn't freeze
// the whole program
public class InputQueue {
    LinkedList<String> inputQueue = new LinkedList<>();
    Thread t;

    public InputQueue(InputStream inputStream) {
        t = new Thread() {
            public void run() {
                Scanner in = new Scanner(inputStream);
                while(true) {
                    if (in.hasNextLine())
                        inputQueue.add(in.nextLine());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {

                    }
                }
            }
        };
    }

    public boolean hasItems() {
        return !inputQueue.isEmpty();
    }

    public String nextItem() {
        return inputQueue.pop();
    }

    public void start() {
        t.start();
    }

    public void stop() {
        t.interrupt();
    }

}
