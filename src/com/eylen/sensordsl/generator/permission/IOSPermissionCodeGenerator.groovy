package com.eylen.sensordsl.generator.permission

import com.eylen.sensordsl.Permission
import com.eylen.sensordsl.generator.utils.Constants
import groovy.text.SimpleTemplateEngine

class IOSPermissionCodeGenerator implements  PermissionCodeGenerator{
    private List<String> permissions;
    private SimpleTemplateEngine templateEngine

    public IOSPermissionCodeGenerator(){
        this.permissions = new ArrayList<>()
        this.templateEngine = new SimpleTemplateEngine()
    }

    @Override
    List<String> generateCode(Set<Permission> permissionList, File file) {
        String templateDirPath = Constants.TEMPLATES + "/ios/permission/"
        permissionList.each {permission->
            String permissionString = ""
            switch (permission){
                case Permission.FINE_LOCATION:
                case Permission.COARSE_LOCATION:
                    permissionString = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "location_permission.template"))).make().toString()
                    break
            }
            if (permissionString) {
                String permissionName = permissionString.substring(permissionString.indexOf("<key>") + 5)
                permissionName = permissionName.substring(0, permissionName.indexOf("</key>"))
                println "permissionName = $permissionName"
                if (!permissionExist(file, permissionName)) {
                    permissionString.eachLine {
                        permissions << it
                    }
                }
            }
        }
        return permissions
    }

    @Override
    int getPermissionsStart(File file) {
        return file.text.indexOf("<key>")
    }

    boolean permissionExist(File file, String permissionName) {
        return file.text.contains(permissionName)
    }
}
