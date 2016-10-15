package fi.thl.termed.domain.function;

import java.util.function.Function;

import java.util.UUID;

import fi.thl.termed.domain.SchemeRole;

public class SchemeRoleToSchemeId implements Function<SchemeRole, UUID> {

  @Override
  public UUID apply(SchemeRole schemeRole) {
    return schemeRole.getSchemeId();
  }

}
