import java.net.ServerSocket;
import java.util.ArrayList;

public class Main {

    public static void main(String [] args) {
        int port = 110;
        int timeout = 600;
        ArrayList<Server> m_servers = new ArrayList<>();
        ArrayList<Thread> m_threads = new ArrayList<>();
        new User("Marco", "Polo");
        try {
            boolean serverRunning = true;

            try (ServerSocket socket = new ServerSocket(port)) {
                while (serverRunning) {
                    Server server = new Server(socket.accept(), timeout);
                    Thread thread = new Thread(server);
                    thread.start();
                    m_threads.add(thread);
                    m_servers.add(server);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }
}
