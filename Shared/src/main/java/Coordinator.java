public abstract class Coordinator extends Thread {

    protected ICommunicator communicator;

    public Coordinator(ICommunicator communicator) {
        this.communicator = communicator;
    }

}
