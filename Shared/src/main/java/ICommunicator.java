public interface ICommunicator {

    public String getTargetIP();
    public String getTargetPort();
    public Member[] getMembers();
    public void sendMessage(String message);
}
