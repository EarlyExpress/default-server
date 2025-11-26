package com.early_express.default_server.global.infrastructure.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class UserHeaderAuthenticationFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain)
			throws ServletException, IOException {

		String path = request.getRequestURI();
		String userId = request.getHeader("X-User-Id");
		String role = request.getHeader("X-User-Role");

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		// 이미 인증된 경우 아무것도 하지 않음
		if (auth != null && auth.isAuthenticated()) {
			filterChain.doFilter(request, response);
			return;
		}

		auth = null;

		// 1) 헤더 기반 인증
		if (userId != null) {
			auth = new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority(role)));
		}
		// 2) /signup 요청만 System 계정 인증 주입
		else if (path.endsWith("/signup")) {
			auth = new UsernamePasswordAuthenticationToken("시스템", null, Collections.emptyList());
		}
		// 3) 그 외는 익명
		else {
			auth = new UsernamePasswordAuthenticationToken("익명", null, Collections.emptyList());
		}

		SecurityContextHolder.getContext().setAuthentication(auth);
		filterChain.doFilter(request, response);
	}
}
