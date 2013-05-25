/* Copyright © 2013 Mind Eratosthenes Kft.
 * Licence: http://www.apache.org/licenses/LICENSE-2.0
 */
package com.mind_era.knime.roles.nodes.set;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.util.Pair;

import com.mind_era.knime.roles.PredefinedRoles;
import com.mind_era.knime.roles.Role;
import com.mind_era.knime.roles.RoleHandler;
import com.mind_era.knime.roles.RoleRegistry;
import com.mind_era.knime.util.SettingsModelPairs;

/**
 * This is the model implementation of SetRoles. Allows you to assign roles to
 * specific columns.
 * 
 * @author Gabor Bakos
 */
public class SetRolesNodeModel extends NodeModel {
	static final String CFGKEY_ROLE = "role";
	static final String DEFAULT_ROLE = PredefinedRoles.label.representation();

	private SettingsModelPairs<StringCell, StringCell> role = new SettingsModelPairs<>(
			CFGKEY_ROLE, StringCell.TYPE, StringCell.TYPE,
			Collections.<Pair<StringCell, StringCell>> emptyList(), false,
			false);

	/**
	 * Constructor for the node model.
	 */
	protected SetRolesNodeModel() {
		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		final Collection<Pair<StringCell, StringCell>> pairs = role
				.getEnabledPairs();
		final Map<String, Collection<Role>> colNamesToRoles = colNamesToRoles(pairs);
		final RoleHandler roleHandler = new RoleHandler(new RoleRegistry());

		final DataColumnSpec[] newColSpecs = new DataColumnSpec[inData[0]
				.getDataTableSpec().getNumColumns()];
		for (int i = newColSpecs.length; i-- > 0;) {
			final DataColumnSpec origSpec = inData[0].getDataTableSpec()
					.getColumnSpec(i);
			final String name = origSpec.getName();
			final Collection<? extends Role> roles = colNamesToRoles
					.containsKey(name) ? colNamesToRoles.get(name)
					: Collections.<Role> emptyList();
			newColSpecs[i] = roleHandler.setRoles(origSpec, roles);
		}
		final DataTableSpec newSpec = new DataTableSpec(newColSpecs);
		return new BufferedDataTable[] { exec.createSpecReplacerTable(
				inData[0], newSpec) };
	}

	/**
	 * @param pairs
	 * @return
	 */
	private Map<String, Collection<Role>> colNamesToRoles(
			final Collection<Pair<StringCell, StringCell>> pairs) {
		final Map<String, Collection<Role>> ret = new HashMap<>();
		final RoleRegistry reg = new RoleRegistry();
		for (final Pair<StringCell, StringCell> pair : pairs) {
			final String col = pair.getFirst().getStringValue();
			if (!ret.containsKey(col)) {
				ret.put(col, new LinkedHashSet<Role>());
			}
			ret.get(col).add(reg.role(pair.getSecond().getStringValue()));
		}
		return Collections.unmodifiableMap(ret);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		final Collection<Pair<StringCell, StringCell>> enabledPairs = role
				.getEnabledPairs();
		System.out.println(enabledPairs);
		// TODO: generated method stub
		return new DataTableSpec[] { null };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		role.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		role.loadSettingsFrom(settings);
		for (final Pair<StringCell, StringCell> pair : role.getEnabledPairs()) {
			System.out.println(pair.getFirst().getStringValue() + " - "
					+ pair.getSecond().getStringValue());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		role.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// TODO: generated method stub
	}

}
