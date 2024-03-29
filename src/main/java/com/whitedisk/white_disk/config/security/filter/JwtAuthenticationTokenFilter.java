package com.whitedisk.white_disk.config.security.filter;

import com.qiwenshare.common.exception.QiwenException;
import com.whitedisk.white_disk.service.impl.SysParamServiceImpl;
import com.whitedisk.white_disk.service.impl.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author white
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Resource
    private SysParamServiceImpl sysParamService;

    @Value("${white.disk.version}")
    String whiteDiskVersion;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String version = sysParamService.getValue("version");
        if (!whiteDiskVersion.equals(version)) {
            throw new QiwenException(999999, "脚本未初始化，请在数据库执行数据初始化脚本，存放路径： '/resources/import.sql'！");
        }

        String token = request.getHeader("token");
        if (StringUtils.isNotBlank(token) && !"undefined".equals(token)) {

            String userId = userService.getUserIdByToken(token);

            // 验证
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userService.loadUserByUsername(String.valueOf(userId));
                if (userDetails.isEnabled()) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
        }
        chain.doFilter(request, response);
    }
}
