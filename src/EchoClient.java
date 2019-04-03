/*  echo / client simple
    Master Informatique 2012 -- Université Aix-Marseille
    Emmanuel Godard - Bilel Derbel
*/

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class EchoClient {

  public static void main(String[] args) throws IOException {

    Socket echoSocket; // la socket client
    String ip; // adresse IPv4 du serveur en notation pointée
    int port; // port TCP serveur
    BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    PrintWriter out;
    BufferedReader in;
    boolean fini = false;

    /* Traitement des arguments */
    if (args.length != 2) {
      /* erreur de syntaxe */
      System.out.println("Usage: java EchoClient @server @port");
      System.exit(1);
    }
    ip = args[0];
    port = Integer.parseInt(args[1]);

    if (port > 65535) {
      System.err.println("Port hors limite");
      System.exit(3);
    }

    /* Connexion */
    System.out.println("Essai de connexion à  " + ip + " sur le port " + port + "\n");
    try {
      echoSocket = new Socket(ip, port);
      //System.err.println("le n° de la socket est : " + echoSocket);
      System.out.println("USAGE : CONNECT <pseudo>");
      /* Initialisation d'agréables flux d'entrée/sortie */
      out = new PrintWriter(echoSocket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
    } catch (UnknownHostException e) {
      System.err.println("Connexion: hôte inconnu : " + ip);
      e.printStackTrace();
      return;
    }

    EchoClient client = new EchoClient();
    client.demarrer(echoSocket);

    return;
  }

  public void demarrer(Socket client) throws IOException {

    Thread t_envoie = new Thread(new HandlerEnvoie(client));
    t_envoie.start();
    Thread t_recoit = new Thread(new HandlerRecoit(client));
    t_recoit.start();
  }

  class HandlerRecoit implements Runnable{

    Socket s;
    String ip; // adresse IPv4 du serveur en notation pointée
    int port; // port TCP serveur
    BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    BufferedReader in;
    boolean fini = false;

    HandlerRecoit(Socket socket) throws IOException {
      this.s = socket;
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      port = socket.getPort();
    }

    @Override
    public void run() {
      try {
        while (true) {
          /* Jusqu'à fermeture de la socket (ou de stdin)     */
          /* recopier à l'écran ce qui est lu dans la socket  */
          /* recopier dans la socket ce qui est lu dans stdin */

          String lu;

          /* réception des données */
          lu = in.readLine();
          if (lu == null) {
            System.err.println("Connexion terminée par l'hôte distant");
            break; /* on sort de la boucle infinie */
          }
          System.out.println(lu);

          if (fini == true) break; /* on sort de la boucle infinie */
        }

        /* On ferme tout */
        in.close();
        stdin.close();
        s.close();

        System.err.println("Fin de la session.");
      } catch (IOException e) {
        System.err.println("Erreur E/S socket");
        //e.printStackTrace();
        System.exit(8);
      }

    }
  }

  class HandlerEnvoie implements Runnable{

    Socket s;
    String ip; // adresse IPv4 du serveur en notation pointée
    int port; // port TCP serveur
    BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    PrintWriter out;
    boolean fini = false;

    HandlerEnvoie(Socket socket) throws IOException {
      this.s = socket;
      out = new PrintWriter(socket.getOutputStream(), true);
      port = socket.getPort();
    }

    @Override
    public void run() {
      try {
        while (true) {
          /*/* Jusqu'à fermeture de la socket (ou de stdin)     */
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
            s.shutdownOutput(); /* terminaison explicite de la socket
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
        s.close();

        System.err.println("Fin de la session.");
      } catch (IOException e) {
        System.err.println("Erreur E/S socket");
        //e.printStackTrace();
        System.exit(8);
      }
    }
  }
}
