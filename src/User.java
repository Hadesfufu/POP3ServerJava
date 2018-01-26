import java.util.ArrayList;

public class User {

    static private ArrayList<User> m_users = new ArrayList<>();

    private String username;
    private String password;

    static public User getUser(String username){
        for(User user: m_users){
            if(user.username.equals(username))
                return user;
        }
        return null;
    }

    public User(String name, String pwd){
        this.username = name;
        this.password = pwd;
        m_users.add(this);
    }

}
