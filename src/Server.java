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
            User user = null;
            out.println("+OK POP3 server ready");

			/* Thread-blocking while loop waits for commands from the client */
            while ((input = in.readLine()) != null) {
				/* Handle the client command */
				System.out.println(input);
                if(state == State.Closed){

                }else if (state == State.Authorisation){
                    System.out.println("user :" + input);
                    if (input.startsWith("USER")) {
                        String username = input.split(" ")[1];
                        user = User.getUser(username);
                        if(user == null){
                            output = "-ERR username not recognized";
                        }
                        else if(user.getLock() == true){
                            output = "-ERR user is already in use";
                        }
                        else{
                            output = "+OK Waiting for password";
                            state = State.PwdWaiting;
                        }
                    }
                }else if (state == State.PwdWaiting){
                    if (input.startsWith("PASS")) {
                        if(user == null){
                            System.err.println("User is null, impossible");
                        }
                        else if(user.getLock() == true){
                            output = "-ERR user is already in use";
                        }
                        String pwd = input.split(" ")[1];
                        if(user.getPassword().equals(pwd)){
                            output = "+OK Password is correct, logged in";
                            state = State.Transaction;
                            user.setLock(true);
                        }
                        else{
                            output = "-ERR wrong password";
                        }
                    }
                }
                else{
				    if (input.startsWith("QUIT")) {
                        break;
                    }
                    else if (input.startsWith("APOP")) {

                    }
                    else if (input.startsWith("STAT")) {
                        Integer sum = 0, nb = 0;
				        for(Mail mail : user.getMails()){
                            sum += mail.getSize();
                            nb++;
                        }
                        output = " +OK " + nb + " " + sum;
                    }
                    else if (input.startsWith("RETR")) {
                        Integer id = Integer.parseInt(input.split(" ")[1]);
                        for(Mail mail : user.getMails()){
                            if(mail.getMessageId() == id){
                                output = "+OK " + mail.getMessageId() + " " + mail.getSize() + "\n";
                                output += "From : " + mail.getFromName() + " <" + mail.getFromAdress() + ">\n";
                                output += "To : " + mail.getUser().getUsername() + " <" + mail.getUser().getAddress() + ">\n";
                                output += "Subject : " + mail.getObject() + "\n";
                                output += "Date : " + mail.getDate().toString() + "\n";
                                output += "Id : " + mail.getMessageId() + "\n";
                                output += mail.getContent();
                                break;
                            }
                        }
                    }
                    else if (input.startsWith("LIST")) {
                        output = "+OK " + user.getMails().size() + " messages:";
				        for(Mail mail : user.getMails()){
                            output+= "\n" + mail.getMessageId() + " " + mail.getSize();
                        }
                        output+="\n.";
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
