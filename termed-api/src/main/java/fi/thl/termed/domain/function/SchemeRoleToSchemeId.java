package fi.thl.termed.domain.function;

import com.google.common.base.Function;

import java.util.UUID;

import fi.thl.termed.domain.SchemeRole;

public class SchemeRoleToSchemeId implements Function<SchemeRole, UUID> {

  @Override
  public UUID apply(SchemeRole schemeRole) {
    return schemeRole.getSchemeId();
  }

}
