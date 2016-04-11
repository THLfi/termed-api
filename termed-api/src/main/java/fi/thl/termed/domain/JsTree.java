package fi.thl.termed.domain;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Model for data consumed by jsTree (a jQuery tree visualization plugin).
 */
public class JsTree {

  private String id;

  private String text;

  private Object icon;

  private Map<String, Boolean> state;

  private Object children;

  @SerializedName("a_attr")
  private Map<String, String> linkElementAttributes;

  @SerializedName("li_attr")
  private Map<String, String> listElementAttributes;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Object getIcon() {
    return icon;
  }

  public void setIcon(Object icon) {
    this.icon = icon;
  }

  public Map<String, Boolean> getState() {
    return state;
  }

  public void setState(Map<String, Boolean> state) {
    this.state = state;
  }

  public Object getChildren() {
    return children;
  }

  public void setChildren(Object children) {
    this.children = children;
  }

  public Map<String, String> getLinkElementAttributes() {
    return linkElementAttributes;
  }

  public void setLinkElementAttributes(Map<String, String> linkElementAttributes) {
    this.linkElementAttributes = linkElementAttributes;
  }

  public Map<String, String> getListElementAttributes() {
    return listElementAttributes;
  }

  public void setListElementAttributes(Map<String, String> listElementAttributes) {
    this.listElementAttributes = listElementAttributes;
  }

}
