package by.prakapienka.clientserver;

import by.prakapienka.clientserver.client.TestTcpClient;
import org.junit.Test;

public class TcpServerClientTest {

    @Test
    public void testMultipleClientsOnServerIntegrationTest() throws InterruptedException {
        TcpServer server = new TcpServer(4444);
        server.start();
        TestTcpClient client1 = new TestTcpClient("127.0.0.1", 4444, "Client 1");
        client1.start();
        TestTcpClient client2 = new TestTcpClient("127.0.0.1", 4444, "Client 2");
        client2.start();
        TestTcpClient client3 = new TestTcpClient("127.0.0.1", 4444, "Client 3");
        client3.start();
        TestTcpClient client4 = new TestTcpClient("127.0.0.1", 4444, "Client 4");
        client4.start();
        TestTcpClient client5 = new TestTcpClient("127.0.0.1", 4444, "Client 5");
        client5.start();
        Thread.sleep(1000);
        client1.sendMessage("1");
        Thread.sleep(1000);
        client1.sendMessage("2");
        client2.sendMessage("2");
        Thread.sleep(200);
        client3.sendMessage("3");
        Thread.sleep(300);
        client4.sendMessage("1");
        Thread.sleep(200);
        client5.sendMessage("2");

        Thread.sleep(2000);

        client1.sendMessage("0");
        client2.sendMessage("0");
        client3.sendMessage("0");
        client4.sendMessage("0");
        client5.sendMessage("0");

    }
}
