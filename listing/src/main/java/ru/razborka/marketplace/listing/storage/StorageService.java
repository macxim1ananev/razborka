package ru.razborka.marketplace.listing.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String store(MultipartFile file, String subdirectory) throws java.io.IOException;
}
