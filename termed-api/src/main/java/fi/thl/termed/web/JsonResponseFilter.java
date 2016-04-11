package fi.thl.termed.web;

import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import fi.thl.termed.util.JsonUtils;

/**
 * Not very efficient HTTP Response filter for debugging JSON data. For any request returning
 * application/json, checks if request parameter "json.filter.key" has values. If it has values,
 * retains only matching fields from returned JSON objects.
 */
public class JsonResponseFilter implements Filter {

  private Gson gson = new GsonBuilder().setPrettyPrinting().create();
  private JsonParser jsonParser = new JsonParser();

  public void init(FilterConfig filterConfig) throws ServletException {
  }

  public void destroy() {
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    OutputStream out = response.getOutputStream();
    ResponseWrapper wrappedResponse = new ResponseWrapper((HttpServletResponse) response);

    chain.doFilter(request, wrappedResponse);

    String contentType = Strings.nullToEmpty(wrappedResponse.getContentType());
    String keyFilter = Strings.nullToEmpty(request.getParameter("json.filter.key"));

    if (contentType.startsWith("application/json") && !keyFilter.isEmpty()) {

      JsonElement jsonElement = jsonParser.parse(wrappedResponse.toString());
      JsonElement filteredJsonElement =
          JsonUtils.filterKeys(jsonElement,
                               Predicates.in(Arrays.asList(keyFilter.replace(" ", "").split(","))));

      out.write(gson.toJson(filteredJsonElement).getBytes());
    } else {
      out.write(wrappedResponse.toString().getBytes());
    }

  }

  private static class ResponseWrapper extends HttpServletResponseWrapper {

    private ByteArrayOutputStream output;

    public ResponseWrapper(HttpServletResponse response) {
      super(response);
      output = new ByteArrayOutputStream();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
      return new ByteArrayServletOutputStream(output);
    }

    public String toString() {
      return output.toString();
    }

  }

  private static class ByteArrayServletOutputStream extends ServletOutputStream {

    private ByteArrayOutputStream outputStream;

    private ByteArrayServletOutputStream(ByteArrayOutputStream outputStream) {
      this.outputStream = outputStream;
    }

    public void write(int param) throws IOException {
      outputStream.write(param);
    }

    @Override
    public boolean isReady() {
      return false;
    }

    @Override
    public void setWriteListener(WriteListener listener) {
    }

  }

}
