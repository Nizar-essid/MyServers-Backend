package com.myservers.backend.files.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;

@Service
public class FilesService {

    public String uploadDocument( MultipartFile file,String path) {



        String fileName = insertString(file.getOriginalFilename(), String.valueOf(new Date().getTime()),file.getOriginalFilename().lastIndexOf(".")-1);


        try
        {
         //   System.out.println(path);
            file.transferTo(new File(path + fileName));

            return fileName;
        } catch (Exception e) {
            System.out.println("error:" + e.getMessage());
        }
        return "false";
    }

    public static String insertString(
            String originalString,
            String stringToBeInserted,
            int index)
    {

        // Create a new string
        String newString = originalString.substring(0, index + 1)
                + stringToBeInserted
                + originalString.substring(index + 1);

        // return the modified String
        return newString;
    }
}
