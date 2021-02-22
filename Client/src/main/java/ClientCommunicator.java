import java.util.HashSet;

public class ClientCommunicator implements ICommunicator {

    public Member self;
    public String serverIP;
    public String serverPort;
    public HashSet<Member> members;

    public ClientCommunicator(String uniqueID, String selfIP, String selfPort, String serverIP, String serverPort) {
        this.self = new Member(uniqueID, selfIP, selfPort);
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.members = new HashSet<>();
    }

    @Override
    public String getTargetIP() {
        return this.serverIP;
    }

    @Override
    public String getTargetPort() {
        return this.serverPort;
    }

    @Override
    public Member[] getMembers() {
        return (Member[]) this.members.toArray();
    }
}
