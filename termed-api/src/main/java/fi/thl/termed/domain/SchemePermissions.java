package fi.thl.termed.domain;

import java.util.List;

public class SchemePermissions {

  private List<Permission> permissions;
  private List<ClassPermissions> classPermissions;

  public List<Permission> getPermissions() {
    return permissions;
  }

  public List<ClassPermissions> getClassPermissions() {
    return classPermissions;
  }

  public class ClassPermissions {

    private String id;
    private List<Permission> permissions;
    private List<TextAttributePermissions> textAttributePermissions;
    private List<ReferenceAttributePermissions> referenceAttributePermissions;

    public String getId() {
      return id;
    }

    public List<Permission> getPermissions() {
      return permissions;
    }

    public List<TextAttributePermissions> getTextAttributePermissions() {
      return textAttributePermissions;
    }

    public List<ReferenceAttributePermissions> getReferenceAttributePermissions() {
      return referenceAttributePermissions;
    }

  }

  public class TextAttributePermissions {

    private String id;
    private List<Permission> permissions;

    public String getId() {
      return id;
    }

    public List<Permission> getPermissions() {
      return permissions;
    }

  }

  public class ReferenceAttributePermissions {

    private String id;
    private List<Permission> permissions;

    public String getId() {
      return id;
    }

    public List<Permission> getPermissions() {
      return permissions;
    }

  }

}
