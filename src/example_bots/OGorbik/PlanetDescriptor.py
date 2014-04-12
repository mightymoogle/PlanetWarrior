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

from PlanetFormulas import PlanetFormulas

class PlanetDescriptor(object):
	def __init__(self, planet, planet_wars, mk = 1.0, grow_rate = 1.0):
		self._planet = planet
		self._planet_wars = planet_wars
		
		self._attack_rate_correction = 0.1	# Attack rate divider
		self._capture_rate_correction = 0.1	# Capture coefficient divider
		
		self._planet_formulas = PlanetFormulas(self._planet, self._planet_wars, mk, grow_rate)
		
	def MyEstimate(self):
		a = self.A()
		d = self.D()
		i = self.I()
		z = self.Z()

		return ((d / a) + i) / z
		
	def EnemyEstimate(self):
		a = self.A(False)
		d = self.D(False)
		i = self.I(False)
		z = self.Z(False)

		return ((d / a) + i) / z
		
	def A(self, my_estimate = True):
		return self._planet_formulas.AttackProb() + self._attack_rate_correction
	
	def D(self, my_estimate = True):
		return self._planet_formulas.DefenseCoeff()
	
	def I(self, my_estimate = True):
		return self._planet_formulas.GrowthRateCoeff()
	
	def Z(self, my_estimate = True):
		return self._planet_formulas.CaptureCoeff() + self._capture_rate_correction
	
	def __str__(self):
		result = '\t\t{\n'
		
		result += '\t\t\ta = ' + str(self.A()) + '\n'
		result += '\t\t\td = ' + str(self.D()) + '\n'
		result += '\t\t\ti = ' + str(self.I()) + '\n'
		result += '\t\t\tz = ' + str(self.Z()) + '\n'
		result += '\t\t\tPlanet estimate from my point of view = ' + str(self.MyEstimate()) + '\n'
		result += '\t\t\tPlanet estimate from enemy point of view = ' + str(self.EnemyEstimate()) + '\n'
		
		result += '\t\t}\n'
		
		return result
		
