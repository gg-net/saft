/*
 * Copyright (C) 2014 GG-Net GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.ggnet.saft.sample.search;

import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 *
 * @author oliver.guenther
 */
@AllArgsConstructor
@ToString
public class MicroUnitDossier implements SearchResult {

    public final int uniqueUnitId;
    
    public final long dossierId;
    
    public final String shortDescription;
    
    @Override
    public String shortDescription() {
        return shortDescription;
    }
    
}
