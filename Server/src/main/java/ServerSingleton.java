import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;

// this class stores all the information that is global to the server
public class ServerSingleton {
    private static ServerSingleton instance;

    public static ServerSingleton getInstance() {
        if (instance == null) {
            instance = new ServerSingleton();
        }
        return instance;
    }

    private Set<ServerThread> serverThreads = new HashSet<>();
    private HashMap<Member, ServerThread> members;
    private Member coordinator;
    private PrintWriter coordinatorOut;
    private ServerCoordinator serverCoordinator;

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

    public String returnTime(){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date date;
        date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }


    public void whisper(String targetMember, String msg, String messageFrom) {
        Iterator<ServerThread> it = serverThreads.iterator();
        while (it.hasNext()) {
            ServerThread currentThread = it.next();
            String compare = currentThread.getName();
            // Compare to only send to specific Thread
            if (compare.equals(targetMember) ) {
                currentThread.getPrintWriter().println("["+returnTime()+"]"+"WHISPER:" + messageFrom + " - " + msg);
            }
         }
    }

    public void return_to_self(String message, String UID){
        Iterator<ServerThread> it = serverThreads.iterator();

        while (it.hasNext()) {
            ServerThread currentThread = it.next();
            String compare = currentThread.getName();
            // Compare to only send to specific Thread
            if (compare.equals(UID) ) {
                currentThread.getPrintWriter().println(message);
            }
        }
    }


    public void viewmembers(String UID){
       return_to_self(getMembers().toString(), UID);
    }

    public String returnMembers(){
        return getMembers().toString();
    }

    public void selectNewCoordinator() {
        // first of all, remove the old coordinator from members
        members.remove(coordinator);

        // then select a new random coordinator
        try {
            ServerThread newCoordinatorThread = members.values().toArray(new ServerThread[0])[0];


            // tell the thread they are now the coordinator
            newCoordinatorThread.setAsCoordinator();

            // then set it as the new coordinator
            setCoordinator(newCoordinatorThread);
        } catch (IndexOutOfBoundsException e) {
            // the member array is empty, return to original state
            members.clear();
        }

    }

    private ServerSingleton() {
        this.members = new HashMap<>();
    }


    public  synchronized HashSet<Member> getMembers() {
        return new HashSet<Member>(members.keySet());
    }

    public synchronized void addMember(Member member, ServerThread thread) {
        members.put(member, thread);
    }

    public synchronized void removeMember(Member member) {
        members.remove(member);
    }

    public synchronized void setCoordinator(ServerThread coordinatorThread) {
        this.coordinator = new Member(coordinatorThread.getName(), coordinatorThread.getTargetIP(), coordinatorThread.getTargetPort());
        this.coordinatorOut = coordinatorThread.getOut();
        if (serverCoordinator == null) {
            serverCoordinator = new ServerCoordinator(coordinatorThread);
            serverCoordinator.start();
        } else {
            serverCoordinator.setThread(coordinatorThread);
        }
    }

    public synchronized ServerCoordinator getServerCoordinator() { return serverCoordinator; }

    public synchronized Member getCoordinator() { return coordinator; }

    public synchronized PrintWriter getCoordinatorOut() {
        return coordinatorOut;
    }

}
