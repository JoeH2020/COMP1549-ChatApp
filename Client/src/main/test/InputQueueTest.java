import junit.framework.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.plugins.tiff.BaselineTIFFTagSet;
import javax.print.DocFlavor;
import java.io.ByteArrayInputStream;

public class InputQueueTest {

    ByteArrayInputStream inputStream;

    @Test
    public void emptyInputStream() throws InterruptedException {
        // make an empty input stream
        inputStream = new ByteArrayInputStream(new byte[0]);

        // start the input queue
        InputQueue queue = new InputQueue(inputStream);
        queue.start();

        // give it some time to start
        Thread.sleep(500);

        // make sure that it states there are no items
        Assert.assertFalse(queue.hasItems());
    }

    @Test
    public void singleLine() throws InterruptedException {
        // make a one line input stream
        inputStream = new ByteArrayInputStream("HelloWorld".getBytes());

        // start the input queue
        InputQueue queue = new InputQueue(inputStream);
        queue.start();

        // give it some time to start
        Thread.sleep(500);

        // make sure that it states there's an item
        Assert.assertTrue(queue.hasItems());

        // make sure it returns the correct item
        Assert.assertEquals("HelloWorld", queue.nextItem());

        // make sure it states there are no more items
        Assert.assertFalse(queue.hasItems());
    }

    @Test
    public void multipleLines() throws InterruptedException {
        // make a two line input stream
        inputStream = new ByteArrayInputStream("HelloWorld\nGoodbyeWorld".getBytes());

        // start the input queue
        InputQueue queue = new InputQueue(inputStream);
        queue.start();

        // give it some time to start
        Thread.sleep(500);

        // make sure that it states there's an item
        Assert.assertTrue(queue.hasItems());

        // make sure it returns the correct item
        Assert.assertEquals("HelloWorld", queue.nextItem());

        // give it some time to read the next item
        Thread.sleep(250);

        // make sure it states there are more items
        Assert.assertTrue(queue.hasItems());

        // make sure it returns the correct item again
        Assert.assertEquals("GoodbyeWorld", queue.nextItem());

        // make sure it states there are no more items
        Assert.assertFalse(queue.hasItems());
    }
}
