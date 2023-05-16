package com.ibm.cio.cmr.request.filter;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.user.AppUser;

/**
 * To redirect to previous URI when necessary.
 * 
 * @author Hesller Huller
 *
 */
@Component
@WebFilter(filterName = "RedirectPreviousURIFilter", urlPatterns = "/*")
public class RedirectPreviousURIFilter implements Filter {

  FilterChain filterChain;

  protected static final Logger LOG = Logger.getLogger(RedirectPreviousURIFilter.class);

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

    HttpServletRequest httpReq = (HttpServletRequest) request;
    HttpServletResponse httpResp = (HttpServletResponse) response;

    AppUser user = AppUser.getUser(httpReq);

    if (user == null) {
      filterChain.doFilter(request, response);
      return;
    }

    if (!redirectToPreviousURI(httpReq, httpResp)) {
      filterChain.doFilter(request, response);
    }

  }

  private boolean redirectToPreviousURI(HttpServletRequest httpReq, HttpServletResponse httpResp) {
    return executeRedirect().andThen(clearPreviousURIFromSession(httpReq)).apply(getPreviousURI(httpReq), httpResp);
  }

  private BiFunction<String, HttpServletResponse, Boolean> executeRedirect() {
    return (previousURI, httpResp) -> {
      if (filterPreviousURI(previousURI)) {
        try {
          httpResp.sendRedirect(previousURI);
          return true;
        } catch (IOException e) {
          // TODO Auto-generated catch block
          return false;
        }
      }
      return false;
    };
  }

  private Boolean filterPreviousURI(String previousURI) {
    if (StringUtils.isBlank(previousURI)) {
      return false;
    }

    if (previousURI.matches("(.*)/request/[0-9]+(.*)")) {
      return true;
    }

    if (previousURI.matches("(.*)/massrequest/[0-9]+(.*)")) {
      return true;
    }

    if (previousURI.matches("(.*)CreateCMR/approval/(.*)")) {
      return true;
    }

    return false;
  }

  private Function<Boolean, Boolean> clearPreviousURIFromSession(HttpServletRequest request) {
    return value -> {
      if (value) {
        request.getSession().removeAttribute("previousURI");
        return true;
      }
      return false;
    };
  }

  private String getPreviousURI(HttpServletRequest httpReq) {
    return (String) httpReq.getSession().getAttribute("previousURI");
  }

}
