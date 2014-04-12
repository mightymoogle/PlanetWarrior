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

from PlanetDescriptor import PlanetDescriptor

class TurnDescriptor(object):
	def __init__(self, planet_wars, mk = 1.0, grow_rate = 1.0):
		self._my_planets = planet_wars.MyPlanets()
		self._enemy_planets = planet_wars.EnemyPlanets()
		self._neutral_planets = planet_wars.NeutralPlanets()
		
		self._planet_wars = planet_wars
		
		self._mk = mk
		self._gr = grow_rate
		
	def MyPlanetDescriptors(self):
		return [PlanetDescriptor(planet, self._planet_wars, self._mk, self._gr) for planet in self._my_planets]
	
	def EnemyPlanetDescriptors(self):
		return [PlanetDescriptor(planet, self._planet_wars, self._mk, self._gr) for planet in self._enemy_planets]
	
	def NeutralPlanetDescriptors(self):
		return [PlanetDescriptor(planet, self._planet_wars, self._mk, self._gr) for planet in self._neutral_planets]
	
	def MyPower(self):
		return sum([p.MyEstimate() for p in self.MyPlanetDescriptors()])
	
	def EnemyPower(self):
		return sum([p.EnemyEstimate() for p in self.EnemyPlanetDescriptors()])
	
	def __str__(self):
		result = '{\n'
		
		result += '\tMyPower = ' + str(self.MyPower()) + '\n'
		result += '\tEnemyPower = ' + str(self.EnemyPower()) + '\n'
		
		result += '\tMy Planet Descriptors = [\n'
		for planet in self.MyPlanetDescriptors():
			result += str(planet)
		result += '\t]\n'	
		
		result += '\tEnemy Planet Descriptors = [\n'
		for planet in self.EnemyPlanetDescriptors():
			result += str(planet)
		result += '\t]\n'	
		
		result += '\tNeutral Planet Descriptors = [\n'
		for planet in self.NeutralPlanetDescriptors():
			result += str(planet)
		result += '\t]\n'	
		
		result += '}\n'
		
		return result
		
		
