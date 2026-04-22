package org.example.backend.shared.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadAvatarResponse {

    private String storageKey;

    private String url;

    private long size;
}
