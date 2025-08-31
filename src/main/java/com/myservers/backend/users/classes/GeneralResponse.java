package com.myservers.backend.users.classes;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
private List<UserResponse> dataUsers;
    private Map<String, Object> dataMap;
    private Object singleData;

//    public static StructuredTaskScope.ShutdownOnSuccess<Object> builder() {
//        return null;
//    }
}
