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
  password varchar(255) NOT NULL,
  app_role varchar(20) NOT NULL,
  CONSTRAINT users_pkey PRIMARY KEY (username),
  CONSTRAINT users_app_role_check CHECK (app_role IN ('USER', 'ADMIN', 'SUPERUSER'))
);

--
-- Scheme tables (meta model)
--

CREATE TABLE property (
  id varchar(255),
  uri varchar(2000),
  index integer,
  CONSTRAINT property_pkey PRIMARY KEY (id),
  CONSTRAINT property_id_check CHECK (id ~ '^[A-Za-z0-9_\\-]+$')
);

CREATE TABLE property_property_value (
  subject_id varchar(255),
  property_id varchar(255),
  index integer,
  lang varchar(2) NOT NULL,
  value text NOT NULL,
  CONSTRAINT property_property_value_pkey PRIMARY KEY (subject_id, property_id, index),
  CONSTRAINT property_property_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES property(id),
  CONSTRAINT property_property_property_id_fkey FOREIGN KEY (property_id) REFERENCES property(id),
  CONSTRAINT property_property_lang_fkey FOREIGN KEY (lang) REFERENCES lang(lang)
);

CREATE INDEX property_property_value_subject_id_idx ON property_property_value(subject_id);

CREATE TABLE scheme (
  id uuid,
  code varchar(255),
  uri varchar(2000),
  CONSTRAINT scheme_pkey PRIMARY KEY (id),
  CONSTRAINT scheme_code_check CHECK (code ~ '^[A-Za-z0-9_\\-]+$'),
  CONSTRAINT scheme_code_unique UNIQUE (code)
);

CREATE TABLE scheme_property_value (
  scheme_id uuid,
  property_id varchar(255),
  index integer,
  lang varchar(2) NOT NULL,
  value text NOT NULL,
  CONSTRAINT scheme_property_value_pkey PRIMARY KEY (scheme_id, property_id, index),
  CONSTRAINT scheme_property_value_scheme_id_fkey FOREIGN KEY (scheme_id) REFERENCES scheme(id) ON DELETE CASCADE,
  CONSTRAINT scheme_property_value_property_id_fkey FOREIGN KEY (property_id) REFERENCES property(id),
  CONSTRAINT scheme_property_value_lang_fkey FOREIGN KEY (lang) REFERENCES lang(lang)
);

CREATE INDEX scheme_property_value_scheme_id_idx ON scheme_property_value(scheme_id);

CREATE TABLE class (
  scheme_id uuid,
  id varchar(255),
  uri varchar(2000),
  index integer,
  CONSTRAINT class_pkey PRIMARY KEY (scheme_id, id),
  CONSTRAINT class_scheme_id_fkey FOREIGN KEY (scheme_id) REFERENCES scheme(id) ON DELETE CASCADE,
  CONSTRAINT class_id_check CHECK (id ~ '^[A-Za-z0-9_\\-]*$'),
  CONSTRAINT class_scheme_id_uri_unique UNIQUE (scheme_id, uri)
);

CREATE INDEX class_scheme_id_idx ON class(scheme_id);

