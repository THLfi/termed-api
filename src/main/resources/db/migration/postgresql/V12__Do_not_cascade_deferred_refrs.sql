
-- fix deferred node reference check to _not_ cascade on delete

ALTER TABLE node_reference_attribute_value
  DROP CONSTRAINT node_reference_attribute_value_value_fkey;

ALTER TABLE node_reference_attribute_value
  ADD CONSTRAINT node_reference_attribute_value_value_fkey
    FOREIGN KEY (value_graph_id, value_type_id, value_id)
    REFERENCES node(graph_id, type_id, id)
      DEFERRABLE
      INITIALLY DEFERRED;
