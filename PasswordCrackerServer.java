import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class PasswordCrackerServer {
    public static void main(String[] args) throws Exception {
        InetAddress localhost = InetAddress.getByName("localhost");
        HttpServer server = HttpServer.create(new InetSocketAddress(localhost, 0), 0);
        int port = server.getAddress().getPort(); // Récupérer le port attribué par le système
        server.createContext("/crack", new PasswordCrackHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started on localhost:" + port);
    }

    static class PasswordCrackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Lire les données du formulaire depuis la requête
                Scanner scanner = new Scanner(exchange.getRequestBody()).useDelimiter("\\A");
                String requestBody = scanner.hasNext() ? scanner.next() : "";

                // Extraire les données du formulaire
                String[] formData = requestBody.split("&");
                String login = null;
                String password = null;
                for (String pair : formData) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2) {
                        if ("login".equals(keyValue[0])) {
                            login = keyValue[1];
                        } else if ("password".equals(keyValue[0])) {
                            password = keyValue[1];
                        }
                    }
                }

                // Traiter les données récupérées 
                System.out.println("Login: " + login);
                System.out.println("Password: " + password);

                //  OK
                String response = "Données reçues avec succès !";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                //  supporte que les requêtes POST
                exchange.sendResponseHeaders(405, -1); 
            }
        }
    }
}
