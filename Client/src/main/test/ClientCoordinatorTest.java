import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.when;

public class ClientCoordinatorTest {

    @Mock
    static ClientCommunicator clientCommunicator;

    @Mock
    static ServerSocket serverSocket;
    static Socket socket;

    static ClientCoordinator clientCoordinator;

    static final String PING_CONFIRM = "ALIVE";
    static final String PING_MESSAGE = "CHECKALIVE";
    static final String TIMEOUT_MESSAGE = "TIMEOUT";

    static Member[] members;

    static ByteArrayOutputStream serverOutputStream = new ByteArrayOutputStream();

    @BeforeAll
    public static void beforeAll() throws IOException {

    }

    @BeforeEach
    public void beforeEach() {
        // empty the serverOutputStream
        serverOutputStream.reset();
    }

    @Test
    public void coordinatorTimedOut() {
    }

    @Test
    public void coordinatorStillAlive() {
    }

}
