package com.eylen.sensordsl.generator.permission

import com.eylen.sensordsl.Permission
import com.eylen.sensordsl.generator.utils.Constants
import groovy.text.SimpleTemplateEngine

class AndroidPermissionCodeGenerator implements PermissionCodeGenerator{
    private List<String> permissions;
    private SimpleTemplateEngine templateEngine

    public AndroidPermissionCodeGenerator(){
        this.permissions = new ArrayList<>()
        this.templateEngine = new SimpleTemplateEngine()
    }

    @Override
    List<String> generateCode(Set<Permission> permissionList, File file) {
        String templateDirPath = Constants.TEMPLATES + "/android/permission/"
        permissionList.each {permission->
            String permissionString
            switch (permission){
                case Permission.CAMERA:
                    permissionString = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "camera_permission.template"))).make().toString()

                    break
                case Permission.VIDEO:
                    permissionString = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "video_permission.template"))).make().toString()
                    break
                case Permission.STORE:
                    permissionString = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "store_permission.template"))).make().toString()
                    break
                case Permission.FINE_LOCATION:
                case Permission.COARSE_LOCATION:
                    permissionString = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "location_permission.template"))).make(fine:permission==Permission.FINE_LOCATION).toString()
                    break
            }
            if (permissionString) {
                String permissionName = permissionString.substring(permissionString.indexOf("android:name=\"") + 14)
                permissionName = permissionName.substring(0, permissionName.indexOf("\""))
                if (!permissionExist(file, permissionName))
                    permissions << permissionString
            }
        }
        return permissions
    }

    @Override
    int getPermissionsStart(File file) {
        return file.text.indexOf("<uses-permission")
    }

    private boolean permissionExist(File file, String permissionName){
        return file.text.contains(permissionName)
    }


}
