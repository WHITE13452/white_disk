package com.whitedisk.white_disk.config.security.filter;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author white
 */
@Component
public class UrlFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {
    /**
     * 在用户发出请求时，根据请求的url查出该url需要哪些权限才能访问，并将所需权限给SecurityConfig
     */
    @Override
    public Collection<ConfigAttribute> getAttributes(Object o) throws IllegalArgumentException {
        // 获取 请求 url 地址
//        String requestUrl = ((FilterInvocation) o).getRequestUrl();
//        // 得到所请求的 url 和 资源权限 的对应关系（这里可以用缓存处理）
//        SysResource resource = sysResourceService.getPermissionByUrl(requestUrl);
//        if (resource != null && resource.getVerification() == 1) {
//            return SecurityConfig.createList(resource.getPermission());
//        } else {
        // 如果都没有匹配上，我们返回默认值，这个值就像一个特殊的标识符，自定义，在UrlAccessDecisionManager中自定义规则即可
        return SecurityConfig.createList("ROLE_ANONYMOUS");
//        }
//        return null;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
