
CREATE TABLE node_namespace_sequence (
  graph_id uuid,
  namespace varchar(1000),
  value bigint NOT NULL,
  CONSTRAINT node_namespace_sequence_pkey PRIMARY KEY (graph_id, namespace),
  CONSTRAINT node_namespace_sequence_graph_fkey FOREIGN KEY (graph_id)
    REFERENCES graph(id) ON DELETE CASCADE
);
