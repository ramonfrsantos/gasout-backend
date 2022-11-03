package br.com.gasoutapp.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
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
    private static final String CABECALHO_DE_AUTORIZACAO_INVALIDO = "Authorization header must be provided";

    @SuppressWarnings("unused")
    private List<String> publicPaths = Arrays.asList(
            "oauth",
            "/webjars",
            "/health",
            "/partners/me",
            "/me",
            "/auth",
            "/user"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("#login security filter");
        Optional<String> optionalToken = getToken((HttpServletRequest) request);
        optionalToken.ifPresent(token -> {

            token = token.replace("Bearer ", "");
            token = CriptexCustom.decrypt(token);
            Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();

            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);
            List<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();

            roles.forEach(role -> {
                authorities.add(new SimpleGrantedAuthority(role));
            });

            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
            usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            System.out.println("#login autenticado");
        });
        System.out.println("#login não autenticado");
        filterChain.doFilter(request, response);
    }

    protected Optional<String> getToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HEADER_STRING));
    }

    protected void sendHttpForbidden(ServletResponse response) throws IOException {
        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, CABECALHO_DE_AUTORIZACAO_INVALIDO);
    }

    @SuppressWarnings("unused")
    private boolean anyMatch(List<String> paths, String fullPath) {
        return paths.stream().anyMatch(fullPath::contains);
    }
}