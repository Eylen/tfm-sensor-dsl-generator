package com.eylen.sensordsl.generator.permission

import com.eylen.sensordsl.Permission
import com.eylen.sensordsl.generator.ICodeGenerator

interface PermissionCodeGenerator{
    public List<String> generateCode(Set<Permission> permissionList, File file)

    public int getPermissionsStart(File file)
}
