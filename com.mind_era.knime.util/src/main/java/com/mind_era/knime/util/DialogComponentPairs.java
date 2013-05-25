/* Copyright © 2013 Mind Eratosthenes Kft.
 * Licence: http://knime.org/downloads/full-license
 */
package com.mind_era.knime.util;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import org.knime.core.data.DataCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.util.Pair;

/**
 * A {@link DialogComponent} to handle adding a list of pairs as a
 * configuration.
 * 
 * @author Gabor Bakos
 * @param <Left>
 *            The type of the left values.
 * @param <Right>
 *            The type of the right values.
 */
public class DialogComponentPairs<Left extends DataCell, Right extends DataCell>
		extends DialogComponent {

	private javax.swing.table.DefaultTableModel tableModel;
	private final EnumSet<Columns> visibleColumns;

	private static final int LEFT_COL = 0;
	private static final int RIGHT_COL = LEFT_COL + 1;
	private static final int ADD_COL = RIGHT_COL + 1;
	private static final int REMOVE_COL = ADD_COL + 1;
	private static final int UP_COL = REMOVE_COL + 1;
	private static final int DOWN_COL = UP_COL + 1;
	private static final int ENABLE_COL = DOWN_COL + 1;
	private static final int colCount = ENABLE_COL + 1;

	/**
	 * Optional columns in the {@link DialogComponentPairs}.
	 */
	public enum Columns {
		/** Add new row above */
		Add,
		/** Remove current row */
		Remove,
		/** Move current row up (switch with above) */
		Up,
		/** Move current row down (switch with below) */
		Down,
		/** Enable row in the output values */
		Enable;
	}

	/**
	 * Constructs a {@link DialogComponentPairs}.
	 * 
	 * @param model
	 *            The corresponding {@link SettingsModelPairs} object.
	 * @param leftHeader
	 *            The header text for the left column.
	 * @param rightHeader
	 *            The header text for the right column.
	 * @param visibleColumns
	 *            The visible columns.
	 */
	public DialogComponentPairs(final SettingsModelPairs<Left, Right> model,
			final String leftHeader, final String rightHeader,
			final EnumSet<Columns> visibleColumns) {
		super(model);
		this.visibleColumns = visibleColumns.clone();
		final JPanel controls = new JPanel();
		final JTable table = new JTable();
		// table.setAutoCreateColumnsFromModel(false);
		table.getTableHeader().setReorderingAllowed(false);
		tableModel = (javax.swing.table.DefaultTableModel) table.getModel();
		tableModel.setColumnIdentifiers(new Object[] { "left", "right", "Add",
				"Del", "Up", "Down", "\u2713" });
		tableModel.setNumRows(1);
		tableModel.setColumnCount(colCount);
		controls.add(new JScrollPane(table));
		final TableColumnModel colModel = table.getColumnModel();
		colModel.getColumn(LEFT_COL).setCellRenderer(
				model.getLeftType().getRenderer(null));
		colModel.getColumn(RIGHT_COL).setCellRenderer(
				model.getRightType().getRenderer(null));
		final int maxColWidth = 44;
		hide(ADD_COL, Columns.Add, colModel, maxColWidth);
		hide(REMOVE_COL, Columns.Remove, colModel, maxColWidth);
		hide(UP_COL, Columns.Up, colModel, maxColWidth);
		hide(DOWN_COL, Columns.Down, colModel, maxColWidth);
		hide(ENABLE_COL, Columns.Enable, colModel, maxColWidth);
		for (int i = RIGHT_COL + 1; i < colCount; ++i) {
			colModel.getColumn(i).setModelIndex(i);
		}
		controls.setName("controls");
		final javax.swing.JScrollPane pane = new javax.swing.JScrollPane(
				controls);
		pane.setPreferredSize(new java.awt.Dimension(500, 300));
		final javax.swing.Action addAction = new javax.swing.AbstractAction("+") {
			private static final long serialVersionUID = -6930940431718125770L;

			@Override
			public void actionPerformed(final java.awt.event.ActionEvent event) {
				tableModel.insertRow(
						Integer.parseInt(event.getActionCommand()),
						new Object[] { null, null, "+", "-", "^", "v",
								!visibleColumns.contains(Columns.Enable) });
			}
		};
		tableModel.setValueAt("+", 0, ADD_COL);

		ButtonColumn.install(table, addAction, ADD_COL);
		final Action toggleEnableAction = new AbstractAction("toggleEnable") {
			private static final long serialVersionUID = 6573077582975369749L;
			private static final int column = ENABLE_COL;

			@Override
			public void actionPerformed(final java.awt.event.ActionEvent event) {
				final int row = Integer.parseInt(event.getActionCommand());
				final Object value = tableModel.getValueAt(row, column);
				if (value instanceof Boolean) {
					final Boolean enabled = (Boolean) value;
					tableModel.setValueAt(
							Boolean.valueOf(!enabled.booleanValue()), row,
							column);
				}
			}
		};
		ButtonColumn.installCheckBox(table, toggleEnableAction, ENABLE_COL);

		ButtonColumn.install(table, new AbstractAction("-") {
			private static final long serialVersionUID = -7892386498201126105L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				tableModel.removeRow(Integer.parseInt(e.getActionCommand()));
			}
		}, REMOVE_COL);
		ButtonColumn.install(table, new AbstractAction("^") {
			private static final long serialVersionUID = 386788743311420037L;

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void actionPerformed(final ActionEvent e) {
				final int row = Integer.parseInt(e.getActionCommand());
				final Vector vector = tableModel.getDataVector();
				if (row > 0) {
					final Object r = vector.remove(row);
					vector.insertElementAt(r, row - 1);
					tableModel.fireTableDataChanged();
				}
			}
		}, UP_COL);
		ButtonColumn.install(table, new AbstractAction("v") {
			private static final long serialVersionUID = -9202428866774266726L;

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void actionPerformed(final ActionEvent e) {
				final int row = Integer.parseInt(e.getActionCommand());
				final Vector vector = tableModel.getDataVector();
				if (row < tableModel.getRowCount() - 2) {
					final Object r = vector.remove(row + 1);
					vector.insertElementAt(r, row);
					tableModel.fireTableDataChanged();
				}
			}
		}, DOWN_COL);
		controls.setPreferredSize(new Dimension(500, 300));
		// TODO add button to remove unused (not enabled) pairs
		getComponentPanel().add(pane);
		// http://blog.eclipse-tips.com/2008/02/eclipse-icons.html
		// final Image addIcon = PlatformUI.getWorkbench().getSharedImages()
		// .getImage(ISharedImages.IMG_OBJ_ADD);

	}

	/**
	 * Auto-hides a column based on {@link #visibleColumns} and the
	 * {@code colKey}.
	 * 
	 * @param colIndex
	 *            The index of column.
	 * @param colKey
	 *            The {@link Columns} value which represents this column in
	 *            {@link #visibleColumns}.
	 * @param colModel
	 *            The {@link TableColumnModel}.
	 * @param maxColWidth
	 *            The maximum width when visible.
	 */
	protected void hide(final int colIndex, final Columns colKey,
			final TableColumnModel colModel, final int maxColWidth) {
		colModel.getColumn(colIndex).setMaxWidth(
				this.visibleColumns.contains(colKey) ? maxColWidth : 0);
		colModel.getColumn(colIndex).setMinWidth(
				this.visibleColumns.contains(colKey) ? maxColWidth : 0);
		colModel.getColumn(colIndex).setPreferredWidth(
				this.visibleColumns.contains(colKey) ? maxColWidth : 0);
		colModel.getColumn(colIndex).setWidth(
				this.visibleColumns.contains(colKey) ? maxColWidth : 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.defaultnodesettings.DialogComponent#updateComponent()
	 */
	@Override
	protected void updateComponent() {
		final SettingsModelPairs<?, ?> model = (SettingsModelPairs<?, ?>) getModel();
		int i = 0;
		tableModel.setRowCount(0);
		tableModel.setRowCount(1);
		tableModel.setValueAt("+", 0, ADD_COL);
		for (final Pair<?, ?> pair : model.getValues()) {
			tableModel.insertRow(i, new Object[] { null, null, "+", "-", "^",
					"v", "" });
			if (pair.getFirst() instanceof StringCell) {
				final StringCell left = (StringCell) pair.getFirst();
				tableModel.setValueAt(left, i, LEFT_COL);

			} else if (pair.getFirst() instanceof String) {
				final String left = (String) pair.getFirst();
				tableModel.setValueAt(new StringCell(left), i, LEFT_COL);
			}
			if (pair.getSecond() instanceof StringCell) {
				final StringCell right = (StringCell) pair.getSecond();
				tableModel.setValueAt(right, i, RIGHT_COL);

			} else if (pair.getSecond() instanceof String) {
				final String right = (String) pair.getSecond();
				tableModel.setValueAt(right, i, RIGHT_COL);
			}
			tableModel.setValueAt(model.getEnabledRows().get(i), i, ENABLE_COL);
			++i;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.DialogComponent#
	 * validateSettingsBeforeSave()
	 */
	@Override
	protected void validateSettingsBeforeSave() throws InvalidSettingsException {
		updateModel();
	}

	/**
	 * Updates the {@link #getModel()}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void updateModel() {
		final int rowCount = tableModel.getRowCount() - 1;
		final BitSet enabledRows = new BitSet();
		final List<Pair<Left, Right>> values = new ArrayList<>(rowCount);
		for (int i = 0; i < rowCount; ++i) {
			enabledRows.set(i, (Boolean) tableModel.getValueAt(i, 6));
			final Object leftVal = tableModel.getValueAt(i, 0);
			final Object rightVal = tableModel.getValueAt(i, 1);
			final StringCell left = convert(leftVal), right = convert(rightVal);
			final Pair pair = new Pair(left, right);
			values.add(pair);
		}
		final SettingsModelPairs<Left, Right> model = (SettingsModelPairs<Left, Right>) getModel();
		model.setValues(values);
		model.setEnabledRows(enabledRows);
	}

	/**
	 * Converts a value to {@link StringCell}.
	 * 
	 * @param val
	 *            An {@link Object}.
	 * @return {@code val} as a {@link StringCell}, or {@code null}, if it was
	 *         {@code null}.
	 */
	private static StringCell convert(final Object val) {
		if (val instanceof String) {
			return new StringCell((String) val);
		}
		if (val instanceof StringCell) {
			return (StringCell) val;
		}
		if (val == null) {
			return null;
		}
		return new StringCell(val.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.DialogComponent#
	 * checkConfigurabilityBeforeLoad(org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	protected void checkConfigurabilityBeforeLoad(final PortObjectSpec[] specs)
			throws NotConfigurableException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.defaultnodesettings.DialogComponent#setEnabledComponents
	 * (boolean)
	 */
	@Override
	protected void setEnabledComponents(final boolean enabled) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.defaultnodesettings.DialogComponent#setToolTipText
	 * (java.lang.String)
	 */
	@Override
	public void setToolTipText(final String text) {
		// TODO Auto-generated method stub

	}
}
