package fi.thl.termed.web.dump;

import static com.google.common.base.Charsets.UTF_8;
import static fi.thl.termed.util.service.SaveMode.saveMode;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Dump;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.UrlWithCredentials;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/dump", "/api/restore"})
public class DumpWriteFromRemoteController {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private Gson gson;

  @Autowired
  private Service<ImmutableSet<GraphId>, Dump> dumpService;

  private CloseableHttpClient httpClient = HttpClientBuilder.create().build();

  @PostJsonMapping(produces = {}, params = "remote=true")
  @ResponseStatus(NO_CONTENT)
  public void restoreRemote(@RequestBody UrlWithCredentials remote,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) throws IOException {

    if (user.getAppRole() == AppRole.ADMIN || user.getAppRole() == AppRole.SUPERUSER) {
      HttpGet request = new HttpGet(remote.getUrl());
      request.addHeader(ACCEPT, APPLICATION_JSON_UTF8_VALUE);
      request.addHeader(AUTHORIZATION, basicAuth(remote.getUsername(), remote.getPassword()));

      log.info("Downloading {} as {}", remote.getUrl(), remote.getUsername());

      try (CloseableHttpResponse response = httpClient.execute(request)) {
        Dump dump = gson.fromJson(
            new InputStreamReader(response.getEntity().getContent(), UTF_8), Dump.class);

        log.info("Restoring");

        dumpService.save(dump, saveMode(mode), opts(sync), user);
      }

      log.info("Done");
    }
  }

  private String basicAuth(String username, String password) {
    return "Basic " + encodeBase64(username + ":" + password);
  }

  private String encodeBase64(String str) {
    return Base64.getEncoder().encodeToString(str.getBytes(UTF_8));
  }

}
