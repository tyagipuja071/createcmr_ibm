/**
 * 
 */
package com.ibm.cio.cmr.request.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.ibm.cio.cmr.request.util.SystemParameters;

/**
 * @author 136786PH1
 *
 */
public class SecurityFilter implements Filter {

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filter) throws IOException, ServletException {
    HttpServletResponse httpResp = (HttpServletResponse) resp;
    boolean skipContentSec = "N".equals(SystemParameters.getString("SEC.HEADER.CONSEC"));
    if (!skipContentSec) {
      String sources = SystemParameters.getString("SEC.HEADER.SOURCES");
      String policy = "1.w3.s81c.com 1.www.s81c.com www.ibm.com ajax.googleapis.com";
      if (!StringUtils.isBlank(sources)) {
        policy = sources;
      }
      httpResp.addHeader("Content-Security-Policy", "'self' " + policy + ", frame-ancestors 'self'");
    }
    httpResp.addHeader("X-Content-Type-Options", "nosniff");
    filter.doFilter(req, resp);
  }

}
