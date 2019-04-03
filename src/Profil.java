import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;

public class Profil {
    String pseudo;
    SocketChannel socket;
    boolean isConnected;
    ArrayBlockingQueue<Profil> file = new ArrayBlockingQueue<Profil>(100);
    String message;

    public Profil(SocketChannel socket) {
        this.socket = socket;
        this.isConnected = false;
    }
}
