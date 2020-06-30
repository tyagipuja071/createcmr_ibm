/**
 * 
 */
package com.ibm.cio.cmr.request.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;

/**
 * @author Jeffrey Zamora
 * 
 */
@WebFilter(urlPatterns = "/*")
public class ChangeLogCleanerFilter implements Filter {

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    ChangeLogListener.clean();

    HttpServletRequest req = (HttpServletRequest) request;
    String uri = req.getRequestURI();
    if (uri.endsWith("/")) {
      String intermediate = uri.substring(0, uri.lastIndexOf("/")); // remove
                                                                    // last /
      intermediate = intermediate.substring(1); // remove context path
      intermediate = intermediate.substring(intermediate.indexOf("/"));
      System.err.println("forward to: " + intermediate);
      request.getRequestDispatcher(intermediate).forward(request, response);
      return;
    }
    filterChain.doFilter(request, response);
  }

  @Override
  public void init(FilterConfig arg0) throws ServletException {
  }

}
