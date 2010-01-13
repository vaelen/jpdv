/*
Japanese Dependency Vectors (jpdv) - A tool for creating Japanese semantic vector spaces.
Copyright (C) 2010 Andrew Young <andrew at vaelen.org>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version. This program is distributed in the
hope that it will be useful, but WITHOUT ANY WARRANTY; without
even the implied warranty of MERCHANTABILITY or FITNESS FOR
A PARTICULAR PURPOSE. See the GNU General Public License
for more details. You should have received a copy of the GNU General
Public License along with this program. If not, see <http://www.gnu.org/licenses/>.

Linking this library statically or dynamically with other modules is
making a combined work based on this library. Thus, the terms and
conditions of the GNU General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you permission
to link this library with independent modules to produce an executable, regardless
of the license terms of these independent modules, and to copy and distribute
the resulting executable under terms of your choice, provided that you also meet,
for each linked independent module, the terms and conditions of the license of
that module. An independent module is a module which is not derived from or
based on this library. If you modify this library, you may extend this exception
to your version of the library, but you are not obligated to do so. If you do not
wish to do so, delete this exception statement from your version.
*/

package jpdv.functions.impl
import jpdv.engine.BaseForm

// Map<BaseForm, Map<BaseForm, Double>> space

int THRESHOLD = 2000
//int THRESHOLD = 400

// Generate list of basis elements
def basisElementSet = new HashSet<BaseForm>()
def targetCounts = [:]
def totalTargets = 0
def basisCounts = [:]
def totalBasis = 0
def combinedCounts = [:]
def totalCombined = 0
space.each {
    def target = it.key
    targetCounts[target] = targetCounts.get(target, 0) + 1
    totalTargets++
    it.value.each {
        def basis = it.key
        def combined = [target, basis]
        basisCounts[basis] = basisCounts.get(basis, 0) + 1
        totalBasis++
        combinedCounts[combined] = combinedCounts.get(combined, 0) + 1
        totalCombined++
        basisElementSet << basis
    }
}

println "# of Basis Mappings: ${basisElementSet.size()}"

if(THRESHOLD > 0 && basisElementSet.size() > THRESHOLD) {
    // Calculate Pointwise Mutual Information (PMI) values.

    def pmi = [:]
    combinedCounts.each {
        def combined = it.key
        def target = combined[0]
        def basis = combined[1]
        def pTarget = targetCounts[target] / totalTargets
        def pBasis = basisCounts[basis] / totalBasis
        def pCombined = it.value / totalCombined
        def pmiList = pmi[basis]
        if(pmiList == null) {
            pmiList = []
            pmi[basis] = pmiList
        }
        def currentPMI = Math.log(pCombined / (pTarget * pBasis))
        if(currentPMI < 0) {
            currentPMI = 0
        }
        pmiList << currentPMI


    }

    def averagePMI = [:]
    pmi.each {
        // Each entry contains a list of PMI values for a given basis element
        def basis = it.key
        def pmiList = it.value
        averagePMI[basis] = pmiList.sum() / totalTargets
    }

    while(basisElementSet.size() > THRESHOLD) {
        // Reduce number of basis elements by removing those with the lowest PMI.
        def basis = basisElementSet.min { averagePMI[it] }
        basisElementSet -= basis
    }

    println "# of Final Basis Mappings: ${basisElementSet.size()}"
}





return basisElementSet