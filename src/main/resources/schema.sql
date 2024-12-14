DROP TABLE IF EXISTS cell;
DROP TABLE IF EXISTS column_table;  -- Updated table name
DROP TABLE IF EXISTS sheet;

CREATE TABLE IF NOT EXISTS sheet (
    id varchar(255) NOT NULL PRIMARY KEY,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS column_table (  -- Updated table name
    id varchar(255) NOT NULL PRIMARY KEY,
    sheet_id VARCHAR(255),
    name varchar(255) NOT NULL,
    column_type VARCHAR(20) NOT NULL,
    FOREIGN KEY (sheet_id) REFERENCES sheet(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cell (
    id varchar(255) NOT NULL PRIMARY KEY,
    column_id varchar(255),
    row_index int,
    is_with_lookup boolean,
    cell_value VARCHAR(65535),  -- Changed from 'value' to 'cell_value'
    FOREIGN KEY (column_id) REFERENCES column_table(id) ON DELETE CASCADE  -- Updated to new table name
);

CREATE INDEX idx_column_name ON column_table(name);  -- Updated to new table name
CREATE INDEX idx_cell_column_id ON cell(column_id);