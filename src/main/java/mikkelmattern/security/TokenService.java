package mikkelmattern.security;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.Set;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import mikkelmattern.DTOs.UserOutDTO;
import mikkelmattern.exceptions.ApiException;

/**
 * Creates and verifies JWTs signed with HMAC-SHA256.
 * <p>
 * Tokens hold the {@link UserOutDTO} (username as the subject, plus {@code "id"} and
 * {@code "email"} claims) and the user's roles as a comma-separated {@code "roles"} claim,
 * and expire after 30 minutes.
 */
public class TokenService {

    private static final long EXPIRY_MILLIS = 30 * 60 * 1000;   // 30 minutes
    private static final String ISSUER = "guide-api";

    private final byte[] secret;

    /**
     * Creates a token service that signs and verifies tokens with the given secret.
     *
     * @param secret the signing secret, at least 32 characters long
     * @throws IllegalStateException if the secret is {@code null} or shorter than 32 characters
     */
    public TokenService(String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("Token secret must be at least 32 characters");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Creates a signed JWT for a user.
     * <p>
     * The token can be decoded by anyone holding it, so the DTO must never contain secrets.
     *
     * @param user  the user to put in the token: the username becomes the subject,
     *              and the id and email become the {@code "id"} and {@code "email"} claims
     * @param roles the user's role names, stored as a comma-separated {@code "roles"} claim
     * @return the serialized token, ready to send to the client
     * @throws ApiException with status 500 if the token could not be signed
     */
    public String createToken(UserOutDTO user, Set<String> roles) {
        try {
            Date now = new Date();
            // Builds claim
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.username())
                    .issuer(ISSUER)
                    .claim("id", user.id())
                    .claim("email", user.email())
                    .claim("roles", String.join(",", roles))
                    .issueTime(now)
                    .expirationTime(new Date(now.getTime() + EXPIRY_MILLIS))
                    .build();

            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(secret));
            return jwt.serialize();

        } catch (JOSEException e) {
            throw new ApiException(500, "Could not create token");
        }
    }

    /**
     * Verifies a JWT's signature and expiry and reads the user from its claims.
     *
     * @param token the serialized JWT from the Authorization header
     * @return the user the token belongs to, with their roles
     * @throws ApiException with status 401 if the token can't be parsed, has a bad signature
     *                      or has expired
     */
    public AuthUser verify(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);

            if (!jwt.verify(new MACVerifier(secret))) {
                throw new ApiException(401, "Invalid token");
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date expiry = claims.getExpirationTime();
            if (expiry == null || expiry.before(new Date())) {
                throw new ApiException(401, "Token has expired");
            }

            String rolesClaim = claims.getStringClaim("roles");

            Set<String> roles = (rolesClaim == null || rolesClaim.isBlank())
                    ? Set.of()
                    : Set.of(rolesClaim.split(","));
            return new AuthUser(
                    claims.getLongClaim("id"),
                    claims.getSubject(),
                    claims.getStringClaim("email"),
                    roles);
        } catch (ParseException | JOSEException e) {
            throw new ApiException(401, "Invalid token");
        }
    }


}
