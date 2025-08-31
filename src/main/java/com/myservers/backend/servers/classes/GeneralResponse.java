package com.myservers.backend.servers.classes;

import lombok.*;

import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@Builder
public class GeneralResponse {

    private Long status;
    private String result;
private Boolean trueFalse=true;
private ArrayList<Object> data;
    private Object singleData;

//    public static StructuredTaskScope.ShutdownOnSuccess<Object> builder() {
//        return null;
//    }
}
