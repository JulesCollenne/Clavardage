import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class SalonServer {

    List<Profil> profils = new ArrayList<>();

    public void demarrer() throws IOException{

        int cmptClient = 0;

        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", 12345));
        serverSocket.configureBlocking(false);

        System.out.println("Server initialisé");

        Selector selector = Selector.open();

        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();

            while (iter.hasNext()) {

                SelectionKey key = iter.next();

                if (key.isAcceptable()) {
                    SocketChannel client = serverSocket.accept();
                    client.configureBlocking(false);
                    cmptClient++;

                    Profil c = new Profil(client);
                    client.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE, c);
                }

                if (key.isValid() && key.isReadable()) {
                    ByteBuffer buffer = ByteBuffer.allocate(256);
                    boolean firstConnect = false;
                    Profil c = (Profil) key.attachment();
                    c.socket = (SocketChannel) key.channel();
                    c.socket.read(buffer);
                    String message = new String(buffer.array());

                    if(buffer.position()==0){
                        c.socket.close();
                        c = null;
                    }

                    buffer.clear();

                    if(c != null) {
                        if (!c.isConnected) {
                            if (message.substring(0, 7).equals("CONNECT") && message.length() > 8) {

                                String pseudo = message.trim();
                                pseudo = pseudo.substring(8);

                                c.pseudo = pseudo;

                                c.isConnected = true;
                                ByteBuffer connexion = ByteBuffer.wrap("Connexion ok\n".getBytes());
                                c.socket.write(connexion);
                                connexion.clear();
                                firstConnect = true;
                                profils.add(c);
                                //buffer.clear();
                            } else {
                                ByteBuffer connexion = ByteBuffer.wrap("ERROR CONNECT aborting clavardamu protocol.\n".getBytes());
                                c.socket.write(connexion);
                                firstConnect = true;
                                connexion.clear();
                                c.socket.close();
                            }
                        }

                        if (!firstConnect) {
                            if (message.length() > 4 && message.substring(0,3).equals("MSG")) {
                                message = message.substring(4);
                                System.out.println(c.pseudo + " > " + message.trim());
                                c.message = message.trim();
                                for (Profil p : profils) {
                                    p.file.add(c);
                                }
                            }
                        }
                    }
                }

                if(key.isValid() && key.isWritable()){
                    //ByteBuffer connexion = ByteBuffer.allocate(256);
                    Profil c = (Profil) key.attachment();
                    if(c != null) {
                        for(Profil p : c.file){
                            ByteBuffer connexion = ByteBuffer.wrap((p.pseudo+" > "+p.message+"\n").getBytes());
                            c.socket.write(connexion);
                            c.file.remove(p);
                        }
                    }
                }
                iter.remove();
            }
        }
    }

    public void connect(){
        String ip = "localhost";
        int port = 12345;

        String file = "pairs.cfg";
        String line = null;
        boolean isMaster = false;


        Scanner input=new Scanner(file);
        input.useDelimiter(" "); //delimitor is one space

        while(input.hasNext()){
            System.out.println(input.next());
        }

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while(input.hasNext()){
                if(input.next().equals("peer") && input.hasNext() && input.next().equals("=") && input.hasNext()) {
                    ip = input.next();
                    if(input.hasNext())
                        port = Integer.parseInt(input.next());
                }
                else{
                    break;
                }

                /* Connexion */
                System.out.println("Essai de connexion à  " + ip + " sur le port " + port + "\n");
                Socket echoSocket;
                try {
                    echoSocket = new Socket(ip, port);
                    //System.err.println("le n° de la socket est : " + echoSocket);
                    System.out.println("USAGE : CONNECT <pseudo>");
                    /* Initialisation d'agréables flux d'entrée/sortie */
                    PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                } catch (UnknownHostException e) {
                    System.err.println("Connexion: hôte inconnu : " + ip);
                    e.printStackTrace();
                    return;
                }

                EchoClient client = new EchoClient();
                client.demarrer(echoSocket);

                System.out.println(line);
            }
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + file + "'");
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + file + "'");
            // ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, CancelledKeyException {
        SalonServer server = new SalonServer();
        server.demarrer();
    }
}
