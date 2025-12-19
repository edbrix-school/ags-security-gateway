package com.asg.security.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class MenuItemDto {

    private String menuId;
    private String menuName;
    private Integer menuLevel;
    private String menuGroup;
    private String taskflowUrl;
    private String userId;
    private String docType;
    private String moduleId;
    private String hideInMainMenu;
    private List<MenuItemDto> children = new ArrayList<>(); // ✅ Prevents null

}
