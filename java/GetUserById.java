import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SDK demo method: getUserById (Java implementation).
 *
 * Reads user records from the shared flat file (data/users.json) and
 * returns the username for a given user ID.
 *
 * Deliberately dependency-free: parses the known, simple JSON structure
 * with a regex so it compiles and runs with a plain JDK (11+).
 *
 * Usage:
 *   javac GetUserById.java
 *   java GetUserById <id>     # look up a specific ID
 *   java GetUserById          # runs a built-in demo lookup (ID 101)
 */
public class GetUserById {

    private static final int DEMO_ID = 101;

    /** Candidate locations for the shared flat file, relative to the working directory. */
    private static final String[] DATA_FILE_CANDIDATES = {
        "../data/users.json",   // run from the java/ folder
        "data/users.json"       // run from the repo root
    };

    /** Matches one user object; id/username are extracted separately so key order does not matter. */
    private static final Pattern OBJECT_PATTERN = Pattern.compile("\\{[^{}]*\\}");
    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("\"username\"\\s*:\\s*\"([^\"]*)\"");

    /**
     * Returns the username for the given user ID, or null if not found.
     */
    public static String getUserById(int userId) throws IOException {
        String json = readDataFile();
        Matcher object = OBJECT_PATTERN.matcher(json);
        while (object.find()) {
            String user = object.group();
            Matcher id = ID_PATTERN.matcher(user);
            if (id.find() && Integer.parseInt(id.group(1)) == userId) {
                Matcher username = USERNAME_PATTERN.matcher(user);
                return username.find() ? username.group(1) : null;
            }
        }
        return null;
    }

    private static String readDataFile() throws IOException {
        for (String candidate : DATA_FILE_CANDIDATES) {
            Path p = Path.of(candidate);
            if (Files.exists(p)) {
                return Files.readString(p);
            }
        }
        throw new IOException("Could not find data/users.json - run from the repo root or the java/ folder");
    }

    public static void main(String[] args) {
        int userId;
        if (args.length > 0) {
            try {
                userId = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("ERROR: '" + args[0] + "' is not a valid integer ID");
                System.exit(2);
                return;
            }
        } else {
            System.out.println("No ID supplied - running demo lookup for ID " + DEMO_ID);
            userId = DEMO_ID;
        }

        try {
            String username = getUserById(userId);
            if (username == null) {
                System.out.println("User with ID " + userId + " not found");
                System.exit(1);
            } else {
                System.out.println("getUserById(" + userId + ") -> " + username);
            }
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(2);
        }
    }
}
