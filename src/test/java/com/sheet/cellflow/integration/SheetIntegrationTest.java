package com.sheet.cellflow.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheet.cellflow.cell.dto.CellResponseDto;
import com.sheet.cellflow.cell.model.Cell;
import com.sheet.cellflow.cell.repository.CellRepository;
import com.sheet.cellflow.column.dto.ColumnResponseDto;
import com.sheet.cellflow.column.model.Column;
import com.sheet.cellflow.column.model.ColumnType;
import com.sheet.cellflow.column.repository.ColumnRepository;
import com.sheet.cellflow.exception.CellOperationException;
import com.sheet.cellflow.sheet.dto.CellResponseApiDto;
import com.sheet.cellflow.sheet.dto.SheetResponseDto;
import com.sheet.cellflow.sheet.model.Sheet;
import com.sheet.cellflow.sheet.repository.SheetRepository;

@SpringBootTest
@AutoConfigureMockMvc
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Transactional // Ensures that the database is rolled back after each test
class SheetIntegrationTest {
    private static final String MOCK_SHEET_ID = "sheetId";
    private static final String MOCK_COLUMN_ID = "columnId";
    private static final String MOCK_CELL_ID = "cellId";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SheetRepository sheetRepo;

    @Autowired
    private ColumnRepository columnRepo;

