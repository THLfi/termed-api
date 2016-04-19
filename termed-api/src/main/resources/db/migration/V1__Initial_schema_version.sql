--
-- Termed database schema
--

--
-- Common stuff
--

CREATE TABLE lang (
  lang varchar(2),
  CONSTRAINT lang_pkey PRIMARY KEY (lang),
  CONSTRAINT lang_lang_check CHECK (lang ~ '^[a-z]*$')
);

CREATE TABLE users (
  username varchar(255),
  password varchar(255),
  app_role varchar(20),
  CONSTRAINT users_pkey PRIMARY KEY (username),
  CONSTRAINT users_app_role_check CHECK (app_role IN ('USER', 'ADMIN', 'SUPERUSER'))
);

--
-- Scheme tables (meta model)
--

CREATE TABLE property (
  id varchar(255) PRIMARY KEY,
  uri varchar(2000),
  index integer,
  CHECK (id ~ '^[A-Za-z0-9_\\-]+$')
);

CREATE TABLE property_property_value (
  subject_id varchar(255),
  property_id varchar(255),
  index integer,
  lang varchar(2) NOT NULL,
  value text NOT NULL,
  PRIMARY KEY (subject_id, property_id, index),
  FOREIGN KEY (subject_id) REFERENCES property(id),
  FOREIGN KEY (property_id) REFERENCES property(id),
  FOREIGN KEY (lang) REFERENCES lang(lang)
);

CREATE INDEX ON property_property_value(subject_id);

CREATE TABLE scheme (
  id uuid PRIMARY KEY,
  code varchar(255),
  uri varchar(2000),
  CHECK (code ~ '^[A-Za-z0-9_\\-]+$')
);

CREATE UNIQUE INDEX ON scheme(code);

CREATE TABLE scheme_property_value (
  scheme_id uuid,
  property_id varchar(255),
  index integer,
  lang varchar(2) NOT NULL,
  value text NOT NULL,
  PRIMARY KEY (scheme_id, property_id, index),
  FOREIGN KEY (scheme_id) REFERENCES scheme(id) ON DELETE CASCADE,
  FOREIGN KEY (property_id) REFERENCES property(id),
  FOREIGN KEY (lang) REFERENCES lang(lang)
);

CREATE INDEX ON scheme_property_value(scheme_id);

CREATE TABLE class (
  scheme_id uuid,
  id varchar(255),
  uri varchar(2000),
  index integer,
  PRIMARY KEY (scheme_id, id),
  FOREIGN KEY (scheme_id) REFERENCES scheme(id) ON DELETE CASCADE,
  CHECK (id ~ '^[A-Za-z0-9_\\-]*$')
);

CREATE INDEX ON class(scheme_id);
CREATE UNIQUE INDEX ON class(scheme_id, uri);

CREATE TABLE class_property_value (
  class_scheme_id uuid,
  class_id varchar(255),
  property_id varchar(255),
  index integer,
  lang varchar(2) NOT NULL,
  value text NOT NULL,
  PRIMARY KEY (class_scheme_id, class_id, property_id, index),
  FOREIGN KEY (class_scheme_id, class_id) REFERENCES class(scheme_id, id) ON DELETE CASCADE,
  FOREIGN KEY (property_id) REFERENCES property(id),
  FOREIGN KEY (lang) REFERENCES lang(lang)
);

CREATE INDEX ON class_property_value(class_scheme_id, class_id);

CREATE TABLE text_attribute (
  scheme_id uuid,
  domain_id varchar(255),
  id varchar(255),
  uri varchar(2000),
  index integer,
  PRIMARY KEY (scheme_id, domain_id, id),
  FOREIGN KEY (scheme_id, domain_id) REFERENCES class(scheme_id, id) ON DELETE CASCADE,
  CHECK (id ~ '^[A-Za-z0-9_\\-]+$')
);

CREATE UNIQUE INDEX ON text_attribute(scheme_id, domain_id, uri);

CREATE TABLE text_attribute_property_value (
  text_attribute_scheme_id uuid,
  text_attribute_domain_id varchar(255),
  text_attribute_id varchar(255),
  property_id varchar(255),
  index integer,
  lang varchar(2) NOT NULL,
  value text NOT NULL,
  PRIMARY KEY (text_attribute_scheme_id, text_attribute_domain_id, text_attribute_id, property_id, index),
  FOREIGN KEY (text_attribute_scheme_id, text_attribute_domain_id, text_attribute_id) REFERENCES text_attribute(scheme_id, domain_id, id) ON DELETE CASCADE,
  FOREIGN KEY (property_id) REFERENCES property(id),
  FOREIGN KEY (lang) REFERENCES lang(lang)
);

