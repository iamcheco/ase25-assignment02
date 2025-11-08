import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Fuzzer {
    static final Random rnd = new Random();

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java Fuzzer \"html_parser_win_x86_64.exe\"");
            return;
        }
        String target = args[0];
        String seed = Files.readString(Path.of("seed.html"));
        int iterations = 500; // change as needed

        for (int i = 0; i < iterations; i++) {
            String mutated = mutate(seed);
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", target);
            pb.redirectErrorStream(true); // combine stdout+stderr
            Process p = pb.start();

            // send mutated input to stdin of process
            try (OutputStream os = p.getOutputStream()) {
                os.write(mutated.getBytes());
                os.flush();
            } catch (IOException e) {
                System.err.println("Failed to write to process stdin: " + e.getMessage());
            }

            // capture output
            String output = readStream(p.getInputStream());

            int exitCode = p.waitFor();

            // print summary per iteration
            System.out.printf("Iter %d: exit=%d, outlen=%d%n", i, exitCode, output.length());

            // If non-zero exit code -> possible crash/bug: save input + output
            if (exitCode != 0) {
                String base = String.format("crash_iter_%03d", i);
                Files.writeString(Path.of(base + ".html"), mutated);
                Files.writeString(Path.of(base + ".log"), "exit=" + exitCode + "\n\n" + output);
                System.out.println(">>> Possible crash detected! Saved: " + base + ".html and " + base + ".log");
                // keep going — remove break if you want to continue searching
            }
        }
    }

    static String readStream(InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    // Compose several small mutators
    static String mutate(String s) {
        // apply 1-3 random mutations
        int count = 1 + rnd.nextInt(3);
        String res = s;
        for (int i = 0; i < count; i++) {
            int which = rnd.nextInt(4);
            switch (which) {
                case 0: res = flipRandomChar(res); break;
                case 1: res = deleteRandomChunk(res); break;
                case 2: res = duplicateRandomChunk(res); break;
                case 3: res = insertRandomTag(res); break;
            }
        }
        return res;
    }

    static String flipRandomChar(String s) {
        if (s.isEmpty()) return s;
        int idx = rnd.nextInt(s.length());
        char c = (char)(32 + rnd.nextInt(95)); // printable
        return s.substring(0, idx) + c + s.substring(idx+1);
    }

    static String deleteRandomChunk(String s) {
        if (s.length() < 3) return "";
        int a = rnd.nextInt(s.length());
        int b = Math.min(s.length(), a + 1 + rnd.nextInt(10));
        return s.substring(0, a) + s.substring(b);
    }

    static String duplicateRandomChunk(String s) {
        if (s.length() < 3) return s;
        int a = rnd.nextInt(s.length());
        int b = Math.min(s.length(), a + 1 + rnd.nextInt(10));
        String chunk = s.substring(a, b);
        int insertAt = rnd.nextInt(s.length());
        return s.substring(0, insertAt) + chunk + s.substring(insertAt);
    }

    static String insertRandomTag(String s) {
        String[] tags = {"<div>", "</div>", "<span>", "</span>", "<img src='x'>", "<a href='#'>link</a>", "<!--comment-->"};
        String tag = tags[rnd.nextInt(tags.length)];
        int pos = rnd.nextInt(s.length()+1);
        return s.substring(0, pos) + tag + s.substring(pos);
    }
}
