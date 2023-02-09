package com.whitedisk.white_disk.config.security.manager;

import com.aliyun.oss.HttpMethod;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author white
 * 用户权限和url所需要的权限进行对比
 */
@Component
public class UrlAccessDecisionManager implements AccessDecisionManager {
    @Override
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException {
        FilterInvocation filterInvocation= (FilterInvocation) object;
        if (HttpMethod.OPTIONS.name().equals(filterInvocation.getRequest().getMethod())){
            return;
        }
        //configAttributes是url给过来的权限列表
        for (ConfigAttribute configAttribute : configAttributes) {
            //看是否有用户信息
            if(!"ROLE_ANONYMOUS".equals(configAttribute.toString())){
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (principal == null) {
                    throw new AccessDeniedException("Expire");
                }
                //判断权限是否足够
                Collection<? extends GrantedAuthority> authorities=authentication.getAuthorities();
                for (GrantedAuthority authority : authorities) {
                    //authority.getAuthority()是jwt过滤的权限
                    //configAttribute.getAttribute()是url中所需要的权限
                    if(authority.getAuthority().equals(configAttribute.getAttribute())){
                        return;
                    }
                }
                throw new AccessDeniedException("not allow to access");
            }
        }
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }
}
