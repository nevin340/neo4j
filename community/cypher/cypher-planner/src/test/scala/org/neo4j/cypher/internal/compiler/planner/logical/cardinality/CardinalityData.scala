/*
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
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
package org.neo4j.cypher.internal.compiler.planner.logical.cardinality

trait CardinalityData {
  def forQuery(testUnit: CardinalityTestHelper#TestUnit): CardinalityTestHelper#TestUnit
}

trait ABCDCardinalityData extends CardinalityData {

  self: RandomizedCardinalityModelTestSuite =>

    val Asel = .2          // How selective a :A predicate is
    val Bsel = .1          // How selective a :B predicate is
    val Csel = .01         // How selective a :C predicate is
    val Dsel = .001        // How selective a :D predicate is
    val Esel = Bsel        // How selective a :E predicate is

    val A = N * Asel       // Nodes with label A
    val B = N * Bsel       // Nodes with label B
    val C = N * Csel       // Nodes with label C
    val D = N * Dsel       // Nodes with label D
    val E = N * Esel       // Nodes with label E

    val Aprop = 0.5        // Selectivity of index on :A(prop)
    val Bprop = 0.003      // Selectivity of index on :B(prop)
    val Abar = 0.002       // Selectivity of index on :A(bar)

    val A_T1_A_sel = 5.0 / A
    val A_T1_B_sel = 0.5
    val A_T1_C_sel = 0.05
    val A_T1_D_sel = 0.005

    val A_T1_A    = A * A * A_T1_A_sel
    val A_T1_B    = A * B * A_T1_B_sel
    val A_T1_C    = A * C * A_T1_C_sel
    val A_T1_D    = A * D * A_T1_D_sel

    val B_T1_B_sel = 10.0 / B
    val B_T1_C_sel = 0.1
    val B_T1_A_sel = 0.01
    val B_T1_D_sel = 0.001

    val B_T1_B    = B * B * B_T1_B_sel
    val B_T1_C    = B * C * B_T1_C_sel
    val B_T1_A    = B * A * B_T1_A_sel
    val B_T1_D    = B * D * B_T1_D_sel

    val C_T1_D_sel= 0.02
    val C_T1_D    = C * D * C_T1_D_sel

    val D_T1_C_sel = 0.3
    val D_T1_C     = D * C * D_T1_C_sel

    // No T1 rels from E nodes

    val A_T2_A_sel = 0
    val A_T2_B_sel = 5

    val A_T2_A = A * A * A_T2_A_sel
    val A_T2_B = A * B * A_T2_B_sel

    val B_T2_B = 0
    val B_T2_C_sel = 0.0031
    val B_T2_C = B * C * B_T2_C_sel

    // No T2 rels from C nodes

    val D_T2_C_sel = 0.07
    val D_T2_C = D * C * D_T2_C_sel

    val E_T2_B_sel = 0.01
    val E_T2_C_sel = 0.01
    val E_T2_D_sel = 0.001

    val E_T2_A = 0
    val E_T2_B = E * B * E_T2_B_sel
    val E_T2_C = E * C * E_T2_C_sel
    val E_T2_D = E * D * E_T2_D_sel

    // Sums

    val A_T1_ANY = A_T1_A + A_T1_B + A_T1_C + A_T1_D
    val A_T1_ANY_sel = A_T1_ANY / (N * A)
    val ANY_T1_A = A_T1_A + B_T1_A
    val ANY_T1_A_sel = ANY_T1_A / (N * A)

    val A_T2_ANY = A_T2_A + A_T2_B
    val A_ANY_ANY = A_T1_ANY + A_T2_ANY

    val B_T1_ANY = B_T1_A + B_T1_B + B_T1_C + B_T1_D
    val ANY_T1_B = B_T1_B + A_T1_B
    val ANY_T1_B_sel = ANY_T1_B / N

    val B_T2_ANY = B_T2_C
    val B_ANY_ANY = B_T1_ANY + B_T2_ANY
    val ANY_T2_B = A_T2_B + B_T2_B + E_T2_B

    val D_T1_ANY = D_T1_C
    val D_T2_ANY = D_T2_C
    val D_ANY_ANY = D_T1_ANY + D_T2_ANY

    val E_T1_ANY = 0    // No T1 relationships from E nodes
    val E_T2_ANY = E_T2_A + E_T2_B + E_T2_C + E_T2_D

    // Relationship count: the total number of relationships in the system
    val R = A_ANY_ANY + B_ANY_ANY + C_T1_D + D_ANY_ANY + E_T2_ANY

    def forQuery(testUnit: CardinalityTestHelper#TestUnit) =
      testUnit.
        withNodeName("a").
        withNodeName("b").
        withNodeName("c").
        withNodeName("d").
        withRelationshipName("r1").
        withRelationshipName("r2").
        withRelationshipName("r3").
        withRelationshipName("r4").
        withGraphNodes(N).
        withLabel('A, A).
        withLabel('B, B).
        withLabel('C, C).
        withLabel('D, D).
        withLabel('E, E).
        withLabel('EMPTY, 0).
        withIndexSelectivity(('A, 'prop) -> Aprop).
        withIndexSelectivity(('B, 'prop) -> Bprop).
        withIndexSelectivity(('A, 'bar) -> Abar).
        withIndexPropertyExistsSelectivity(('A, 'prop) -> 1.0).
        withIndexPropertyExistsSelectivity(('B, 'prop) -> 1.0).
        withIndexPropertyExistsSelectivity(('A, 'bar) -> 1.0).
        withRelationshipCardinality(('A -> 'T1 -> 'A) -> A_T1_A).
        withRelationshipCardinality(('A -> 'T1 -> 'B) -> A_T1_B).
        withRelationshipCardinality(('A -> 'T1 -> 'C) -> A_T1_C).
        withRelationshipCardinality(('A -> 'T1 -> 'D) -> A_T1_D).
        withRelationshipCardinality(('A -> 'T2 -> 'A) -> A_T2_A).
        withRelationshipCardinality(('A -> 'T2 -> 'B) -> A_T2_B).
        withRelationshipCardinality(('B -> 'T1 -> 'B) -> B_T1_B).
        withRelationshipCardinality(('B -> 'T1 -> 'C) -> B_T1_C).
        withRelationshipCardinality(('B -> 'T1 -> 'A) -> B_T1_A).
        withRelationshipCardinality(('B -> 'T1 -> 'D) -> B_T1_D).
        withRelationshipCardinality(('B -> 'T2 -> 'C) -> B_T2_C).
        withRelationshipCardinality(('C -> 'T1 -> 'D) -> C_T1_D).
        withRelationshipCardinality(('D -> 'T1 -> 'C) -> D_T1_C).
        withRelationshipCardinality(('D -> 'T2 -> 'C) -> D_T2_C).
        withRelationshipCardinality(('E -> 'T2 -> 'B) -> E_T2_B).
        withRelationshipCardinality(('E -> 'T2 -> 'C) -> E_T2_C).
        withRelationshipCardinality(('E -> 'T2 -> 'D) -> E_T2_D)
}
