public class ServerCoordinator extends Coordinator{
    public ServerCoordinator(ICommunicator communicator) {
        super(communicator);
    }

    @Override
    public void run() {
        // getTargetIP()
        // getTargetPort()
        // (they'd be ClientCoordinator ip and port)
        // connect to them
        // check if he's alive
    }
}
