package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.CampusAddReq;
import com.gdplatform.dto.CampusResp;
import com.gdplatform.dto.CampusUpdateReq;
import com.gdplatform.entity.SysCampus;
import com.gdplatform.mapper.CampusMapper;
import com.gdplatform.service.CampusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampusServiceImpl implements CampusService {

    private final CampusMapper campusMapper;

    @Override
    public Page<CampusResp> pageCampuses(long current, long size, String keyword) {
        Page<CampusResp> page = new Page<>(current, size);
        long offset = (current - 1) * size;
        List<CampusResp> records = campusMapper.selectCampusPage(keyword, offset, size);
        long total = campusMapper.countCampusPage(keyword);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<CampusResp> listAll() {
        List<SysCampus> campuses = campusMapper.selectList(
                Wrappers.<SysCampus>lambdaQuery()
                        .orderByAsc(SysCampus::getSortOrder)
                        .orderByDesc(SysCampus::getCreateTime));
        return campuses.stream().map(this::toResp).toList();
    }

    @Override
    public CampusResp getById(Long campusId) {
        SysCampus campus = campusMapper.selectById(campusId);
        if (campus == null) {
            throw new BizException("校区不存在");
        }
        return toResp(campus);
    }

    @Override
    @Transactional
    public void add(CampusAddReq req) {
        SysCampus exist = campusMapper.selectOne(
                Wrappers.<SysCampus>lambdaQuery()
                        .eq(SysCampus::getCampusName, req.getCampusName()));
        if (exist != null) {
            throw new BizException("校区名称已存在");
        }
        if (req.getCampusCode() != null && !req.getCampusCode().isBlank()) {
            SysCampus byCode = campusMapper.selectOne(
                    Wrappers.<SysCampus>lambdaQuery()
                            .eq(SysCampus::getCampusCode, req.getCampusCode()));
            if (byCode != null) {
                throw new BizException("校区编码已存在");
            }
        }
        SysCampus campus = new SysCampus();
        campus.setCampusName(req.getCampusName());
        campus.setCampusCode(req.getCampusCode());
        campus.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        campusMapper.insert(campus);
    }

    @Override
    @Transactional
    public void update(CampusUpdateReq req) {
        SysCampus campus = campusMapper.selectById(req.getCampusId());
        if (campus == null) {
            throw new BizException("校区不存在");
        }
        SysCampus byName = campusMapper.selectOne(
                Wrappers.<SysCampus>lambdaQuery()
                        .eq(SysCampus::getCampusName, req.getCampusName())
                        .ne(SysCampus::getCampusId, req.getCampusId()));
        if (byName != null) {
            throw new BizException("校区名称已存在");
        }
        if (req.getCampusCode() != null && !req.getCampusCode().isBlank()) {
            SysCampus byCode = campusMapper.selectOne(
                    Wrappers.<SysCampus>lambdaQuery()
                            .eq(SysCampus::getCampusCode, req.getCampusCode())
                            .ne(SysCampus::getCampusId, req.getCampusId()));
            if (byCode != null) {
                throw new BizException("校区编码已存在");
            }
        }
        campus.setCampusName(req.getCampusName());
        campus.setCampusCode(req.getCampusCode());
        campus.setSortOrder(req.getSortOrder());
        campusMapper.updateById(campus);
    }

    @Override
    @Transactional
    public void delete(Long campusId) {
        campusMapper.deleteById(campusId);
    }

    @Override
    @Transactional
    public Long findOrCreateByName(String campusName) {
        if (campusName == null || campusName.isBlank()) {
            return null;
        }
        SysCampus exist = campusMapper.selectOne(
                Wrappers.<SysCampus>lambdaQuery()
                        .eq(SysCampus::getCampusName, campusName));
        if (exist != null) {
            return exist.getCampusId();
        }
        SysCampus campus = new SysCampus();
        campus.setCampusName(campusName);
        campus.setSortOrder(0);
        campusMapper.insert(campus);
        return campus.getCampusId();
    }

    private CampusResp toResp(SysCampus campus) {
        CampusResp resp = new CampusResp();
        resp.setCampusId(campus.getCampusId());
        resp.setCampusName(campus.getCampusName());
        resp.setCampusCode(campus.getCampusCode());
        resp.setSortOrder(campus.getSortOrder());
        resp.setCreateTime(campus.getCreateTime());
        resp.setUpdateTime(campus.getUpdateTime());
        return resp;
    }
}
