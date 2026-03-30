package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.SelectionApplyRequest;
import com.gdplatform.entity.ProjectSelection;

public interface ProjectSelectionService {
    void apply(SelectionApplyRequest request);
    Page<ProjectSelection> pageSelections(long current, long size);
}
