CREATE KEYSPACE IF NOT EXISTS crawl
WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

USE crawl;

DROP TABLE IF EXISTS page;

CREATE TABLE IF NOT EXISTS crawl.page
(
host varchar,
scraped_at timestamp,
url varchar,
statusCode int,
htmlContent varchar,
title varchar,
primary key(url,host));

CREATE INDEX page_index_status ON page(statusCode);
CREATE INDEX page_index_scraped_at ON page(scraped_at);