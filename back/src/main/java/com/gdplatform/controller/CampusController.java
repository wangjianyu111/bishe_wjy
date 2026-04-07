package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.CampusAddReq;
import com.gdplatform.dto.CampusResp;
import com.gdplatform.dto.CampusUpdateReq;
import com.gdplatform.service.CampusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/campuses")
@RequiredArgsConstructor
public class CampusController {

    private final CampusService campusService;

    @GetMapping("/page")
    public R<Page<CampusResp>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return R.ok(campusService.pageCampuses(current, size, keyword));
    }

    @GetMapping
    public R<List<CampusResp>> list() {
        return R.ok(campusService.listAll());
    }

    @GetMapping("/{campusId}")
    public R<CampusResp> getById(@PathVariable Long campusId) {
        return R.ok(campusService.getById(campusId));
    }

    @PostMapping
    public R<Void> add(@Valid @RequestBody CampusAddReq req) {
        campusService.add(req);
        return R.ok();
    }

    @PutMapping
    public R<Void> update(@Valid @RequestBody CampusUpdateReq req) {
        campusService.update(req);
        return R.ok();
    }

    @DeleteMapping("/{campusId}")
    public R<Void> delete(@PathVariable Long campusId) {
        campusService.delete(campusId);
        return R.ok();
    }
}
