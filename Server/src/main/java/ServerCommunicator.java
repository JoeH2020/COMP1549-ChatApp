import java.util.HashSet;

public class ServerCommunicator implements ICommunicator {

    public Member coordinator;
    public HashSet<Member> members;

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
