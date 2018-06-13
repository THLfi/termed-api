--
-- Termed database schema
--

--
-- Common stuff
--

CREATE TABLE lang (
  lang varchar(35),
  CONSTRAINT lang_pkey PRIMARY KEY (lang),
  CONSTRAINT lang_lang_check CHECK (lang ~ '^[A-Za-z0-9\\-]*$')
);

CREATE TABLE users (
  username varchar(255),
  password varchar(255) NOT NULL,
  app_role varchar(20) NOT NULL,
  CONSTRAINT users_pkey PRIMARY KEY (username),
  CONSTRAINT users_app_role_check CHECK (app_role IN ('USER', 'ADMIN', 'SUPERUSER'))
);

--
-- Graph tables (meta model)
--

CREATE TABLE property (
  id varchar(255),
  uri varchar(2000),
  index integer,
  CONSTRAINT property_pkey PRIMARY KEY (id),
  CONSTRAINT property_id_check CHECK (id ~ '^[A-Za-z0-9_\\-]+$')
);

CREATE TABLE property_property (
  subject_id varchar(255),
  property_id varchar(255),
  index integer,
  lang varchar(35) NOT NULL,
  value text NOT NULL,
  CONSTRAINT property_property_pkey PRIMARY KEY (subject_id, property_id, index),
  CONSTRAINT property_property_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES property(id),
  CONSTRAINT property_property_property_id_fkey FOREIGN KEY (property_id) REFERENCES property(id),
  CONSTRAINT property_property_lang_fkey FOREIGN KEY (lang) REFERENCES lang(lang)
);

CREATE INDEX property_property_subject_id_idx ON property_property(subject_id);

CREATE TABLE graph (
  id uuid,
  code varchar(255),
  uri varchar(2000),
  CONSTRAINT graph_pkey PRIMARY KEY (id),
  CONSTRAINT graph_code_check CHECK (code ~ '^[A-Za-z0-9_\\-]+$'),
  CONSTRAINT graph_code_unique UNIQUE (code)
);

CREATE TABLE graph_property (
  graph_id uuid,
  property_id varchar(255),
  index integer,
  lang varchar(35) NOT NULL,
  value text NOT NULL,
  CONSTRAINT graph_property_pkey PRIMARY KEY (graph_id, property_id, index),
  CONSTRAINT graph_property_graph_id_fkey
    FOREIGN KEY (graph_id) REFERENCES graph(id) ON DELETE CASCADE,
  CONSTRAINT graph_property_property_id_fkey
    FOREIGN KEY (property_id) REFERENCES property(id),
  CONSTRAINT graph_property_lang_fkey FOREIGN KEY (lang) REFERENCES lang(lang)
);

CREATE INDEX graph_property_graph_id_idx ON graph_property(graph_id);

CREATE TABLE type (
  graph_id uuid,
  id varchar(255),
  uri varchar(2000),
  node_code_prefix varchar(255),
  index integer,
  CONSTRAINT type_pkey PRIMARY KEY (graph_id, id),
  CONSTRAINT type_graph_id_fkey FOREIGN KEY (graph_id) REFERENCES graph(id),
  CONSTRAINT type_id_check CHECK (id ~ '^[A-Za-z0-9_\\-]*$'),
  CONSTRAINT type_graph_id_uri_unique UNIQUE (graph_id, uri),
  CONSTRAINT type_node_code_prefix_check CHECK (node_code_prefix ~ '^[A-Za-z0-9_\\-]*$')
);

CREATE INDEX type_graph_id_idx ON type(graph_id);

