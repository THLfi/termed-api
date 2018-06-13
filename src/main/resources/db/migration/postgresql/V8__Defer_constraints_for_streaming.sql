--
-- Alter few constraints to deferred to allow e.g. streaming inserts with circular references
--

-- defer range check if target type is not yet inserted

ALTER TABLE reference_attribute
  DROP CONSTRAINT reference_attribute_range_fkey;

ALTER TABLE reference_attribute
  ADD CONSTRAINT reference_attribute_range_fkey
    FOREIGN KEY (range_graph_id, range_id)
    REFERENCES type(graph_id, id)
      ON DELETE CASCADE
      ON UPDATE CASCADE
      DEFERRABLE
      INITIALLY DEFERRED;

-- defer node reference check, target node might not be inserted yet

ALTER TABLE node_reference_attribute_value
  DROP CONSTRAINT node_reference_attribute_value_value_fkey;

ALTER TABLE node_reference_attribute_value
  ADD CONSTRAINT node_reference_attribute_value_value_fkey
    FOREIGN KEY (value_graph_id, value_type_id, value_id)
    REFERENCES node(graph_id, type_id, id)
      ON DELETE CASCADE
      DEFERRABLE
      INITIALLY DEFERRED;

-- set unique columns to deferrable to allow swapping

ALTER TABLE node
  DROP CONSTRAINT node_graph_id_type_id_code_unique;

ALTER TABLE node
  ADD CONSTRAINT node_graph_id_type_id_code_unique
    UNIQUE (graph_id, type_id, code)
    DEFERRABLE
    INITIALLY DEFERRED;

ALTER TABLE node
  DROP CONSTRAINT node_graph_id_uri_unique;

ALTER TABLE node
  ADD CONSTRAINT node_graph_id_uri_unique
    UNIQUE (graph_id, uri)
    DEFERRABLE
    INITIALLY DEFERRED;

ALTER TABLE node
  DROP CONSTRAINT node_graph_id_type_id_number_unique;

ALTER TABLE node
  ADD CONSTRAINT node_graph_id_type_id_number_unique
    UNIQUE (graph_id, type_id, number)
    DEFERRABLE
    INITIALLY DEFERRED;
