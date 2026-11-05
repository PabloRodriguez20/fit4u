package com.fit4u.ui;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.function.Function;

/** Modelo de tabla de solo lectura: cada columna saca su valor de la fila con una función. */
final class ModeloTabla<T> extends AbstractTableModel {

    private final String[] columnas;
    private final List<Function<T, Object>> valores;
    private List<T> filas = List.of();

    ModeloTabla(String[] columnas, List<Function<T, Object>> valores) {
        this.columnas = columnas;
        this.valores = valores;
    }

    void setFilas(List<T> filas) {
        this.filas = filas;
        fireTableDataChanged();
    }

    T fila(int indice) {
        return filas.get(indice);
    }

    @Override
    public int getRowCount() {
        return filas.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int columna) {
        return columnas[columna];
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        return valores.get(columna).apply(filas.get(fila));
    }
}
