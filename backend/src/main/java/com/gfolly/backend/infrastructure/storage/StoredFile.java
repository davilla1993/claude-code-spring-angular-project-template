package com.gfolly.backend.infrastructure.storage;

import java.io.InputStream;

/** Fichier lu depuis le stockage. L'appelant doit fermer le flux. */
public record StoredFile(InputStream content, long size) {}
