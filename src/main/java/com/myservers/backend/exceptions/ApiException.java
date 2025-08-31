package com.myservers.backend.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import java.time.ZonedDateTime;

@AllArgsConstructor
@Getter
@Setter
public class ApiException {
    private final  String message;
    private final String throwable;
    private final HttpStatus httpStatus;
    private final ZonedDateTime zonedDateTime;

}
