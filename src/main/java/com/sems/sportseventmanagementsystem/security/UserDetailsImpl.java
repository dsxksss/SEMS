package com.sems.sportseventmanagementsystem.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sems.sportseventmanagementsystem.model.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Spring Security用户详情实现类
 */
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String username;
    @JsonIgnore
    private final String password;
    private final String email;
    private final String realName;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserDetailsImpl build(User user) {
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase());
        
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getRealName(),
                Collections.singletonList(authority)
        );
    }

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取用户邮箱
     *
     * @return 用户邮箱
     */
    public String getEmail() {
        return email;
    }

    /**
     * 获取用户真实姓名
     *
     * @return 用户真实姓名
     */
    public String getRealName() {
        return realName;
    }

    /**
     * 获取用户角色
     *
     * @return 用户角色
     */
    public String getRole() {
        if (authorities != null && !authorities.isEmpty()) {
            // 获取第一个权限，移除"ROLE_"前缀
            return authorities.iterator().next().getAuthority().replace("ROLE_", "");
        }
        return "USER"; // 默认角色
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
} 