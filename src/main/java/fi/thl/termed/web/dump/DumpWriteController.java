package fi.thl.termed.web.dump;

import static fi.thl.termed.util.service.SaveMode.saveMode;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import fi.thl.termed.domain.Dump;
import fi.thl.termed.domain.DumpId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/dump", "/api/restore"})
public class DumpWriteController {

  @Autowired
  private Service<DumpId, Dump> dumpService;

  @PostJsonMapping(produces = {})
  @ResponseStatus(NO_CONTENT)
  public void restore(@RequestBody Dump dump,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "generateCodes", defaultValue = "false") boolean generateCodes,
      @RequestParam(name = "generateUris", defaultValue = "false") boolean generateUris,
      @AuthenticationPrincipal User user) {
    dumpService.save(dump, saveMode(mode), opts(sync, generateCodes, generateUris), user);
  }

}
