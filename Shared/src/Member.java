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

    public void setUID(String UID) {
        this.UID = UID;
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
}