CREATE TABLE class_property_value (
  class_scheme_id uuid,
  class_id varchar(255),
  property_id varchar(255),
  index integer,
  lang varchar(2) NOT NULL,
  value text NOT NULL,
  CONSTRAINT class_property_value_pkey PRIMARY KEY (class_scheme_id, class_id, property_id, index),
  CONSTRAINT class_property_value_class_scheme_id_class_id_fkey FOREIGN KEY (class_scheme_id, class_id) REFERENCES class(scheme_id, id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT class_property_value_property_id_fkey FOREIGN KEY (property_id) REFERENCES property(id),
  CONSTRAINT class_property_value_lang_fkey FOREIGN KEY (lang) REFERENCES lang(lang)
);

CREATE INDEX class_property_value_class_id_idx ON class_property_value(class_scheme_id, class_id);

CREATE TABLE text_attribute (
  scheme_id uuid,
  domain_id varchar(255),
  id varchar(255),
  uri varchar(2000),
  regex varchar(255) NOT NULL,
  index integer,
  CONSTRAINT text_attribute_pkey PRIMARY KEY (scheme_id, domain_id, id),
  CONSTRAINT text_attribute_id_check CHECK (id ~ '^[A-Za-z0-9_\\-]+$'),
  CONSTRAINT text_attribute_domain_fkey FOREIGN KEY (scheme_id, domain_id) REFERENCES class(scheme_id, id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT text_attribute_uri_unique UNIQUE (scheme_id, domain_id, uri),
  CONSTRAINT text_attribute_regex_unique UNIQUE (scheme_id, domain_id, id, regex)
);

CREATE INDEX text_attribute_scheme_id_idx ON text_attribute(scheme_id);
CREATE INDEX text_attribute_class_id_idx ON text_attribute(scheme_id, domain_id);

CREATE TABLE text_attribute_property_value (
  text_attribute_scheme_id uuid,
  text_attribute_domain_id varchar(255),
  text_attribute_id varchar(255),
  property_id varchar(255),
  index integer,
  lang varchar(2) NOT NULL,
  value text NOT NULL,
  CONSTRAINT text_attribute_property_value_pkey PRIMARY KEY (text_attribute_scheme_id, text_attribute_domain_id, text_attribute_id, property_id, index),
  CONSTRAINT text_attribute_property_value_subject_fkey FOREIGN KEY (text_attribute_scheme_id, text_attribute_domain_id, text_attribute_id) REFERENCES text_attribute(scheme_id, domain_id, id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT text_attribute_property_value_property_fkey FOREIGN KEY (property_id) REFERENCES property(id),
  CONSTRAINT text_attribute_property_value_lang_fkey FOREIGN KEY (lang) REFERENCES lang(lang)
);

CREATE INDEX text_attribute_property_value_subject_id_idx ON text_attribute_property_value(text_attribute_scheme_id, text_attribute_domain_id, text_attribute_id);

CREATE TABLE reference_attribute (
  scheme_id uuid,
  domain_id varchar(255),
  id varchar(255),
  uri varchar(2000),
  range_scheme_id uuid NOT NULL,
  range_id varchar(255) NOT NULL,
  index integer,
  CONSTRAINT reference_attribute_pkey PRIMARY KEY (scheme_id, domain_id, id),
  CONSTRAINT reference_attribute_domain_fkey FOREIGN KEY (scheme_id, domain_id) REFERENCES class(scheme_id, id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT reference_attribute_uri_unique UNIQUE (scheme_id, domain_id, uri),
  CONSTRAINT reference_attribute_range_fkey FOREIGN KEY (range_scheme_id, range_id) REFERENCES class(scheme_id, id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT reference_attribute_range_unique UNIQUE (scheme_id, domain_id, id, range_scheme_id, range_id),
  CONSTRAINT reference_attribute_id_check CHECK (id ~ '^[A-Za-z0-9_\\-]+$')
);

CREATE INDEX reference_attribute_scheme_id_idx ON reference_attribute(scheme_id);
CREATE INDEX reference_attribute_class_id_idx ON reference_attribute(scheme_id, domain_id);

CREATE TABLE reference_attribute_property_value (
  reference_attribute_scheme_id uuid,
  reference_attribute_domain_id varchar(255),
  reference_attribute_id varchar(255),
  property_id varchar(255),
  index integer,
  lang varchar(2) NOT NULL,
  value text NOT NULL,
  CONSTRAINT reference_attribute_property_value_pkey PRIMARY KEY (reference_attribute_scheme_id, reference_attribute_domain_id, reference_attribute_id, property_id, index),
  CONSTRAINT reference_attribute_property_value_subject_fkey FOREIGN KEY (reference_attribute_scheme_id, reference_attribute_domain_id, reference_attribute_id) REFERENCES reference_attribute(scheme_id, domain_id, id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT reference_attribute_property_value_property_fkey FOREIGN KEY (property_id) REFERENCES property(id),
  CONSTRAINT reference_attribute_property_value_lang_fkey FOREIGN KEY (lang) REFERENCES lang(lang)
);

CREATE INDEX reference_attribute_property_value_subject_id_idx ON reference_attribute_property_value(reference_attribute_scheme_id, reference_attribute_domain_id, reference_attribute_id);

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
  CONSTRAINT resource_pkey PRIMARY KEY (scheme_id, type_id, id),
  CONSTRAINT resource_type_fkey FOREIGN KEY (scheme_id, type_id) REFERENCES class(scheme_id, id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT resource_scheme_id_fkey FOREIGN KEY (scheme_id) REFERENCES scheme(id) ON DELETE CASCADE,
  CONSTRAINT resource_created_by_fkey FOREIGN KEY (created_by) REFERENCES users(username),
  CONSTRAINT resource_last_modified_by_fkey FOREIGN KEY (last_modified_by) REFERENCES users(username),
  CONSTRAINT resource_scheme_id_type_id_code_unique UNIQUE (scheme_id, type_id, code),
  CONSTRAINT resource_scheme_id_uri_unique UNIQUE (scheme_id, uri),
  CONSTRAINT resource_code_check CHECK (code ~ '^[A-Za-z0-9_\\-]+$')
);

CREATE INDEX resource_scheme_id_idx ON resource(scheme_id);
CREATE INDEX resource_scheme_id_type_id_idx ON resource(scheme_id, type_id);

CREATE TABLE resource_text_attribute_value (
  scheme_id uuid,
  resource_type_id varchar(255),
  resource_id uuid,
  attribute_id varchar(255),
  index integer,
  lang varchar(2) NOT NULL,
  value text NOT NULL,
  regex varchar(255) NOT NULL,
  CONSTRAINT resource_text_attribute_value_pkey PRIMARY KEY (scheme_id, resource_type_id, resource_id, attribute_id, index),
  CONSTRAINT resource_text_attribute_value_resource_fkey FOREIGN KEY (scheme_id, resource_type_id, resource_id) REFERENCES resource(scheme_id, type_id, id) ON DELETE CASCADE,
  CONSTRAINT resource_text_attribute_value_attribute_fkey FOREIGN KEY (scheme_id, resource_type_id, regex, attribute_id) REFERENCES text_attribute(scheme_id, domain_id, regex, id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT resource_text_attribute_value_lang_fkey FOREIGN KEY (lang) REFERENCES lang(lang),
  CONSTRAINT resource_text_attribute_value_value_check CHECK (value ~ regex)
);

CREATE INDEX resource_text_attribute_value_resource_idx ON resource_text_attribute_value(scheme_id, resource_type_id, resource_id);

CREATE TABLE resource_reference_attribute_value (
  scheme_id uuid,
  resource_type_id varchar(255),
  resource_id uuid,
  attribute_id varchar(255),
  index integer,
  value_scheme_id uuid NOT NULL,
  value_type_id varchar(255) NOT NULL,
  value_id uuid NOT NULL,
  CONSTRAINT resource_reference_attribute_value_pkey PRIMARY KEY (scheme_id, resource_type_id, resource_id, attribute_id, index),
  CONSTRAINT resource_reference_attribute_value_resource_fkey FOREIGN KEY (scheme_id, resource_type_id, resource_id) REFERENCES resource(scheme_id, type_id, id) ON DELETE CASCADE,
  CONSTRAINT resource_reference_attribute_value_attribute_fkey FOREIGN KEY (scheme_id, resource_type_id, value_scheme_id, value_type_id, attribute_id) REFERENCES reference_attribute(scheme_id, domain_id, range_scheme_id, range_id, id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT resource_reference_attribute_value_value_fkey FOREIGN KEY (value_scheme_id, value_type_id, value_id) REFERENCES resource(scheme_id, type_id, id) ON DELETE CASCADE
);

CREATE INDEX resource_reference_attribute_value_resource_idx ON resource_reference_attribute_value(scheme_id, resource_type_id, resource_id);
CREATE INDEX resource_reference_attribute_value_value_idx ON resource_reference_attribute_value(value_scheme_id, value_type_id, value_id);
