package careplus.client.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class SimpleTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;

    public interface RowMapper {
        Object valueAt(Object row, int column);
    }

    private final String[] columns;
    private final RowMapper rowMapper;
    private List<?> rows = new ArrayList<Object>();

    public SimpleTableModel(String[] columns, RowMapper rowMapper) {
        this.columns = columns;
        this.rowMapper = rowMapper;
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return rowMapper.valueAt(rows.get(rowIndex), columnIndex);
    }

    public Object getRow(int rowIndex) {
        return rows.get(rowIndex);
    }

    public void setRows(List<?> rows) {
        this.rows = rows == null ? new ArrayList<Object>() : rows;
        fireTableDataChanged();
    }
}
