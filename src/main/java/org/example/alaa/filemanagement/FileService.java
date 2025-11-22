package org.example.alaa.filemanagement;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FileService {

    private final DocumentRepository documentRepository;

    public String uploadFile(MultipartFile file, String description) {

        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Uploaded file is empty");
            }

            //Base upload directory
            Path uploadRoot = Paths.get(Constant.UPLOAD_DIR).toAbsolutePath().normalize();

            //Create subfolder with UUID
            String folderName = generateRandomUUID();
            Path folderPath = uploadRoot.resolve(folderName);

            Files.createDirectories(folderPath);

            //Extract file name
            String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

            //Final file path
            Path filePath = folderPath.resolve(originalFileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            FileType fileType = FileType.fromExtension(file);

            Document document = Document.builder()
                    .name(originalFileName)
                    .size(file.getSize())
                    .type(fileType.getId())
                    .description(description)
                    .path(filePath.toString())
                    .build();

            documentRepository.save(document);

            return "File Uploaded Successfully";

        } catch (Exception exception) {
            throw new RuntimeException("Failed to upload file: " + exception.getMessage(), exception);
        }
    }

    public ResponseEntity<Resource> downloadFile(Long documentId) {
        try {
            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("File Not Found"));

            Path path = Paths.get(document.getPath()).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new FileNotFoundException("Could not read file: " + document.getName());
            }

            // Detect content-type
            String mimeType = Files.probeContentType(path);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "file; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            throw new RuntimeException("Error downloading file", e);
        }
    }

    public ResponseEntity<Resource> previewFile(Long id) {
        try {
            Document document = documentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("File Not Found"));

            Path path = Paths.get(document.getPath()).normalize();

            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("Could not read file: " + document.getName());
            }

            String mimeType = Files.probeContentType(path);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<String> deleteFile(Long id) {
        try {
            Document document = documentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("File Not Found"));

            Path filePath = Paths.get(document.getPath()).normalize(); //File path
            Path parent = filePath.getParent(); //UUID Folder
            if (Files.exists(parent)) {
                Files.walk(parent)
                                .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            }catch (Exception e) {
                                throw new RuntimeException("Failed to delete: " + path, e);
                            }
                        });
                documentRepository.deleteById(id);
                return new ResponseEntity<>("File Deleted Successfully", HttpStatus.OK);
            }else {
                return new ResponseEntity<>("File Not Found", HttpStatus.NOT_FOUND);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateRandomUUID(){
        return java.util.UUID.randomUUID().toString();
    }

}
