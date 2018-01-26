import javax.print.attribute.standard.MediaSizeName;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Server implements Runnable {

    private String nomDeDomaine;
    private Socket socket;
    private InputStreamReader streamReader;
    private PrintWriter out;
    private BufferedReader in;

    private State state;

    enum State{
        Closed,
        Authorisation,
        PwdWaiting,
        Transaction
    }

    public Server(String nomDeDomaine){
        this.nomDeDomaine = nomDeDomaine;
    }

    public Server(Socket socket, int timeout) throws SocketException {
        this.socket = socket;
        this.socket.setSoTimeout(timeout * 1000);
        state = State.Authorisation;
		/* Server connection message */
        System.out.println("[" + socket.getInetAddress() + "] " + "Just connected" );
    }

    @Override
    public void run() {

        try {
            streamReader = new InputStreamReader(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(streamReader);
            String input;
            String output = "-ERR unknown error";

            out.println("+OK POP3 server ready");

			/* Thread-blocking while loop waits for commands from the client */
            while ((input = in.readLine()) != null) {
				/* Handle the client command */
				System.out.println(input);
                if(state == State.Closed){

                }else if (state == State.Authorisation){
                    if (input.startsWith("USER")) {
                        String username = input.split(" ")[1];
                        System.out.println(username);
                        User user = User.getUser(username);
                        if(user == null){
                            output = "-ERR username not recognized";
                        }
                        else{
                            output = "+OK Waiting for password";
                        }
                    }
                }else if (state == State.PwdWaiting){

                }else{
				    if (input.startsWith("QUIT")) {
                        break;
                    }
                }
				out.println(output);
            }
        } catch (SocketTimeoutException e) {
            System.out.println("[" + socket.getInetAddress() + "] " + "Socket Timeout");
        } catch (IOException e) {
            System.err.println("Stream Error");
        } finally {
            try {
                streamReader.close();
                in.close();
                out.close();
                socket.close();
            } catch (Exception e){
                e.printStackTrace();
            }
            System.out.println("[" + socket.getInetAddress() + "] " + "User Disconnected");
        }
    }
}
