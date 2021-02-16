public abstract class Coordinator implements Runnable {

    private ICommunicator communicator;

    public Coordinator(ICommunicator communicator) {
        this.communicator = communicator;
    }

    @Override
    public abstract void run();
}
