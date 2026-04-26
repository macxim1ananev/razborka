package ru.razborka.marketplace.listing.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.razborka.marketplace.common.exception.BusinessException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    private final Path root;
    private final String publicBaseUrl;

    public LocalStorageService(
            @Value("${app.storage.local.root:./data/uploads}") String rootPath,
            @Value("${app.storage.public-base-url:http://localhost:8080/files}") String publicBaseUrl
    ) throws IOException {
        this.root = Path.of(rootPath).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
        Files.createDirectories(this.root);
    }

    @Override
    public String store(MultipartFile file, String subdirectory) throws IOException {
        if (file.isEmpty()) {
            throw new BusinessException("FILE", "Пустой файл");
        }
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) {
            ext = original.substring(dot).toLowerCase(Locale.ROOT);
        }
        if (!ext.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            throw new BusinessException("FILE", "Допустимы изображения: jpg, png, gif, webp");
        }
        String safeSub = subdirectory.replaceAll("[^a-zA-Z0-9_-]", "");
        Path dir = root.resolve(safeSub).normalize();
        if (!dir.startsWith(root)) {
            throw new BusinessException("FILE", "Некорректный путь");
        }
        Files.createDirectories(dir);
        String name = UUID.randomUUID() + ext;
        Path target = dir.resolve(name);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return publicBaseUrl + "/" + safeSub + "/" + name;
    }
}
