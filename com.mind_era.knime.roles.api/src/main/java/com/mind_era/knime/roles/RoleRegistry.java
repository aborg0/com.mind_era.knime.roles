/* Copyright © 2013 Mind Eratosthenes Kft.
 * Licence: http://www.apache.org/licenses/LICENSE-2.0
 */
package com.mind_era.knime.roles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * The role registry class will be offered as a service to perform various role
 * specific tasks, checks.
 * 
 * Based on the following article:
 * http://www.eclipsezone.com/eclipse/forums/t97608.rhtml
 * 
 * @author Gabor Bakos
 */
public class RoleRegistry {
	private final Collection<Role> roles;
	private final Map<String, Role> representations;
	private final Map<String, Collection<Role>> alternativeNames;

	private final Map<String, Role> roleClassNameToRoles = new HashMap<>();

	private final Map<Role, Collection<String>> providerIds;
	private final Map<Role, Collection<String>> visualiserIds;
	private final Map<Role, Collection<String>> consumerIds;
	{
		final IExtensionRegistry extensionRegistry = Platform
				.getExtensionRegistry();
		final IConfigurationElement[] registerConfigurationElements = extensionRegistry
				.getConfigurationElementsFor("com.mind_era.knime.roles.Register");
		final List<Role> registered = new ArrayList<Role>();
		final Map<String, Role> representations = new TreeMap<>();
		final Map<String, Collection<Role>> alternativeNames = new TreeMap<>();
		for (final Role role : PredefinedRoles.values()) {
			registered.add(role);
			representations.put(role.representation(), role);
		}
		for (final IConfigurationElement registerElement : registerConfigurationElements) {
			try {
				final Role role = (Role) registerElement
						.createExecutableExtension("class");
				final String rep = role.representation();
				if (representations.containsKey(rep)) {
					throw new IllegalStateException(
							"Multiple roles registered with same representations: "
									+ representations.get(rep) + " and " + role);
				}
				registered.add(role);
			} catch (final CoreException e) {
				throw new IllegalStateException(e);
			}
		}
		for (final Role role : registered) {
			for (final String name : role.alternativeNames()) {
				if (!alternativeNames.containsKey(name)) {
					alternativeNames.put(name, new ArrayList<Role>());
				}
				alternativeNames.get(name).add(role);
			}
			roleClassNameToRoles.put(role.getClass().getName(), role);
		}
		roles = Collections.unmodifiableList(registered);
		this.representations = Collections.unmodifiableMap(representations);
		this.alternativeNames = Collections.unmodifiableMap(alternativeNames);
		final IConfigurationElement[] providerConfigurationElements = extensionRegistry
				.getConfigurationElementsFor("com.mind_era.knime.roles.Provider");
		this.providerIds = Collections
				.unmodifiableMap(parseConfigElements(providerConfigurationElements));
		final IConfigurationElement[] consumerConfigurationElements = extensionRegistry
				.getConfigurationElementsFor("com.mind_era.knime.roles.Consumer");
		this.consumerIds = Collections
				.unmodifiableMap(parseConfigElements(consumerConfigurationElements));
		final IConfigurationElement[] visualizerConfigurationElements = extensionRegistry
				.getConfigurationElementsFor("com.mind_era.knime.roles.Visualiser");
		this.visualiserIds = Collections
				.unmodifiableMap(parseConfigElements(visualizerConfigurationElements));
	}

	/**
	 * Parses the {@link IConfigurationElement}s found to node factory ids and
	 * {@link Role}s.
	 * 
	 * @param configurationElements
	 *            Some {@link IConfigurationElement}s with
	 *            {@code ((NodeFactoryReference , (RoleReference)+))+}
	 *            structure.
	 * @return A {@link Map} from {@link Role}s to the node factory ids.
	 */
	private Map<Role, ? extends Collection<String>> parseConfigElements(
			final IConfigurationElement[] configurationElements) {
		final Map<Role, Collection<String>> retIds = new HashMap<>(
				configurationElements.length);
		for (final IConfigurationElement configurationElement : configurationElements) {
			final IConfigurationElement[] nodeFactoryAndRoles = configurationElement
					.getChildren();
			for (final IConfigurationElement nodeFactoryAndRole : nodeFactoryAndRoles) {
				final IConfigurationElement[] children = nodeFactoryAndRole
						.getChildren();
				final IConfigurationElement nodeFactoryReference = children[0];
				final String nodeFactoryId = nodeFactoryReference
						.getAttribute("id");
				for (final IConfigurationElement roleReference : children[1]
						.getChildren()) {
					final String className = roleReference
							.getAttribute("class");
					final Role role = roleClassNameToRoles.get(className);
					if (!retIds.containsKey(role)) {
						retIds.put(role, new LinkedHashSet<String>());
					}
					retIds.get(role).add(nodeFactoryId);
				}
			}
		}
		return retIds;
	}

	/**
	 * @return the roles
	 */
	public Collection<Role> roles() {
		return roles;
	}

	/**
	 * @param representation
	 *            the {@link Role#representation()} of the {@link Role}.
	 * @return the {@link Role} with the specified {@code representation} (
	 *         {@link Role#representation()})
	 * @throws NoSuchElementException
	 *             When no {@link Role} with the specified
	 *             {@code representation} exists in the registry.
	 */
	public Role role(final String representation) throws NoSuchElementException {
		if (representations.containsKey(representation)) {
			return representations.get(representation);
		}
		throw new NoSuchElementException(representation);
	}

	/**
	 * @param columnName
	 *            A column name.
	 * @return A possible empty (but always unmodifiable) {@link Collection} of
	 *         the {@link Role}s that could be used for that column. The order
	 *         of the {@link Role}s within it has no information.
	 */
	public Collection<Role> possibleRolesForName(final String columnName) {
		return safeGet(columnName, alternativeNames);
	}

	/**
	 * @param role
	 *            A {@link Role}.
	 * @return The id of the node factories (optionally) providing output with
	 *         {@code role}.
	 */
	public Collection<String> providerNodeFactoriesFor(final Role role) {
		return safeGet(role, providerIds);
	}

	/**
	 * @param role
	 *            A {@link Role}.
	 * @return The id of the node factories (optionally) providing visualisation
	 *         for {@code role}.
	 */
	public Collection<String> visualiserNodeFactoriesFor(final Role role) {
		return safeGet(role, visualiserIds);
	}

	/**
	 * @param role
	 *            A {@link Role}.
	 * @return The id of the node factories (optionally) consuming input with
	 *         {@code role}.
	 */
	public Collection<String> consumerNodeFactoriesFor(final Role role) {
		return safeGet(role, consumerIds);
	}

	/**
	 * @param role
	 * @return
	 */
	private static <K, V> Collection<V> safeGet(final K key,
			final Map<K, ? extends Collection<V>> map) {
		if (!map.containsKey(key)) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableCollection(map.get(key));
	}
}
