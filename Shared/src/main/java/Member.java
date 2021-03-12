import java.util.Objects;

public class Member {

    private String UID;
    private String port;
    private String IP;

    public Member(String UID, String IP, String port) {
        this.UID = UID;
        this.port = port;
        this.IP = IP;
    }

    public String getUID() {
        return UID;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }


    // these two methods allow us to check if a hashcode contains a Member
    // by only checking the UID
    @Override
    public int hashCode() {
        return Objects.hash(UID);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Member) {
            Member m = (Member) o;
            if (m.getUID().equals(this.UID)) {
                return true;
            }
        }
        return false;
    }
}
