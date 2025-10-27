package com.lgcns.studify_be.security;

import com.lgcns.studify_be.user.User;
import com.lgcns.studify_be.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 비밀번호는 passwordHash 사용
        return new org.springframework.security.core.userdetails.User(
                u.getEmail(),          // 아이디(식별자)
                u.getPasswordHash(),   // 해시된 비밀번호
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
