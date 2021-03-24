import junit.framework.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.when;

public class ServerThreadTest {

    @Mock
    static ServerThread serverCommunicator;

    @Mock
    static ServerSocket serverSocket;
    static Socket socket;
    static ServerCoordinator serverCoordinator;
    static Member[] members;

    static ByteArrayOutputStream serverOutputStream = new ByteArrayOutputStream();

    @BeforeAll
    public static void beforeAll() throws IOException {

        serverCommunicator = Mockito.mock(ServerThread.class);

        //Creating an array with two 'fake' members.
        members = new Member[3];
        members[0] = new Member("John", "192.168.0.3", "4300");
        members[1] = new Member("Mike", "192.168.0.2", "4300");
        members[2] = new Member("Peter", "192.168.0.1", "4300");

        //Creating mock methods to return correct values which in this case
        // are the coordinator's ip and port.
        when(serverCommunicator.getTargetIP()).thenReturn("192.168.0.3");
        when(serverCommunicator.getTargetPort()).thenReturn("4300");
        when(serverCommunicator.getMembers()).thenReturn(members);

        ServerSocket serverSocket = Mockito.mock(ServerSocket.class);
//        Socket socket = Mockito.mock(Socket.class);

        serverCoordinator = new ServerCoordinator(serverCommunicator) {
            @Override
            protected Socket getClientSocket(ServerSocket listener) {return socket;}

            };

        //in = new Scanner(socket.getInputStream());
        //out = new PrintWriter(socket.getOutputStream(), true);
//        when(socket.getOutputStream()).thenReturn(serverOutputStream);

    }



    @BeforeEach
    public void beforeEach() {
        // empty the serverOutputStream
        serverOutputStream.reset();

        // reset the serverSingleton
        ServerSingleton.reset();
    }

    /*
    CONNECTION TESTS
     */

    @Test
    public void firstMember() throws IOException, InterruptedException {
        // this test makes sure that the first member is appropriately
        // connected and set as coordinator

        // first mock the socket so that it returns our OutputStream when requested
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Socket socket = Mockito.mock(Socket.class);
        when(socket.getOutputStream()).thenReturn(outputStream);

        // now create an appropriate InputStream for the class to work properly
        // the class requests the name
        String input = "Paul\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        when(socket.getInputStream()).thenReturn(inputStream);

        // also mock getInetAddress and getPort for everything to work
        when(socket.getInetAddress()).thenReturn(InetAddress.getLocalHost());
        when(socket.getPort()).thenReturn(7738);

        // now run the class
        ServerThread thread = new ServerThread(socket);
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(thread);

        // wait a bit for everything to work
        Thread.sleep(1000);

        String expectedOutput = "SUBMITNAME\nNAMEACCEPTED\nSETCOORDINATOR\nJOINPaul\nOnline Members List: [Paul]\n";
        Assert.assertEquals(expectedOutput, outputStream.toString());

    }

    @Test
    public void secondMember() throws IOException, InterruptedException {
        // this method tests all subsequent connections, which should not be set as coordinators

        // first mock the socket so that it returns our OutputStream when requested
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Socket socket = Mockito.mock(Socket.class);
        when(socket.getOutputStream()).thenReturn(outputStream);

        // now create an appropriate InputStream for the class to work properly
        // the class requests the name
        String input = "Second\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        when(socket.getInputStream()).thenReturn(inputStream);

        // also mock getInetAddress and getPort for everything to work
        when(socket.getInetAddress()).thenReturn(InetAddress.getLocalHost());
        when(socket.getPort()).thenReturn(7738);

        // finally set up the server singleton to return existing members
        ServerSingleton singleton = ServerSingleton.getInstance();
        singleton.addMember(new Member("Paul", null, null), null);

        // now run the class
        ServerThread thread = new ServerThread(socket);
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(thread);

        // wait a bit for everything to work
        Thread.sleep(1000);

        String expectedOutput = "SUBMITNAME\nNAMEACCEPTED\nJOINSecond\nOnline Members List: [Paul, Second]\n";
        Assert.assertEquals(expectedOutput, outputStream.toString());

    }

    @Test
    public void secondMemberWithExistingName() throws IOException, InterruptedException {
        // this method tests if members that try to login with existing names get rejected

        // first mock the socket so that it returns our OutputStream when requested
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Socket socket = Mockito.mock(Socket.class);
        when(socket.getOutputStream()).thenReturn(outputStream);

        // now create an appropriate InputStream for the class to work properly
        // the class requests the name
        String input = "Paul\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        when(socket.getInputStream()).thenReturn(inputStream);

        // also mock getInetAddress and getPort for everything to work
        when(socket.getInetAddress()).thenReturn(InetAddress.getLocalHost());
        when(socket.getPort()).thenReturn(7738);

        // finally set up the server singleton to return existing members
        ServerSingleton singleton = ServerSingleton.getInstance();
        singleton.addMember(new Member("Paul", null, null), null);

        // now run the class
        ServerThread thread = new ServerThread(socket);
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(thread);

        // wait a bit for everything to work
        Thread.sleep(1000);

        String expectedOutput = "SUBMITNAME\nNAMEACCEPTED\nSETCOORDINATOR\nJOINPaul\nOnline Members List: [Paul]\n";
        Assert.assertEquals(expectedOutput, outputStream.toString());
    }

