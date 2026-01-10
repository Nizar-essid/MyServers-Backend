package com.myservers.backend.shop.services;

import com.myservers.backend.security.auth.entities.Admin;
import com.myservers.backend.security.auth.entities.User;
import com.myservers.backend.shop.classes.QuoteRequestRequest;
import com.myservers.backend.shop.classes.QuoteRequestResponse;
import com.myservers.backend.shop.entities.Product;
import com.myservers.backend.shop.entities.QuoteRequest;
import com.myservers.backend.shop.entities.QuoteRequestStatus;
import com.myservers.backend.shop.repositories.ProductRepository;
import com.myservers.backend.shop.repositories.QuoteRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuoteRequestService {

    @Autowired
    private QuoteRequestRepository quoteRequestRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    public QuoteRequestResponse createQuoteRequest(QuoteRequestRequest request, User user) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        QuoteRequest quoteRequest = QuoteRequest.builder()
                .product(product)
                .user(user)
                .contactName(request.getContactName())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .message(request.getMessage())
                .status(QuoteRequestStatus.PENDING)
                .dateCreation(new Date())
                .build();

        QuoteRequest saved = quoteRequestRepository.save(quoteRequest);
        return convertToResponse(saved);
    }

    public List<QuoteRequestResponse> getAllQuoteRequests() {
        return quoteRequestRepository.findAllByOrderByDateCreationDesc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<QuoteRequestResponse> getQuoteRequestsByStatus(QuoteRequestStatus status) {
        return quoteRequestRepository.findByStatusOrderByDateCreationDesc(status)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<QuoteRequestResponse> getQuoteRequestsByUser(Integer userId) {
        return quoteRequestRepository.findByUserIdOrderByDateCreationDesc(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public QuoteRequestResponse updateQuoteRequestStatus(Long id, QuoteRequestStatus status, String adminNotes, Admin processedBy) {
        QuoteRequest quoteRequest = quoteRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote request not found"));

        quoteRequest.setStatus(status);
        quoteRequest.setAdminNotes(adminNotes);
        quoteRequest.setProcessedBy(processedBy);
        quoteRequest.setDateProcessed(new Date());

        QuoteRequest saved = quoteRequestRepository.save(quoteRequest);
        return convertToResponse(saved);
    }

    private QuoteRequestResponse convertToResponse(QuoteRequest quoteRequest) {
        return QuoteRequestResponse.builder()
                .id(quoteRequest.getId())
                .product(productService.getProductById(quoteRequest.getProduct().getId()))
                .userId(quoteRequest.getUser() != null ? Long.valueOf(quoteRequest.getUser().getId()): null)
                .userName(quoteRequest.getUser() != null ?
                  STR."\{quoteRequest.getUser().getFirstname()} \{quoteRequest.getUser().getLastname()}" : null)
                .contactName(quoteRequest.getContactName())
                .contactEmail(quoteRequest.getContactEmail())
                .contactPhone(quoteRequest.getContactPhone())
                .message(quoteRequest.getMessage())
                .status(quoteRequest.getStatus().name())
                .dateCreation(quoteRequest.getDateCreation())
                .dateProcessed(quoteRequest.getDateProcessed())
                .processedById(quoteRequest.getProcessedBy() != null ? Long.valueOf(quoteRequest.getProcessedBy().getId()) : null)
                .processedByName(quoteRequest.getProcessedBy() != null ?
                    quoteRequest.getProcessedBy().getFirstname() + " " + quoteRequest.getProcessedBy().getLastname() : null)
                .adminNotes(quoteRequest.getAdminNotes())
                .build();
    }
}

