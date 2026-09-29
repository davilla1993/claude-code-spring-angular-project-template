package com.gfolly.quantly_backend.system.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class SystemConfigController {

    @Value("${app.mode:standalone}")
    private String appMode;

    @GetMapping
    public Map<String, String> getConfig() {
        return Map.of("appMode", appMode);
    }
}