CREATE TABLE reference_attribute (
  scheme_id uuid,
  domain_id varchar(255),
  range_scheme_id uuid,
  range_id varchar(255),
  id varchar(255),
  uri varchar(2000),
  index integer,
  PRIMARY KEY (scheme_id, domain_id, range_scheme_id, range_id, id),
  FOREIGN KEY (scheme_id, domain_id) REFERENCES class(scheme_id, id) ON DELETE CASCADE,
  FOREIGN KEY (range_scheme_id, range_id) REFERENCES class(scheme_id, id) ON DELETE CASCADE,
  CHECK (id ~ '^[A-Za-z0-9_\\-]+$')
);

CREATE UNIQUE INDEX ON reference_attribute(scheme_id, domain_id, range_scheme_id, range_id, uri);

CREATE TABLE reference_attribute_property_value (
  reference_attribute_scheme_id uuid,
  reference_attribute_domain_id varchar(255),
  reference_attribute_range_scheme_id uuid,
  reference_attribute_range_id varchar(255),
  reference_attribute_id varchar(255),
  property_id varchar(255),
  index integer,
  lang varchar(2) NOT NULL,
  value text NOT NULL,
  PRIMARY KEY (reference_attribute_scheme_id, reference_attribute_domain_id, reference_attribute_range_scheme_id, reference_attribute_range_id, reference_attribute_id, property_id, index),
  FOREIGN KEY (reference_attribute_scheme_id, reference_attribute_domain_id, reference_attribute_range_scheme_id, reference_attribute_range_id, reference_attribute_id) REFERENCES reference_attribute(scheme_id, domain_id, range_scheme_id, range_id, id) ON DELETE CASCADE,
  FOREIGN KEY (property_id) REFERENCES property(id),
  FOREIGN KEY (lang) REFERENCES lang(lang)
);

--
-- Resource tables (the actual data)
--

CREATE TABLE resource (
  scheme_id uuid,
  type_id varchar(255),
  id uuid,
  code varchar(255),
  uri varchar(2000),
  created_by varchar(255) NOT NULL,
  created_date timestamp NOT NULL,
  last_modified_by varchar(255) NOT NULL,
  last_modified_date timestamp NOT NULL,
  PRIMARY KEY (scheme_id, type_id, id),
  FOREIGN KEY (scheme_id, type_id) REFERENCES class(scheme_id, id),
  FOREIGN KEY (scheme_id) REFERENCES scheme(id) ON DELETE CASCADE,
  FOREIGN KEY (created_by) REFERENCES users(username),
  FOREIGN KEY (last_modified_by) REFERENCES users(username),
  CHECK (code ~ '^[A-Za-z0-9_\\-]+$')
);

CREATE INDEX ON resource(scheme_id);
CREATE INDEX ON resource(scheme_id, type_id);
CREATE UNIQUE INDEX ON resource(scheme_id, type_id, code);
CREATE UNIQUE INDEX ON resource(scheme_id, uri);

CREATE TABLE resource_text_attribute_value (
  scheme_id uuid,
  resource_type_id varchar(255),
  resource_id uuid,
  attribute_id varchar(255),
  index integer,
  lang varchar(2) NOT NULL,
  value text NOT NULL,
  PRIMARY KEY (scheme_id, resource_type_id, resource_id, attribute_id, index),
  FOREIGN KEY (scheme_id, resource_type_id, resource_id) REFERENCES resource(scheme_id, type_id, id) ON DELETE CASCADE,
  FOREIGN KEY (scheme_id, resource_type_id, attribute_id) REFERENCES text_attribute(scheme_id, domain_id, id),
  FOREIGN KEY (lang) REFERENCES lang(lang)
);

CREATE INDEX ON resource_text_attribute_value(scheme_id, resource_type_id, resource_id);

CREATE TABLE resource_reference_attribute_value (
  scheme_id uuid,
  resource_type_id varchar(255),
  resource_id uuid,
  attribute_id varchar(255),
  index integer,
  value_scheme_id uuid NOT NULL,
  value_type_id varchar(255) NOT NULL,
  value_id uuid NOT NULL,
  PRIMARY KEY (scheme_id, resource_type_id, resource_id, attribute_id, index),
  FOREIGN KEY (scheme_id, resource_type_id, resource_id) REFERENCES resource(scheme_id, type_id, id) ON DELETE CASCADE,
  FOREIGN KEY (value_scheme_id, value_type_id, value_id) REFERENCES resource(scheme_id, type_id, id) ON DELETE CASCADE,
  FOREIGN KEY (scheme_id, resource_type_id, value_scheme_id, value_type_id, attribute_id) REFERENCES reference_attribute(scheme_id, domain_id, range_scheme_id, range_id, id)
);

CREATE INDEX ON resource_reference_attribute_value(scheme_id, resource_type_id, resource_id);
CREATE INDEX ON resource_reference_attribute_value(value_scheme_id, value_type_id, value_id);
