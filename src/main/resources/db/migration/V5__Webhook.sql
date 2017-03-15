--
-- Tables for storing information about webhooks
--

CREATE TABLE webhook (
  id uuid,
  url varchar(2000) NOT NULL,
  CONSTRAINT webhook_pkey PRIMARY KEY (id)
);
