package com.myservers.backend.files.controllers;

import com.myservers.backend.files.services.FilesService;
import com.myservers.backend.servers.classes.GeneralResponse;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@CrossOrigin(origins = "*" , exposedHeaders = "**")

@RequestMapping("/api/v1/files/basic")
@RestController
public class FileController {

@Autowired
private FilesService fileService;
    @Autowired
    private Environment env;

    @CrossOrigin(origins = "*" , exposedHeaders = "**")
@GetMapping("/test")
public ResponseEntity test() throws IOException {
        final String fullpath=env.getProperty("webApplication.imagesPath")+"Paypal_2014_logo1726518664296.png";
      //  System.out.println("fullpath="+fullpath);
        ClassPathResource imageResource = new ClassPathResource(fullpath);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return ResponseEntity.ok()
                .headers(headers)
                .body((Resource) new InputStreamResource(imageResource.getInputStream()));    }
    @CrossOrigin(origins = "*" , exposedHeaders = "**")
    @PostMapping("/upload")
    public GeneralResponse uploadTest(@RequestPart("file") MultipartFile file) {
        try{
        String path = env.getProperty("webApplication.imagesPath");

        String fileName = fileService.uploadDocument(file,path);

        return GeneralResponse.builder()
                .result(fileName)
                .status(200L)
                .build();

    }catch (Exception e){
            return GeneralResponse.builder()
                    .result("Erreur serveur"+e.getMessage())
                    .status(501L)
                    .trueFalse(false)
                    .build();}
    }

    @CrossOrigin(origins = "*" , exposedHeaders = "**")
    @GetMapping(value="/open/{path}" , consumes = MediaType.ALL_VALUE, produces = MediaType.IMAGE_JPEG_VALUE)
    public org.springframework.core.io.Resource getDocumentContent(@PathVariable String path) throws IOException {
//        final String fullpath=env.getProperty("webApplication.imagesPath")+path;
//        System.out.println(fullpath);
//        ClassPathResource imageResource = new ClassPathResource(fullpath);
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//        return ResponseEntity.ok()
//                .headers(headers)
//                .body((Resource) new InputStreamResource(imageResource.getInputStream()));
        Path imagePath = Paths.get(env.getProperty("webApplication.imagesPath"), path);
        return new UrlResource(imagePath.toUri());
    }

    @CrossOrigin(origins = "*" , exposedHeaders = "**")
    @GetMapping(value="/applications/icons/{filename:.+}" , consumes = MediaType.ALL_VALUE, produces = MediaType.IMAGE_JPEG_VALUE)
    public org.springframework.core.io.Resource getApplicationIcon(@PathVariable String filename) throws IOException {
        Path iconPath = Paths.get(env.getProperty("webApplication.imagesPath"),"uploads/applications/icons", filename);
        return new UrlResource(iconPath.toUri());
    }
}
