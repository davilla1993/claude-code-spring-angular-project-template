package com.gfolly.backend.infrastructure.security;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Permet de lire plusieurs fois le corps d'une requête (inspection par RateLimitFilter puis controller).
 * La lecture est bornée : les endpoints concernés n'attendent que de petits JSON.
 */
class CachedBodyRequestWrapper extends HttpServletRequestWrapper {

    private static final int MAX_BODY_BYTES = 16 * 1024;

    private final byte[] cachedBody;

    CachedBodyRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = request.getInputStream().readNBytes(MAX_BODY_BYTES);
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream stream = new ByteArrayInputStream(cachedBody);
        return new ServletInputStream() {
            @Override public int read() { return stream.read(); }
            @Override public int read(byte[] b, int off, int len) { return stream.read(b, off, len); }
            @Override public boolean isFinished() { return stream.available() == 0; }
            @Override public boolean isReady() { return true; }
            @Override public void setReadListener(ReadListener listener) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }
}