CREATE TABLE type_property (
  type_graph_id uuid,
  type_id varchar(255),
  property_id varchar(255),
  index integer,
  lang varchar(35) NOT NULL,
  value text NOT NULL,
  CONSTRAINT type_property_pkey
    PRIMARY KEY (type_graph_id, type_id, property_id, index),
  CONSTRAINT type_property_type_fkey
    FOREIGN KEY (type_graph_id, type_id)
    REFERENCES type(graph_id, id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT type_property_property_id_fkey
    FOREIGN KEY (property_id) REFERENCES property(id),
  CONSTRAINT type_property_lang_fkey FOREIGN KEY (lang) REFERENCES lang(lang)
);

CREATE INDEX type_property_type_id_idx ON type_property(type_graph_id, type_id);

CREATE TABLE text_attribute (
  domain_graph_id uuid,
  domain_id varchar(255),
  id varchar(255),
  uri varchar(2000),
  regex varchar(255) NOT NULL,
  index integer,
  CONSTRAINT text_attribute_pkey PRIMARY KEY (domain_graph_id, domain_id, id),
  CONSTRAINT text_attribute_id_check CHECK (id ~ '^[A-Za-z0-9_\\-]+$'),
  CONSTRAINT text_attribute_domain_fkey
    FOREIGN KEY (domain_graph_id, domain_id)
    REFERENCES type(graph_id, id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT text_attribute_uri_unique UNIQUE (domain_graph_id, domain_id, uri),
  CONSTRAINT text_attribute_regex_unique UNIQUE (domain_graph_id, domain_id, id, regex)
);

CREATE INDEX text_attribute_domain_graph_id_idx
    ON text_attribute(domain_graph_id);
CREATE INDEX text_attribute_type_id_idx
    ON text_attribute(domain_graph_id, domain_id);

CREATE TABLE text_attribute_property (
  text_attribute_domain_graph_id uuid,
  text_attribute_domain_id varchar(255),
  text_attribute_id varchar(255),
  property_id varchar(255),
  index integer,
  lang varchar(35) NOT NULL,
  value text NOT NULL,
  CONSTRAINT text_attribute_property_pkey
    PRIMARY KEY (text_attribute_domain_graph_id, text_attribute_domain_id, text_attribute_id, property_id, index),
  CONSTRAINT text_attribute_property_subject_fkey
    FOREIGN KEY (text_attribute_domain_graph_id, text_attribute_domain_id, text_attribute_id)
    REFERENCES text_attribute(domain_graph_id, domain_id, id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT text_attribute_property_property_fkey
    FOREIGN KEY (property_id) REFERENCES property(id),
  CONSTRAINT text_attribute_property_lang_fkey FOREIGN KEY (lang) REFERENCES lang(lang)
);

CREATE INDEX text_attribute_property_subject_id_idx
    ON text_attribute_property(text_attribute_domain_graph_id, text_attribute_domain_id, text_attribute_id);

CREATE TABLE reference_attribute (
  domain_graph_id uuid,
  domain_id varchar(255),
  id varchar(255),
  uri varchar(2000),
  range_graph_id uuid NOT NULL,
  range_id varchar(255) NOT NULL,
  index integer,
  CONSTRAINT reference_attribute_pkey PRIMARY KEY (domain_graph_id, domain_id, id),
  CONSTRAINT reference_attribute_domain_fkey
    FOREIGN KEY (domain_graph_id, domain_id)
    REFERENCES type(graph_id, id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT reference_attribute_uri_unique UNIQUE (domain_graph_id, domain_id, uri),
  CONSTRAINT reference_attribute_range_fkey
    FOREIGN KEY (range_graph_id, range_id)
    REFERENCES type(graph_id, id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT reference_attribute_range_unique
    UNIQUE (domain_graph_id, domain_id, id, range_graph_id, range_id),
  CONSTRAINT reference_attribute_id_check CHECK (id ~ '^[A-Za-z0-9_\\-]+$')
);

CREATE INDEX reference_attribute_domain_graph_id_idx
    ON reference_attribute(domain_graph_id);
CREATE INDEX reference_attribute_type_id_idx
    ON reference_attribute(domain_graph_id, domain_id);

CREATE TABLE reference_attribute_property (
  reference_attribute_domain_graph_id uuid,
  reference_attribute_domain_id varchar(255),
  reference_attribute_id varchar(255),
  property_id varchar(255),
  index integer,
  lang varchar(35) NOT NULL,
  value text NOT NULL,
  CONSTRAINT reference_attribute_property_pkey
    PRIMARY KEY (reference_attribute_domain_graph_id, reference_attribute_domain_id, reference_attribute_id, property_id, index),
  CONSTRAINT reference_attribute_property_subject_fkey
    FOREIGN KEY (reference_attribute_domain_graph_id, reference_attribute_domain_id, reference_attribute_id)
    REFERENCES reference_attribute(domain_graph_id, domain_id, id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT reference_attribute_property_property_fkey
    FOREIGN KEY (property_id) REFERENCES property(id),
  CONSTRAINT reference_attribute_property_lang_fkey
    FOREIGN KEY (lang) REFERENCES lang(lang)
);

CREATE INDEX reference_attribute_property_subject_id_idx
    ON reference_attribute_property(reference_attribute_domain_graph_id, reference_attribute_domain_id, reference_attribute_id);

--
-- Node tables (the actual data)
--

CREATE TABLE node_sequence (
  graph_id uuid,
  type_id varchar(255),
  value bigint NOT NULL,
  CONSTRAINT node_sequence_pkey PRIMARY KEY (graph_id, type_id),
  CONSTRAINT node_sequence_type_fkey FOREIGN KEY (graph_id, type_id)
    REFERENCES type(graph_id, id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE node (
  graph_id uuid,
  type_id varchar(255),
  id uuid,
  code varchar(255),
  uri varchar(2000),
  number bigint NOT NULL,
  created_by varchar(255) NOT NULL,
  created_date timestamp NOT NULL,
  last_modified_by varchar(255) NOT NULL,
  last_modified_date timestamp NOT NULL,
  CONSTRAINT node_pkey PRIMARY KEY (graph_id, type_id, id),
  CONSTRAINT node_type_fkey FOREIGN KEY (graph_id, type_id) REFERENCES type(graph_id, id) ON UPDATE CASCADE,
  CONSTRAINT node_created_by_fkey FOREIGN KEY (created_by) REFERENCES users(username),
  CONSTRAINT node_last_modified_by_fkey FOREIGN KEY (last_modified_by) REFERENCES users(username),
  CONSTRAINT node_graph_id_type_id_code_unique UNIQUE (graph_id, type_id, code),
  CONSTRAINT node_graph_id_uri_unique UNIQUE (graph_id, uri),
  CONSTRAINT node_graph_id_type_id_number_unique UNIQUE (graph_id, type_id, number),
  CONSTRAINT node_code_check CHECK (code ~ '^[A-Za-z0-9_\\-]+$')
);

CREATE INDEX node_graph_id_idx
    ON node(graph_id);
CREATE INDEX node_graph_id_type_id_idx
    ON node(graph_id, type_id);

CREATE TABLE node_text_attribute_value (
  node_graph_id uuid,
  node_type_id varchar(255),
  node_id uuid,
  attribute_id varchar(255),
  index integer,
  lang varchar(35) NOT NULL,
  value text NOT NULL,
  regex varchar(255) NOT NULL,
  CONSTRAINT node_text_attribute_value_pkey
    PRIMARY KEY (node_graph_id, node_type_id, node_id, attribute_id, index),
  CONSTRAINT node_text_attribute_value_node_fkey
    FOREIGN KEY (node_graph_id, node_type_id, node_id)
    REFERENCES node(graph_id, type_id, id) ON DELETE CASCADE,
  CONSTRAINT node_text_attribute_value_attribute_fkey
    FOREIGN KEY (node_graph_id, node_type_id, regex, attribute_id)
    REFERENCES text_attribute(domain_graph_id, domain_id, regex, id) ON UPDATE CASCADE,
  CONSTRAINT node_text_attribute_value_lang_fkey FOREIGN KEY (lang) REFERENCES lang(lang),
  CONSTRAINT node_text_attribute_value_value_check CHECK (value ~ regex)
);

CREATE INDEX node_text_attribute_value_node_idx
    ON node_text_attribute_value(node_graph_id, node_type_id, node_id);

CREATE TABLE node_reference_attribute_value (
  node_graph_id uuid,
  node_type_id varchar(255),
  node_id uuid,
  attribute_id varchar(255),
  index integer,
  value_graph_id uuid NOT NULL,
  value_type_id varchar(255) NOT NULL,
  value_id uuid NOT NULL,
  CONSTRAINT node_reference_attribute_value_pkey
    PRIMARY KEY (node_graph_id, node_type_id, node_id, attribute_id, index),
  CONSTRAINT node_reference_attribute_value_node_fkey
    FOREIGN KEY (node_graph_id, node_type_id, node_id)
    REFERENCES node(graph_id, type_id, id) ON DELETE CASCADE,
  CONSTRAINT node_reference_attribute_value_attribute_fkey
    FOREIGN KEY (node_graph_id, node_type_id, value_graph_id, value_type_id, attribute_id)
    REFERENCES reference_attribute(domain_graph_id, domain_id, range_graph_id, range_id, id) ON UPDATE CASCADE,
  CONSTRAINT node_reference_attribute_value_value_fkey
    FOREIGN KEY (value_graph_id, value_type_id, value_id)
    REFERENCES node(graph_id, type_id, id) ON DELETE CASCADE
);

CREATE INDEX node_reference_attribute_value_node_idx
    ON node_reference_attribute_value(node_graph_id, node_type_id, node_id);
CREATE INDEX node_reference_attribute_value_value_idx
    ON node_reference_attribute_value(value_graph_id, value_type_id, value_id);
