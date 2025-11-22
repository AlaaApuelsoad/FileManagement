package org.example.alaa.filemanagement;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Getter
public enum FileType {

    IMAGE(1, "Image"),
    PDF(2, "PDF"),
    WORD(3, "Word"),
    EXCEL(4, "Excel"),
    TEXT(5, "Text"),
    VIDEO(6, "Video"),
    AUDIO(7, "Audio"),
    OTHER(8, "Other");

    private final int id;
    private final String label;

    FileType(int id, String label) {
        this.id = id;
        this.label = label;
    }

    // ------------------------------------------
    // Convert ID -> Enum
    // ------------------------------------------
    public static FileType fromId(int id) {
        for (FileType type : values()) {
            if (type.getId() == id) {
                return type;
            }else{
                throw new RuntimeException("Invalid File Type");
            }
        }
        return null;
    }

    // ------------------------------------------
    // Convert file extension -> Enum
    // ------------------------------------------
    public static FileType fromExtension(MultipartFile file) {

        String fileName = file.getOriginalFilename();
        String ext = Objects.requireNonNull(fileName).substring(fileName.lastIndexOf(".") + 1);

        return switch (ext.toLowerCase()) {
            case "png", "jpg", "jpeg", "gif", "bmp" -> IMAGE;
            case "pdf" -> PDF;
            case "doc", "docx" -> WORD;
            case "xls", "xlsx" -> EXCEL;
            case "txt", "csv" -> TEXT;
            case "mp4", "mov", "avi" -> VIDEO;
            case "mp3", "wav" -> AUDIO;
            default -> throw new RuntimeException("Invalid File Type");
        };
    }

}
