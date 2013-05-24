/* Copyright © 2013 Mind Eratosthenes Kft.
 * Licence: http://knime.org/downloads/full-license
 */
package com.mind_era.knime.util;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
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
 * @param <Right>
 */
public class DialogComponentPairs<Left extends DataCell, Right extends DataCell>
		extends DialogComponent {

	private javax.swing.table.DefaultTableModel tableModel;

	/**
	 * @param model
	 * @param leftHeader
	 * @param rightHeader
	 */
	public DialogComponentPairs(final SettingsModelPairs<Left, Right> model,
			final String leftHeader, final String rightHeader) {
		super(model);
		final javax.swing.JPanel controls = new javax.swing.JPanel();
		final javax.swing.JTable table = new javax.swing.JTable(/*
																 * new
																 * javax.swing
																 * .table.
																 * DefaultTableModel
																 * (){ public
																 * boolean
																 * isCellEditable
																 * (int row, int
																 * column) {
																 * return false;
																 * } }
																 */);
		// table.setAutoCreateColumnsFromModel(false);
		table.getTableHeader().setReorderingAllowed(false);
		tableModel = (javax.swing.table.DefaultTableModel) table.getModel();
		tableModel.setColumnIdentifiers(new Object[] { "left", "right", "Add",
				"Del", "Up", "Down", "\u2713" });
		tableModel.setNumRows(1);
		tableModel.setColumnCount(7);
		controls.add(new javax.swing.JScrollPane(table));
		final javax.swing.table.TableColumnModel colModel = table
				.getColumnModel();// new
									// javax.swing.table.DefaultTableColumnModel();
		colModel.getColumn(2).setMaxWidth(50);
		colModel.getColumn(3).setMaxWidth(50);
		colModel.getColumn(4).setMaxWidth(50);
		colModel.getColumn(5).setMaxWidth(50);
		colModel.getColumn(6).setMaxWidth(50);
		/*
		 * colModel.setColumnSelectionAllowed(false);
		 * //table.setColumnModel(colModel);
		 * //table.getTableHeader().setVisible(true); final
		 * javax.swing.table.TableColumn leftColumn = new
		 * javax.swing.table.TableColumn(); colModel.addColumn(leftColumn);
		 * leftColumn.setHeaderValue("left"); //table.addColumn(leftColumn);
		 * final javax.swing.table.TableColumn rightColumn = new
		 * javax.swing.table.TableColumn(); rightColumn.setHeaderValue("right");
		 * colModel.addColumn(rightColumn); //table.addColumn(rightColumn);
		 * javax.swing.table.TableColumn addColumn = new
		 * javax.swing.table.TableColumn(); addColumn.setHeaderValue("Add");
		 * addColumn.setWidth(10); colModel.addColumn(addColumn);
		 */
		// table.addColumn(addColumn);
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
						new Object[] { null, null, "+", "-", "^", "v", false });
			}
		};
		tableModel.setValueAt("+", 0, 2);

		// table.setEnabled(false);
		/*
		 * table.setDefaultEditor(Object.class, new
		 * javax.swing.DefaultCellEditor(new javax.swing.JTextField()){ public
		 * java.awt.Component getTableCellEditorComponent(javax.swing.JTable
		 * table, Object value, boolean isSelected, int row, int column){ if
		 * (value == null) { return new javax.swing.JPanel(); } if (value
		 * instanceof java.awt.Component) { return (java.awt.Component)value; }
		 * return new javax.swing.JLabel(row + " - " + column); } });
		 * table.setDefaultRenderer(Object.class, new
		 * javax.swing.table.TableCellRenderer(){ public java.awt.Component
		 * getTableCellRendererComponent(javax.swing.JTable table, Object value,
		 * boolean isSelected, boolean hasFocus, int row, int column) { if
		 * (value == null) { return new javax.swing.JPanel(); } if (value
		 * instanceof java.awt.Component) { return (java.awt.Component)value; }
		 * return new javax.swing.JLabel(row + " - " + column); } });
		 */
		new com.mind_era.knime.util.ButtonColumn(table, addAction, 2);
		final javax.swing.Action toggleEnableAction = new javax.swing.AbstractAction(
				"toggleEnable") {
			private static final long serialVersionUID = 6573077582975369749L;
			private static final int column = 6;

			@Override
			public void actionPerformed(final java.awt.event.ActionEvent event) {
				System.out.println("toggle " + event.getActionCommand());
				// javax.swing.JOptionPane.showMessageDialog(null, "hello");
				final int row = Integer.parseInt(event.getActionCommand());
				tableModel.setValueAt(
						!(Boolean) tableModel.getValueAt(row, column), row,
						column);
			}
		};
		new com.mind_era.knime.util.ButtonColumn(table, toggleEnableAction, 6) {
			@Override
			protected javax.swing.AbstractButton createButton() {
				return new javax.swing.JCheckBox();
			}
		};

		new ButtonColumn(table, new AbstractAction("-") {

			@Override
			public void actionPerformed(final ActionEvent e) {
				tableModel.removeRow(Integer.parseInt(e.getActionCommand()));
			}
		}, 3);
		new ButtonColumn(table, new AbstractAction("^") {
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
		}, 4);
		new ButtonColumn(table, new AbstractAction("v") {

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
		}, 5);
		controls.setPreferredSize(new Dimension(500, 300));
		// TODO add button to remove unused (not enabled) pairs
		// controls.add(table);
		getComponentPanel().add(pane);
		// http://blog.eclipse-tips.com/2008/02/eclipse-icons.html
		final Image addIcon = PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_OBJ_ADD);

		// m.setValueAt(new JButton("+"), 0, 2);
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
		for (final Pair<?, ?> pair : model.getValues()) {
			tableModel.setValueAt(new StringCell((String) pair.getFirst()), i,
					0);
			tableModel.setValueAt(new StringCell((String) pair.getSecond()), i,
					1);
			tableModel.setValueAt(model.getEnabledRows().get(i), i, 6);
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
		final int rowCount = tableModel.getRowCount() - 1;
		final BitSet enabledRows = new BitSet();
		final List<Pair<Left, Right>> values = new ArrayList<>(rowCount);
		for (int i = 0; i < rowCount; ++i) {
			enabledRows.set(i, (Boolean) tableModel.getValueAt(i, 6));
			final Object leftVal = tableModel.getValueAt(i, 0);
			final Object rightVal = tableModel.getValueAt(i, 1);
			if (rightVal instanceof String) {
				final String rightStr = (String) rightVal;
				if (leftVal instanceof String) {
					final String leftStr = (String) leftVal;
					values.add(new Pair(new StringCell(leftStr),
							new StringCell(rightStr)));
				}
			}
		}
		final SettingsModelPairs<Left, Right> model = (SettingsModelPairs<Left, Right>) getModel();
		model.setValues(values);
		model.setEnabledRows(enabledRows);
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
