package fi.thl.termed.web.external.node.dto;

import java.util.HashSet;
import java.util.Set;

public class NodeToDtoMapperConfig {

  private int maxReferenceDepth = 1;
  private int maxReferrerDepth = 0;

  private boolean loadType = false;
  private boolean loadGraph = false;
  private boolean loadAudit = false;

  private boolean useUriKeys = false;

  // empty means select all
  private Set<String> selectProperty = new HashSet<>();
  private Set<String> selectReference = new HashSet<>();
  private Set<String> selectReferrer = new HashSet<>();

  public int getMaxReferenceDepth() {
    return maxReferenceDepth;
  }

  public void setMaxReferenceDepth(int maxReferenceDepth) {
    this.maxReferenceDepth = maxReferenceDepth;
  }

  public int getMaxReferrerDepth() {
    return maxReferrerDepth;
  }

  public void setMaxReferrerDepth(int maxReferrerDepth) {
    this.maxReferrerDepth = maxReferrerDepth;
  }

  public boolean isLoadType() {
    return loadType;
  }

  public void setLoadType(boolean loadType) {
    this.loadType = loadType;
  }

  public boolean isLoadGraph() {
    return loadGraph;
  }

  public void setLoadGraph(boolean loadGraph) {
    this.loadGraph = loadGraph;
  }

  public boolean isLoadAudit() {
    return loadAudit;
  }

  public void setLoadAudit(boolean loadAudit) {
    this.loadAudit = loadAudit;
  }

  public boolean isUseUriKeys() {
    return useUriKeys;
  }

  public void setUseUriKeys(boolean useUriKeys) {
    this.useUriKeys = useUriKeys;
  }

  public Set<String> getSelectProperty() {
    return selectProperty;
  }

  public void setSelectProperty(Set<String> selectProperty) {
    this.selectProperty = selectProperty;
  }

  public Set<String> getSelectReference() {
    return selectReference;
  }

  public void setSelectReference(Set<String> selectReference) {
    this.selectReference = selectReference;
  }

  public Set<String> getSelectReferrer() {
    return selectReferrer;
  }

  public void setSelectReferrer(Set<String> selectReferrer) {
    this.selectReferrer = selectReferrer;
  }

}
