import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientEnvoie{

    Socket s;
    Socket echoSocket; // la socket client
    String ip; // adresse IPv4 du serveur en notation pointée
    int port; // port TCP serveur
    BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    PrintWriter out;
    boolean fini = false;

    public ClientEnvoie(Socket s) {
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

                    String tampon;

                    if (fini == true) break; /* on sort de la boucle infinie */

                    /* recopier dans la socket ce qui est entré au clavier */
                    tampon = stdin.readLine();
                    if (tampon == null) {
                        fini = true;
                        System.err.println("Connexion terminée !!");
                        System.err.println("Hôte distant informé...");
                        echoSocket.shutdownOutput(); /* terminaison explicite de la socket
                                          dans le sens client -> serveur */
                        /* On ne sort pas de la boucle tout de suite ... */
                    } else {
                        /* envoi des données */
                        out.println(tampon);
                    }
                }

                /* On ferme tout */
                out.close();
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
