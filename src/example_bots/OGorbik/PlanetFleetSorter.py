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

class PlanetFleetSorter(object):
	def __init__(self, planet_wars):
		self._planet_wars = planet_wars

	def FriendlyPlanets(self, planet):
		if planet.Owner() == 0:
			# Neutral planet
			return []
		elif planet.Owner() == 1:
			# My planet
			return [p for p in self._planet_wars.MyPlanets() if planet.PlanetID() != p.PlanetID()]
		else:
			# Enemy planet
			return [p for p in self._planet_wars.EnemyPlanets() if planet.Owner() == p.Owner() and planet.PlanetID() != p.PlanetID()]

	def EnemyPlanets(self, planet):
		if planet.Owner() == 0:
			# Neutral planet
			return []
		elif planet.Owner() == 1:
			# My planet
			return self._planet_wars.EnemyPlanets()
		else:
			# Enemy planet
			planet_list = self._planet_wars.MyPlanets()
			planet_list.extend([p for p in self._planet_wars.EnemyPlanets() if planet.Owner() != p.Owner()])

			return planet_list

	def FriendlyFleets(self, planet):
		if planet.Owner() == 0:
			# Neutral planet
			return []
		elif planet.Owner() == 1:
			# My planet
			return [f for f in self._planet_wars.MyFleets() if f.DestinationPlanet() == planet.PlanetID()]
		else:
			# Enemy planet
			return [f for f in self._planet_wars.EnemyFleets() if f.DestinationPlanet() == planet.PlanetID() and f.Owner() == planet.Owner()]

	def EnemyFleets(self, planet):
		if planet.Owner() == 0:
			# Neutral planet
			return []
		elif planet.Owner() == 1:
			# My planet
			return [f for f in self._planet_wars.EnemyFleets() if f.DestinationPlanet() == planet.PlanetID()]
		else:
			# Enemy planet
			fleet_list = [f for f in self._planet_wars.MyFleets() if f.DestinationPlanet() == planet.PlanetID()]
			fleet_list.extend([f for f in self._planet_wars.EnemyFleets() if f.DestinationPlanet() == planet.PlanetID() and f.Owner() != planet.Owner()])

			return fleet_list
