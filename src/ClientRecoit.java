import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientRecoit{

    Socket s;
    Socket echoSocket; // la socket client
    String ip; // adresse IPv4 du serveur en notation pointée
    int port; // port TCP serveur
    BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    BufferedReader in;
    boolean fini = false;

    public ClientRecoit(Socket s) {
        this.s = s;
    }

    class Handler implements Runnable{


        @Override
        public void run() {
            try {
                while (true) {
                    /* Jusqu'à fermeture de la socket (ou de stdin)     */
                    /* recopier à l'écran ce qui est lu dans la socket  */
                    /* recopier dans la socket ce qui est lu dans stdin */

                    String lu;
                    String tampon;

                    /* réception des données */
                    lu = in.readLine();
                    if (lu == null) {
                        System.err.println("Connexion terminée par l'hôte distant");
                        break; /* on sort de la boucle infinie */
                    }
                    System.out.println("reçu: " + lu);

                    if (fini == true) break; /* on sort de la boucle infinie */
                }

                /* On ferme tout */
                in.close();
                stdin.close();
                echoSocket.close();

                System.err.println("Fin de la session.");
            } catch (IOException e) {
                System.err.println("Erreur E/S socket");
                e.printStackTrace();
                System.exit(8);
            }

        }
    }


}
