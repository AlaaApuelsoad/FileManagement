package org.example.alaa.filemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FileManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileManagementApplication.class, args);
        System.out.println("File Management Application Started");
        System.out.println("upload directory is : " + Constant.UPLOAD_DIR);
    }

}
