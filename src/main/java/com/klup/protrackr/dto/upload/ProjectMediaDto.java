package com.klup.protrackr.dto.upload;

import com.klup.protrackr.dto.common.SimpleUserDto;

import java.time.Instant;

public record ProjectMediaDto(
        Long id,
        Long projectId,
        SimpleUserDto uploader,
        String fileUrl,
        String fileName,
        String fileType,
        Instant createdAt
) {}

