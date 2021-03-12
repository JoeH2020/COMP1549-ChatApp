import java.util.HashSet;

// this class stores all the information that is global to the server
public class ServerSingleton {
    private static ServerSingleton instance;

    public static ServerSingleton getInstance() {
        if (instance == null) {
            instance = new ServerSingleton();
        }
        return instance;
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
