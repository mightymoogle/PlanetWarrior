"""
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
"""

from utils import max_growth_rate, sum_growth_rate
from PlanetWars import Planet
from PlanetFleetSorter import PlanetFleetSorter

class PlanetFormulas(object):
	def __init__(self, planet, planet_wars, mk = 1.0, grow_rate = 1.0):
		'''
		Constructor
		'''
		self._planet = planet
		self._planet_wars = planet_wars

		# TODO: Scale coefficients
		self._planet_attack_prob_w = mk	# Attack probability weight for planet
		self._fleet_attack_prob_w = mk		# Attack probability weight for fleet

		self._planet_def_prob_w = mk		# Defense rate weight for planet
		self._fleet_def_prob_w = mk		# Defense rate weight for fleet

		self._growth_rate_w = grow_rate			# Growth rate weight

		# Those numbers are used for correcting the data in case of division by zero
		self._attack_prob_div_correction_pl = 0.1	# Attack probability (planet)
		self._attack_prob_div_correction_fl = 0.1	# Attack probability (fleet)

		self._def_prob_div_correction_pl = 0.1		# Defense rate (planet)
		self._def_prob_div_correction_fl = 0.1		# Defense rate (fleet)
		
		self._capt_ship_div_correction = 0.1		# Capture coefficient (ships)
		self._capt_attack_div_correction = 0.1		# Capture coefficient (attack probability)		

	def AttackProb(self, enemy_planet_list = None, enemy_fleet_list = None):
		'''
		Probability that planet will be attacked (ai)
		'''
		if enemy_planet_list == None:
			enemy_planet_list = self.EnemyPlanets()

		if enemy_fleet_list == None:
			enemy_fleet_list = self.EnemyFleets()

		result = 0.0

		for enemy_planet in enemy_planet_list:
			num_ships = enemy_planet.NumShips()
			dist = self._planet_wars.Distance(self._planet.PlanetID(), enemy_planet.PlanetID())
			divider = dist * self._planet_attack_prob_w + self._attack_prob_div_correction_pl

			result += num_ships / divider

		for enemy_fleet in enemy_fleet_list:
			num_ships = enemy_fleet.NumShips()
			divider = enemy_fleet.TurnsRemaining() * self._fleet_attack_prob_w + self._attack_prob_div_correction_fl
				
			result += num_ships / divider

		return result

	def DefenseCoeff(self, friendly_planet_list = None, friendly_fleet_list = None, num_ships = None):
		'''
		Coefficient that describes planet defense (di)
		'''
		if friendly_planet_list == None:
			friendly_planet_list = self.FriendlyPlanets()

		if friendly_fleet_list == None:
			friendly_fleet_list = self.FriendlyFleets()
			
		result = self._planet.NumShips() if num_ships == None else num_ships		

		for planet in friendly_planet_list:
			planet_formulas = PlanetFormulas(planet, self._planet_wars)

			a = planet_formulas.AttackProb()
			num_ships = planet.NumShips()
			dist = self._planet_wars.Distance(self._planet.PlanetID(), planet.PlanetID())
			divider = dist * a * self._planet_def_prob_w + self._def_prob_div_correction_pl

			result += num_ships / divider

		for fleet in friendly_fleet_list:
			num_ships = fleet.NumShips()
			divider = fleet.TurnsRemaining() * self._fleet_def_prob_w + self._def_prob_div_correction_fl

			result += num_ships / divider

		return result

	def GrowthRateCoeff(self):
		'''
		Planet ship growth rate coefficient (ii)
		'''
		growth_rate = self._planet.GrowthRate()
		max_rate = max_growth_rate(self._planet_wars)
		sum_rate = sum_growth_rate(self._planet_wars)

		# TODO: Check this formula (I don't like it)
		return (growth_rate * sum_rate * self._growth_rate_w) / max_rate

	def CaptureCoeff(self, enemy_planet_list = None, enemy_fleet_list = None, possible_new_owner = None):
		'''
		Planet capture coefficient (zi)
		'''
		if enemy_planet_list == None:
			enemy_planet_list = self.EnemyPlanets()

		if enemy_fleet_list == None:
			enemy_fleet_list = self.EnemyFleets()

		min_dist = 9999999999999999
		closest_enemy_planet = None

		for planet in enemy_planet_list:
			dist = self._planet_wars.Distance(planet.PlanetID(), self._planet.PlanetID())
	
			if dist < min_dist:
				min_dist = dist
				closest_enemy_planet = planet

		num_attackers = closest_enemy_planet.NumShips() if closest_enemy_planet != None else 0
		num_attackers += sum([f.NumShips() for f in enemy_fleet_list])

		num_defenders = abs(self._planet.NumShips())

		ships_after_capture = abs(num_attackers - num_defenders)
		new_owner = self._planet.Owner() if num_attackers <= num_defenders else possible_new_owner if possible_new_owner != None else 0

		planet_after_capture = Planet(
			self._planet.PlanetID(),
			new_owner,
			ships_after_capture,
			self._planet.GrowthRate(),
			self._planet.X(),
			self._planet.Y()                              
		)

		formulas = PlanetFormulas(planet_after_capture, self._planet_wars)
		m = num_attackers / (num_defenders + self._capt_ship_div_correction)
		a = formulas.AttackProb()

		return (self.GrowthRateCoeff() * m) / (a + self._capt_attack_div_correction)
		
	def EnemyPlanets(self):
		planet_fleet_sorter = PlanetFleetSorter(self._planet_wars)
		
		return planet_fleet_sorter.EnemyPlanets(self._planet)

	def FriendlyPlanets(self):
		planet_fleet_sorter = PlanetFleetSorter(self._planet_wars)
		
		return planet_fleet_sorter.FriendlyPlanets(self._planet)

	def FriendlyFleets(self):
		planet_fleet_sorter = PlanetFleetSorter(self._planet_wars)
		
		return planet_fleet_sorter.FriendlyFleets(self._planet)

	def EnemyFleets(self):
		planet_fleet_sorter = PlanetFleetSorter(self._planet_wars)
		
		return planet_fleet_sorter.EnemyFleets(self._planet)
