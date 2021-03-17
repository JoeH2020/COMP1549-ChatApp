import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// this class stores all the information that is global to the server
public class ServerSingleton {
    private static ServerSingleton instance;
    private Set<ServerThread> serverThreads = new HashSet<>();



    public static ServerSingleton getInstance() {
        if (instance == null) {
            instance = new ServerSingleton();
        }
        return instance;
    }

    public void addThreadInstance(ServerThread thread) {
        if (!serverThreads.contains(thread)) {
            serverThreads.add(thread);
            return;
        }
    }

    public void broadcast(String msg) {
        Iterator<ServerThread> it = serverThreads.iterator();
        while(it.hasNext()){
            it.next().getPrintWriter().println(msg);
        }
    }

    private HashSet<Member> members;

    private ServerSingleton() {
        this.members = new HashSet<>();
    }

    public  synchronized HashSet<Member> getMembers() {
        return members;
    }

    public synchronized void addMember(Member member) {
        members.add(member);
    }

    public synchronized void removeMember(Member member) {
        members.remove(member);
    }
}
