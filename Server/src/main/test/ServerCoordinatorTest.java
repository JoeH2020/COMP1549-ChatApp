import org.apache.maven.settings.Server;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Scanner;

import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.when;

public class ServerCoordinatorTest {

    @BeforeEach
    public void beforeEach() {
        // reset the singleton
        ServerSingleton.reset();
    }

    @Test
    public void coordinatorTimedOut() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        // first mock the serverThread
        ServerThread thread = Mockito.mock(ServerThread.class);
        when(thread.getSelf()).thenReturn(new Member("Coordinator", null, null));

        // we need to mock the singleton too
        ServerSingleton singleton = Mockito.mock(ServerSingleton.class);

        // now we need to make sure the actual singleton class returns this instance when asked
        // there is no way other than reflections to do this (without PowerMockito)
        Class singletonClass = ServerSingleton.class;
        Field instanceField = singletonClass.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, singleton);

        // now let it run for 16 seconds
        ServerCoordinator serverCoordinator = new ServerCoordinator(thread);
        serverCoordinator.start();
        Thread.sleep(16000);

        // now let's check that everything that should have happened has happened
        Mockito.verify(thread).sendMessage("CHECKALIVE");
        Mockito.verify(singleton).broadcast("TIMEOUTCoordinator");
        Mockito.verify(singleton).selectNewCoordinator();
    }

    @Test
    public void coordinatorStillAlive() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        // first mock the serverThread
        ServerThread thread = Mockito.mock(ServerThread.class);
        when(thread.getSelf()).thenReturn(new Member("Coordinator", null, null));

        // we need to mock the singleton too
        ServerSingleton singleton = Mockito.mock(ServerSingleton.class);

        // now we need to make sure the actual singleton class returns this instance when asked
        // there is no way other than reflections to do this (without PowerMockito)
        Class singletonClass = ServerSingleton.class;
        Field instanceField = singletonClass.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, singleton);

        // now let it run for 8 seconds and send a positive response
        ServerCoordinator serverCoordinator = new ServerCoordinator(thread);
        serverCoordinator.start();
        Thread.sleep(8000);
        serverCoordinator.positiveResponse();
        Thread.sleep(2000);

        // now let's check that everything that should have happened has happened
        Mockito.verify(thread).sendMessage("CHECKALIVE");
        Mockito.verify(singleton, Mockito.times(0)).broadcast("TIMEOUTCoordinator");
        Mockito.verify(singleton, Mockito.times(0)).selectNewCoordinator();
    }
}
