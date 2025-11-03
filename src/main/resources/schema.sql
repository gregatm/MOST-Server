DROP SEQUENCE IF EXISTS thing_ids;
CREATE SEQUENCE thing_ids AS BIGINT START WITH 16 INCREMENT BY 16 ;

DROP TABLE IF EXISTS thing;
CREATE TABLE thing (
  id UUID UNIQUE DEFAULT gen_random_uuid(),
  acl_id BIGINT PRIMARY KEY DEFAULT nextval('thing_ids'),
  name VARCHAR(255)
);

DROP TABLE IF EXISTS sensor;
CREATE TABLE sensor (
  id UUID UNIQUE DEFAULT gen_random_uuid(),
  name VARCHAR(255)
);

DROP TABLE IF EXISTS datastream;
CREATE TABLE datastream (
  id UUID UNIQUE DEFAULT gen_random_uuid(),
  name VARCHAR(255),
  description VARCHAR(512),
  thing_id UUID REFERENCES thing(id),
  sensor_id UUID REFERENCES sensor(id)
);

INSERT INTO THING(name) VALUES('Thing 1');