    /*
    INPUT TESTS
     */
    @Test
    public void message() throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        // this test makes sure that messages are propagated

        // we need to mock the singleton for this
        ServerSingleton singleton = Mockito.mock(ServerSingleton.class);
        when(singleton.getMembers()).thenReturn(new HashSet<Member>());
        when(singleton.getTime()).thenReturn("1500");

        // now we need to make sure the actual singleton class returns this instance when asked
        // there is no way other than reflections to do this (without PowerMockito)
        Class singletonClass = ServerSingleton.class;
        Field instanceField = singletonClass.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, singleton);


        // mock the socket so that it returns our OutputStream when requested
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Socket socket = Mockito.mock(Socket.class);
        when(socket.getOutputStream()).thenReturn(outputStream);

        // now create an appropriate InputStream for the class to work properly
        // the class requests the name
        String input = "Paul\nMESSAGEHello World\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        when(socket.getInputStream()).thenReturn(inputStream);

        // also mock getInetAddress and getPort for everything to work
        when(socket.getInetAddress()).thenReturn(InetAddress.getLocalHost());
        when(socket.getPort()).thenReturn(7738);

        // now run the class
        ServerThread thread = new ServerThread(socket);
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(thread);

        // wait a bit for everything to work
        Thread.sleep(1000);

        // make sure the broadcast method has been called with the hello world message
        Mockito.verify(singleton, Mockito.times(1)).broadcast("JOINPaul");
        Mockito.verify(singleton, Mockito.times(1)).broadcast("MESSAGE1500Hello World");
    }

    @Test
    public void viewMembers() throws NoSuchFieldException, IllegalAccessException, IOException, InterruptedException {
        // this test makes sure a correct message is sent out when requesting online members

        // let's make a list of members that we want to be returned
        HashSet<Member> members = new HashSet<>();
        members.add(new Member("John", null, null));
        members.add(new Member("George", null, null));

        // we need to mock the singleton for this
        ServerSingleton singleton = Mockito.mock(ServerSingleton.class);
        when(singleton.getMembers()).thenReturn(members);
        when(singleton.getTime()).thenReturn("1500");

        // now we need to make sure the actual singleton class returns this instance when asked
        // there is no way other than reflections to do this (without PowerMockito)
        Class singletonClass = ServerSingleton.class;
        Field instanceField = singletonClass.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, singleton);

        // mock the socket so that it returns our OutputStream when requested
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Socket socket = Mockito.mock(Socket.class);
        when(socket.getOutputStream()).thenReturn(outputStream);

        // now create an appropriate InputStream for the class to work properly
        // the class requests the name
        String input = "Paul\nVIEWMEMBERS\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        when(socket.getInputStream()).thenReturn(inputStream);

        // also mock getInetAddress and getPort for everything to work
        when(socket.getInetAddress()).thenReturn(InetAddress.getLocalHost());
        when(socket.getPort()).thenReturn(7738);

        // now run the class
        ServerThread thread = new ServerThread(socket);
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(thread);

        // wait a bit for everything to work
        Thread.sleep(1000);

        // make sure the outputStream has been written what we expect
        String expectedString = "SUBMITNAME\nNAMEACCEPTED\nMEMBERSPaul,George,John\n";
        String outputString = outputStream.toString("UTF-8");
        Assert.assertEquals(expectedString, outputString);

    }

    @Test
    public void whisper() throws NoSuchFieldException, IllegalAccessException, IOException, InterruptedException {
        // this test makes sure that the whisper function works correctly

        // we need to mock the singleton for this
        ServerSingleton singleton = Mockito.mock(ServerSingleton.class);
        when(singleton.getMembers()).thenReturn(new HashSet<Member>());
        when(singleton.getTime()).thenReturn("1500");

        // now we need to make sure the actual singleton class returns this instance when asked
        // there is no way other than reflections to do this (without PowerMockito)
        Class singletonClass = ServerSingleton.class;
        Field instanceField = singletonClass.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, singleton);

        // mock the socket so that it returns our OutputStream when requested
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Socket socket = Mockito.mock(Socket.class);
        when(socket.getOutputStream()).thenReturn(outputStream);

        // now create an appropriate InputStream for the class to work properly
        // the class requests the name
        String input = "Paul\nWHISPERJack:Hey Jack! :)\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        when(socket.getInputStream()).thenReturn(inputStream);

        // also mock getInetAddress and getPort for everything to work
        when(socket.getInetAddress()).thenReturn(InetAddress.getLocalHost());
        when(socket.getPort()).thenReturn(7738);

        // now run the class
        ServerThread thread = new ServerThread(socket);
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(thread);

        // wait a bit for everything to work
        Thread.sleep(1000);

        // make sure the serverSingleton.whisper method has been called with the right parameters
        Mockito.verify(singleton).whisper("Jack", "Hey Jack! :)", "Paul");
    }

    /*
    TODO
    test all keywords:
    CHECKALIVE
    ALIVE
    TIMEOUT
    DISCONNECTED
     */
}
