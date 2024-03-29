import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

// This class has the purpose of reading and storing user input, so that calling Scanner.nextLine() doesn't freeze
// the whole program
public class InputQueue {
    LinkedList<String> inputQueue = new LinkedList<>();
    Thread t;
    Scanner in;

    public InputQueue(InputStream inputStream) {
        t = new Thread() {
            public void run() {
                in = new Scanner(inputStream);
                while(true) {
                    if (in.hasNextLine())
                        inputQueue.add(in.nextLine());
                    try {
                        Thread.sleep(100);
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
