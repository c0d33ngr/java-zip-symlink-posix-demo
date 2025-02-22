# Preserving POSIX Permissions and Symbolic Links in ZIP Archives

This repository is a proof of concept and guide for preserving POSIX file permissions and symbolic links when creating and extracting ZIP archives using Java. It demonstrates how to handle these attributes, which are often lost when using standard ZIP tools.

## Why This Matters

When working with ZIP archives on POSIX-compliant systems (e.g., Linux, macOS), file permissions (e.g., executable flags) and symbolic links are often lost during archiving and extraction. This project shows how to preserve these attributes using Java's `java.nio.file` package.

## Features

- **Preserve POSIX Permissions**: Retains file permissions (e.g., `rwxr-xr-x`) during archiving and extraction.
- **Handle Symbolic Links**: Stores and recreates symbolic links correctly, ensuring they point to the correct target files.
- **Proof of Concept**: Demonstrates how to implement this functionality in Java.

## How It Works

### Archiving
1. **Regular Files and Directories**:
   - Files and directories are copied into the ZIP archive.
   - POSIX permissions are retrieved using `Files.getPosixFilePermissions()` and stored in the archive.

2. **Symbolic Links**:
   - Symbolic links are detected using `Files.isSymbolicLink()`.
   - The target path of the symbolic link is stored in a separate `.symlink` file within the archive.

### Extraction
1. **Regular Files and Directories**:
   - Files and directories are extracted with their original permissions using `Files.setPosixFilePermissions()`.

2. **Symbolic Links**:
   - The target path is read from the `.symlink` file.
   - The symbolic link is recreated using `Files.createSymbolicLink()`.

## Code Overview

### `ZipPosixExample.java`
This program creates a ZIP archive while preserving POSIX permissions and symbolic links.

#### Key Steps:
1. Detect symbolic links and store their target paths.
2. Copy files and directories into the archive.
3. Store POSIX permissions for each file.

### `ZipExtractExample.java`
This program extracts a ZIP archive while recreating symbolic links and restoring POSIX permissions.

#### Key Steps:
1. Extract files and directories.
2. Recreate symbolic links using stored target paths.
3. Restore POSIX permissions for each file.

## Usage

### Prerequisites
- Java 8 or higher.
- A POSIX-compliant file system (e.g., Linux, macOS).

### Steps

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/java-zip-posix-howto.git
   cd java-zip-posix-howto
