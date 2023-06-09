package com.studygroup.util.cookie;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.WebUtils;

@Slf4j
@Builder
public class CookieUtil {

  private CookieUtil(){

  }
  public static Cookie defaultJwtTokenCookie(String value) {
    return CookieBuilder.defaultBuilder("jwtToken", value).defaultConstructorWithOtherDefault().build();
  }

  public static Cookie create(String name, String value, Integer maxAge, String domain) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setMaxAge(maxAge);
    cookie.setDomain(domain);
    cookie.setPath("/");
    return cookie;
  }

  public static void clear(HttpServletResponse httpServletResponse, String name) {
    Cookie cookie = new Cookie(name, null);
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    cookie.setMaxAge(0);
    cookie.setDomain("localhost");
    httpServletResponse.addCookie(cookie);
  }

  public static String getValue(HttpServletRequest httpServletRequest, String name) {
    Cookie cookie = WebUtils.getCookie(httpServletRequest, name);
    return cookie != null ? cookie.getValue() : null;
  }

}
