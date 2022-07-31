package com.offcn.oauth;

import org.junit.jupiter.api.Test;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

class ParseJwtTest {

    @Test
    void testParseToken() {
        //令牌
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6ImFkbWluLHVzZXIiLCJuYW1lIjoibG9uZ2NoZW4iLCJpZCI6IjEifQ.PgGEMLPLs6hW_v6yz_Ni0AEzwCKcBi01dquF4aP0TLPinQKJn2ExxsFo6uaDTeH_hXa8RuC0XgZzsQ2oGMCcHn-b6uyL0oeWBwRBNxT6UHqa2_JucbrLSo-gyuVnF7eCeRIgvxxVrfWBoqt8-3WM2RIxudIUzVENMBJr3hPOo1InhQz5UyXFrysqM1YDwPdBe4jH_-MXaoaSAY4Yt-Nk7bJtulmko7G6U0sbF9uh58kePyQxzgkIzNYHtuRjySf80hFwADvazFtiDLfDPIIF1JYBu6dSZ9wjEosMXV4AelQNZKA1PmN8aOZWPQ_6kUPvdr-UTSolXHLKT_6snBHHZQ";
        //公钥
        String publicKey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxMrBJQWLhuvCh6yhTCoaDVJgJG6WrHB0dX3vqGjAd6gRb/9+DrizWOS5NBnYzqfv+PUv+k+jE1x1SnThUUboE3MLBR1PwuvtSesJAqFP6dxEY5+tuTcyFw6riXKAm+nvs6xUdc70bwAtKa4sIbfR5ytx8rB6bTaI1B/YCiPOMTDutdi2xzT89eNxVmYO/s+5uXAYx+R26I6Xk+ccuHTRrsM6nghq/fxfDlgWZGQDmI9jkK2qG1mrnwHeeT7OP8zrXYEC4EnYuDRmmlpr3IrZDCmuR2b1teQO7wL36F1MkbunYarUo7LT5ZOYkDY54kZDo+JiDNZw7+IHilL8hhfggwIDAQAB-----END Warning:PUBLIC KEY-----";
        //解析密文
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publicKey));
        String claims = jwt.getClaims();
        System.out.println(claims);
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }
}
