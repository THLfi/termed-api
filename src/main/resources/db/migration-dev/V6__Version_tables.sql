--
-- Tables for storing node revision history
--

CREATE SEQUENCE revision_seq;

CREATE TABLE revision (
  number bigint PRIMARY KEY
);

CREATE TABLE node_aud (
  graph_id uuid,
  type_id varchar(255),
  id uuid,
  code varchar(255),
  uri varchar(2000),
  number bigint,
  created_by varchar(255),
  created_date timestamp,
  last_modified_by varchar(255),
  last_modified_date timestamp,
  revision bigint NOT NULL,
  revision_type char(6) NOT NULL,
  CONSTRAINT node_aud_pkey
    PRIMARY KEY (graph_id, type_id, id, revision),
--  CONSTRAINT node_aud_revision_fkey
--    FOREIGN KEY (revision) REFERENCES revision(number),
  CONSTRAINT node_aud_revision_type_check
    CHECK (revision_type IN ('INSERT', 'UPDATE', 'DELETE'))
);

CREATE TABLE node_text_attribute_value_aud (
  node_graph_id uuid,
  node_type_id varchar(255),
  node_id uuid,
  attribute_id varchar(255),
  index integer,
  lang varchar(35),
  value text,
  regex varchar(255),
  revision bigint NOT NULL,
  revision_type char(6) NOT NULL,
  CONSTRAINT node_text_attribute_value_aud_pkey
    PRIMARY KEY (node_graph_id, node_type_id, node_id, attribute_id, index, revision),
--  CONSTRAINT node_text_attribute_value_aud_revision_fkey
--    FOREIGN KEY (revision) REFERENCES revision(number),
  CONSTRAINT node_text_attribute_value_aud_revision_type_check
    CHECK (revision_type IN ('INSERT', 'UPDATE', 'DELETE'))
);

CREATE TABLE node_reference_attribute_value_aud (
  node_graph_id uuid,
  node_type_id varchar(255),
  node_id uuid,
  attribute_id varchar(255),
  index integer,
  value_graph_id uuid,
  value_type_id varchar(255),
  value_id uuid,
  revision bigint NOT NULL,
  revision_type char(6) NOT NULL,
  CONSTRAINT node_reference_attribute_value_aud_pkey
    PRIMARY KEY (node_graph_id, node_type_id, node_id, attribute_id, index, revision),
--  CONSTRAINT node_reference_attribute_value_aud_revision_fkey
--    FOREIGN KEY (revision) REFERENCES revision(number),
  CONSTRAINT node_reference_attr_value_aud_revision_type_check
    CHECK (revision_type IN ('INSERT', 'UPDATE', 'DELETE'))
);
