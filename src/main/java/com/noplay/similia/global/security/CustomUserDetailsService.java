package com.noplay.similia.global.security;

import com.noplay.similia.user.domain.Member;
import com.noplay.similia.user.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Here, username is actually the memberId extracted from the JWT
        Long memberId = Long.valueOf(username);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다. id: " + memberId));

        return new CustomUserDetails(member);
    }
}
