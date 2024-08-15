import java.io.*;
import java.util.*;

public class FileSimilarity {
    
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java Sum filepath1 filepath2 filepathN");
            System.exit(1);
        }
        // Create a map to store the fingerprint for each file
        Map<String, List<Long>> fileFingerprints = new HashMap<>();

        Map<String, Thread> threads = new HashMap<>();

        // Calculate the fingerprint for each file
        for (String path : args) {
            Thread thread = new Thread(new FileOperation(path, fileFingerprints), path);
            threads.put(path, thread);
            thread.start();
        }

        for (String path : args) {
            threads.get(path).join();
        }

        // Compare each pair of files
        for (int i = 0; i < args.length; i++) {
            for (int j = i + 1; j < args.length; j++) {
                String file1 = args[i];
                String file2 = args[j];
                List<Long> fingerprint1 = fileFingerprints.get(file1);
                List<Long> fingerprint2 = fileFingerprints.get(file2);
                Thread thread = new Thread(new Similarity(args[i], args[j], fingerprint1, fingerprint2));
                thread.start();
            }
        }
    }

    public static class FileOperation implements Runnable{

    private final String path;
    private Map<String, List<Long>> fileFingerprints;
        
    public FileOperation(String path, Map<String, List<Long>> fileFingerprints) {
        this.path = path;
        this.fileFingerprints = fileFingerprints;
    }

    @Override
    public void run() {
        try {
            List<Long> fingerprint = fileSum(path);
            fileFingerprints.put(path, fingerprint);
        } catch (IOException ex) {
        }
    }

    private static List<Long> fileSum(String filePath) throws IOException {
        File file = new File(filePath);
        List<Long> chunks = new ArrayList<>();
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[100];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                long sum = sum(buffer, bytesRead);
                chunks.add(sum);
            }
        }
        return chunks;
    }

    private static long sum(byte[] buffer, int length) {
        long sum = 0;
        for (int i = 0; i < length; i++) {
            sum += Byte.toUnsignedInt(buffer[i]);
        }
        return sum;
        }
    }

    public static class Similarity implements Runnable {
        private final String file1;
        private final String file2;
        private final List<Long> fingerprint1;
        private final List<Long> fingerprint2;
            
        public Similarity(String file1, String file2, List<Long> fingerprint1, List<Long> fingerprint2) {
            this.file1 = file1;
            this.file2 = file2;
            
            this.fingerprint1 = fingerprint1;
            this.fingerprint2 = fingerprint2;
        }
        @Override
        public void run() {
            try {
                float similarityScore = similarity(fingerprint1, fingerprint2);
                System.out.println("Similarity between " + file1 + " and " + file2 + ": " + (similarityScore * 100) + "%");
            } catch (IOException ex) {
            }
        }

        private static float similarity(List<Long> base, List<Long> target) throws IOException {
            int counter = 0;
            List<Long> targetCopy = new ArrayList<>(target);

            for (Long value : base) {
                if (targetCopy.contains(value)) {
                    counter++;
                    targetCopy.remove(value);
                }
            }

            return (float) counter / base.size();
        }
    }
}
