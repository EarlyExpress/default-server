package com.early_express.default_server.global.infrastructure.persistence;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

	@Override
	public Optional<String> getCurrentAuditor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// JWT 인증일 때
		if (authentication instanceof JwtAuthenticationToken jwtAuth) {
			String userId = jwtAuth.getToken().getClaim("user_id");
			return Optional.ofNullable(userId);
		}

		// 커스텀 필터일 때 (헤더에서 user-id 가져오기)
		Object principal = authentication.getPrincipal();
		if (principal instanceof String p) {
			return Optional.of(p);
		}

		// JWT claim도 없고 principal도 문자열이 아닐 때
		return Optional.empty();
	}
}
