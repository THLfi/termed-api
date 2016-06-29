--
-- Tables to describe scheme roles with related permissions
--

CREATE TABLE scheme_role (
  scheme_id uuid,
  role varchar(20),
  CONSTRAINT scheme_role_pkey PRIMARY KEY (scheme_id, role),
  CONSTRAINT scheme_role_scheme_id_fkey FOREIGN KEY (scheme_id) REFERENCES scheme(id) ON DELETE CASCADE,
  CONSTRAINT scheme_role_role_check CHECK (role ~ '^[A-Za-z0-9_\\-]+$')
);

CREATE TABLE user_scheme_role (
  username varchar(255),
  scheme_id uuid,
  role varchar(20),
  CONSTRAINT user_scheme_role_pkey PRIMARY KEY (username, scheme_id, role),
  CONSTRAINT user_scheme_role_username_fkey FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
  CONSTRAINT user_scheme_role_scheme_id_role_fkey FOREIGN KEY (scheme_id, role) REFERENCES scheme_role(scheme_id, role) ON DELETE CASCADE
);

CREATE TABLE scheme_permission (
  scheme_id uuid,
  role varchar(20),
  permission varchar(20),
  CONSTRAINT scheme_permission_pkey PRIMARY KEY (scheme_id, role, permission),
  CONSTRAINT scheme_permission_scheme_id_fkey FOREIGN KEY (scheme_id) REFERENCES scheme(id) ON DELETE CASCADE,
  CONSTRAINT scheme_permission_scheme_role_fkey FOREIGN KEY (scheme_id, role) REFERENCES scheme_role(scheme_id, role) ON DELETE CASCADE,
  CONSTRAINT scheme_permission_permission_check CHECK (permission IN ('READ', 'INSERT', 'UPDATE', 'DELETE'))
);

CREATE TABLE class_permission (
  class_scheme_id uuid,
  class_id varchar(255),
  role varchar(20),
  permission varchar(20),
  CONSTRAINT class_permission_pkey PRIMARY KEY (class_scheme_id, class_id, role, permission),
  CONSTRAINT class_permission_class_fkey FOREIGN KEY (class_scheme_id, class_id) REFERENCES class(scheme_id, id) ON DELETE CASCADE,
  CONSTRAINT class_permission_scheme_role_fkey FOREIGN KEY (class_scheme_id, role) REFERENCES scheme_role(scheme_id, role) ON DELETE CASCADE,
  CONSTRAINT class_permission_permission_check CHECK (permission IN ('READ', 'INSERT', 'UPDATE', 'DELETE'))
);

CREATE TABLE text_attribute_permission (
  text_attribute_scheme_id uuid,
  text_attribute_domain_id varchar(255),
  text_attribute_id varchar(255),
  role varchar(20),
  permission varchar(20),
  CONSTRAINT text_attribute_permission_pkey PRIMARY KEY (text_attribute_scheme_id, text_attribute_domain_id, text_attribute_id, role, permission),
  CONSTRAINT text_attribute_permission_attribute_fkey FOREIGN KEY (text_attribute_scheme_id, text_attribute_domain_id, text_attribute_id) REFERENCES text_attribute(scheme_id, domain_id, id) ON DELETE CASCADE,
  CONSTRAINT text_attribute_permission_scheme_role_fkey FOREIGN KEY (text_attribute_scheme_id, role) REFERENCES scheme_role(scheme_id, role) ON DELETE CASCADE,
  CONSTRAINT text_attribute_permission_permission_check CHECK (permission IN ('READ', 'INSERT', 'UPDATE', 'DELETE'))
);

CREATE TABLE reference_attribute_permission (
  reference_attribute_scheme_id uuid,
  reference_attribute_domain_id varchar(255),
  reference_attribute_id varchar(255),
  role varchar(20),
  permission varchar(20),
  CONSTRAINT reference_attribute_permission_pkey PRIMARY KEY (reference_attribute_scheme_id, reference_attribute_domain_id, reference_attribute_id, role, permission),
  CONSTRAINT reference_attribute_permission_attribute_fkey FOREIGN KEY (reference_attribute_scheme_id, reference_attribute_domain_id, reference_attribute_id) REFERENCES reference_attribute(scheme_id, domain_id, id) ON DELETE CASCADE,
  CONSTRAINT reference_attribute_permission_scheme_role_fkey FOREIGN KEY (reference_attribute_scheme_id, role) REFERENCES scheme_role(scheme_id, role) ON DELETE CASCADE,
  CONSTRAINT reference_attribute_permission_permission_check CHECK (permission IN ('READ', 'INSERT', 'UPDATE', 'DELETE'))
);
