/* Copyright © 2013 Mind Eratosthenes Kft.
 * Licence: http://www.apache.org/licenses/LICENSE-2.0
 */
package com.mind_era.knime.roles.nodes.set;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.mind_era.knime.roles.RoleRegistry;

/**
 * <code>NodeDialog</code> for the "SetRoles" Node. Allows you to assign roles
 * to specific columns.
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Gabor Bakos
 */
public class SetRolesNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring the SetRoles node.
	 */
	protected SetRolesNodeDialog() {
		addDialogComponent(new DialogComponentStringSelection(
				new SettingsModelString(SetRolesNodeModel.CFGKEY_ROLE,
						SetRolesNodeModel.DEFAULT_ROLE), "role",
				new RoleRegistry().roleRepresentations().toArray(new String[0])));
	}
}
