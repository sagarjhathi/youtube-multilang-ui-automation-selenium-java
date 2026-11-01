package main.java.yt_multi_lang_ui_validator.fileReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One Excel sheet read once and looked up BY NAME instead of by row number. The key column (for example the
 * language) gives the name of each row; the header row gives the column names.
 *
 * A duplicate name stops at load. A missing name or column throws an error naming the file, the sheet and the
 * name, so a wrong or misplaced row can never be compared silently. Row numbers in messages are the ones Excel shows.
 */
public class SheetLookup {

	/** Sheets already read, so every test class shares one read of each file. */
	private static final Map<String, SheetLookup> CACHE = new HashMap<>();

	private final String fileName;
	private final String sheetName;
	private final Map<String, SheetRow> rows = new LinkedHashMap<>();

	/** One row of a sheet: look a cell up by its column header. */
	public static class SheetRow {

		private final SheetLookup sheet;
		private final String name;
		private final int rowNumber;
		private final Map<String, String> cells;

		private SheetRow(SheetLookup sheet, String name, int rowNumber, Map<String, String> cells) {
			this.sheet = sheet;
			this.name = name;
			this.rowNumber = rowNumber;
			this.cells = cells;
		}

		/** The cell under this header. An empty cell is "" (as FileReader gives it); an unknown header throws. */
		public String get(String columnName) {
			String value = cells.get(columnName);
			if (value == null) {
				throw new IllegalStateException(sheet.where() + " has no column '" + columnName + "' (row '" + name + "'), its columns are " + cells.keySet());
			}
			return value;
		}
	}

	private SheetLookup(String fileName, String sheetName) {
		this.fileName = fileName;
		this.sheetName = sheetName;
	}

	/** Reads the sheet once (later calls with the same file, sheet and key header reuse it). */
	public static synchronized SheetLookup load(String fileName, String sheetName, String keyHeader) {
		String cacheKey = fileName + "|" + sheetName + "|" + keyHeader;
		SheetLookup lookup = CACHE.get(cacheKey);
		if (lookup == null) {
			lookup = fromRows(fileName, sheetName, keyHeader, readCells(fileName, sheetName));
			CACHE.put(cacheKey, lookup);
		}
		return lookup;
	}

	/** Builds a lookup from cell values already in memory (first row = headers). This is what {@link #load} uses, and what the offline tests use. */
	public static SheetLookup fromRows(String fileName, String sheetName, String keyHeader, List<List<String>> cellRows) {
		SheetLookup lookup = new SheetLookup(fileName, sheetName);
		if (cellRows.isEmpty()) {
			throw new IllegalStateException(lookup.where() + " is empty, it needs a header row");
		}
		List<String> headers = new ArrayList<>();
		for (String header : cellRows.get(0)) {
			String name = header.trim();
			if (name.isEmpty() || headers.contains(name)) {
				throw new IllegalStateException(lookup.where() + " has a blank or repeated column header (" + cellRows.get(0) + "), columns are used by name");
			}
			headers.add(name);
		}
		int keyColumn = headers.indexOf(keyHeader);
		if (keyColumn < 0) {
			throw new IllegalStateException(lookup.where() + " has no column '" + keyHeader + "', its columns are " + headers);
		}
		for (int row = 1; row < cellRows.size(); row++) {
			List<String> values = cellRows.get(row);
			String name = keyColumn < values.size() ? values.get(keyColumn).trim() : "";
			if (name.isEmpty()) {
				throw new IllegalStateException(lookup.where() + " row " + (row + 1) + " has no value in '" + keyHeader + "'");
			}
			Map<String, String> cells = new LinkedHashMap<>();
			for (int column = 0; column < headers.size(); column++) {
				cells.put(headers.get(column), column < values.size() ? values.get(column).trim() : "");
			}
			SheetRow existing = lookup.rows.get(name);
			if (existing != null) {
				throw new IllegalStateException(lookup.where() + " has '" + name + "' twice (rows " + existing.rowNumber + " and " + (row + 1) + ")");
			}
			lookup.rows.put(name, new SheetRow(lookup, name, row + 1, cells));
		}
		return lookup;
	}

	private static List<List<String>> readCells(String fileName, String sheetName) {
		FileReader reader = new FileReader();
		try {
			reader.loadWorkbook(fileName);
			reader.loadSheet(sheetName);
			int columns = reader.getColumnCount(0);
			List<List<String>> cellRows = new ArrayList<>();
			for (int row = 0; row < reader.getRowCount(); row++) {
				List<String> values = new ArrayList<>();
				for (int column = 0; column < columns; column++) {
					values.add(reader.getCellValue(row, column));
				}
				cellRows.add(values);
			}
			return cellRows;
		} finally {
			reader.closeWorkbook();
		}
	}

	/** The row of this name. A name that is not in the sheet throws. */
	public SheetRow get(String name) {
		SheetRow row = name == null ? null : rows.get(name.trim());
		if (row == null) {
			throw new IllegalStateException(where() + " has no row for '" + name + "'");
		}
		return row;
	}

	/** The names in sheet order. */
	public List<String> names() {
		return new ArrayList<>(rows.keySet());
	}

	public int size() {
		return rows.size();
	}

	/** The file and sheet, for messages, e.g. data/Country_Name_Code.xlsx (sheet 'CountryNameCode'). */
	public String source() {
		return where();
	}

	private String where() {
		return fileName + " (sheet '" + sheetName + "')";
	}
}
