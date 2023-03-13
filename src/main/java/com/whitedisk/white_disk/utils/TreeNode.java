package com.whitedisk.white_disk.utils;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author white
 * 树节点
 */
@Data
public class TreeNode {
    /**
     * 节点id
     */
    private Long id;
    /**
     * 节点名
     */
    private String label;
    /**
     * 深度
     */
    private Long depth;
    /**
     * 是否被关闭
     */
    private String state = "closed";

    private String filePath = "/";

    /**
     * 子节点列表
     */
    private List<TreeNode> children = new ArrayList<>();
}
