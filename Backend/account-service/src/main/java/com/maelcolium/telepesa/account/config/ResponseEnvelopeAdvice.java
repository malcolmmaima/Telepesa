package com.maelcolium.telepesa.account.config;

import com.maelcolium.telepesa.models.dto.ApiResponse;
import com.maelcolium.telepesa.models.dto.PaginationMeta;
import com.maelcolium.telepesa.exceptions.ErrorResponse;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.LocalDateTime;

@ControllerAdvice
public class ResponseEnvelopeAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body instanceof ApiResponse) {
            return body;
        }
        if (body instanceof ErrorResponse) {
            return body; // do not wrap error responses
        }
        if (body instanceof Page<?> page) {
            PaginationMeta meta = PaginationMeta.builder()
                    .pageNumber(page.getNumber())
                    .pageSize(page.getSize())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .first(page.isFirst())
                    .last(page.isLast())
                    .build();
            return ApiResponse.builder()
                    .success(true)
                    .message("OK")
                    .data(page.getContent())
                    .page(meta)
                    .path(request.getURI().getPath())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        return ApiResponse.builder()
                .success(true)
                .message("OK")
                .data(body)
                .path(request.getURI().getPath())
                .timestamp(LocalDateTime.now())
                .build();
    }
}













