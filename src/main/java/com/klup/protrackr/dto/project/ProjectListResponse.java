package com.klup.protrackr.dto.project;

import java.util.List;

public record ProjectListResponse(
        List<ProjectDto> projects
) {}

