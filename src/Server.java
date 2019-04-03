import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class Server {

    List<Profil> profils = new ArrayList<>();
    List<Socket> servers = new ArrayList<>();
    String ip;
    int port;
    int num;
    boolean isMaster;

    public Server(String ip, int port, int num) {
        this.ip = ip;
        this.port = port;
        this.num = num;
        isMaster = false;
    }

    public void demarrer() throws IOException{

        int cmptClient = 0;

        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(ip, port));
        serverSocket.configureBlocking(false);

        this.connectAll();

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

    public void connect(String ip,int port){
        try {
            /* Connexion */
            System.out.println("Essai de connexion à  " + ip + " sur le port " + port + "\n");
            Socket echoSocket;
            try {
                echoSocket = new Socket(ip, port);

                PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));

                servers.add(echoSocket);

            } catch (UnknownHostException e) {
                System.err.println("Connexion: hôte inconnu : " + ip);
                e.printStackTrace();
                return;
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void connectAll(){

        System.out.println("saluit connectALL");

        String fileName = "pairs"+num+".cfg";
        String line = null;
        File file = new File(fileName);
        String ip;
        int port;
        String tmp;
        System.out.println(" before le try ");

        try {
            Scanner input=new Scanner(file);
            input.useDelimiter(" "); //delimitor is one space

            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            System.out.println("before while");

            while(input.hasNext()){
                if(!input.next().equals("master")) {
                    isMaster = true;
                    System.out.println("master");
                }

                if(input.next().equals("peer") && input.hasNext() && input.next().equals("=")) {
                    ip = input.next();
                    if(input.hasNext()) {
                        tmp = input.next();
                        port = Integer.parseInt(tmp.substring(0, tmp.length() - 1));
                    }
                    else break;
                    System.out.println(ip+" "+port);
                }
                else{
                    break;
                }

                this.connect(ip,port);


                }
            for(Socket s : servers){
                System.out.println("salut");
                System.out.println(s.getInetAddress());

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
        Server server = new Server(args[0],Integer.parseInt(args[1]),Integer.parseInt(args[2]));
        server.demarrer();
    }
}
