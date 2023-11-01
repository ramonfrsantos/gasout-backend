package br.com.gasoutapp.config.security;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;

import br.com.gasoutapp.domain.user.User;

@Service
public class TokenService {

	final int HORAS_TIMEOUT = 2;
	
	@Value("{api.security.token.secret}")
	private String secret;

	public String generateToken(User user) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			
			String token = JWT.create()
					.withIssuer("auth-api")
					.withSubject(user.getLogin())
					.withExpiresAt(generateExpirationDate())
					.sign(algorithm);
			
			return token;
		} catch(JWTCreationException ex) {
			throw new RuntimeException("Error while generating token", ex);
		}
	}
	
	public String validateToken(String token) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			return JWT.require(algorithm)
					.withIssuer("auth-api")
					.build()
					.verify(token)
					.getSubject();
		} catch(JWTVerificationException ex) {
			return "";
		}
	}
	
	private Instant generateExpirationDate() {
		return LocalDateTime.now().plusHours(HORAS_TIMEOUT).toInstant(ZoneOffset.of("-03:00"));
	}

	
}
