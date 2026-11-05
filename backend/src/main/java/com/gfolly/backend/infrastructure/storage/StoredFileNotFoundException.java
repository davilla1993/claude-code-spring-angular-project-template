package com.gfolly.backend.infrastructure.storage;

import com.gfolly.backend.shared.util.ErrorMessages;

public class StoredFileNotFoundException extends RuntimeException {

    public StoredFileNotFoundException() {
        super(ErrorMessages.FILE_NOT_FOUND);
    }
}
