/* echo / serveur basique
   Master Informatique 2012 -- Université Aix-Marseille
   Bilel Derbel, Emmanuel Godard
*/

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.io.*;

public class EchoServer{

    List<String> pseudos = new ArrayList<>();

    /* Démarrage et délégation des connexions entrantes */
    public void demarrer(int port) {
        ServerSocket ssocket; // socket d'écoute utilisée par le serveur

        System.out.println("Lancement du serveur sur le port " + port);
        try {
            ssocket = new ServerSocket(port);
            ssocket.setReuseAddress(true); /* rend le port réutilisable rapidement */
            while (true) {
                Thread t = new Thread(new Handler(ssocket.accept()));
                t.start();
            }
        } catch (IOException ex) {
            System.out.println("Arrêt anormal du serveur.");
            return;
        }
    }

    public static void main(String[] args) {
        EchoServer serveur = new EchoServer();
        serveur.demarrer(12345);
        return;
    }

    /*
       echo des messages reçus (le tout via la socket).
       NB classe Runnable : le code exécuté est défini dans la
       méthode run().
    */
    public class Handler implements Runnable {

        Socket socket;
        PrintWriter out;
        BufferedReader in;
        InetAddress hote;
        int port;

        Handler(Socket socket) throws IOException {
            this.socket = socket;
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            hote = socket.getInetAddress();
            port = socket.getPort();
        }

        public void run() {
            String tampon;
            long compteur = 0;
            String currentPseudo;
            boolean connected = false;
            try {
                /* envoi du message d'accueil */
                out.println("Bonjour " + hote + "! (vous utilisez le port " + port + ")");
                out.println("Utilisez la commande CONNECT <votre_pseudo>");

                tampon = in.readLine();
                if (tampon.length() > 8) {
                    if (tampon.substring(0, 7).equals("CONNECT")) {
                        currentPseudo = tampon.substring(8);
                        if (verifyPseudo(currentPseudo)) {
                            connected = true;
                            pseudos.add(currentPseudo);
                            out.println("Votre pseudo est : " + currentPseudo);
                        }
                    }
                }
                if(!connected) {
                    out.println("ERROR CONNECT aborting clavardamu protocol.");
                    socket.close();
                    return;
                }
                do {
                    /* Faire echo et logguer */
                    tampon = in.readLine();
                    if (tampon != null) {
                        if (tampon.length() > 4 && tampon.substring(0,3).equals("MSG")){
                            compteur++;
                        /* log */

                        System.err.println("[" + hote + ":" + port + "]: " + compteur + ":" + tampon);
                        /* echo vers le client */
                        tampon = tampon.substring(3,tampon.length());
                        out.println("> " + tampon);
                    }
                    else {
                        out.println("ERROR clavardamu.");
                    }
                    } else {
                        break;
                    }
                } while (true);

                /* le correspondant a quitté */
                in.close();
                out.println("Au revoir...");
                out.close();
                socket.close();

                System.err.println("[" + hote + ":" + port + "]: Terminé...");
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }

        public boolean verifyPseudo(String pseudo){
            return !pseudos.contains(pseudo);
        }

    }
}
