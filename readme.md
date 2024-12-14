# CellFlow - Anchor Home Assignment

**Author:**  💫 Adir Ben Yosef  
**Email:** [adirbenyossef@gmail.com](mailto:adirbenyossef@gmail.com)  
**GitHub:** [https://github.com/adirbenyossef](https://github.com/adirbenyossef)

## ⛓️ Table of Contents
1. [Getting started](#getting-started)
2. [Usage](#usage)
  - [Sheet Management](#sheet-management)
  - [Set Cell Value](#set-cell-value)
  - [Get Sheet by ID](#get-sheet-by-id)
3. [System Overview](#system-overview)
4. [Core Features](#core-features)
   - [Sheet Functionality](#sheet-functionality)
   - [Column Functionality](#column-functionality)
   - [Cell Functionality](#cell-functionality)
   - [Lookup Functionality](#lookup-functionality)
5. [Error Handling and Circuit Breaker](#error-handling-and-circuit-breaker)
6. [Concurrency and Transaction Management](#concurrency-and-transaction-management)
7. [Data Structures](#data-structures)
8. [Testing Strategy](#testing-strategy)
9. [Architecture](#architecture)
10. [Edge Cases](#edge-cases)
11. [Limitations](#limitations)
12. [Design Benefits](#design-benefits)

<a name="getting-started"/>

## Getting started

- Open the terminal and do the following commands
- `git clone git@github.com:adirbenyossef/CellFlow.git`
- `cd CellFlow`
- `mvn install`
- `mvn spring-boot:run`

* tests can be run by `mvn test`

System will run in port 3000

<a name="usage"/>

## Usage

<a name="sheet-management"/>

#### Sheet Management
- **Endpoint:** `POST /sheet`
- **Body:**
  - columns - List of name and type
    - name - String represent column name
    - type - String represent supported column types, accetable values: `STRING`,`INT`, `DOUBLE`, `BOOLEAN`
- **Example Body:**
```json
  {
    "columns": [
      { "name": "Title", "type": "STRING" },
      { "name": "Age", "type": "INT" }
    ]
  }
```
- **Response:** String represent sheet id
- **Example Response:** `4cee1fb8-2884-4b05-a2fe-dde4410a3cf7`

<a name="set-cell-value"/>

### Set Cell Value
- **Endpoint:** `POST /sheet/{sheetId}/cell`
- **Body:**
  - columnName - String represent column name
  - rowIndex - Integer represent column index
  - value - Object represent cell value - can by any value!
- **Example Body:**
```json
  {
    "columnName": "Title",
    "rowIndex": 1,
    "value": "GO Developer"
  }
```
- **Response:**
  - id - String represent cell id
  - rowIndex - Integer represent column index
  - value - Object represent cell value
  - columnId - String represent column id 
  - isWithLookup - Boolean indication for if value is a lookup
- **Example Response:**
```json
  {
    "id": "<UUID>",
    "rowIndex": 1,
    "value": "GO Developer",
    "columnId": "<UUID>", //unique id for column with name `Title` 
    "isWithLookup": false
  }
```

<a name="set-cell-value-lookup"/>

### Set Cell Value With Lookup

Same as [Set Cell Value](#set-cell-value) above but with String value of: `lookup(Title,1)` -> where Title is columnName, 1 is rowIndex
- **Example Body:**
```json
  {
    "columnName": "Age",
    "rowIndex": 1,
    "value": "lookup(Title,1)"
  }
```
- **Example Response:**
```json
  {
    "id": "<UUID>",
    "rowIndex": 1,
    "value": "GO Developer",
    "columnId": "<UUID>", //unique id for column with name `Title` 
    "isWithLookup": true
  }
```

<a name="get-sheet-by-id"/>

### Get Sheet by ID
- **Endpoint:** `GET /sheet/{sheetId}`
- **Response:**
  - id - String represent sheet id
  - columns - List of Columns:
    - id - String represent column id
    - name - String represent column name
    - type - String represent ColumnType (possible values: `STRING`,`INT`, `DOUBLE`, `BOOLEAN`)
    - sheetId - String represent sheet id
    - cells - List of Cells
      - id - String represent column name
      - rowIndex - Integer represent column index
      - value - Object represent cell value
      - columnId - String represent column id 
      - isWithLookup - Boolean indication for if value is a lookup
- **Example Response:**
```json
  {
      "id": "<UUID>", //unique id for sheet
      "columns": [
          {
              "id": "<UUID>", //unique id for column with name `Title` 
              "name": "Title",
              "type": "STRING",
              "sheetId": "<UUID>", //unique id for sheet
              "cells": [
                  {
                    "id": "<UUID>", //unique id for cell
                    "rowIndex": 1,
                    "value": "GO Developer",
                    "columnId": "<UUID>", //unique id for column with name `Title` 
                    "isWithLookup": false
                  }
              ]
          },
      ]
  }
```
    

<a name="system-overview"/>

## System Overview
The system is a lightweight HTTP server to manage spreadsheet-like data with features for creating, updating, and querying sheets. To ensure robustness:
- **Transaction Management:** A unique transactionId is auto-generated for all CREATE, UPDATE, and DELETE operations to maintain operation traceability and integrity.
- **Error Handling and Resiliency:** Incorporates a Circuit Breaker pattern to handle repeated errors gracefully and prevent cascading failures.

This design ensures scalability, extensibility, and error resilience while being straightforward to implement.


<a name="core-features"/>

## Core Features

<a name="sheet-functionality"/>

### Sheet Functionality
- **Functionality:**
  - controller with usage as described above
    - getById 
    - setSheet
    - setCell
  - `find(int sheetId)` - return sheet by id with columns from column service
  - `setSheet` - return String unique id for sheet or error
  - `isSheetExist(int sheetId)` - return boolean if sheetId exist in db
  - `setCell` - return `CellResponse` and call lookup service (created for avoiding circular dependencies)

<a name="column-functionality"/>

### Column Functionality
- **Functionality:**
  - `isValidColumnType` - Supports type validation for the value against the column type accetable values: `STRING`,`INT`, `DOUBLE`, `BOOLEAN`
  - `filterBySheetId` - return columns by sheetId
  - `findByNameAndSheetId` - return column by sheetId and sheet name (if there is more then 1 sheet name it throw error)
  - `setColumns` - save columns entity in db

<a name="cell-functionality"/>

### cell-functionality
- **Functionality:**
  - `filterByColumnId` - return list of cells by `columnId`
  - `setCell` - save cell entity in db
  - `findByColumnIdAndRowIndex` - find specific cell by `columnId` and `rowIndex`
  - `findLookupCellValue` - recursive function that find the cell string value or return "Lookup MAX Exceeded"


<a name="lookup-functionality"/>

### Lookup Functionality
- **Functionality:**
  - `isWithLookup` - return boolean if the value is a lookup
  - `getLookupCellValue` - save cell entity in db
  - `findByColumnIdAndRowIndex` - find specific cell by `columnId` and `rowIndex`
  - `validateLookupFunction` - validate target cell value against column type, also for cycles, throws error when max lookup chain exceeded

<a name="error-handling-and-circuit-breaker"/>

## Error Handling and Circuit Breaker
- **Circuit Breaker:**
  - Tracks the number of errors within a defined window (e.g., 5 errors in 1 minute).
  - Moves to an OPEN state if the error threshold is exceeded, temporarily halting further requests.
  - Automatically transitions to a HALF-OPEN state to test if recovery is possible.

### Example States:
- **CLOSED:** All requests are processed normally.
- **OPEN:** Rejects further requests with a 503 Service Unavailable response.
- **HALF-OPEN:** Tests limited requests to check recovery.

<a name="concurrency-and-transaction-management"/>

## Concurrency and Transaction Management
- **Transaction IDs:** Auto-generated for each write operation (`setSheet`, `setCell`).
- **Concurrency Handling:** 
  - Currently lightweight and single-threaded, with future extensions for locks to manage concurrent updates. 
  - Cuurently used for remove sheet from db if column creation fails

<a name="data-structures"/>

## Data Structures
- **Sheet:** Represents the spreadsheet.
  - `id`: Unique identifier.
- **Column:** Represents individual Column data.
  - `id`: Unique identifier.
  - `sheetId`: Refer to Sheet table.
  - `name`: String represet column name.
  - `columnType`: enumarator ColumnType can be 'STRING' | 'INT' | 'DOUBLE' | 'BOOLEAN'.
- **Cell:** Represents individual cell data.
  - `id`: Stored or computed value.
  - `columnId`: Refer to Column table.
  - `rowIndex`: index in the cell.
  - `isWithLookup`: boolean if the value have lookup function.
  - `cellValue`: String that will be parsed to ColumnType 
- **Transaction:** Represents a write operation.
  - `transactionId`: Unique identifier for the operation.
  - `type`: CREATE/UPDATE/DELETE.
  - `timestamp`: Time of the transaction.
  - `status`: SUCCESS/FAILED.

<a name="testing-strategy"/>

## Testing Strategy
1. **Unit Testing**
  - **Sheet Controller:** - validate sheet controller operations
  - **Sheet Service:** - validate sheet service operations
  - **Column Service:** - validate column service operations
  - **Cell Service:** - validate cell service operations
  - **Lookup Service:** - validate lookup service operations
  - Verify the transactionId generation and logging. ?
2. **Integration**
   - **Positive Tests:**
    - Set sheet with column name for each supported types (`STRING`,`INT`, `DOUBLE`, `BOOLEAN`)
    - Set cell with valid params
    - Get sheet and validate response
    - Get sheet with lookup cell and validate response
   - **Negative Tests:** 
    - Set sheet with valid column name and invalid type (i.e `OBJECT`)
    - Set sheet with invalid column name and valid type (i.e `OBJECT`)
    - Set cell with invalid params
    - Set cell with self-referencing lookup.
    - Set cell with circular references across multiple cells.
    - Get sheet that doesnt exist
    - Get sheet with lookup max cycle chains
    - Get sheet with lookup with incompatible type

<a name="architecture"/>

## Architecture
- **Controller:** Represent the `Rest` handler.
- **Dto:** Represent the `Rest` request and responses
- **Service:** Handle business logic and interactions with the data structures.
- **Model:** Represent the entity by the [data structure](#data-structures) provided above.
- **Repository:** Represent the db queries.

<a name="limitations"/>

## Limitations
1. Schema Modifications.
2. Update not supported
3. Delete not supported