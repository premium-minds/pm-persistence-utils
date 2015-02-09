/**
 * Copyright (C) 2015 Premium Minds.
 *
 * This file is part of pm-persistence-utils.
 *
 * pm-persistence-utils is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * pm-persistence-utils is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with pm-persistence-utils. If not, see <http://www.gnu.org/licenses/>.
 */

package com.premiumminds.persistence;

import java.util.List;
import java.util.Map;

public class JpaHelper {
	/**
	 * Transforms the list returned in a getResultList() in a Map with the first two fields returned by that query.
	 * @param list with the array of objects to be saved
	 * @param map where objects will be saved. Has to be initialized before with the right types of the objects in the list.
	 * @return map
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> transformListToMap(List<Object[]> list, Map<K, V> map) {
		for (Object[] elem : list) {
			map.put((K)elem[0], (V)elem[1]);
		}
		return map;
	}
}
