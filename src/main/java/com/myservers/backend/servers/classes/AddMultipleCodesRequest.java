package com.myservers.backend.servers.classes;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AddMultipleCodesRequest {

    private List<CodeDetails> codes;
    private long id_server;
}
