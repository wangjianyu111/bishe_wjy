package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.CampusAddReq;
import com.gdplatform.dto.CampusResp;
import com.gdplatform.dto.CampusUpdateReq;

import java.util.List;

public interface CampusService {

    Page<CampusResp> pageCampuses(long current, long size, String keyword);

    List<CampusResp> listAll();

    CampusResp getById(Long campusId);

    void add(CampusAddReq req);

    void update(CampusUpdateReq req);

    void delete(Long campusId);

    Long findOrCreateByName(String campusName);
}
