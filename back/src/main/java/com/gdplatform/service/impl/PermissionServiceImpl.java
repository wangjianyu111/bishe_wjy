package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.MenuNode;
import com.gdplatform.dto.PermissionAddReq;
import com.gdplatform.dto.PermissionTreeNode;
import com.gdplatform.dto.PermissionUpdateReq;
import com.gdplatform.entity.SysPermission;
import com.gdplatform.mapper.SysPermissionMapper;
import com.gdplatform.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final SysPermissionMapper sysPermissionMapper;

    @Override
    public List<String> listPermCodes(Long userId) {
        return sysPermissionMapper.selectPermCodesByUserId(userId);
    }

    @Override
    public List<MenuNode> buildMenuTree(Long userId) {
        List<SysPermission> all = sysPermissionMapper.selectPermissionsByUserId(userId);
        List<SysPermission> menus = all.stream()
                .filter(p -> p.getPermType() != null && (p.getPermType() == 1 || p.getPermType() == 2))
                .filter(p -> p.getVisible() == null || p.getVisible() == 1)
                .sorted(Comparator.comparing(SysPermission::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(SysPermission::getPermId))
                .toList();

        List<MenuNode> nodes = menus.stream().map(p -> {
            MenuNode n = new MenuNode();
            BeanUtils.copyProperties(p, n);
            return n;
        }).toList();

        Map<Long, MenuNode> map = nodes.stream().collect(Collectors.toMap(MenuNode::getPermId, n -> n));
        List<MenuNode> roots = new ArrayList<>();
        for (MenuNode n : nodes) {
            Long pid = n.getParentId() == null ? 0L : n.getParentId();
            if (pid == 0L) {
                roots.add(n);
            } else {
                MenuNode parent = map.get(pid);
                if (parent != null) {
                    parent.getChildren().add(n);
                } else {
                    roots.add(n);
                }
            }
        }
        sortMenuChildren(roots);
        return roots;
    }

    private void sortMenuChildren(List<MenuNode> list) {
        list.sort(Comparator.comparing(MenuNode::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(MenuNode::getPermId));
        for (MenuNode n : list) {
            if (!n.getChildren().isEmpty()) {
                sortMenuChildren(n.getChildren());
            }
        }
    }

    @Override
    public Page<SysPermission> pagePermissions(long current, long size, String keyword, Integer permType) {
        Page<SysPermission> page = new Page<>(current, size);
        var q = Wrappers.<SysPermission>lambdaQuery();
        if (keyword != null && !keyword.isBlank()) {
            q.and(w -> w.like(SysPermission::getPermName, keyword)
                    .or().like(SysPermission::getPermCode, keyword));
        }
        if (permType != null) {
            q.eq(SysPermission::getPermType, permType);
        }
        q.orderByDesc(SysPermission::getCreateTime);
        return sysPermissionMapper.selectPage(page, q);
    }

    @Override
    public List<PermissionTreeNode> getPermissionTree() {
        List<PermissionTreeNode> all = sysPermissionMapper.selectAllAsTree();
        return buildTree(all);
    }

    @Override
    public List<SysPermission> getPermissionList() {
        return sysPermissionMapper.selectAllList();
    }

    @Override
    @Transactional
    public SysPermission add(PermissionAddReq req) {
        SysPermission p = new SysPermission();
        p.setParentId(req.getParentId() == null ? 0L : req.getParentId());
        p.setPermName(req.getPermName());
        p.setPermCode(req.getPermCode());
        p.setPermType(req.getPermType());
        p.setPath(req.getPath());
        p.setComponent(req.getComponent());
        p.setIcon(req.getIcon());
        p.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        p.setVisible(req.getVisible() == null ? 1 : req.getVisible());
        sysPermissionMapper.insert(p);
        return p;
    }

    @Override
    @Transactional
    public SysPermission update(PermissionUpdateReq req) {
        SysPermission p = sysPermissionMapper.selectById(req.getPermId());
        if (p == null) {
            throw new BizException("权限不存在");
        }
        p.setParentId(req.getParentId() == null ? 0L : req.getParentId());
        p.setPermName(req.getPermName());
        p.setPermCode(req.getPermCode());
        p.setPermType(req.getPermType());
        p.setPath(req.getPath());
        p.setComponent(req.getComponent());
        p.setIcon(req.getIcon());
        p.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        p.setVisible(req.getVisible() == null ? 1 : req.getVisible());
        sysPermissionMapper.updateById(p);
        return p;
    }

    @Override
    @Transactional
    public void delete(Long permId) {
        long childrenCount = sysPermissionMapper.selectCount(
                Wrappers.<SysPermission>lambdaQuery().eq(SysPermission::getParentId, permId));
        if (childrenCount > 0) {
            throw new BizException("请先删除子权限");
        }
        sysPermissionMapper.deleteById(permId);
    }

    private List<PermissionTreeNode> buildTree(List<PermissionTreeNode> all) {
        Map<Long, PermissionTreeNode> map = all.stream()
                .collect(Collectors.toMap(PermissionTreeNode::getPermId, n -> n));
        List<PermissionTreeNode> roots = new ArrayList<>();
        for (PermissionTreeNode n : all) {
            Long pid = n.getParentId() == null ? 0L : n.getParentId();
            if (pid == 0L) {
                roots.add(n);
            } else {
                PermissionTreeNode parent = map.get(pid);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(n);
                } else {
                    roots.add(n);
                }
            }
        }
        sortChildren(roots);
        return roots;
    }

    private void sortChildren(List<PermissionTreeNode> list) {
        list.sort(Comparator.comparing(PermissionTreeNode::getSortOrder,
                Comparator.nullsLast(Integer::compareTo)));
        for (PermissionTreeNode n : list) {
            if (n.getChildren() != null && !n.getChildren().isEmpty()) {
                sortChildren(n.getChildren());
            }
        }
    }
}
