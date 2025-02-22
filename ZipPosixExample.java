import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.Set;
import java.util.Map;

public class ZipPosixExample {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ZipPosixExample <fileOrDirectoryToArchive> <archiveName>");
            return;
        }

        String fileOrDirectoryToArchive = args[0];
        String archiveName = args[1];

        Path sourcePath = Paths.get(fileOrDirectoryToArchive);
        Path zipPath = Paths.get(archiveName);

        // Environment properties to enable POSIX attributes
        Map<String, String> env = Map.of("create", "true", "enablePosixFileAttributes", "true");

        try (FileSystem zipFs = FileSystems.newFileSystem(URI.create("jar:" + zipPath.toUri()), env)) {
            if (Files.isDirectory(sourcePath)) {
                // Archive the entire directory
                Files.walk(sourcePath).forEach(path -> {
                    try {
                        // Resolve the relative path of the file/directory
                        Path relativePath = sourcePath.relativize(path);
                        Path entry = zipFs.getPath(relativePath.toString());

                        if (Files.isDirectory(path)) {
                            // Create the directory in the ZIP archive
                            Files.createDirectories(entry);
                        } else if (Files.isSymbolicLink(path)) {
                            // Handle symbolic links
                            Path linkTarget = Files.readSymbolicLink(path);
                            // Store the link target in a separate file
                            Path linkTargetFile = zipFs.getPath(relativePath + ".symlink");
                            Files.write(linkTargetFile, linkTarget.toString().getBytes());
                            System.out.println("Archived (symlink): " + path + " -> " + linkTarget);
                        } else {
                            // Copy the file to the ZIP archive
                            Files.copy(path, entry, StandardCopyOption.REPLACE_EXISTING);

                            // Retrieve POSIX permissions from the source file
                            PosixFileAttributes sourceAttributes = Files.readAttributes(path, PosixFileAttributes.class);
                            Set<PosixFilePermission> permissions = sourceAttributes.permissions();

                            // Set the same POSIX permissions in the ZIP archive
                            Files.setPosixFilePermissions(entry, permissions);

                            System.out.println("Archived: " + path);
                        }
                    } catch (IOException e) {
                        System.err.println("Failed to archive: " + path);
                        e.printStackTrace();
                    }
                });
            } else if (Files.isRegularFile(sourcePath)) {
                // Archive a single file
                Path entry = zipFs.getPath(sourcePath.getFileName().toString());

                // Copy the file to the ZIP archive
                Files.copy(sourcePath, entry, StandardCopyOption.REPLACE_EXISTING);

                // Retrieve POSIX permissions from the source file
                PosixFileAttributes sourceAttributes = Files.readAttributes(sourcePath, PosixFileAttributes.class);
                Set<PosixFilePermission> permissions = sourceAttributes.permissions();

                // Set the same POSIX permissions in the ZIP archive
                Files.setPosixFilePermissions(entry, permissions);

                System.out.println("Archived: " + sourcePath);
            } else {
                System.err.println("Source path is neither a file nor a directory: " + sourcePath);
            }
        } catch (IOException e) {
            System.err.println("Failed to create or open ZIP file: " + zipPath);
            e.printStackTrace();
        }
    }
}
