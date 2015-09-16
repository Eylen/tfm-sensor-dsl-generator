package com.eylen.sensordsl.generator.permission

import com.eylen.sensordsl.Permission

/**
 * Created by Saioa on 16/09/2015.
 */
class IOSPermissionCodeGenerator implements  PermissionCodeGenerator{
    @Override
    List<String> generateCode(Set<Permission> permissionList, File file) {

    }

    @Override
    int getPermissionsStart(File file) {
        return 0
    }
}
