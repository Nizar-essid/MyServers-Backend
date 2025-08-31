package com.myservers.backend.servers.classes;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AddCodeRequest {

private CodeDetails code_details;

private long  id_server;}

