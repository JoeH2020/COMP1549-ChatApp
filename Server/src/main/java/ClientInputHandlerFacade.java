import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

public class ClientInputHandlerFacade {

    private ServerThread thread;
    private ServerSingleton serverSingleton = ServerSingleton.getInstance();

    public ClientInputHandlerFacade(ServerThread thread) {
        this.thread = thread;
    }

    public void handleInput(String input) {
        if (input.startsWith("MESSAGE")) {
            // parse and broadcast the message
            String message = input.substring(7);
            String time = serverSingleton.getTime();
            String toBroadcast = "MESSAGE" + time + message;
            serverSingleton.broadcast(toBroadcast);
        } else if (input.startsWith("VIEWMEMBERS")) {
            // send back a list of members
            HashSet<Member> members = serverSingleton.getMembers();
            StringBuilder toReturn = new StringBuilder("MEMBERS");
            Iterator<Member> membersIterator = members.iterator();
            while (membersIterator.hasNext()) {
                toReturn.append(membersIterator.next().getUID());
                if (membersIterator.hasNext()) toReturn.append(',');
            }
            thread.sendMessage(toReturn.toString());
        } else if (input.startsWith("WHISPER")) {
            // send out the whisper to the specified client
            String[] inputArray = input.split(":");
            String firstPart = inputArray[0];
            String message = String.join(":",
                    Arrays.copyOfRange(inputArray, 1, inputArray.length));
            String target = firstPart.substring(7);
            String sender = thread.getSelf().getUID();
            serverSingleton.whisper(target, message, sender);
        } else if (input.startsWith("CHECKALIVE")) {
            serverSingleton.broadcast(input);
        } else if (input.startsWith("ALIVE")) {
            // when getting an ALIVE response, forward it to the coordinator
            serverSingleton.getCoordinatorOut().println(input);
            if (thread.isCoordinatorThread()) {
                // if this is the coordinator, tell the server coordinator this thread is alive
                serverSingleton.getServerCoordinator().positiveResponse();
            }
        } else if (input.startsWith("TIMEOUT")) {
            // the coordinator is telling us someone has timed out,
            // propagate it to everyone else
            serverSingleton.broadcast(input);

            // also remove them from server list
            String name = input.substring(7);
            Member toRemove = new Member(name, null, null);
            serverSingleton.removeMember(toRemove);
        }
    }
}
