import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


// Classe représentant un mot de passe
class Password {
    private String password;

    public Password(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}

// Interface commune pour le craquage de mot de passe
interface Cracker {
    boolean crack(String password);
}

// Implémentation de la méthode de force brute
class BruteForceCracker implements Cracker {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private static final int MAX_PASSWORD_LENGTH = 5;

    @Override
    public boolean crack(String password) {
        StringBuilder attempt = new StringBuilder();
        return crackRecursive(attempt, password, 0);
    }

    private boolean crackRecursive(StringBuilder attempt, String password, int depth) {
        if (depth > MAX_PASSWORD_LENGTH) {
            return false;  // Le mot de passe est trop long
        }

        if (attempt.toString().equals(password)) {
            System.out.println("Mot de passe craqué : " + attempt.toString());
            return true;
        }

        for (char c : ALPHABET.toCharArray()) {
            attempt.append(c);
            if (crackRecursive(attempt, password, depth + 1)) {
                return true;
            }
            attempt.setLength(attempt.length() - 1);
        }

        return false;
    }
}

// Implémentation de la méthode par dictionnaire
class DictionaryCracker implements Cracker {
    private List<String> dictionary;

    public DictionaryCracker(String dictionaryFilePath) {
        dictionary = loadDictionary(dictionaryFilePath);
    }

    private List<String> loadDictionary(String filePath) {
        List<String> dictionary = new ArrayList<>();
        try {
            File file = new File(filePath);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String word = scanner.nextLine();
                dictionary.add(word);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return dictionary;
    }

    @Override
    public boolean crack(String password) {
        for (String word : dictionary) {
            if (word.equals(password)) {
                System.out.println("Mot de passe craqué : " + word);
                return true;
            }
        }
        return false;
    }
}

// Implémentation du craquage en ligne
class OnlineCracker implements Cracker {
    private String url;
    private String login;

    public OnlineCracker(String url, String login) {
        this.url = url;
        this.login = login;
    }

    @Override
    public boolean crack(String password) {
        try {
            URI uri = new URI(this.url);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            String jsonInputString = "{\"login\": \"" + login + "\", \"password\": \"" + password + "\"}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                // En supposant que la réponse contient un message de succès si le mot de passe est correct
                if (response.toString().contains("success")) {
                    System.out.println("Mot de passe craqué : " + password);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }
}



// Fabrique simple pour le LocalCracker
class LocalCrackerFactory {
    public LocalCracker createLocalCracker() {
        Cracker cracker = new BruteForceCracker();
        return new LocalCracker(cracker);
    }
}

// Classe représentant le craquage local
class LocalCracker {
    private Cracker cracker;

    public LocalCracker(Cracker cracker) {
        this.cracker = cracker;
    }

    public void crackPassword(String password) {
        cracker.crack(password);
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choisissez le type de craquage :");
        System.out.println("1. Craquage local (force brute)");
        System.out.println("2. Craquage local (dictionnaire)");
        
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) {
            LocalCrackerFactory factory = new LocalCrackerFactory();
            LocalCracker localCracker = factory.createLocalCracker();

            System.out.println("Entrez le mot de passe :");
            String password = scanner.nextLine();

            localCracker.crackPassword(password);
        } else if (choice == 2) {
            System.out.println("Entrez le chemin du fichier de dictionnaire :");
            String dictionaryFilePath = scanner.nextLine();

            DictionaryCracker dictionaryCracker = new DictionaryCracker(dictionaryFilePath);

            System.out.println("Entrez le mot de passe :");
            String password = scanner.nextLine();

            dictionaryCracker.crack(password);
        } 
        
        else {
            System.out.println("Choix invalide.");
        }

        scanner.close();
    }
}
