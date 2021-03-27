import java.util.Arrays;

public class UserInputHandlerFacade {

    private ClientCommunicator communicator;

    public UserInputHandlerFacade(ClientCommunicator communicator) {
        this.communicator = communicator;
    }

    public void handleInput(String input) {
        if (input.equals("/VIEWMEMBERS")) {
            communicator.sendMessage("VIEWMEMBERS");
        } else if (input.startsWith("/WHISPER")) {
            String[] words = input.split(" ");
            String to = words[1];
            String[] messageArray = Arrays.copyOfRange(words, 2, words.length);
            String message = String.join(" ", messageArray);
            String toSend = "WHISPER" + to + ":" + message;
            communicator.sendMessage(toSend);
            System.out.println("Whispered to %s: %s".formatted(to, message));
        } else if (input.startsWith("/TIMEOUT")) {
            if (communicator.getCoordinator() != null) {
                communicator.getCoordinator().interrupt();
                communicator.timeOut();
            }
        } else {
            communicator.sendMessage("MESSAGE" + communicator.getSelf().getUID() + ":" + input);
        }
    }
}
