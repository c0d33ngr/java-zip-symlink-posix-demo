import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Map;
import java.util.Set;

public class ZipExtractExample {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ZipExtractExample <archiveFilePath> <destinationDirectory>");
            return;
        }

        String archiveFilePath = args[0];      // Path to the ZIP file
        String destinationDirectory = args[1]; // Destination directory for extraction

        Path zipPath = Paths.get(archiveFilePath);
        Path extractToDir = Paths.get(destinationDirectory);

        // Create the destination directory if it doesn't exist
        try {
            Files.createDirectories(extractToDir);
        } catch (IOException e) {
            System.err.println("Failed to create destination directory: " + extractToDir);
            e.printStackTrace();
            return;
        }

        URI uri = URI.create("jar:file:" + zipPath.toAbsolutePath());

        // Enable POSIX support when opening the ZIP file
        Map<String, String> env = Map.of("enablePosixFileAttributes", "true");

        try (FileSystem zipFs = FileSystems.newFileSystem(uri, env)) {
            // Iterate over all files/directories in the ZIP archive
            for (Path root : zipFs.getRootDirectories()) {
                Files.walk(root)
                     .forEach(path -> {
                         try {
                             // Resolve the relative path of the file/directory
                             Path relativePath = root.relativize(path);
                             Path destinationPath = extractToDir.resolve(relativePath.toString());

                             if (Files.isDirectory(path)) {
                                 // Create the directory in the destination
                                 Files.createDirectories(destinationPath);
                             } else if (path.toString().endsWith(".symlink")) {
                                 // Handle symbolic links
                                 Path linkTargetPath = Paths.get(new String(Files.readAllBytes(path)));
                                 Path symlinkPath = extractToDir.resolve(relativePath.toString().replace(".symlink", ""));
                                 Files.createSymbolicLink(symlinkPath, linkTargetPath);
                                 System.out.println("Extracted (symlink): " + symlinkPath + " -> " + linkTargetPath);
                             } else {
                                 // Copy the file to the destination
                                 Files.copy(path, destinationPath, StandardCopyOption.REPLACE_EXISTING);

                                 // Retrieve and set POSIX permissions
                                 Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);
                                 Files.setPosixFilePermissions(destinationPath, permissions);

                                 System.out.println("Extracted: " + destinationPath);
                             }
                         } catch (IOException e) {
                             System.err.println("Failed to extract: " + path);
                             e.printStackTrace();
                         }
                     });
            }
        } catch (IOException e) {
            System.err.println("Failed to open ZIP file: " + zipPath);
            e.printStackTrace();
        }
    }
}
