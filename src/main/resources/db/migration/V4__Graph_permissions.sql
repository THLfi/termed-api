--
-- Tables to describe graph roles with related permissions
--

CREATE TABLE graph_role (
  graph_id uuid,
  role varchar(20),
  CONSTRAINT graph_role_pkey PRIMARY KEY (graph_id, role),
  CONSTRAINT graph_role_graph_id_fkey
    FOREIGN KEY (graph_id) REFERENCES graph(id) ON DELETE CASCADE,
  CONSTRAINT graph_role_role_check CHECK (role ~ '^[A-Za-z0-9_\\-]+$')
);

CREATE INDEX graph_role_graph_id_idx
    ON graph_role(graph_id);

CREATE TABLE user_graph_role (
  username varchar(255),
  graph_id uuid,
  role varchar(20),
  CONSTRAINT user_graph_role_pkey PRIMARY KEY (username, graph_id, role),
  CONSTRAINT user_graph_role_username_fkey
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
  CONSTRAINT user_graph_role_graph_id_role_fkey
    FOREIGN KEY (graph_id, role) REFERENCES graph_role(graph_id, role) ON DELETE CASCADE
);

CREATE INDEX user_graph_role_username_idx
    ON user_graph_role(username);

CREATE TABLE graph_permission (
  graph_id uuid,
  role varchar(20),
  permission varchar(20),
  CONSTRAINT graph_permission_pkey
    PRIMARY KEY (graph_id, role, permission),
  CONSTRAINT graph_permission_graph_id_fkey
    FOREIGN KEY (graph_id) REFERENCES graph(id) ON DELETE CASCADE,
  CONSTRAINT graph_permission_graph_role_fkey
    FOREIGN KEY (graph_id, role)
    REFERENCES graph_role(graph_id, role) ON DELETE CASCADE,
  CONSTRAINT graph_permission_permission_check
    CHECK (permission IN ('READ', 'INSERT', 'UPDATE', 'DELETE'))
);

CREATE INDEX graph_permission_graph_id_idx
    ON graph_permission(graph_id);

CREATE TABLE type_permission (
  type_graph_id uuid,
  type_id varchar(255),
  role varchar(20),
  permission varchar(20),
  CONSTRAINT type_permission_pkey
    PRIMARY KEY (type_graph_id, type_id, role, permission),
  CONSTRAINT type_permission_type_fkey
    FOREIGN KEY (type_graph_id, type_id)
    REFERENCES type(graph_id, id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT type_permission_graph_role_fkey
    FOREIGN KEY (type_graph_id, role)
    REFERENCES graph_role(graph_id, role) ON DELETE CASCADE,
  CONSTRAINT type_permission_permission_check
    CHECK (permission IN ('READ', 'INSERT', 'UPDATE', 'DELETE'))
);

CREATE INDEX type_permission_type_id_idx
    ON type_permission(type_graph_id, type_id);

CREATE TABLE text_attribute_permission (
  text_attribute_domain_graph_id uuid,
  text_attribute_domain_id varchar(255),
  text_attribute_id varchar(255),
  role varchar(20),
  permission varchar(20),
  CONSTRAINT text_attribute_permission_pkey
    PRIMARY KEY (text_attribute_domain_graph_id, text_attribute_domain_id, text_attribute_id, role, permission),
  CONSTRAINT text_attribute_permission_attribute_fkey
    FOREIGN KEY (text_attribute_domain_graph_id, text_attribute_domain_id, text_attribute_id)
    REFERENCES text_attribute(domain_graph_id, domain_id, id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT text_attribute_permission_graph_role_fkey
    FOREIGN KEY (text_attribute_domain_graph_id, role)
    REFERENCES graph_role(graph_id, role) ON DELETE CASCADE,
  CONSTRAINT text_attribute_permission_permission_check
    CHECK (permission IN ('READ', 'INSERT', 'UPDATE', 'DELETE'))
);

CREATE INDEX text_attribute_permission_attribute_idx
    ON text_attribute_permission(text_attribute_domain_graph_id, text_attribute_domain_id, text_attribute_id);

CREATE TABLE reference_attribute_permission (
  reference_attribute_domain_graph_id uuid,
  reference_attribute_domain_id varchar(255),
  reference_attribute_id varchar(255),
  role varchar(20),
  permission varchar(20),
  CONSTRAINT reference_attribute_permission_pkey
    PRIMARY KEY (reference_attribute_domain_graph_id, reference_attribute_domain_id, reference_attribute_id, role, permission),
  CONSTRAINT reference_attribute_permission_attribute_fkey
    FOREIGN KEY (reference_attribute_domain_graph_id, reference_attribute_domain_id, reference_attribute_id)
    REFERENCES reference_attribute(domain_graph_id, domain_id, id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT reference_attribute_permission_graph_role_fkey
    FOREIGN KEY (reference_attribute_domain_graph_id, role)
    REFERENCES graph_role(graph_id, role) ON DELETE CASCADE,
  CONSTRAINT reference_attribute_permission_permission_check
    CHECK (permission IN ('READ', 'INSERT', 'UPDATE', 'DELETE'))
);

CREATE INDEX reference_attribute_permission_attribute_idx
    ON reference_attribute_permission(reference_attribute_domain_graph_id, reference_attribute_domain_id, reference_attribute_id);