    @Autowired
    private CellRepository cellRepo;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        cleanupTestData(); // Add this to ensure clean state
        populateTestData();
    }

    private void cleanupTestData() {
        cellRepo.deleteAll();
        columnRepo.deleteAll();
        sheetRepo.deleteAll();
    }

    private void populateTestData() {
        // Create and save a mock sheet
        Sheet sheet = new Sheet(MOCK_SHEET_ID);
        sheetRepo.save(sheet);

        // Create and save a mock column
        Column column = new Column(MOCK_COLUMN_ID, MOCK_SHEET_ID, "Title", ColumnType.STRING);
        columnRepo.save(column);

        // Create and save a mock cell
        Cell cell = new Cell(MOCK_CELL_ID, "GO Developer", MOCK_COLUMN_ID, 1, false);
        cellRepo.save(cell);
    }

    // Positive Tests
    @Test
    void testGetSheetAndValidateResponse() throws Exception {
        mockMvc.perform(get("/sheet/" + MOCK_SHEET_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns").exists())
                .andDo((MvcResult result) -> {
                    String jsonRes = result.getResponse().getContentAsString();
                    //sheet layer
                    SheetResponseDto sheetResponseDto = objectMapper.readValue(jsonRes, SheetResponseDto.class);
                    assertEquals(MOCK_SHEET_ID, sheetResponseDto.id());

                    //column layer
                    ColumnResponseDto columnResponseDto = sheetResponseDto.columns().getFirst();
                    assertEquals(MOCK_COLUMN_ID, columnResponseDto.id());
                    assertEquals("Title", columnResponseDto.name());
                    assertEquals(ColumnType.STRING, columnResponseDto.type());
                    assertEquals(ColumnType.STRING, columnResponseDto.type());
                    assertEquals(MOCK_SHEET_ID, columnResponseDto.sheetId());

                    //cell layer
                    CellResponseDto cellResponseDto = columnResponseDto.cells().getFirst();
                    assertEquals(MOCK_CELL_ID, cellResponseDto.id());
                    assertEquals("GO Developer", cellResponseDto.value().toString());
                    assertEquals(1, cellResponseDto.rowIndex());
                    assertEquals(MOCK_COLUMN_ID, cellResponseDto.columnId());
                    assertFalse(cellResponseDto.isWithLookup());
                });
    }

    @Test
    void testSetSheetWithSupportedTypes() throws Exception {
            String requestJson = 
            """
            {
                "columns": [
                    {"name": "COL STRING", "type": "STRING"},
                    {"name": "COL INT", "type": "INT"},
                    {"name": "COL DOUBLE", "type": "DOUBLE"},
                    {"name": "COL BOOLEAN", "type": "BOOLEAN"}
                ]
            }
            """;
        
        mockMvc.perform(post("/sheet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                // .andExpect(status().isCreated())
                .andDo((MvcResult result) -> {
                    String sheetId = result.getResponse().getContentAsString();
                    String uriTemplate = String.format("/sheet/%1$s", sheetId);          
                    mockMvc.perform(get(uriTemplate))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.columns").exists());        
                });
    }

    @Test
    void testSetCellWithValidParams() throws Exception {
        String requestJson = """
            {
                "columnName": "Title",
                "rowIndex": 2,
                "value": "Java developer"
            }
        """;
        
        String uri = String.format("/sheet/%1$s", MOCK_SHEET_ID);
        mockMvc.perform(post(uri + "/cell")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andDo((MvcResult result) -> {
                    String jsonRes = result.getResponse().getContentAsString();
                    CellResponseApiDto cellResponseApiDto = objectMapper.readValue(jsonRes, CellResponseApiDto.class);
                    CellResponseDto cellResponse = cellResponseApiDto.cells().getFirst();
                    assertEquals("Java developer", cellResponse.value());
                    assertEquals(2, cellResponse.rowIndex());
                    assertEquals("columnId", cellResponse.columnId());
                    assertFalse(cellResponse.isWithLookup());

                    mockMvc.perform(get(uri))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.columns").exists());
                });
    }

    @Test
    void testSetCellWithLookup() throws Exception {
        String requestJson = """
            {
                "columnName": "Title",
                "rowIndex": 2,
                "value": "lookup(Title,1)"
            }
        """;
        
        String uri = String.format("/sheet/%1$s", MOCK_SHEET_ID);
        mockMvc.perform(post(uri + "/cell")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andDo((MvcResult result) -> {
                    String jsonRes = result.getResponse().getContentAsString();
                    CellResponseApiDto cellResponseApiDto = objectMapper.readValue(jsonRes, CellResponseApiDto.class);
                    CellResponseDto cellResponse = cellResponseApiDto.cells().getFirst();
                    assertEquals("GO Developer", cellResponse.value());
                    assertEquals(2, cellResponse.rowIndex());
                    assertEquals("columnId", cellResponse.columnId());
                    assertEquals(true, cellResponse.isWithLookup());

                    mockMvc.perform(get(uri))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.columns").exists());
                });
    }

    @Test
    void testGetSheetWithLookupCellAndValidateResponse() throws Exception {
        mockMvc.perform(get("/sheet/" + MOCK_SHEET_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns[0].cells[0].isWithLookup").exists());
    }

    // Negative Tests
    @Test
    void testSetSheetWithInvalidType() throws Exception {
        String requestJson = 
            """
            {
                "columns": [
                    {"name": "COL OBJ", "type": "OBJECT"}
                ]
            }
            """;
        mockMvc.perform(post("/sheet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestJson)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSetSheetWithInvalidColumnName() throws Exception {
        String requestJson = 
            """
            {
                "columns": [
                    {"name": 1, "type": "STRING"}
                ]
            }
            """;
        mockMvc.perform(post("/sheet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestJson)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSetCellWithInvalidParams() throws Exception {
        String requestJson = """
            {
                "columnName": "Title",
                "rowIndex": 2,
                "value": null
            }
        """;
        mockMvc.perform(post("/sheet/sheetId/cell")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestJson)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSetCellWithSelfReferencingLookup() throws Exception {
        String requestJson = """
            {
                "columnName": "Title",
                "rowIndex": 1,
                "value": "lookup(Title,1)"
            }
        """;

        mockMvc.perform(post("/sheet/sheetId/cell")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestJson)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetSheetWithLookupMaxCycleChains() throws Exception {
        // set up

        // Create and save a mock sheet
        Sheet sheet = new Sheet("CIRCULAR_SHEET");
        sheetRepo.save(sheet);

        // With mock column
        Column column = new Column("CIRCULAR_COLUMN_ID", "CIRCULAR_SHEET", "CIRCULAR_COL_NAME", ColumnType.STRING);
        columnRepo.save(column);

        // And a mock cell
        Cell cell = new Cell("CIRCULAR_CELL_ID", "lookup(CIRCULAR_COLUMN_ID,2)", "CIRCULAR_COLUMN_ID", 1, true);
        Cell cell2 = new Cell("CIRCULAR_CELL_ID1", "lookup(CIRCULAR_COLUMN_ID,3)", "CIRCULAR_COLUMN_ID", 2, false);
        Cell cell3 = new Cell("CIRCULAR_CELL_ID2", "GO Developer", "CIRCULAR_COLUMN_ID", 3, false);
        cellRepo.save(cell);
        cellRepo.save(cell2);
        cellRepo.save(cell3);

        String requestJson = """
            {
                "columnName": "CIRCULAR_COL_NAME",
                "rowIndex": 4,
                "value": "lookup(CIRCULAR_COL_NAME,1)"
            }
        """;
        try {
            mockMvc.perform(post("/sheet/CIRCULAR_SHEET/cell")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson));
        } catch(CellOperationException e) {
            //TODO should be in response error
            assertEquals("Max lookup chain reached", e.getMessage());
        } 
        
    }

    @Test
    void testGetSheetThatDoesNotExist() throws Exception {
        mockMvc.perform(get("/sheet/nonExistentSheetId"))
                .andExpect(status().isNotFound());
    }


    @Test
    void testGetSheetWithLookupIncompatibleType() throws Exception {

         // set up

        // Create and save a mock sheet
        Sheet sheet = new Sheet("SHEET_ID");
        sheetRepo.save(sheet);

        // With mock column
        Column column = new Column("COL_ID1", "SHEET_ID", "COL_STRING", ColumnType.STRING);
        Column column2 = new Column("COL_ID2", "SHEET_ID", "COL_INT", ColumnType.INT);
        columnRepo.save(column);
        columnRepo.save(column2);

        // And a mock cell
        Cell cell2 = new Cell("COL_ID1", "STRING", "COL_ID1", 1, false);
        cellRepo.save(cell2);

        String requestJson = """
            {
                "columnName": "COL_INT",
                "rowIndex": 1,
                "value": "lookup(COL_STRING,1)"
            }
        """;
        mockMvc.perform(post("/sheet/sheetId/cell")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestJson)))
                .andExpect(status().isBadRequest());
        }
}