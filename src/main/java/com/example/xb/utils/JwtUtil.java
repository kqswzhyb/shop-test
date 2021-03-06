package com.example.xb.utils;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtil {

    /**
     * key（按照签名算法的字节长度设置key）
     */
    @Value("${token.secret}")
    private String SECRET_KEY;
    /**
     * 过期时间（毫秒单位）
     */
    @Value("${token.expireTime}")
    private long TOKEN_EXPIRE_MILLIS;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private  RedisCache redisCache;

    /**
     * 创建token
     *
     * @param claimMap
     * @return
     */
    public String createToken(Map<String, Object> claimMap) {
        long currentTimeMillis = System.currentTimeMillis();
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date(currentTimeMillis))    // 设置签发时间
                .setExpiration(new Date(currentTimeMillis + TOKEN_EXPIRE_MILLIS))   // 设置过期时间
                .addClaims(claimMap)
                .signWith(SignatureAlgorithm.HS256, generateKey())
                .compact();
    }

    /**
     * 验证token
     *
     * @param token
     * @return 0 验证成功，1、2、3、4、5 验证失败
     */
    public boolean verifyToken(String token) throws Exception {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(generateKey()).parseClaimsJws(token);
            Object userId=claims.getBody().get("user_id");
            Object userName=claims.getBody().get("user_name");
            String cache= redisCache.getCacheObject((String) userId);
            if(cache!=null) {
                Map<String, Object> map = this.parseToken(cache);
                return map.get("user_name").toString().equals(userName.toString());
            }else {
//                throw new Exception("Invalid JWT");
                return false;
            }
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            e.printStackTrace();
//            throw new Exception("Invalid JWT");
            return false;
        }
    }

    /**
     * 解析token
     *
     * @param token
     * @return
     */
    public Map<String, Object> parseToken(String token) {
        return Jwts.parser()  // 得到DefaultJwtParser
                .setSigningKey(generateKey()) // 设置签名密钥
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 生成安全密钥
     *
     * @return
     */
    public Key generateKey() {
        return new SecretKeySpec(SECRET_KEY.getBytes(), SignatureAlgorithm.HS256.getJcaName());
    }

    /**
     * 获取请求中的token
     *
     * @param req
     * @return
     */
    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }

    public Authentication getAuthentication(String token) {
        String jti = getJti(token);
        String userId=getUserId(token);
        String cache= redisCache.getCacheObject(userId);
        String username=null;
        Map<String, Object> map=new HashMap<>();
        if(cache!=null) {
            map = this.parseToken(cache);
            username = map.get("jti").toString().equals(jti)?getUsername(token):null;
        }else {
            return null;
        }
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(map.get("user_id").toString());
        return new UsernamePasswordAuthenticationToken(userDetails, "", AuthorityUtils.commaSeparatedStringToAuthorityList("0".equals(map.get("role_id").toString())?"ROLE_admin":null));
    }

    public String getUsername(String token) {
        Jws<Claims> claims = Jwts.parser().setSigningKey(generateKey()).parseClaimsJws(token);
        return (String) claims.getBody().get("user_name");
    }

    public String getUserId(String token) {
        Jws<Claims> claims = Jwts.parser().setSigningKey(generateKey()).parseClaimsJws(token);
        return (String) claims.getBody().get("user_id");
    }
    public String getJti(String token) {
        Jws<Claims> claims = Jwts.parser().setSigningKey(generateKey()).parseClaimsJws(token);
        return (String) claims.getBody().get("jti");
    }
    public String getUserId(HttpServletRequest req) {
        String token = resolveToken(req);
        Map<String, Object> map = parseToken(token);
        return (String) map.get("user_id");
    }

    public String getJwtUserId() {
       return ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
    }

}
