package com.designtouch.backend.controller;

import com.designtouch.backend.dto.AddOnResponse;
import com.designtouch.backend.repository.AddOnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/addons")
@RequiredArgsConstructor
public class AddOnController {

    private final AddOnRepository repository;

    @GetMapping
    public List<AddOnResponse> getAll() {
        return repository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(AddOnResponse::from)
                .toList();
    }
}
