package com.myservers.backend.servers.services;


import com.myservers.backend.exceptions.ApiRequestException;
import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.servers.classes.*;
import com.myservers.backend.servers.entities.*;
import com.myservers.backend.servers.classes.UpdateOnDemandServerRequest;
import com.myservers.backend.servers.repositories.CodeRepository;
import com.myservers.backend.servers.repositories.ServerRepository;
import com.myservers.backend.users.classes.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServersService {
    @Autowired
    private ServerRepository serverRepository;
    @Autowired
    private CodeRepository codeRepository;
    @Autowired
    private CodesService codeService;
    @Autowired
    private CategoryService categoryService;

    public Server saveServer(Server p)
    {
        return serverRepository.save(p);
    }
    
    public void setServerCategory(Server server, Long categoryId) {
        if (categoryId != null) {
            if (!categoryService.isLeafCategory(categoryId)) {
                throw new ApiRequestException("Cannot assign server to a category that has children. Only leaf categories can contain servers.", HttpStatus.BAD_REQUEST);
            }
            server.setCategory(categoryService.getCategoryByIdEntity(categoryId));
        } else {
            server.setCategory(null);
        }
    }
    public Iterable<Server> getServer() {
        return serverRepository.findByState(true);
    }
    public void delete(long id) {
        serverRepository.deleteById(id);
    }
    public GeneralResponse deleteServer(Integer id) {
try{
       if(!codeService.getCodesREquestedorPurshasedWithinServer(Long.valueOf(id)).isEmpty())
       {
           return GeneralResponse.builder()
                   .status(403L)
                   .result("Not possible to delete the server as it has purchased/requested codes")
                   .build();
       }

        if(serverRepository.updateStateById(false, Long.valueOf(id))>0){
            return GeneralResponse.builder()
                    .status(200L)
                    .result("success")
                    .build();
        }
        else  return GeneralResponse.builder()
                .status(404L)
                .result("Not possible to delete the server as it does not exist")
                .build();
    }
    catch (Exception e){
        return GeneralResponse.builder()
                .status(500L)
                .result(e.getMessage())
                .build();}
    }

    public List<Server> getAllServers() {
        return this.serverRepository.findByState(true);
    }
    public Optional<Server> getServer(Long id) {
        return serverRepository.findById(id);
    }

    public Server getServerById(Long id) {
        return serverRepository.findById(id).orElseThrow();
    }
    public GeneralResponse saveServer(AddServerDetails serverDetails, Admin user) {
      try{  var server=serverRepository.findById(Long.valueOf(serverDetails.getId())).orElseThrow();
        if(server!=null){
            server.setLogo(serverDetails.getLogo());
            server.setName_serv(serverDetails.getName_serv());
            server.setUpdated_by(user);
            server.setState(true);
            
            // Handle category assignment (only leaf categories)
            if (serverDetails.getCategoryId() != null) {
                if (!categoryService.isLeafCategory(serverDetails.getCategoryId())) {
                    return GeneralResponse.builder()
                            .status(400L)
                            .result("Cannot assign server to a category that has children. Only leaf categories can contain servers.")
                            .trueFalse(false)
                            .build();
                }
                server.setCategory(categoryService.getCategoryByIdEntity(serverDetails.getCategoryId()));
            } else {
                server.setCategory(null);
            }
            
            serverRepository.save(server);
        return GeneralResponse.builder()
                .status(200L)
                .result("success")
                .trueFalse(true)
                .build();
        }
        else  return GeneralResponse.builder()
                .status(404L)
                .result("server not exist")
                .trueFalse(false)
                .build();

    }
    catch (Exception e){
        return GeneralResponse.builder()
                .status(500L)
                .result("erreur serveur: " + e.getMessage())
                .trueFalse(false)
                .build();}
    }

    public GeneralResponse updateOnDemandServer(UpdateOnDemandServerRequest serverDetails, Admin user) {
        try {
            Optional<Server> serverOpt = serverRepository.findById(serverDetails.getId());
            if (serverOpt.isEmpty()) {
                return GeneralResponse.builder()
                        .status(404L)
                        .result("Server not found")
                        .trueFalse(false)
                        .build();
            }

            Server server = serverOpt.get();
            
            // Vérifier que c'est bien un serveur à la demande
            if (server.getServerType() != ServerType.ONDEMAND) {
                return GeneralResponse.builder()
                        .status(400L)
                        .result("This server is not an on-demand server")
                        .trueFalse(false)
                        .build();
            }

            // Mettre à jour les champs
            server.setName_serv(serverDetails.getName_serv());
            server.setDescription(serverDetails.getDescription());
            server.setPrice(serverDetails.getPrice());
            server.setDuration_months(serverDetails.getDuration_months());
            server.setActive(serverDetails.getActive());
            server.setLogo(serverDetails.getLogo());
            server.setUpdated_by(user);
            
            // Handle category assignment (only leaf categories)
            if (serverDetails.getCategoryId() != null) {
                if (!categoryService.isLeafCategory(serverDetails.getCategoryId())) {
                    return GeneralResponse.builder()
                            .status(400L)
                            .result("Cannot assign server to a category that has children. Only leaf categories can contain servers.")
                            .trueFalse(false)
                            .build();
                }
                server.setCategory(categoryService.getCategoryByIdEntity(serverDetails.getCategoryId()));
            } else {
                server.setCategory(null);
            }
            
            serverRepository.save(server);
            
            return GeneralResponse.builder()
                    .status(200L)
                    .result("On-demand server updated successfully")
                    .trueFalse(true)
                    .build();
                    
        } catch (Exception e) {
            return GeneralResponse.builder()
                    .status(500L)
                    .result("Error updating on-demand server: " + e.getMessage())
                    .trueFalse(false)
                    .build();
        }
    }

}
