/**
 * 
 */
package com.ibm.cio.cmr.request.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ibm.cio.cmr.request.model.TypeaheadModel;
import com.ibm.cio.cmr.request.service.TypeaheadService;

/**
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class TypeaheadController {

  @Autowired
  private TypeaheadService service;

  /**
   * Gets the client messages mapped to the configuration
   * 
   * @param request
   * @param response
   * @return
   * @throws IOException
   */
  @RequestMapping(value = "/typeahead/{id}")
  public void getClientMessages(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
    List<TypeaheadModel> msgs = service.getSuggestions(id, request.getParameter("term"));
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(msgs);
    response.getOutputStream().write(json.getBytes());
  }

}
