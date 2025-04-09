import server.SimpleGameServer;
import utils.Constants;

public class Main {
    public static void main(String[] args) {
        try {
            SimpleGameServer server = new SimpleGameServer(Constants.SERVER_PORT);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
