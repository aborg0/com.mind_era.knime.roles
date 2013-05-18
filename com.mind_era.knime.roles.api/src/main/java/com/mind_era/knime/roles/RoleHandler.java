/**
 * 
 */
package com.mind_era.knime.roles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;

/**
 * Gets or sets properties for columns.
 * 
 * @author Gabor Bakos
 */
public class RoleHandler {

	private final RoleRegistry registry;

	/**
	 * 
	 */
	public RoleHandler(final RoleRegistry registry) {
		super();
		this.registry = registry;
	}

	public Map<String, Collection<? extends Role>> roles(
			final DataTableSpec spec) {
		final Map<String, Collection<? extends Role>> ret = new TreeMap<>();
		for (final DataColumnSpec dataColumnSpec : spec) {
			final String property = dataColumnSpec.getProperties().getProperty(
					Role.PROPERTY_KEY);
			if (property != null) {
				ret.put(dataColumnSpec.getName(), rolesOfProperty(property));
			}
		}
		return ret;
	}

	/**
	 * @param property
	 * @return
	 */
	private List<? extends Role> rolesOfProperty(final String property) {
		return map(property.split(";"));
	}

	private List<? extends Role> rolesOfSpec(final DataColumnSpec spec) {
		final String property = spec.getProperties().getProperty(
				Role.PROPERTY_KEY);
		if (property == null) {
			return Collections.emptyList();
		}
		return rolesOfProperty(property);
	}

	/**
	 * @param list
	 * @return
	 */
	private List<? extends Role> map(final String... list) {
		final List<Role> ret = new ArrayList<Role>(list.length);
		for (final String string : list) {
			ret.add(registry.role(string));
		}
		return Collections.unmodifiableList(ret);
	}

	public DataColumnSpec addRoles(final DataColumnSpec spec,
			final Role... roles) {
		final DataColumnSpecCreator creator = new DataColumnSpecCreator(spec);
		final List<Role> rolesOfSpec = new ArrayList<>(rolesOfSpec(spec));
		for (final Role role : roles) {
			boolean found = false;
			for (final Role currentRole : rolesOfSpec) {
				found |= currentRole.equals(role);
			}
			if (!found) {
				rolesOfSpec.add(role);
			}
		}
		final String rolesAsString = rolesToString(rolesOfSpec);
		creator.setProperties(spec.getProperties().cloneAndOverwrite(
				Collections.singletonMap(Role.PROPERTY_KEY, rolesAsString)));
		return creator.createSpec();
	}

	public String rolesToString(final Role... roles) {
		return rolesToString(Arrays.asList(roles));
	}

	/**
	 * @param roles
	 * @return
	 */
	public String rolesToString(final Iterable<Role> roles) {
		final StringBuilder ret = new StringBuilder();
		for (final Iterator<Role> iterator = roles.iterator(); iterator
				.hasNext();) {
			final Role role = iterator.next();
			ret.append(role.representation());
			if (iterator.hasNext()) {
				ret.append(';');
			}
		}
		return ret.toString();
	}
}
