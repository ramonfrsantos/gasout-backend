package br.com.gasoutapp.infrastructure.config.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    public static final String SECRET = "gasoutapp";
    private static final String HEADER_STRING = "Authorization";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var optionalToken = getToken(request);
        optionalToken.ifPresent(token -> {
            token = token.replace("Bearer ", "");
            token = CriptexCustom.decrypt(token);
            Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();

            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));

            var usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
            usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        });
        filterChain.doFilter(request, response);
    }

    protected Optional<String> getToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HEADER_STRING));
    }

}