import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;

public class ServerInputHandlerFacade {

    private ClientCommunicator communicator;

    public ServerInputHandlerFacade(ClientCommunicator communicator) {
        this.communicator = communicator;
    }

    public void handleInput(String input) {
        if (input.startsWith("SUBMITNAME")) {
            // Need to call the method to print out the username here
            communicator.sendMessage(communicator.getSelf().getUID());
        } else if (input.startsWith("NAMEACCEPTED")) {
            // Need to call the method to update the GUI window with the username and allow user entry
            System.out.println("Connected to server.");
            // after connecting request a member list
            communicator.sendMessage("VIEWMEMBERS");
        } else if (input.startsWith("NAMEREFUSED")) {
            System.out.println("Username is already in use. Please choose a new username.");
            // Quit the program because the username is specified in command input
            System.exit(0);
        } else if (input.startsWith("MESSAGE")) {
            // Need to call the method to add the user message to the active window
            String time = input.substring(7, 11);
            String hours = time.substring(0, 2);
            String minutes = time.substring(2);
            String message = input.substring(11);
            System.out.println("[%s:%s] %s".formatted(hours, minutes, message));
        } else if (input.startsWith("SETCOORDINATOR")) {
            communicator.setCoordinator();
        } else if (input.startsWith("CHECKALIVE")) {
            // this is a ping from the coordinator, respond appropriately
            communicator.sendMessage("ALIVE" + communicator.getSelf().getUID());
        } else if (input.startsWith("ALIVE")) {
            ClientCoordinator coordinator = communicator.getCoordinator();
            if (coordinator != null)
                coordinator.confirmedAlive(input.substring(5));
        } else if (input.startsWith("JOIN")) {
            // someone has joined the server, add them to the list
            String name = input.substring(4);
            System.out.println(name + " has joined the server.");
            communicator.addMember(new Member(name, null, null));
        } else if (input.startsWith("TIMEOUT")) {
            // someone has timed out, remove them from the members list
            String name = input.substring(7);
            Member toRemove = new Member(name, null, null);
            communicator.removeMember(toRemove);
            // now tell the user
            System.out.println(name + " has timed out.");
        } else if (input.startsWith("DISCONNECTED")) {
            // someone has disconnected, remove from members and inform users
            String name = input.substring(12);
            Member toRemove = new Member(name, null, null);
            communicator.removeMember(toRemove);
            // now tell the user
            System.out.println(name + " has disconnected.");
        } else if (input.startsWith("WHISPER")) {
            String[] inputArray = input.split(":");
            String time = inputArray[0].substring(7, 11);
            String hours = time.substring(0, 2);
            String minutes = time.substring(2);
            String sender = inputArray[0].substring(11);
            // need to do this in case message has colons
            String[] messageArray = Arrays.copyOfRange(inputArray, 1, inputArray.length);
            String message = String.join(":", messageArray);
            String toPrint = "[%s:%s] %s whispered: %s".formatted(hours, minutes, sender, message);
            System.out.println(toPrint);
        } else if (input.startsWith("MEMBERS")) {
            // print the members
            String membersString = input.substring(7);
            String[] memberNames = membersString.split(",");
            StringBuilder toPrint = new StringBuilder("Members: ");
            Iterator<String> memberNamesIterator = Arrays.stream(memberNames).iterator();
            while (memberNamesIterator.hasNext()) {
                toPrint.append(memberNamesIterator.next());
                if (memberNamesIterator.hasNext()) toPrint.append(", ");
            }
            // add them to the members list if it's empty
            communicator.addMembers(memberNames);
            System.out.println(toPrint);
        } else {
            System.out.println(input);
        }
    }
}
