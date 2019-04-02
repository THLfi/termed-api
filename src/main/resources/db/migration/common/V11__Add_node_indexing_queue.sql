
CREATE SEQUENCE node_indexing_queue_seq;

CREATE TABLE node_indexing_queue (
  id bigint PRIMARY KEY
);

CREATE TABLE node_indexing_queue_item (
  node_graph_id uuid,
  node_type_id varchar(255),
  node_id uuid,
  node_indexing_queue_id bigint,
  CONSTRAINT indexing_queue_item_pkey
    PRIMARY KEY (node_graph_id, node_type_id, node_id, node_indexing_queue_id),
  CONSTRAINT indexing_queue_item_queue_fkey
    FOREIGN KEY (node_indexing_queue_id)
    REFERENCES node_indexing_queue(id) ON DELETE CASCADE
);

CREATE INDEX node_indexing_queue_item_queue_idx
    ON node_indexing_queue_item(node_indexing_queue_id);
