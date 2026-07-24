package com.example.how2prompt.modules.identity.entity;

/**
 * Khớp CHECK constraint `role IN ('owner','admin','editor','viewer')` ở
 * workspace_members.role (US-9.2 RBAC — schema đã sẵn từ Phase 1 dù RBAC đầy đủ là
 * Phase 4, workspace cá nhân chỉ dùng OWNER).
 */
public enum WorkspaceRole {
    OWNER,
    ADMIN,
    EDITOR,
    VIEWER
}
