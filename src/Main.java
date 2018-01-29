import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Date;

public class Main {

    public static void main(String [] args) {
        int port = 110;
        int timeout = 600;
        ArrayList<Server> m_servers = new ArrayList<>();
        ArrayList<Thread> m_threads = new ArrayList<>();
        new User("Marco", "Polo");
        new User("jean", "jean");
        initMails();
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

    public static void initMails(){
        Mail mail = new Mail(User.getUser("jean"));
        mail.setContent("This is a message to say hello. \n\n hello then.");
        mail.setDate(new Date());
        mail.setFromAdress("test@domain.fr");
        mail.setFromName("test");
        mail.setMessageId(1);
        mail.setObject("The test message");
    }

}
